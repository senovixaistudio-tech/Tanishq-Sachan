package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.audio.RecordingState
import com.example.audio.VoiceRecordingManager
import com.example.data.ChatRepository
import com.example.model.AttachmentItem
import com.example.model.AttachmentType
import com.example.model.ChatMessage
import com.example.model.ChatMessageDoc
import com.example.model.MessageSender
import com.example.model.NeuralReasoningStep
import com.example.model.UserProfileDoc
import com.example.model.ValidationStatus
import com.example.ui.components.ShowcasePreset
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val stagedAttachments: List<AttachmentItem> = emptyList(),
    val composerText: String = "",
    val isAnalyzingGlobal: Boolean = false,
    val analyzingStage: String? = null,
    val selectedModel: String = "ScenoviX Ultra 3.5",
    val isVoiceRecording: Boolean = false,
    val voiceDurationFormatted: String = "00:00",
    val voiceAmplitude: Float = 0.1f,
    val voiceRmsDb: Float = -42f,
    val waveformHistory: List<Float> = List(36) { 0.1f },
    val voiceLiveTranscript: String = "",
    val playingAudioId: String? = null,
    val inspectingAttachment: AttachmentItem? = null,
    val showSpecsDialog: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val chatRepository = ChatRepository(application)
    private val voiceRecordingManager = VoiceRecordingManager(viewModelScope)
    private var currentUserId: String? = null
    private var currentUserEmail: String? = null

    init {
        seedInitialShowcaseConversation()
        observeVoiceRecording()
    }

    fun initUserSession(userId: String, email: String) {
        if (currentUserId == userId) return
        currentUserId = userId
        currentUserEmail = email

        viewModelScope.launch {
            // Fetch remote user profile or save new profile
            chatRepository.getUserProfile().onSuccess { profile ->
                if (profile != null) {
                    _uiState.update { it.copy(selectedModel = profile.selectedModel) }
                } else {
                    chatRepository.saveUserProfile(
                        UserProfileDoc(
                            userId = userId,
                            email = email,
                            selectedModel = _uiState.value.selectedModel
                        )
                    )
                }
            }

            // Realtime observation of user chat messages
            try {
                chatRepository.observeMessages().collect { docs ->
                    if (docs.isNotEmpty()) {
                        val mapped = docs.map { doc ->
                            val existing = _uiState.value.messages.find { it.id == doc.id }
                            if (existing != null) {
                                existing.copy(content = doc.text, timestamp = doc.timestamp)
                            } else {
                                ChatMessage(
                                    id = doc.id,
                                    sender = if (doc.sender == "USER") MessageSender.USER else MessageSender.AI,
                                    timestamp = doc.timestamp,
                                    content = doc.text,
                                    tokenStats = doc.tokenStats,
                                    reasoningSteps = if (doc.hasReasoning) listOf(
                                        NeuralReasoningStep("Neural Synthesis", "Synthesized across multimodal cross-attention graph", 60)
                                    ) else emptyList(),
                                    modelTag = _uiState.value.selectedModel
                                )
                            }
                        }
                        _uiState.update { it.copy(messages = mapped) }
                    } else {
                        uploadSeedMessagesToFirestore(userId)
                    }
                }
            } catch (ignored: Exception) {
                // If offline or first sync, local optimistic list remains visible
            }
        }
    }

    private fun uploadSeedMessagesToFirestore(userId: String) {
        viewModelScope.launch {
            _uiState.value.messages.forEach { msg ->
                val doc = ChatMessageDoc(
                    id = msg.id,
                    userId = userId,
                    sender = if (msg.sender == MessageSender.USER) "USER" else "AI",
                    text = msg.content,
                    timestamp = msg.timestamp,
                    attachmentCount = msg.attachments.size,
                    tokenStats = msg.tokenStats,
                    hasReasoning = msg.reasoningSteps.isNotEmpty()
                )
                chatRepository.saveMessage(doc)
            }
        }
    }

    private fun observeVoiceRecording() {
        viewModelScope.launch {
            voiceRecordingManager.recordingState.collect { state ->
                _uiState.update { it.copy(isVoiceRecording = state == RecordingState.RECORDING) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.durationSeconds.collect { sec ->
                val m = sec / 60
                val s = sec % 60
                val formatted = String.format(Locale.US, "%02d:%02d", m, s)
                _uiState.update { it.copy(voiceDurationFormatted = formatted) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.currentAmplitude.collect { amp ->
                _uiState.update { it.copy(voiceAmplitude = amp) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.currentRmsDb.collect { db ->
                _uiState.update { it.copy(voiceRmsDb = db) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.waveformHistory.collect { history ->
                _uiState.update { it.copy(waveformHistory = history) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.liveTranscript.collect { transcript ->
                _uiState.update { it.copy(voiceLiveTranscript = transcript) }
            }
        }
        viewModelScope.launch {
            voiceRecordingManager.isPlayingAudio.collect { playingId ->
                _uiState.update { it.copy(playingAudioId = playingId) }
            }
        }
    }

    private fun seedInitialShowcaseConversation() {
        val initialDroneAttachment = AttachmentItem(
            id = "seed-img-1",
            name = "drone_thermal_scan_4k.jpg",
            type = AttachmentType.IMAGE,
            sizeFormatted = "4.8 MB",
            mimeType = "image/jpeg",
            uploadProgress = 1.0f,
            isUploading = false,
            isAnalyzing = false,
            resolution = "3840×2160",
            drawableRes = R.drawable.img_sample_render,
            analysisSummary = "Multi-spectral thermal topology extracted. 14 structural nodes identified with 99.4% confidence.",
            validationStatus = ValidationStatus.VALID,
            validationMessage = "Integrity verified • Zero-loss encoding"
        )

        val initialDocAttachment = AttachmentItem(
            id = "seed-doc-1",
            name = "quantum_attention_whitepaper.pdf",
            type = AttachmentType.DOCUMENT,
            sizeFormatted = "3.2 MB",
            mimeType = "application/pdf",
            pageCount = 28,
            uploadProgress = 1.0f,
            isUploading = false,
            isAnalyzing = false,
            analysisSummary = "28 pages tokenized. Core theorems on O(N) multi-head cross-attention parsed into semantic vector graph.",
            validationStatus = ValidationStatus.VALID,
            validationMessage = "Integrity verified • 28 Pages OCR Indexed"
        )

        val initialMessages = listOf(
            ChatMessage(
                id = "msg-1",
                sender = MessageSender.USER,
                timestamp = "10:14 AM",
                content = "Analyze this 4K drone thermal scan and check for structural stress points.",
                attachments = listOf(initialDroneAttachment)
            ),
            ChatMessage(
                id = "msg-2",
                sender = MessageSender.AI,
                timestamp = "10:14 AM",
                content = """ScenoviX Multimodal Neural Assessment:

1. Thermal Topography:
• Primary sector indicates balanced heat dissipation (22.4°C baseline).
• Quadrant B exhibits minor thermal variance (+4.1°C), likely concentrated solar refraction on composite glass panels.

2. Structural Integrity:
• Zero micro-fractures detected across primary tension cables.
• Spatial geometry aligns with CAD specifications within ±0.03mm tolerance.

3. Actionable Conclusion:
Structure is operating within nominal safety thresholds. Recommended reinspection cycle: 90 days.""",
                attachments = emptyList(),
                reasoningSteps = listOf(
                    NeuralReasoningStep("Spatial Ingestion", "Deconvoluted 8.29M pixels across 3 RGB channels", 45),
                    NeuralReasoningStep("Thermal Heatmap Isolation", "Segmented thermal gradient contours", 60),
                    NeuralReasoningStep("CAD Structural Alignment", "Cross-referenced with volumetric architectural tensor", 75)
                ),
                suggestedActions = listOf("Extract Stress Matrix", "Compare CAD Blueprint", "Export Inspection PDF"),
                tokenStats = "1,842 tokens • Latency 180ms • ScenoviX Ultra 3.5"
            )
        )

        _uiState.update { it.copy(messages = initialMessages) }
    }

    fun onComposerTextChanged(newText: String) {
        _uiState.update { it.copy(composerText = newText) }
    }

    fun startVoiceRecording(context: Context) {
        voiceRecordingManager.startRecording(context)
    }

    fun stopVoiceRecordingAndAttach() {
        val result = voiceRecordingManager.stopRecording()
        val id = UUID.randomUUID().toString()
        val audioAttachment = AttachmentItem(
            id = id,
            name = "voice_recording_${System.currentTimeMillis() % 1000}.m4a",
            type = AttachmentType.VOICE,
            sizeFormatted = "${(result.durationSeconds * 16).coerceAtLeast(48)} KB",
            mimeType = "audio/mp4",
            uploadProgress = 1.0f,
            isUploading = false,
            isAnalyzing = false,
            audioDuration = result.durationFormatted,
            audioFile = result.file,
            transcription = result.transcription,
            validationStatus = ValidationStatus.VALID,
            validationMessage = "Integrity verified • 44.1 kHz AAC Audio"
        )

        _uiState.update { current ->
            val updatedText = if (current.composerText.isBlank() && result.transcription.isNotBlank() && !result.transcription.startsWith("ScenoviX voice memo")) {
                result.transcription
            } else {
                current.composerText
            }
            current.copy(
                stagedAttachments = current.stagedAttachments + audioAttachment,
                composerText = updatedText
            )
        }
    }

    fun stopVoiceRecordingAndInsertTranscript() {
        val result = voiceRecordingManager.stopRecording()
        val textToInsert = result.transcription
        _uiState.update { current ->
            val newText = if (current.composerText.isBlank()) {
                textToInsert
            } else {
                "${current.composerText} $textToInsert"
            }
            current.copy(composerText = newText)
        }
    }

    fun cancelVoiceRecording() {
        voiceRecordingManager.cancelRecording()
    }

    fun setVoiceTranscriptPreset(preset: String) {
        _uiState.update { it.copy(voiceLiveTranscript = preset) }
    }

    fun togglePlayAudio(attachment: AttachmentItem) {
        voiceRecordingManager.playAudio(attachment.audioFile, attachment.id)
    }

    fun setInspectingAttachment(attachment: AttachmentItem?) {
        _uiState.update { it.copy(inspectingAttachment = attachment) }
    }

    fun setShowSpecsDialog(show: Boolean) {
        _uiState.update { it.copy(showSpecsDialog = show) }
    }

    fun setSelectedModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid
        val email = currentUserEmail ?: Firebase.auth.currentUser?.email ?: ""
        if (uid != null) {
            viewModelScope.launch {
                chatRepository.saveUserProfile(
                    UserProfileDoc(
                        userId = uid,
                        email = email,
                        selectedModel = model
                    )
                )
            }
        }
    }

    fun removeStagedAttachment(id: String) {
        _uiState.update { current ->
            current.copy(stagedAttachments = current.stagedAttachments.filterNot { it.id == id })
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList(), stagedAttachments = emptyList()) }
        if (Firebase.auth.currentUser != null) {
            viewModelScope.launch {
                chatRepository.clearAllMessages()
            }
        }
    }

    // Handles adding a new attachment from picker or shortcut
    fun stageAttachment(
        type: AttachmentType,
        name: String? = null,
        uri: Uri? = null,
        drawableRes: Int? = null,
        sizeFormatted: String? = null
    ) {
        val id = UUID.randomUUID().toString()
        val generatedName = name ?: when (type) {
            AttachmentType.IMAGE -> "optical_sensor_capture_${System.currentTimeMillis() % 1000}.png"
            AttachmentType.VIDEO -> "telemetry_stream_${System.currentTimeMillis() % 1000}.mp4"
            AttachmentType.DOCUMENT -> "project_specifications_${System.currentTimeMillis() % 1000}.pdf"
            AttachmentType.CAMERA -> "camera_snapshot_${System.currentTimeMillis() % 1000}.jpg"
            AttachmentType.VOICE -> "neural_audio_${System.currentTimeMillis() % 1000}.wav"
        }

        val item = AttachmentItem(
            id = id,
            name = generatedName,
            type = type,
            sizeFormatted = sizeFormatted ?: when (type) {
                AttachmentType.IMAGE, AttachmentType.CAMERA -> "5.4 MB"
                AttachmentType.VIDEO -> "24.1 MB"
                AttachmentType.DOCUMENT -> "2.8 MB"
                AttachmentType.VOICE -> "1.1 MB"
            },
            mimeType = when (type) {
                AttachmentType.IMAGE, AttachmentType.CAMERA -> "image/png"
                AttachmentType.VIDEO -> "video/mp4"
                AttachmentType.DOCUMENT -> "application/pdf"
                AttachmentType.VOICE -> "audio/wav"
            },
            uploadProgress = 0.1f,
            isUploading = true,
            uri = uri,
            drawableRes = drawableRes ?: if (type == AttachmentType.IMAGE || type == AttachmentType.VIDEO || type == AttachmentType.CAMERA) {
                R.drawable.img_sample_render
            } else null,
            resolution = if (type == AttachmentType.IMAGE || type == AttachmentType.CAMERA) "3840×2160" else null,
            videoDuration = if (type == AttachmentType.VIDEO) "01:24" else null,
            pageCount = if (type == AttachmentType.DOCUMENT) 16 else null
        )

        _uiState.update { current ->
            current.copy(stagedAttachments = current.stagedAttachments + item)
        }

        // Animate simulated file upload progress
        viewModelScope.launch {
            for (p in listOf(0.35f, 0.70f, 0.95f, 1.0f)) {
                delay(180)
                _uiState.update { current ->
                    current.copy(
                        stagedAttachments = current.stagedAttachments.map {
                            if (it.id == id) it.copy(
                                uploadProgress = p,
                                isUploading = p < 1.0f,
                                validationStatus = if (p >= 1.0f) ValidationStatus.VALID else ValidationStatus.PROCESSING
                            ) else it
                        }
                    )
                }
            }
        }
    }

    fun stagePreset(preset: ShowcasePreset) {
        val id = UUID.randomUUID().toString()
        val item = AttachmentItem(
            id = id,
            name = when (preset.type) {
                AttachmentType.IMAGE -> "drone_thermal_scan.jpg"
                AttachmentType.VIDEO -> "telemetry_autonomous_stream.mp4"
                AttachmentType.DOCUMENT -> if (preset.title.contains("Financial")) "financial_balance_sheet.xlsx" else "quantum_attention_whitepaper.pdf"
                else -> "input_media.bin"
            },
            type = preset.type,
            sizeFormatted = preset.size,
            mimeType = when (preset.type) {
                AttachmentType.IMAGE -> "image/jpeg"
                AttachmentType.VIDEO -> "video/mp4"
                AttachmentType.DOCUMENT -> if (preset.title.contains("Financial")) "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" else "application/pdf"
                else -> "application/octet-stream"
            },
            uploadProgress = 0.2f,
            isUploading = true,
            drawableRes = if (preset.type == AttachmentType.IMAGE || preset.type == AttachmentType.VIDEO) R.drawable.img_sample_render else null,
            resolution = if (preset.type == AttachmentType.IMAGE) "3840×2160" else null,
            videoDuration = if (preset.type == AttachmentType.VIDEO) "01:14" else null,
            pageCount = if (preset.type == AttachmentType.DOCUMENT) 28 else null
        )

        _uiState.update { current ->
            current.copy(
                stagedAttachments = current.stagedAttachments + item,
                composerText = preset.prompt
            )
        }

        viewModelScope.launch {
            for (p in listOf(0.45f, 0.85f, 1.0f)) {
                delay(150)
                _uiState.update { current ->
                    current.copy(
                        stagedAttachments = current.stagedAttachments.map {
                            if (it.id == id) it.copy(uploadProgress = p, isUploading = p < 1.0f) else it
                        }
                    )
                }
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.composerText.trim()
        val attachments = _uiState.value.stagedAttachments
        if (text.isEmpty() && attachments.isEmpty()) return

        val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            timestamp = timeString,
            content = text,
            attachments = attachments
        )

        // Clear composer and staged attachments
        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMsg,
                stagedAttachments = emptyList(),
                composerText = ""
            )
        }

        // Save user message to Firestore
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val userDoc = ChatMessageDoc(
                    id = userMsg.id,
                    userId = uid,
                    sender = "USER",
                    text = userMsg.content,
                    timestamp = userMsg.timestamp,
                    attachmentCount = userMsg.attachments.size,
                    tokenStats = null,
                    hasReasoning = false
                )
                chatRepository.saveMessage(userDoc)
            }
        }

        // Trigger AI analysis and response
        viewModelScope.launch {
            val aiMsgId = UUID.randomUUID().toString()
            val hasAttachments = attachments.isNotEmpty()

            // 1. Initial analyzing placeholder state
            val placeholderAi = ChatMessage(
                id = aiMsgId,
                sender = MessageSender.AI,
                timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                content = if (hasAttachments) "Synthesizing cross-modal multimodal tensors…" else "Processing neural cognitive stream…",
                isAnalyzing = true,
                analysisStage = if (hasAttachments) "Validating file signatures & SHA-256 tokens…" else "Tokenizing prompt query nodes…",
                modelTag = _uiState.value.selectedModel
            )

            _uiState.update { it.copy(messages = it.messages + placeholderAi) }

            // 2. Animated analysis pipeline stages
            if (hasAttachments) {
                delay(700)
                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map {
                            if (it.id == aiMsgId) it.copy(
                                analysisStage = "Deconstructing spatial visual & linguistic tokens (5,210 nodes)…"
                            ) else it
                        }
                    )
                }
                delay(800)
                _uiState.update { current ->
                    current.copy(
                        messages = current.messages.map {
                            if (it.id == aiMsgId) it.copy(
                                analysisStage = "Synthesizing high-dimensional cross-attention vector embeddings…"
                            ) else it
                        }
                    )
                }
                delay(700)
            } else {
                delay(800)
            }

            // 3. Final synthesized response
            val completedResponse = generateAiResponse(text, attachments, _uiState.value.selectedModel)
            val finalAi = completedResponse.copy(id = aiMsgId)

            _uiState.update { current ->
                current.copy(
                    messages = current.messages.map {
                        if (it.id == aiMsgId) finalAi else it
                    }
                )
            }

            if (uid != null) {
                val aiDoc = ChatMessageDoc(
                    id = aiMsgId,
                    userId = uid,
                    sender = "AI",
                    text = finalAi.content,
                    timestamp = finalAi.timestamp,
                    attachmentCount = 0,
                    tokenStats = finalAi.tokenStats,
                    hasReasoning = finalAi.reasoningSteps.isNotEmpty()
                )
                chatRepository.saveMessage(aiDoc)
            }
        }
    }

    private fun generateAiResponse(
        prompt: String,
        attachments: List<AttachmentItem>,
        modelTag: String
    ): ChatMessage {
        val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val att = attachments.firstOrNull()

        val content: String
        val reasoning: List<NeuralReasoningStep>
        val suggestions: List<String>
        val stats: String

        if (att != null) {
            when (att.type) {
                AttachmentType.IMAGE, AttachmentType.CAMERA -> {
                    content = """ScenoviX Visual Intelligence Analysis:
Target: ${att.name} • Resolution: ${att.resolution ?: "3840×2160"}

1. Spatial Decomposition:
• High-frequency edge detection confirms clean geometric contours.
• Color gamut distribution shows prominent cyan-violet luminescence with balanced dynamic range.

2. Feature Mapping:
• Detected 12 foreground structural centroids with 99.8% semantic alignment.
• Optical depth mapping suggests atmospheric refraction in upper quadrant.

3. Answers to Query:
"${if (prompt.isNotBlank()) prompt else "Full visual breakdown"}"
All focal points are synthesized and cataloged in the neural index."""
                    reasoning = listOf(
                        NeuralReasoningStep("Tensor Decomposition", "Converted RGB bitmap to 5,120 vision patch embeddings", 40),
                        NeuralReasoningStep("Spatial OCR & Topology", "Extracted 12 structural bounding contours", 65),
                        NeuralReasoningStep("Semantic CoT", "Synthesized visual context against knowledge graph", 55)
                    )
                    suggestions = listOf("Zoom Feature Vectors", "Generate Heatmap Overlay", "Extract Color Histogram")
                    stats = "2,190 tokens • Latency 160ms • $modelTag"
                }
                AttachmentType.VIDEO -> {
                    content = """ScenoviX Temporal Video Engine:
Stream: ${att.name} • Keyframe Rate: 60 FPS • Duration: ${att.videoDuration ?: "01:14"}

1. Temporal Motion Vectors:
• 4,440 discrete frames ingested across 7 key visual scenes.
• Detected dynamic object tracking trajectories with continuous velocity curves.

2. Anomaly Detection:
• No temporal drift or packet corruption detected.
• Frame sequence stability rated at 99.7%.

3. Deep Scene Breakdown:
The video showcases synchronized cybernetic movement telemetry. All key milestones have been tagged and indexed for timestamped scrubbing."""
                    reasoning = listOf(
                        NeuralReasoningStep("Frame Ingestion", "Sampled 60fps I-frames and motion delta tensors", 80),
                        NeuralReasoningStep("Temporal Flow Analysis", "Calculated optical flow across frame sequence", 95),
                        NeuralReasoningStep("Event Extraction", "Identified 4 salient action intervals", 50)
                    )
                    suggestions = listOf("List Keyframe Timestamps", "Export Motion Telemetry", "Detect Face & Object IDs")
                    stats = "3,480 tokens • Latency 225ms • $modelTag"
                }
                AttachmentType.DOCUMENT -> {
                    content = """ScenoviX Document Intelligence:
File: ${att.name} • Size: ${att.sizeFormatted} • Indexed Pages: ${att.pageCount ?: 18}

1. Executive Summary:
• Comprehensive technical taxonomy structured across key architectural modules.
• High token density with zero corrupted character sequences.

2. Key Findings & Data Extraction:
• Primary methodology: Multi-modal attention scaling with sub-quadratic computational complexity.
• Quantitative metrics: 34.2% reduction in inference memory footprint; 4.1x throughput gain.

3. Actionable Takeaways:
Implementation equations and benchmark tables have been translated into structured JSON representations ready for export."""
                    reasoning = listOf(
                        NeuralReasoningStep("Document Parsing", "Processed PDF vector layout, fonts, and table geometries", 70),
                        NeuralReasoningStep("Semantic Chunking", "Divided document into 84 recursive semantic chunks", 60),
                        NeuralReasoningStep("Cross-Document Synthesis", "Extracted quantitative metrics and conclusions", 45)
                    )
                    suggestions = listOf("Export Raw JSON Tables", "Generate 1-Page Brief", "Compare with Prior Version")
                    stats = "4,120 tokens • Latency 195ms • $modelTag"
                }
                AttachmentType.VOICE -> {
                    content = """ScenoviX Neural Acoustic Synthesizer:
Target: ${att.name} • Audio Frequency: 48 kHz High-Fidelity

1. Transcription & Sentiment:
"Acoustic frequency captured with pristine clarity. High signal-to-noise ratio."

2. Speaker Diagnostics:
• Vocal cadence: Confident, structured delivery.
• Background noise suppression: 98.4% effective."""
                    reasoning = listOf(
                        NeuralReasoningStep("Acoustic Ingestion", "Fast Fourier Transform spectral breakdown", 35),
                        NeuralReasoningStep("Phoneme Alignment", "Speech-to-text token conversion", 45)
                    )
                    suggestions = listOf("Summarize Audio Points", "Translate to Multi-Language", "Extract Timestamps")
                    stats = "1,120 tokens • Latency 110ms • $modelTag"
                }
            }
        } else {
            content = """ScenoviX Neural Cognition:

Regarding: "$prompt"

1. Core Synthesis:
ScenoviX AI evaluates this query using multimodal cross-attention. Our neural pipeline combines high-dimensional reasoning with real-time contextual adaptation.

2. Key Capabilities:
• Vision Ingestion: 4K optical analysis, thermal detection, and spatial depth estimation.
• Video Understanding: Frame-by-frame temporal motion analysis and anomaly localization.
• Document Intelligence: Full PDF, XLSX, and DOCX parsing with exact table and theorem extraction.

Tip: Tap the '+' button below to attach images, videos, or documents and experience direct multimodal Q&A."""
            reasoning = listOf(
                NeuralReasoningStep("Semantic Tokenization", "Parsed natural language query vectors", 25),
                NeuralReasoningStep("Knowledge Retrieval", "Searched multimodal capability graph", 45)
            )
            suggestions = listOf("Attach an Image to Test", "Upload PDF Document", "Benchmark Model Speed")
            stats = "980 tokens • Latency 95ms • $modelTag"
        }

        return ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.AI,
            timestamp = timeString,
            content = content,
            reasoningSteps = reasoning,
            suggestedActions = suggestions,
            tokenStats = stats,
            modelTag = modelTag
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecordingManager.stopAudioPlayback()
    }
}
