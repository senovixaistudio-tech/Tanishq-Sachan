package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCoral
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class WaveformVisualizerMode(val label: String) {
    BARS("QUANTUM BARS"),
    OSCILLOSCOPE("NEURAL SCOPE"),
    SPECTRUM("HARMONIC MATRIX")
}

@Composable
fun AudioWaveformVisualizer(
    amplitude: Float,
    rmsDb: Float,
    waveformHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    var visualizerMode by remember { mutableStateOf(WaveformVisualizerMode.BARS) }

    // Smooth continuous animation driver for fluid wave motion
    val infiniteTransition = rememberInfiniteTransition(label = "waveformAnimation")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseOffset"
    )

    val ambientGlowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientGlow"
    )

    // Decibel color status
    val dbColor by animateColorAsState(
        targetValue = when {
            rmsDb > -9f -> CyberCoral
            rmsDb > -24f -> NeonCyan
            else -> MatrixEmerald
        },
        label = "dbColor"
    )

    val peakPercentage = ((amplitude * 100).toInt()).coerceIn(5, 99)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070914))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.35f),
                        ElectricViolet.copy(alpha = 0.5f),
                        CyberCoral.copy(alpha = 0.35f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("audio_waveform_visualizer")
    ) {
        // 1. Telemetry HUD Header: DB Meter, Mode Switches, Sensitivity Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Decibel and Peak readout
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dbColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", rmsDb)} dB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = dbColor,
                    modifier = Modifier.testTag("waveform_db_meter")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• PEAK $peakPercentage%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
            }

            // Mode Selector Pills: [BARS | SCOPE | MATRIX]
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("waveform_mode_selector")
            ) {
                WaveformVisualizerMode.values().forEach { mode ->
                    val isSelected = visualizerMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricViolet.copy(alpha = 0.35f) else Color(0xFF101322))
                            .border(
                                width = 0.7.dp,
                                color = if (isSelected) NeonCyan else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { visualizerMode = mode }
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                            .testTag("waveform_mode_${mode.name.lowercase()}")
                    ) {
                        Text(
                            text = mode.label.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) NeonCyan else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Main Hardware-Accelerated Waveform Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF04060C))
                .testTag("waveform_canvas"),
            contentAlignment = Alignment.Center
        ) {
            when (visualizerMode) {
                WaveformVisualizerMode.BARS -> {
                    QuantumBarsCanvas(
                        amplitude = amplitude,
                        waveformHistory = waveformHistory,
                        phase = phaseOffset,
                        ambientGlow = ambientGlowIntensity
                    )
                }
                WaveformVisualizerMode.OSCILLOSCOPE -> {
                    NeuralOscilloscopeCanvas(
                        amplitude = amplitude,
                        waveformHistory = waveformHistory,
                        phase = phaseOffset
                    )
                }
                WaveformVisualizerMode.SPECTRUM -> {
                    HarmonicMatrixCanvas(
                        amplitude = amplitude,
                        waveformHistory = waveformHistory,
                        phase = phaseOffset
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Lower Calibration Scale & Acoustic Spectrum Metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "20 Hz",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextMuted.copy(alpha = 0.7f)
            )
            Text(
                text = "1 kHz",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextMuted.copy(alpha = 0.7f)
            )
            Text(
                text = "4 kHz",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextMuted.copy(alpha = 0.7f)
            )
            Text(
                text = "12 kHz",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextMuted.copy(alpha = 0.7f)
            )
            Text(
                text = "20 kHz",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextMuted.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Mode 1: Quantum Frequency Spectrum Bars
 * Dynamic mirrored frequency bars with floating peak caps with gravity decay physics.
 */
@Composable
private fun QuantumBarsCanvas(
    amplitude: Float,
    waveformHistory: List<Float>,
    phase: Float,
    ambientGlow: Float
) {
    // Peak cap tracking list for decay physics
    val barCount = 36
    val peakCaps = remember { mutableStateListOf<Float>().apply { repeat(barCount) { add(0.15f) } } }

    LaunchedEffect(amplitude, waveformHistory) {
        for (i in 0 until min(barCount, peakCaps.size)) {
            val historyVal = if (waveformHistory.isNotEmpty()) {
                val index = (i * waveformHistory.size / barCount).coerceIn(0, waveformHistory.size - 1)
                waveformHistory[index]
            } else amplitude

            val dist = abs(i - barCount / 2f) / (barCount / 2f)
            val bellFactor = 1f - (dist * 0.45f)
            val harmonic = (sin(phase + i * 0.4f) * 0.15f)
            val currentVal = ((historyVal * bellFactor + harmonic).coerceIn(0.1f, 1.0f))

            if (currentVal > peakCaps[i]) {
                peakCaps[i] = currentVal
            } else {
                peakCaps[i] = max(0.08f, peakCaps[i] - 0.045f) // Smooth gravity drop
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Center zero-crossing baseline
        drawLine(
            color = Color(0x2238BDF8),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1f
        )

        val barSlotWidth = width / barCount
        val barWidth = barSlotWidth * 0.65f

        for (i in 0 until barCount) {
            val historyVal = if (waveformHistory.isNotEmpty()) {
                val index = (i * waveformHistory.size / barCount).coerceIn(0, waveformHistory.size - 1)
                waveformHistory[index]
            } else amplitude

            val dist = abs(i - barCount / 2f) / (barCount / 2f)
            val bellFactor = 1f - (dist * 0.45f)
            val harmonic = (sin(phase + i * 0.4f) * 0.15f)
            val barAmp = ((historyVal * bellFactor + harmonic).coerceIn(0.1f, 1.0f))
            val barH = (height * 0.88f * barAmp).coerceAtLeast(3f)

            val x = i * barSlotWidth + (barSlotWidth - barWidth) / 2f
            val topY = centerY - barH / 2f

            // Mirrored vertical gradient for each bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberCoral.copy(alpha = 0.9f),
                        ElectricViolet,
                        NeonCyan
                    ),
                    startY = topY,
                    endY = topY + barH
                ),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Floating studio peak cap with gravity
            val peakH = (height * 0.88f * peakCaps.getOrElse(i) { barAmp }).coerceAtLeast(4f)
            val peakTopY = centerY - peakH / 2f
            drawRoundRect(
                color = NeonCyan,
                topLeft = Offset(x, peakTopY - 2.5f),
                size = Size(barWidth, 2f),
                cornerRadius = CornerRadius(1f, 1f)
            )
        }
    }
}

/**
 * Mode 2: Neural Continuous Oscilloscope
 * Fluid anti-aliased sinusoidal continuous bezier waveform with dual harmonics and neon underglow.
 */
@Composable
private fun NeuralOscilloscopeCanvas(
    amplitude: Float,
    waveformHistory: List<Float>,
    phase: Float
) {
    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Subtle cyber crosshair background ticks
        for (gridX in 0 until 9) {
            val gx = width * (gridX / 8f)
            drawLine(
                color = Color(0x1800F5FF),
                start = Offset(gx, 0f),
                end = Offset(gx, height),
                strokeWidth = 0.8f
            )
        }
        drawLine(
            color = Color(0x3000F5FF),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1f
        )

        // Path 1: Primary High-Resonance Acoustic Wave
        val primaryPath = Path()
        val fillPath = Path()
        val steps = 80
        val dx = width / steps

        primaryPath.moveTo(0f, centerY)
        fillPath.moveTo(0f, centerY)

        for (step in 0..steps) {
            val progress = step.toFloat() / steps
            val x = step * dx

            // Blend waveform history if available
            val historyFactor = if (waveformHistory.isNotEmpty()) {
                val idx = (progress * (waveformHistory.size - 1)).toInt().coerceIn(0, waveformHistory.size - 1)
                waveformHistory[idx]
            } else amplitude

            // Complex sinusoidal interference pattern
            val envelope = sin(progress * Math.PI.toFloat()) // Window function: 0 at edges, 1 at center
            val wave1 = sin(phase + progress * 14f) * 0.6f
            val wave2 = sin(phase * 1.8f + progress * 26f) * 0.4f
            val waveTotal = (wave1 + wave2) * envelope * (historyFactor * 0.9f + 0.15f)

            val y = centerY + waveTotal * (height * 0.42f)
            if (step == 0) {
                primaryPath.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                primaryPath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, centerY)
        fillPath.close()

        // Translucent neon energy fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.25f),
                    ElectricViolet.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // Primary glowing beam
        drawPath(
            path = primaryPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    NeonCyan,
                    ElectricViolet,
                    CyberCoral,
                    NeonCyan
                )
            ),
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Path 2: Harmonic Secondary Counter-Phase Wave (Echo / Resonance)
        val echoPath = Path()
        for (step in 0..steps) {
            val progress = step.toFloat() / steps
            val x = step * dx
            val envelope = sin(progress * Math.PI.toFloat())
            val echoWave = cos(-phase * 1.3f + progress * 18f) * envelope * (amplitude * 0.5f + 0.1f)
            val y = centerY + echoWave * (height * 0.35f)
            if (step == 0) echoPath.moveTo(x, y) else echoPath.lineTo(x, y)
        }

        drawPath(
            path = echoPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    CyberCoral.copy(alpha = 0.55f),
                    ElectricViolet.copy(alpha = 0.55f)
                )
            ),
            style = Stroke(
                width = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Mode 3: Harmonic Matrix / Multi-Spectral Ripples
 * High-density frequency bands with radial reflections and energetic particle nodes.
 */
@Composable
private fun HarmonicMatrixCanvas(
    amplitude: Float,
    waveformHistory: List<Float>,
    phase: Float
) {
    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        val columnCount = 48
        val colWidth = width / columnCount

        for (col in 0 until columnCount) {
            val progress = col.toFloat() / columnCount
            val x = col * colWidth

            val historyVal = if (waveformHistory.isNotEmpty()) {
                val idx = (progress * (waveformHistory.size - 1)).toInt().coerceIn(0, waveformHistory.size - 1)
                waveformHistory[idx]
            } else amplitude

            val mod = (sin(phase * 2f + col * 0.5f) * 0.25f)
            val hRatio = ((historyVal + mod).coerceIn(0.08f, 1.0f))
            val h = height * 0.85f * hRatio

            // Staggered matrix dots/bars
            val top = centerY - h / 2f
            val segments = 6
            val segHeight = h / segments

            for (s in 0 until segments) {
                val segY = top + s * segHeight
                val segColor = when (s) {
                    0, 5 -> CyberCoral
                    1, 4 -> ElectricViolet
                    else -> NeonCyan
                }

                drawRoundRect(
                    color = segColor.copy(alpha = 0.85f),
                    topLeft = Offset(x + 1f, segY + 0.8f),
                    size = Size(colWidth - 2f, segHeight - 1.6f),
                    cornerRadius = CornerRadius(1.2f, 1.2f)
                )
            }
        }
    }
}
