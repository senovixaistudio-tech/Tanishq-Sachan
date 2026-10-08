package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttachmentType
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ScenovixSurfaceGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class AttachmentOptionItem(
    val type: AttachmentType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

val AttachmentOptions = listOf(
    AttachmentOptionItem(
        type = AttachmentType.IMAGE,
        title = "Upload Image",
        subtitle = "JPG • PNG • WEBP",
        icon = Icons.Default.Image,
        accentColor = NeonCyan
    ),
    AttachmentOptionItem(
        type = AttachmentType.VIDEO,
        title = "Upload Video",
        subtitle = "MP4 • MOV • WEBM",
        icon = Icons.Default.Movie,
        accentColor = ElectricViolet
    ),
    AttachmentOptionItem(
        type = AttachmentType.DOCUMENT,
        title = "Upload Files",
        subtitle = "PDF • DOCX • XLSX • TXT",
        icon = Icons.Default.Description,
        accentColor = Color(0xFFFFB703)
    ),
    AttachmentOptionItem(
        type = AttachmentType.CAMERA,
        title = "Camera",
        subtitle = "Instant High-Res Sensor",
        icon = Icons.Default.PhotoCamera,
        accentColor = Color(0xFF00FFA3)
    ),
    AttachmentOptionItem(
        type = AttachmentType.VOICE,
        title = "Voice Input",
        subtitle = "Neural Audio Frequency",
        icon = Icons.Default.Mic,
        accentColor = Color(0xFF3B82F6)
    )
)

data class ShowcasePreset(
    val title: String,
    val fileType: String,
    val size: String,
    val prompt: String,
    val type: AttachmentType
)

val ShowcasePresets = listOf(
    ShowcasePreset(
        title = "🛰️ Drone Thermal Scan",
        fileType = "4K JPG",
        size = "4.8 MB",
        prompt = "Analyze thermal anomalies and structural elevation in this drone capture.",
        type = AttachmentType.IMAGE
    ),
    ShowcasePreset(
        title = "🎬 Autonomous Telemetry",
        fileType = "H.265 MP4",
        size = "18.2 MB",
        prompt = "Identify moving collision vectors and calculate frame velocity.",
        type = AttachmentType.VIDEO
    ),
    ShowcasePreset(
        title = "📄 Quantum LLM Whitepaper",
        fileType = "PDF • 28 pgs",
        size = "3.2 MB",
        prompt = "Extract key mathematical theorems and summarize cross-attention scaling.",
        type = AttachmentType.DOCUMENT
    ),
    ShowcasePreset(
        title = "📊 Financial Balance Sheet",
        fileType = "XLSX • 14 sheets",
        size = "1.5 MB",
        prompt = "Parse Q3 EBITDA growth metrics and generate risk sensitivity table.",
        type = AttachmentType.DOCUMENT
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingAttachmentPanel(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectOption: (AttachmentType) -> Unit,
    onSelectPreset: (ShowcasePreset) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0C14),
        scrimColor = Color(0x99000000),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NeonCyan.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("floating_attachment_panel")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Multimodal Ingestion Dock",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "High-fidelity tensor parsing & zero-loss encoding",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_attachment_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Attachment Options
            AttachmentOptions.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF101320))
                        .border(
                            1.dp,
                            option.accentColor.copy(alpha = 0.35f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onSelectOption(option.type)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("attachment_option_${option.type.name}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(option.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = option.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }

                        NeonPillBadge(
                            text = "SELECT",
                            accentColor = option.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Realistic Showcase Presets (One-tap demo workflows)
            Text(
                text = "⚡ INSTANT SHOWCASE PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ShowcasePresets) { preset ->
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F121C))
                            .border(
                                0.8.dp,
                                Brush.linearGradient(
                                    listOf(NeonCyan.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.4f))
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectPreset(preset)
                                onDismiss()
                            }
                            .padding(12.dp)
                            .testTag("preset_${preset.title}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = TextPrimary
                                )
                                NeonPillBadge(
                                    text = preset.fileType,
                                    accentColor = ElectricViolet
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = preset.prompt,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = TextSecondary,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Size: ${preset.size} • Tap to load & analyze",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = NeonCyan.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
