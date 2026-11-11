package com.shanawaz.tvlauncher.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color

data class WallpaperPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val customUri: String? = null
)

/**
 * Manages built-in 4K-friendly Liquid Glass gradient wallpapers and custom user-selected
 * images persisted via Android Storage Access Framework (SAF).
 */
class WallpaperManager(private val context: Context) {

    val builtInWallpapers: List<WallpaperPreset> = listOf(
        WallpaperPreset(
            id = "liquid_glass_icy_blue",
            title = "Liquid Glass Icy Blue",
            subtitle = "Signature macOS-inspired obsidian & arctic cyan glass refraction",
            category = "Liquid Glass",
            primaryColor = Color(0xFF080B14),
            secondaryColor = Color(0xFF0C2D48),
            tertiaryColor = Color(0xFF38BDF8)
        ),
        WallpaperPreset(
            id = "midnight_aurora",
            title = "Midnight Borealis",
            subtitle = "Deep navy horizon with subtle indigo & cyan aurora ribbons",
            category = "Cinematic",
            primaryColor = Color(0xFF060913),
            secondaryColor = Color(0xFF1E1B4B),
            tertiaryColor = Color(0xFF22D3EE)
        ),
        WallpaperPreset(
            id = "obsidian_prism",
            title = "Obsidian Architectural Prism",
            subtitle = "Pure OLED dark slate with frosted white rim illumination",
            category = "Dark OLED",
            primaryColor = Color(0xFF080B14),
            secondaryColor = Color(0xFF111827),
            tertiaryColor = Color(0xFF64748B)
        ),
        WallpaperPreset(
            id = "cobalt_nebula",
            title = "Deep Cobalt Horizon",
            subtitle = "Smooth atmospheric blue gradient engineered for 60Hz TV clarity",
            category = "Blue Gradient",
            primaryColor = Color(0xFF090D16),
            secondaryColor = Color(0xFF172554),
            tertiaryColor = Color(0xFF60A5FA)
        )
    )

    /**
     * Takes persistable URI read permission from Android Storage Access Framework (ACTION_OPEN_DOCUMENT)
     * so custom user wallpapers survive device reboots without requiring broad storage permissions.
     */
    fun persistCustomWallpaperUri(uri: Uri): Result<String> {
        return runCatching {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            uri.toString()
        }
    }

    fun findById(id: String, customUri: String?): WallpaperPreset {
        if (id == "custom_user_uri" && !customUri.isNullOrBlank()) {
            return WallpaperPreset(
                id = "custom_user_uri",
                title = "Custom User Wallpaper",
                subtitle = "Selected via Android Storage Access Framework",
                category = "Custom",
                primaryColor = Color(0xFF080B14),
                secondaryColor = Color(0xFF111827),
                tertiaryColor = Color(0xFF38BDF8),
                customUri = customUri
            )
        }
        return builtInWallpapers.find { it.id == id } ?: builtInWallpapers.first()
    }
}
