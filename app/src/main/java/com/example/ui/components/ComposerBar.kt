package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttachmentItem
import com.example.model.AttachmentType
import com.example.ui.theme.CyberCoral
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ScenovixBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickActionPill(
    val title: String,
    val prompt: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

val QuickCapabilities = listOf(
    QuickActionPill("Analyze Image", "Please analyze this image in detail and extract all key visual patterns.", Icons.Default.Image, NeonCyan),
    QuickActionPill("Understand Video", "Break down the video into key temporal segments and detect anomalies.", Icons.Default.Movie, ElectricViolet),
    QuickActionPill("Read Document", "Parse the document and extract structured insights and sections.", Icons.Default.Description, Color(0xFFFFB703)),
    QuickActionPill("Summarize", "Provide a concise executive summary of the content.", Icons.Default.Summarize, Color(0xFF00FFA3)),
    QuickActionPill("Extract Data", "Extract all numerical tables, metrics, and structured key-value pairs.", Icons.Default.Analytics, Color(0xFF3B82F6)),
    QuickActionPill("Ask About File", "What are the core conclusions and actionable takeaways in this file?", Icons.Default.QuestionAnswer, ElectricViolet)
)

@Composable
fun FuturisticComposer(
    text: String,
    onTextChange: (String) -> Unit,
    stagedAttachments: List<AttachmentItem>,
    onRemoveStagedAttachment: (String) -> Unit,
    onOpenAttachmentSheet: () -> Unit,
    onQuickAttach: (AttachmentType) -> Unit,
    onQuickCapabilityClicked: (String) -> Unit,
    onSend: () -> Unit,
    isVoiceRecording: Boolean,
    voiceDurationFormatted: String,
    voiceAmplitude: Float,
    voiceLiveTranscript: String,
    onToggleVoice: () -> Unit,
    onAttachAudioClip: () -> Unit,
    onInsertTranscriptOnly: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onSelectVoicePreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showInlineQuickBar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ScenovixBackground.copy(alpha = 0.96f))
            .drawBehind {
                // Top border glow line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonCyan.copy(alpha = 0.35f),
                            ElectricViolet.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 12.dp, start = 14.dp, end = 14.dp)
            .testTag("futuristic_composer")
    ) {
        // 1. Futuristic Capabilities Bar (Horizontal scrolling pills)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickCapabilities.forEach { cap ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F121E))
                        .border(
                            0.8.dp,
                            cap.color.copy(alpha = 0.45f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onQuickCapabilityClicked(cap.prompt) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("capability_${cap.title}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = cap.icon,
                            contentDescription = null,
                            tint = cap.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cap.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 2. Staged Attachments Preview Carousel (if any attached before sending)
        AnimatedVisibility(
            visible = stagedAttachments.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "READY FOR INGESTION (${stagedAttachments.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = NeonCyan
                    )
                    Text(
                        text = "Encrypted • Neural Ingestion",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stagedAttachments) { item ->
                        StagedAttachmentChip(
                            attachment = item,
                            onRemove = { onRemoveStagedAttachment(item.id) }
                        )
                    }
                }
            }
        }

        // 3. Inline Quick Drawer: ＋ → Image | Video | Files | Camera | Voice
        AnimatedVisibility(
            visible = showInlineQuickBar,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F121F))
                    .border(0.8.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickAttachmentIconBtn("Image", Icons.Default.Image, NeonCyan) {
                    showInlineQuickBar = false
                    onQuickAttach(AttachmentType.IMAGE)
                }
                QuickAttachmentIconBtn("Video", Icons.Default.Movie, ElectricViolet) {
                    showInlineQuickBar = false
                    onQuickAttach(AttachmentType.VIDEO)
                }
                QuickAttachmentIconBtn("Files", Icons.Default.Description, Color(0xFFFFB703)) {
                    showInlineQuickBar = false
                    onQuickAttach(AttachmentType.DOCUMENT)
                }
                QuickAttachmentIconBtn("Camera", Icons.Default.AutoAwesome, Color(0xFF00FFA3)) {
                    showInlineQuickBar = false
                    onQuickAttach(AttachmentType.CAMERA)
                }
                QuickAttachmentIconBtn("Voice", Icons.Default.Mic, Color(0xFF3B82F6)) {
                    showInlineQuickBar = false
                    onQuickAttach(AttachmentType.VOICE)
                }
            }
        }

        // Voice-To-Text Recording Drawer (Microphone Active)
        VoiceRecordingDrawer(
            isRecording = isVoiceRecording,
            durationFormatted = voiceDurationFormatted,
            currentAmplitude = voiceAmplitude,
            liveTranscript = voiceLiveTranscript,
            onAttachAudioClip = onAttachAudioClip,
            onInsertTranscriptOnly = onInsertTranscriptOnly,
            onCancelRecording = onCancelVoiceRecording,
            onSelectVoicePreset = onSelectVoicePreset
        )

        // 4. Main Futuristic Composer Input Field Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Multifunctional Attachment Button (＋)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF101322))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(NeonCyan, ElectricViolet)
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        // Toggle inline shortcuts or open sheet
                        showInlineQuickBar = !showInlineQuickBar
                    }
                    .testTag("multifunctional_attachment_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach Files",
                    tint = if (showInlineQuickBar) NeonCyan else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Glassmorphic Input Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0E111C))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF262C44),
                                NeonCyan.copy(alpha = 0.25f),
                                Color(0xFF262C44)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_text_input"),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(NeonCyan),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Ask ScenoviX or query attached files…",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    fontSize = 13.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Voice Recording Mic Button
            IconButton(
                onClick = onToggleVoice,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isVoiceRecording) CyberCoral.copy(alpha = 0.25f) else Color(0xFF0F121F)
                    )
                    .border(
                        1.2.dp,
                        if (isVoiceRecording) CyberCoral else NeonCyan.copy(alpha = 0.4f),
                        CircleShape
                    )
                    .testTag("voice_input_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isVoiceRecording) "Stop Voice Recording" else "Record Voice to Text",
                    tint = if (isVoiceRecording) CyberCoral else NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Glowing Send Button
            val canSend = text.isNotBlank() || stagedAttachments.isNotEmpty()
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) {
                            Brush.linearGradient(listOf(NeonCyan, ElectricViolet))
                        } else {
                            SolidColor(Color(0xFF131724))
                        }
                    )
                    .border(
                        1.dp,
                        if (canSend) NeonCyan.copy(alpha = 0.8f) else Color(0xFF1E2336),
                        CircleShape
                    )
                    .clickable(enabled = canSend) { onSend() }
                    .testTag("send_message_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (canSend) ScenovixBackground else TextMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickAttachmentIconBtn(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("quick_attach_$label")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextSecondary
        )
    }
}
