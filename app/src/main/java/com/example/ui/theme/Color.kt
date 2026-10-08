package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cinematic Deep Space & Glassmorphism Palette
val ScenovixBackground = Color(0xFF07080D)
val ScenovixSurface = Color(0xFF0F111A)
val ScenovixSurfaceVariant = Color(0xFF161925)
val ScenovixSurfaceGlass = Color(0xCC0F111A)
val ScenovixSurfaceGlassLight = Color(0x991A1E2F)

// Neon Accents
val NeonCyan = Color(0xFF00E5FF)
val NeonCyanGlow = Color(0x4D00E5FF)
val NeonCyanSubtle = Color(0x1A00E5FF)

val ElectricViolet = Color(0xFF9D4EDD)
val ElectricVioletGlow = Color(0x4D9D4EDD)
val ElectricVioletSubtle = Color(0x1A9D4EDD)

val HologramBlue = Color(0xFF3B82F6)
val MatrixEmerald = Color(0xFF00FFA3)
val MatrixEmeraldSubtle = Color(0x1A00FFA3)
val CyberCoral = Color(0xFFFF3366)
val CyberAmber = Color(0xFFFFB703)

// Text & Monospace Tints
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFFA1A1AA)
val TextTertiary = Color(0xFF71717A)
val TextMuted = Color(0xFF52525B)

// Border Gradients
val GlassBorderStroke = Color(0x33383F55)
val GlassBorderHighlight = Color(0x6600E5FF)

// Dual Glow Gradient Brush
val ScenovixAccentGradient = Brush.horizontalGradient(
    colors = listOf(NeonCyan, ElectricViolet)
)

val ScenovixSurfaceGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF131724),
        Color(0xFF0A0C13)
    )
)

val ScenovixGlowRadial = Brush.radialGradient(
    colors = listOf(
        Color(0x3300E5FF),
        Color(0x159D4EDD),
        Color.Transparent
    )
)
