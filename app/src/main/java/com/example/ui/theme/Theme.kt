package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ScenovixColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ScenovixBackground,
    primaryContainer = NeonCyanSubtle,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = TextPrimary,
    secondaryContainer = ElectricVioletSubtle,
    onSecondaryContainer = ElectricViolet,
    tertiary = MatrixEmerald,
    onTertiary = ScenovixBackground,
    background = ScenovixBackground,
    onBackground = TextPrimary,
    surface = ScenovixSurface,
    onSurface = TextPrimary,
    surfaceVariant = ScenovixSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = ScenovixSurfaceGlass,
    outline = GlassBorderStroke,
    outlineVariant = GlassBorderHighlight,
    error = CyberCoral,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // ScenoviX AI is cinematic dark by design
    dynamicColor: Boolean = false, // Keep high-fidelity neon cybernetic palette
    content: @Composable () -> Unit
) {
    val colorScheme = ScenovixColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ScenovixBackground.toArgb()
                window.navigationBarColor = ScenovixBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
