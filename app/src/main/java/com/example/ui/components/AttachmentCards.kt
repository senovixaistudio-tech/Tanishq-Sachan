package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AttachmentItem
import com.example.model.AttachmentType
import com.example.model.ValidationStatus
import com.example.ui.theme.CyberCoral
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ScenovixSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AttachmentPreviewCard(
    attachment: AttachmentItem,
    modifier: Modifier = Modifier,
    isPlayingAudio: Boolean = false,
    onTogglePlayAudio: ((AttachmentItem) -> Unit)? = null,
    onInspect: () -> Unit = {},
    onCancelUpload: (() -> Unit)? = null
) {
    when (attachment.type) {
        AttachmentType.IMAGE -> ImageAttachmentCard(
            attachment = attachment,
            modifier = modifier,
            onInspect = onInspect,
            onCancelUpload = onCancelUpload
        )
        AttachmentType.VIDEO -> VideoAttachmentCard(
            attachment = attachment,
            modifier = modifier,
            onInspect = onInspect,
            onCancelUpload = onCancelUpload
        )
        AttachmentType.DOCUMENT -> DocumentAttachmentCard(
            attachment = attachment,
            modifier = modifier,
            onInspect = onInspect,
            onCancelUpload = onCancelUpload
        )
        AttachmentType.CAMERA -> ImageAttachmentCard(
            attachment = attachment,
            modifier = modifier,
            onInspect = onInspect,
            onCancelUpload = onCancelUpload
        )
        AttachmentType.VOICE -> VoiceAttachmentCard(
            attachment = attachment,
            modifier = modifier,
            isPlaying = isPlayingAudio,
            onTogglePlay = { onTogglePlayAudio?.invoke(attachment) },
            onInspect = onInspect,
            onCancelUpload = onCancelUpload
        )
    }
}

@Composable
fun ImageAttachmentCard(
    attachment: AttachmentItem,
    modifier: Modifier = Modifier,
    onInspect: () -> Unit,
    onCancelUpload: (() -> Unit)? = null
) {
    ScenovixGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onInspect() }
            .testTag("attachment_image_${attachment.id}"),
        shape = RoundedCornerShape(16.dp),
        borderColors = listOf(NeonCyan.copy(alpha = 0.5f), ElectricViolet.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF090B12))
            ) {
                if (attachment.drawableRes != null) {
                    Image(
                        painter = painterResource(id = attachment.drawableRes),
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (attachment.uri != null) {
                    AsyncImage(
                        model = attachment.uri,
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_sample_render),
                        contentDescription = attachment.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient scrim on bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC090B12))
                            )
                        )
                )

                // Top badges (Type + Resolution)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeonPillBadge(
                        text = "IMAGE • ${attachment.mimeType.substringAfterLast('/').uppercase()}",
                        accentColor = NeonCyan
                    )

                    if (attachment.resolution != null) {
                        NeonPillBadge(
                            text = attachment.resolution,
                            accentColor = ElectricViolet
                        )
                    }

                    if (onCancelUpload != null && attachment.isUploading) {
                        IconButton(
                            onClick = onCancelUpload,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .testTag("cancel_upload_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel upload",
                                tint = CyberCoral,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // If currently uploading or analyzing
                if (attachment.isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                progress = { attachment.uploadProgress },
                                color = NeonCyan,
                                trackColor = Color(0x3300E5FF),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Uploading ${(attachment.uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            // Info footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F121C))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${attachment.sizeFormatted} • Click to inspect neural layers",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MatrixEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun VideoAttachmentCard(
    attachment: AttachmentItem,
    modifier: Modifier = Modifier,
    onInspect: () -> Unit,
    onCancelUpload: (() -> Unit)? = null
) {
    ScenovixGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onInspect() }
            .testTag("attachment_video_${attachment.id}"),
        shape = RoundedCornerShape(16.dp),
        borderColors = listOf(ElectricViolet.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFF0A0C14))
            ) {
                // High-tech cinematic video backdrop
                Image(
                    painter = painterResource(id = R.drawable.img_sample_render),
                    contentDescription = attachment.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark grid filter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99080A10))
                )

                // Play action button with electric violet ring
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.35f))
                        .border(1.5.dp, ElectricViolet, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeonPillBadge(
                        text = "VIDEO • ${attachment.mimeType.substringAfterLast('/').uppercase()}",
                        accentColor = ElectricViolet
                    )

                    attachment.videoDuration?.let { duration ->
                        NeonPillBadge(
                            text = duration,
                            accentColor = NeonCyan
                        )
                    }
                }

                // Simulated telemetry audio waveform bar at bottom of video
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xAA0B0D16))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "H.265 • 60 FPS • Keyframe Spatial Embedding",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = NeonCyan.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "00:00 / ${attachment.videoDuration ?: "01:14"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextSecondary
                    )
                }
            }

            // Info footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F121C))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = ElectricViolet,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${attachment.sizeFormatted} • Tap to view temporal frames",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MatrixEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DocumentAttachmentCard(
    attachment: AttachmentItem,
    modifier: Modifier = Modifier,
    onInspect: () -> Unit,
    onCancelUpload: (() -> Unit)? = null
) {
    val fileExt = attachment.name.substringAfterLast('.', "DOC").uppercase()
    val badgeColor = when (fileExt) {
        "PDF" -> Color(0xFFFF4D4F)
        "XLSX" -> MatrixEmerald
        "DOCX" -> Color(0xFF1890FF)
        "PPTX" -> Color(0xFFFA8C16)
        else -> NeonCyan
    }

    ScenovixGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onInspect() }
            .testTag("attachment_doc_${attachment.id}"),
        shape = RoundedCornerShape(14.dp),
        borderColors = listOf(badgeColor.copy(alpha = 0.45f), ElectricViolet.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F121C))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // File icon box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = fileExt,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = attachment.sizeFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (attachment.pageCount != null) {
                            Text(
                                text = " • ${attachment.pageCount} pages",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = attachment.validationMessage,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = MatrixEmerald.copy(alpha = 0.85f)
                    )
                }
            }

            if (attachment.isUploading) {
                CircularProgressIndicator(
                    progress = { attachment.uploadProgress },
                    color = NeonCyan,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                NeonPillBadge(text = "PARSED", accentColor = MatrixEmerald)
            }
        }
    }
}

@Composable
fun StagedAttachmentChip(
    attachment: AttachmentItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (attachment.type) {
        AttachmentType.IMAGE -> NeonCyan
        AttachmentType.VIDEO -> ElectricViolet
        AttachmentType.DOCUMENT -> Color(0xFFFFB703)
        AttachmentType.CAMERA -> NeonCyan
        AttachmentType.VOICE -> MatrixEmerald
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131726))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("staged_attachment_${attachment.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (attachment.isUploading) {
                CircularProgressIndicator(
                    progress = { attachment.uploadProgress },
                    color = accentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = when (attachment.type) {
                        AttachmentType.IMAGE -> Icons.Default.Image
                        AttachmentType.VIDEO -> Icons.Default.Movie
                        AttachmentType.DOCUMENT -> Icons.Default.Description
                        AttachmentType.CAMERA -> Icons.Default.Image
                        AttachmentType.VOICE -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 130.dp)
                )
                Text(
                    text = "${attachment.sizeFormatted} • ${(attachment.uploadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .testTag("remove_staged_${attachment.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove attachment",
                    tint = TextPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceAttachmentCard(
    attachment: AttachmentItem,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onTogglePlay: (() -> Unit)? = null,
    onInspect: () -> Unit,
    onCancelUpload: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    ScenovixGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onInspect() }
            .testTag("attachment_voice_${attachment.id}"),
        shape = RoundedCornerShape(16.dp),
        borderColors = listOf(
            if (isPlaying) NeonCyan.copy(alpha = 0.7f) else ElectricViolet.copy(alpha = 0.5f),
            NeonCyan.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0F19))
                .padding(14.dp)
        ) {
            // Header: Icon, Name, Duration, and Play Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Glowing circular play button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) NeonCyan.copy(alpha = 0.25f) else ElectricViolet.copy(alpha = 0.2f)
                            )
                            .border(
                                1.2.dp,
                                if (isPlaying) NeonCyan else ElectricViolet,
                                CircleShape
                            )
                            .clickable { onTogglePlay?.invoke() }
                            .testTag("voice_play_toggle_${attachment.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) NeonCyan else TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = attachment.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isPlaying) {
                                PulsingBeacon(color = NeonCyan, dotSize = 5.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${attachment.audioDuration ?: "00:08"} • ${attachment.sizeFormatted}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                            Text(
                                text = " • 44.1 kHz AAC",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = NeonCyan.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                NeonPillBadge(
                    text = if (isPlaying) "PLAYING" else "VOICE MEMO",
                    accentColor = if (isPlaying) NeonCyan else ElectricViolet
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Waveform Bar Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF080A12))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val barHeights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 0.8f, 0.5f, 1.0f, 0.4f, 0.7f, 0.9f, 0.5f, 0.3f, 0.8f, 0.6f, 0.4f, 0.7f, 0.5f)
                barHeights.forEachIndexed { i, baseH ->
                    val dynamicH = if (isPlaying) {
                        (baseH + kotlin.math.sin(wavePhase * 6.28 + i).toFloat() * 0.3f).coerceIn(0.15f, 1.0f)
                    } else {
                        baseH * 0.6f
                    }
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height((20 * dynamicH).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isPlaying) {
                                    Brush.verticalGradient(listOf(NeonCyan, ElectricViolet))
                                } else {
                                    SolidColor(ElectricViolet.copy(alpha = 0.4f))
                                }
                            )
                    )
                }
            }

            // Transcribed Text Accordion (Voice-to-Text result)
            attachment.transcription?.takeIf { it.isNotBlank() }?.let { transcript ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF111422))
                        .border(0.8.dp, Color(0xFF23293D), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MatrixEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speech-To-Text Transcription",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MatrixEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$transcript\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
