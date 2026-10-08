package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AttachmentType
import com.example.ui.components.AttachmentInspectorDialog
import com.example.ui.components.ChatMessageList
import com.example.ui.components.FloatingAttachmentPanel
import com.example.ui.components.ModelSpecsDialog
import com.example.ui.components.FuturisticComposer
import com.example.ui.components.ScenovixTopBar
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ScenovixBackground
import com.example.ui.viewmodel.ChatViewModel

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.ui.auth.ScenovixAuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOutUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScenovixRoot()
            }
        }
    }
}

@Composable
fun ScenovixRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                },
                onUnauthenticated = { },
                scope = coroutineScope
            )
        }
    }

    if (currentUser == null) {
        ScenovixAuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        ScenovixApp(
            userId = currentUser!!.uid,
            userEmail = currentUser!!.email ?: "",
            onSignOut = {
                signOutUser(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenovixApp(
    userId: String,
    userEmail: String,
    onSignOut: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(userId) {
        viewModel.initUserSession(userId, userEmail)
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAttachmentSheet by remember { mutableStateOf(false) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Microphone runtime permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startVoiceRecording(context)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val toggleVoiceRecording = {
        if (uiState.isVoiceRecording) {
            viewModel.stopVoiceRecordingAndAttach()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            if (hasAudioPermission) {
                viewModel.startVoiceRecording(context)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Real device photo/media picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.stageAttachment(
                type = AttachmentType.IMAGE,
                name = "selected_image_${System.currentTimeMillis() % 10000}.jpg",
                uri = uri,
                sizeFormatted = "3.6 MB"
            )
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Real device document picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
            viewModel.stageAttachment(
                type = AttachmentType.DOCUMENT,
                name = fileName,
                uri = uri,
                sizeFormatted = "4.2 MB"
            )
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ScenovixBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ScenovixTopBar(
                onClearChat = {
                    viewModel.clearChat()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onOpenSpecs = {
                    viewModel.setShowSpecsDialog(true)
                },
                onSignOut = onSignOut
            )
        },
        bottomBar = {
            FuturisticComposer(
                text = uiState.composerText,
                onTextChange = { viewModel.onComposerTextChanged(it) },
                stagedAttachments = uiState.stagedAttachments,
                onRemoveStagedAttachment = {
                    viewModel.removeStagedAttachment(it)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onOpenAttachmentSheet = {
                    showAttachmentSheet = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onQuickAttach = { type ->
                    when (type) {
                        AttachmentType.IMAGE -> photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                        AttachmentType.VIDEO -> photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                        AttachmentType.DOCUMENT -> documentPickerLauncher.launch(arrayOf("*/*"))
                        AttachmentType.CAMERA -> {
                            viewModel.stageAttachment(type)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        AttachmentType.VOICE -> {
                            toggleVoiceRecording()
                        }
                    }
                },
                onQuickCapabilityClicked = { promptText ->
                    viewModel.onComposerTextChanged(promptText)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onSend = {
                    viewModel.sendMessage()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                isVoiceRecording = uiState.isVoiceRecording,
                voiceDurationFormatted = uiState.voiceDurationFormatted,
                voiceAmplitude = uiState.voiceAmplitude,
                voiceLiveTranscript = uiState.voiceLiveTranscript,
                onToggleVoice = toggleVoiceRecording,
                onAttachAudioClip = {
                    viewModel.stopVoiceRecordingAndAttach()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onInsertTranscriptOnly = {
                    viewModel.stopVoiceRecordingAndInsertTranscript()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onCancelVoiceRecording = {
                    viewModel.cancelVoiceRecording()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onSelectVoicePreset = { preset ->
                    viewModel.setVoiceTranscriptPreset(preset)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Subtle ambient neon cosmic dust glow in the background
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.15f),
                            radius = size.width * 0.7f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricViolet.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.65f),
                            radius = size.width * 0.8f
                        )
                    )
                }
        ) {
            ChatMessageList(
                messages = uiState.messages,
                onInspectAttachment = { attachment ->
                    viewModel.setInspectingAttachment(attachment)
                },
                onQuickPromptClick = { prompt ->
                    viewModel.onComposerTextChanged(prompt)
                },
                onOpenAttachmentSheet = {
                    showAttachmentSheet = true
                },
                playingAudioId = uiState.playingAudioId,
                onTogglePlayAudio = { attachment ->
                    viewModel.togglePlayAudio(attachment)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )
        }
    }

    // Modal Floating Attachment Ingestion Panel
    if (showAttachmentSheet) {
        FloatingAttachmentPanel(
            sheetState = sheetState,
            onDismiss = { showAttachmentSheet = false },
            onSelectOption = { type ->
                when (type) {
                    AttachmentType.IMAGE -> photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    AttachmentType.VIDEO -> photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                    AttachmentType.DOCUMENT -> documentPickerLauncher.launch(arrayOf("*/*"))
                    AttachmentType.CAMERA -> {
                        viewModel.stageAttachment(type)
                    }
                    AttachmentType.VOICE -> {
                        toggleVoiceRecording()
                    }
                }
                showAttachmentSheet = false
            },
            onSelectPreset = { preset ->
                viewModel.stagePreset(preset)
                showAttachmentSheet = false
            }
        )
    }

    // Modal Attachment Inspector (4K Deep Dive)
    uiState.inspectingAttachment?.let { attachment ->
        AttachmentInspectorDialog(
            attachment = attachment,
            onDismiss = { viewModel.setInspectingAttachment(null) },
            onAskAboutFile = { prompt ->
                viewModel.onComposerTextChanged(prompt)
                viewModel.sendMessage()
            }
        )
    }

    // Modal Model Specifications Dialog
    if (uiState.showSpecsDialog) {
        ModelSpecsDialog(
            selectedModel = uiState.selectedModel,
            onSelectModel = { viewModel.setSelectedModel(it) },
            onDismiss = { viewModel.setShowSpecsDialog(false) }
        )
    }
}
