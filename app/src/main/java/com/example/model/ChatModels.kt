package com.example.model

enum class MessageSender {
    USER,
    AI
}

data class NeuralReasoningStep(
    val title: String,
    val detail: String,
    val durationMs: Int
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val timestamp: String,
    val content: String,
    val attachments: List<AttachmentItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisStage: String? = null,
    val reasoningSteps: List<NeuralReasoningStep> = emptyList(),
    val suggestedActions: List<String> = emptyList(),
    val tokenStats: String? = null,
    val modelTag: String = "ScenoviX Ultra 3.5"
)

data class AiCapability(
    val id: String,
    val title: String,
    val shortDesc: String,
    val iconName: String,
    val defaultPrompt: String,
    val targetType: AttachmentType? = null
)
