package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

data class VoiceRecordingResult(
    val file: File?,
    val durationSeconds: Int,
    val durationFormatted: String,
    val transcription: String
)

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    COMPLETED
}

class VoiceRecordingManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var mediaRecorder: MediaRecorder? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentAudioFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0.1f) // 0.0f to 1.0f
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _currentRmsDb = MutableStateFlow(-42f) // dB scale e.g. -48 to 0 dB
    val currentRmsDb: StateFlow<Float> = _currentRmsDb.asStateFlow()

    private val _waveformHistory = MutableStateFlow<List<Float>>(List(36) { 0.1f })
    val waveformHistory: StateFlow<List<Float>> = _waveformHistory.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow<String?>(null) // audio attachment ID currently playing
    val isPlayingAudio: StateFlow<String?> = _isPlayingAudio.asStateFlow()

    fun startRecording(context: Context) {
        if (_recordingState.value == RecordingState.RECORDING) return

        _durationSeconds.value = 0
        _liveTranscript.value = ""
        _currentAmplitude.value = 0.15f
        _currentRmsDb.value = -32f
        _waveformHistory.value = List(36) { 0.12f }

        val outputDir = context.cacheDir
        val audioFile = File(outputDir, "scenovix_voice_${System.currentTimeMillis()}.m4a")
        currentAudioFile = audioFile

        // Initialize MediaRecorder
        try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(audioFile.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            _recordingState.value = RecordingState.RECORDING
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "MediaRecorder failed to start: ${e.message}")
            // Fallback: still continue recording state for speech recognizer
            _recordingState.value = RecordingState.RECORDING
        }

        // Initialize SpeechRecognizer if available
        initSpeechRecognizer(context)

        // Launch duration timer
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _recordingState.value == RecordingState.RECORDING) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }

        // Launch amplitude polling with rolling waveform buffer
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive && _recordingState.value == RecordingState.RECORDING) {
                val maxAmp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                val normalized = if (maxAmp > 0) {
                    (maxAmp / 32767f).coerceIn(0.08f, 1.0f)
                } else {
                    // Subtle organic pulse if hardware amplitude not accessible
                    val t = System.currentTimeMillis() / 180.0
                    (0.2f + (Math.sin(t) * 0.15f).toFloat() + (Math.cos(t * 1.5) * 0.08f).toFloat()).coerceIn(0.1f, 0.9f)
                }
                _currentAmplitude.value = normalized
                val db = if (normalized > 0.01f) {
                    (20 * Math.log10(normalized.toDouble())).toFloat().coerceIn(-48f, 0f)
                } else {
                    -48f
                }
                _currentRmsDb.value = db

                // Shift rolling waveform history buffer
                val history = _waveformHistory.value
                val newHistory = if (history.size >= 36) {
                    history.drop(1) + normalized
                } else {
                    history + normalized
                }
                _waveformHistory.value = newHistory
                delay(50)
            }
        }
    }

    private fun initSpeechRecognizer(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.d("VoiceRecordingManager", "Speech recognition not available on device")
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    // Update amplitude if recognizer gives rms
                    if (rmsdB > 0) {
                        val norm = (rmsdB / 12f).coerceIn(0.15f, 1.0f)
                        _currentAmplitude.value = norm
                    }
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Log.d("VoiceRecordingManager", "SpeechRecognizer error: $error")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        if (text.isNotBlank()) {
                            _liveTranscript.value = text
                        }
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        if (text.isNotBlank()) {
                            _liveTranscript.value = text
                        }
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
            speechRecognizer = recognizer
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error initializing speech recognizer: ${e.message}")
        }
    }

    fun stopRecording(): VoiceRecordingResult {
        timerJob?.cancel()
        amplitudeJob?.cancel()

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error stopping MediaRecorder: ${e.message}")
        }
        mediaRecorder = null

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceRecordingManager", "Error stopping SpeechRecognizer: ${e.message}")
        }
        speechRecognizer = null

        val duration = _durationSeconds.value
        val durationFormatted = formatSeconds(duration)
        val file = currentAudioFile

        var transcript = _liveTranscript.value.trim()
        if (transcript.isEmpty()) {
            transcript = if (duration > 0) {
                "ScenoviX voice memo ($durationFormatted) recorded for neural audio parsing."
            } else {
                "Voice memo recorded."
            }
        }

        _recordingState.value = RecordingState.IDLE
        _currentAmplitude.value = 0.1f

        return VoiceRecordingResult(
            file = file,
            durationSeconds = duration,
            durationFormatted = durationFormatted,
            transcription = transcript
        )
    }

    fun cancelRecording() {
        timerJob?.cancel()
        amplitudeJob?.cancel()

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (ignored: Exception) {}
        mediaRecorder = null

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (ignored: Exception) {}
        speechRecognizer = null

        currentAudioFile?.delete()
        currentAudioFile = null

        _recordingState.value = RecordingState.IDLE
        _durationSeconds.value = 0
        _liveTranscript.value = ""
        _currentAmplitude.value = 0.1f
    }

    fun playAudio(audioFile: File?, attachmentId: String) {
        if (_isPlayingAudio.value == attachmentId) {
            stopAudioPlayback()
            return
        }

        stopAudioPlayback()

        if (audioFile != null && audioFile.exists()) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        _isPlayingAudio.value = null
                    }
                }
                _isPlayingAudio.value = attachmentId
            } catch (e: Exception) {
                Log.e("VoiceRecordingManager", "MediaPlayer playback failed: ${e.message}")
                _isPlayingAudio.value = null
            }
        } else {
            // Simulated playback for demo audio files
            _isPlayingAudio.value = attachmentId
            scope.launch {
                delay(3000)
                if (_isPlayingAudio.value == attachmentId) {
                    _isPlayingAudio.value = null
                }
            }
        }
    }

    fun stopAudioPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        _isPlayingAudio.value = null
    }

    private fun formatSeconds(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }
}
