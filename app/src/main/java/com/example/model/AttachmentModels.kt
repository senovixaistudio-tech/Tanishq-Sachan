package com.example.model

import android.net.Uri
import java.io.File

enum class AttachmentType(val displayName: String, val extensions: String) {
    IMAGE("Upload Image", "JPG, PNG, WEBP"),
    VIDEO("Upload Video", "MP4, MOV, WEBM"),
    DOCUMENT("Upload Files", "PDF, DOCX, TXT, XLSX, PPTX"),
    CAMERA("Camera", "Instant 4K Capture"),
    VOICE("Voice Memo", "PCM, WAV, M4A")
}

enum class ValidationStatus {
    VALID,
    PROCESSING,
    FAILED
}

data class AttachmentItem(
    val id: String,
    val name: String,
    val type: AttachmentType,
    val sizeFormatted: String,
    val mimeType: String,
    val uploadProgress: Float = 1.0f, // 0.0 to 1.0
    val isUploading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 1.0f,
    val analysisSummary: String? = null,
    val uri: Uri? = null,
    val drawableRes: Int? = null,
    val videoDuration: String? = null,
    val audioDuration: String? = null,
    val audioFile: File? = null,
    val transcription: String? = null,
    val pageCount: Int? = null,
    val resolution: String? = null,
    val validationStatus: ValidationStatus = ValidationStatus.VALID,
    val validationMessage: String = "Integrity verified • Zero-loss encoding"
)
