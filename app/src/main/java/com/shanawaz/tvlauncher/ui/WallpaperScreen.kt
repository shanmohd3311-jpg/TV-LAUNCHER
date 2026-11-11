package com.shanawaz.tvlauncher.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.data.UserLauncherSettings
import com.shanawaz.tvlauncher.manager.WallpaperPreset
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * Wallpaper Customisation Screen:
 * - Built-in dark, icy blue gradient, and cinematic 4K wallpapers
 * - Preview before applying
 * - Custom image selection via Android Storage Access Framework (ACTION_OPEN_DOCUMENT)
 * - Restore default wallpaper
 */
@Composable
fun WallpaperScreen(
    settings: UserLauncherSettings,
    presets: List<WallpaperPreset>,
    onSelectPreset: (String) -> Unit,
    onSelectCustomUri: (Uri) -> Unit
) {
    var previewPreset by remember {
        mutableStateOf(presets.find { it.id == settings.wallpaperId } ?: presets.first())
    }

    val safDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onSelectCustomUri(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Wallpaper Studio",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Preview and apply 4K Liquid Glass backdrops or select a custom image via Android Storage Access Framework.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FocusableActionButton(
                    label = "Choose Custom Image (SAF)",
                    primary = false,
                    onClick = { safDocumentLauncher.launch(arrayOf("image/*")) }
                )
                FocusableActionButton(
                    label = "Restore Default Wallpaper",
                    primary = false,
                    onClick = {
                        previewPreset = presets.first()
                        onSelectPreset(presets.first().id)
                    }
                )
            }
        }

        // Live Preview Stage
        GlassCard(
            isFocused = false,
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    previewPreset.primaryColor,
                                    previewPreset.secondaryColor,
                                    previewPreset.tertiaryColor
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "LIVE PREVIEW · ${previewPreset.category.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidGlassColors.AccentIcyBlue
                        )
                        Text(
                            text = previewPreset.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = previewPreset.subtitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (settings.wallpaperId == previewPreset.id)
                            "Currently applied to TV LAUNCHER Home"
                        else
                            "Previewing ${previewPreset.title}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FocusableActionButton(
                        label = if (settings.wallpaperId == previewPreset.id) "Applied" else "Apply Wallpaper",
                        primary = settings.wallpaperId != previewPreset.id,
                        onClick = { onSelectPreset(previewPreset.id) }
                    )
                }
            }
        }

        // Wallpaper Presets Grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { preset ->
                GlassCard(
                    isFocused = previewPreset.id == preset.id,
                    cornerRadius = 22.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = preset.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidGlassColors.AccentIcyBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FocusableActionButton(
                            label = if (settings.wallpaperId == preset.id) "Active" else "Preview",
                            primary = previewPreset.id == preset.id,
                            onClick = { previewPreset = preset }
                        )
                    }
                }
            }
        }
    }
}
