package com.shanawaz.tvlauncher.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.theme.LiquidGlassColors
import com.shanawaz.tvlauncher.theme.LocalLiquidGlassConfig

/**
 * Reusable macOS-inspired Liquid Glass Panel container.
 * Uses GPU-friendly translucent gradient layers, crisp 1dp specular border,
 * and smooth D-pad focus scale transitions.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    cornerRadius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val config = LocalLiquidGlassConfig.current
    val targetScale = if (isFocused && !config.reducedAnimations) 1.05f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = if (config.reducedAnimations) 0 else 160),
        label = "glassCardScale"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val baseAlpha = (0.10f * config.glassIntensity).coerceIn(0.05f, 0.30f)
    val focusedAlpha = (0.20f * config.glassIntensity).coerceIn(0.10f, 0.40f)

    val backgroundBrush = if (isFocused) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = focusedAlpha),
                config.accentColor.copy(alpha = 0.18f),
                LiquidGlassColors.BackgroundSecondary.copy(alpha = 0.85f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = baseAlpha + 0.04f),
                Color.White.copy(alpha = baseAlpha * 0.5f),
                LiquidGlassColors.BackgroundSecondary.copy(alpha = 0.72f)
            )
        )
    }

    val borderBrush = if (isFocused) {
        Brush.linearGradient(
            colors = listOf(
                config.accentColor,
                Color.White.copy(alpha = 0.9f),
                config.accentColor.copy(alpha = 0.7f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isFocused && !config.lowPerformanceMode) 18.dp else 4.dp,
                shape = shape,
                ambientColor = if (isFocused) config.accentColor else Color.Black,
                spotColor = if (isFocused) config.accentColor else Color.Black
            )
            .clip(shape)
            .background(brush = backgroundBrush, shape = shape)
            .border(
                BorderStroke(width = if (isFocused) 2.dp else 1.dp, brush = borderBrush),
                shape = shape
            ),
        content = content
    )
}
