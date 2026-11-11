package com.shanawaz.tvlauncher.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class LauncherThemeMode {
    DARK, LIGHT, AUTO
}

data class LiquidGlassStyleConfig(
    val themeMode: LauncherThemeMode = LauncherThemeMode.DARK,
    val glassIntensity: Float = 0.75f,
    val accentColor: Color = LiquidGlassColors.AccentIcyBlue,
    val reducedAnimations: Boolean = false,
    val lowPerformanceMode: Boolean = false,
    val uiScale: Float = 1.0f,
    val safeAreaHorizontalDp: Int = 48,
    val safeAreaVerticalDp: Int = 28
)

val LocalLiquidGlassConfig = staticCompositionLocalOf { LiquidGlassStyleConfig() }

@Composable
fun TVLauncherTheme(
    config: LiquidGlassStyleConfig = LiquidGlassStyleConfig(),
    content: @Composable () -> Unit
) {
    val useDark = when (config.themeMode) {
        LauncherThemeMode.DARK -> true
        LauncherThemeMode.LIGHT -> false
        LauncherThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDark) {
        darkColorScheme(
            primary = config.accentColor,
            secondary = LiquidGlassColors.AccentArcticCyan,
            background = LiquidGlassColors.BackgroundPrimary,
            surface = LiquidGlassColors.BackgroundSecondary,
            onPrimary = Color(0xFF080B14),
            onBackground = LiquidGlassColors.TextPrimary,
            onSurface = LiquidGlassColors.TextPrimary
        )
    } else {
        lightColorScheme(
            primary = config.accentColor,
            secondary = LiquidGlassColors.AccentArcticCyan,
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A)
        )
    }

    CompositionLocalProvider(LocalLiquidGlassConfig provides config) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TVLauncherTypography,
            content = content
        )
    }
}
