package com.shanawaz.tvlauncher.theme

import androidx.compose.ui.graphics.Color

/**
 * macOS-inspired Liquid Glass Color Palette for TV LAUNCHER
 * Primary Background: #080B14
 * Secondary Background: #111827
 * Glass Surface: Translucent White
 * Primary Text: #FFFFFF
 * Secondary Text: #AAB4C8
 * Accent: Icy Blue (#38BDF8)
 */
object LiquidGlassColors {
    val BackgroundPrimary = Color(0xFF080B14)
    val BackgroundSecondary = Color(0xFF111827)
    val GlassSurface = Color(0x1AFFFFFF)        // 10% White
    val GlassSurfaceElevated = Color(0x29FFFFFF) // 16% White
    val GlassBorder = Color(0x33FFFFFF)         // 20% White subtle border
    val GlassBorderFocused = Color(0xFF38BDF8)  // High-contrast Icy Blue focus border

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFAAB4C8)
    val TextMuted = Color(0xFF64748B)

    // Selectable Accent Presets
    val AccentIcyBlue = Color(0xFF38BDF8)
    val AccentArcticCyan = Color(0xFF22D3EE)
    val AccentFrostIndigo = Color(0xFF818CF8)
    val AccentEmeraldGlass = Color(0xFF34D399)
    val AccentAmberGlow = Color(0xFFFBBF24)

    val StatusSuccess = Color(0xFF10B981)
    val StatusWarning = Color(0xFFF59E0B)
    val StatusError = Color(0xFFEF4444)

    fun fromHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            AccentIcyBlue
        }
    }
}
