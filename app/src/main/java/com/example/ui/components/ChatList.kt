package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AttachmentItem
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatMessageList(
    messages: List<ChatMessage>,
    onInspectAttachment: (AttachmentItem) -> Unit,
    onQuickPromptClick: (String) -> Unit,
    onOpenAttachmentSheet: () -> Unit,
    playingAudioId: String? = null,
    onTogglePlayAudio: ((AttachmentItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty()) {
        EmptyChatState(
            onOpenAttachmentSheet = onOpenAttachmentSheet,
            onQuickPromptClick = onQuickPromptClick,
            modifier = modifier
        )
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onInspectAttachment = onInspectAttachment,
                    onActionClick = onQuickPromptClick,
                    playingAudioId = playingAudioId,
                    onTogglePlayAudio = onTogglePlayAudio
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onInspectAttachment: (AttachmentItem) -> Unit,
    onActionClick: (String) -> Unit,
    playingAudioId: String? = null,
    onTogglePlayAudio: ((AttachmentItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_message_${message.id}"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            UserMessageBubble(
                message = message,
                onInspectAttachment = onInspectAttachment,
                playingAudioId = playingAudioId,
                onTogglePlayAudio = onTogglePlayAudio
            )
        } else {
            AiResponseArea(
                message = message,
                onInspectAttachment = onInspectAttachment,
                onActionClick = onActionClick,
                playingAudioId = playingAudioId,
                onTogglePlayAudio = onTogglePlayAudio
            )
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessage,
    onInspectAttachment: (AttachmentItem) -> Unit,
    playingAudioId: String? = null,
    onTogglePlayAudio: ((AttachmentItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(0.92f),
        horizontalAlignment = Alignment.End
    ) {
        // Render attachments if any
        if (message.attachments.isNotEmpty()) {
            message.attachments.forEach { att ->
                AttachmentPreviewCard(
                    attachment = att,
                    isPlayingAudio = playingAudioId == att.id,
                    onTogglePlayAudio = onTogglePlayAudio,
                    onInspect = { onInspectAttachment(att) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Bubble text
        if (message.content.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(Color(0xFF141828))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${message.timestamp} • You",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted
        )
    }
}

@Composable
fun AiResponseArea(
    message: ChatMessage,
    onInspectAttachment: (AttachmentItem) -> Unit,
    onActionClick: (String) -> Unit,
    playingAudioId: String? = null,
    onTogglePlayAudio: ((AttachmentItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(0.96f),
        horizontalAlignment = Alignment.Start
    ) {
        // Header with ScenoviX Avatar & Model Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F121E))
                    .border(1.dp, NeonCyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_scenovix_logo),
                    contentDescription = "ScenoviX AI",
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "ScenoviX AI",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.width(6.dp))

            NeonPillBadge(text = message.modelTag, accentColor = ElectricViolet)

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextMuted
            )
        }

        // Analyzing State Banner (if active)
        if (message.isAnalyzing) {
            NeuralAnalyzingBanner(
                isAnalyzing = true,
                stage = message.analysisStage,
                fileName = message.attachments.firstOrNull()?.name ?: "multimodal_feed",
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Attachments inside AI response if present
        if (message.attachments.isNotEmpty()) {
            message.attachments.forEach { att ->
                AttachmentPreviewCard(
                    attachment = att,
                    isPlayingAudio = playingAudioId == att.id,
                    onTogglePlayAudio = onTogglePlayAudio,
                    onInspect = { onInspectAttachment(att) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // AI Response Glass Container
        ScenovixGlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
            backgroundColor = Color(0xFF0B0D16),
            borderColors = listOf(Color(0xFF22273C), ElectricViolet.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Formatted Content
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 22.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = TextPrimary
                )

                // Reasoning Chain (if present)
                if (message.reasoningSteps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    NeuralReasoningChain(steps = message.reasoningSteps)
                }

                // Telemetry Footer
                if (message.tokenStats != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.tokenStats,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = NeonCyan.copy(alpha = 0.8f)
                        )

                        Row {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { }
                            )
                        }
                    }
                }
            }
        }

        // Follow-up Suggestion Chips
        if (message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.suggestedActions.forEach { action ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF101322))
                            .border(0.8.dp, ElectricViolet.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .clickable { onActionClick(action) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "✦ $action",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(
    onOpenAttachmentSheet: () -> Unit,
    onQuickPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Holographic Emblem
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D0F1C))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(NeonCyan, ElectricViolet)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_scenovix_logo),
                contentDescription = "ScenoviX AI Emblem",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ScenoviX AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Think. Create. Understand.",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Next-Generation Multimodal Cognitive Engine\nAttach Images, Videos, or Files for instant neural synthesis.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 17.sp
            ),
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Drag & Drop / Tap Ingestion Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C0E18))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onOpenAttachmentSheet() }
                .padding(18.dp)
                .testTag("empty_state_upload_card")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drop files here or tap + to attach",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Supports 4K JPG • MP4 Video • PDF • XLSX • DOCX",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "EXPLORE SAMPLE COGNITIVE WORKFLOWS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        val sampleWorkflows = listOf(
            "🛰️ Load Drone Scan & analyze thermal elevation",
            "🎬 Decode Autonomous Driving video stream",
            "📑 Read Quantum AI whitepaper & extract formulas"
        )

        sampleWorkflows.forEach { sample ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101322))
                    .border(0.8.dp, Color(0xFF22273D), RoundedCornerShape(12.dp))
                    .clickable { onQuickPromptClick(sample) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = sample,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }
        }
    }
}
