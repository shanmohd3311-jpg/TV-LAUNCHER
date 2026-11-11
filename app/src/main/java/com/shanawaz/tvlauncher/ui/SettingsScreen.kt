package com.shanawaz.tvlauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.data.UserLauncherSettings
import com.shanawaz.tvlauncher.theme.LauncherThemeMode
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * Dedicated Settings Dashboard:
 * - Appearance (Dark/Light/Auto theme, Glass intensity, Accent colour, Animation & Low-perf preferences)
 * - Launcher Settings (Dual Mode Launcher Mode setup, Default Home instructions, Clock 12h/24h, Hidden apps, Screensaver)
 * - Display (UI scaling, Overscan safe-area padding)
 * - About & Reset (Reset appearance, Reset favourites, Complete local factory reset with confirmation)
 */
@Composable
fun SettingsScreen(
    settings: UserLauncherSettings,
    hiddenApps: List<TVApp>,
    onUnhideApp: (TVApp) -> Unit,
    onSelectTheme: (LauncherThemeMode) -> Unit,
    onChangeGlassIntensity: (Float) -> Unit,
    onSelectAccent: (String) -> Unit,
    onChangeClockPrefs: (Boolean, Boolean) -> Unit,
    onChangePerformancePrefs: (Boolean, Boolean) -> Unit,
    onChangeDisplayPrefs: (Float, Int) -> Unit,
    onChangeScreensaverPrefs: (Boolean, Int) -> Unit,
    onOpenLauncherMode: () -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onResetAppearance: () -> Unit,
    onResetFavourites: () -> Unit,
    onResetAll: () -> Unit
) {
    var confirmFullReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Settings Dashboard",
            style = MaterialTheme.typography.displayMedium
        )

        // 1. Appearance Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Appearance & Liquid Glass Engine", style = MaterialTheme.typography.headlineMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = "Dark Theme",
                        primary = settings.themeMode == LauncherThemeMode.DARK,
                        onClick = { onSelectTheme(LauncherThemeMode.DARK) }
                    )
                    FocusableActionButton(
                        label = "Light Theme",
                        primary = settings.themeMode == LauncherThemeMode.LIGHT,
                        onClick = { onSelectTheme(LauncherThemeMode.LIGHT) }
                    )
                    FocusableActionButton(
                        label = "Auto Theme",
                        primary = settings.themeMode == LauncherThemeMode.AUTO,
                        onClick = { onSelectTheme(LauncherThemeMode.AUTO) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = "Glass: Subtle (50%)",
                        primary = settings.glassIntensity <= 0.55f,
                        onClick = { onChangeGlassIntensity(0.50f) }
                    )
                    FocusableActionButton(
                        label = "Glass: Balanced (75%)",
                        primary = settings.glassIntensity in 0.56f..0.85f,
                        onClick = { onChangeGlassIntensity(0.75f) }
                    )
                    FocusableActionButton(
                        label = "Glass: Frosted (95%)",
                        primary = settings.glassIntensity > 0.85f,
                        onClick = { onChangeGlassIntensity(0.95f) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = "Accent: Icy Blue",
                        primary = settings.accentHex == "#38BDF8",
                        onClick = { onSelectAccent("#38BDF8") }
                    )
                    FocusableActionButton(
                        label = "Accent: Arctic Cyan",
                        primary = settings.accentHex == "#22D3EE",
                        onClick = { onSelectAccent("#22D3EE") }
                    )
                    FocusableActionButton(
                        label = "Accent: Frost Indigo",
                        primary = settings.accentHex == "#818CF8",
                        onClick = { onSelectAccent("#818CF8") }
                    )
                    FocusableActionButton(
                        label = "Accent: Emerald Glass",
                        primary = settings.accentHex == "#34D399",
                        onClick = { onSelectAccent("#34D399") }
                    )
                }
            }
        }

        // 2. Dual Mode & Launcher Preferences
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Launcher Mode, Clock & Screensaver", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = "Configure LAUNCHER MODE (Default vs Normal App)",
                        primary = true,
                        onClick = onOpenLauncherMode
                    )
                    FocusableActionButton(
                        label = "SET AS DEFAULT LAUNCHER",
                        primary = false,
                        onClick = onOpenDefaultLauncherSettings
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = if (settings.use24HourClock) "Clock: 24-Hour" else "Clock: 12-Hour",
                        primary = false,
                        onClick = { onChangeClockPrefs(!settings.use24HourClock, settings.showClockSeconds) }
                    )
                    FocusableActionButton(
                        label = if (settings.showClockSeconds) "Seconds: Shown" else "Seconds: Hidden",
                        primary = false,
                        onClick = { onChangeClockPrefs(settings.use24HourClock, !settings.showClockSeconds) }
                    )
                    FocusableActionButton(
                        label = if (settings.screensaverEnabled)
                            "Screensaver: ${settings.screensaverDelayMinutes} min"
                        else
                            "Screensaver: Off",
                        primary = settings.screensaverEnabled,
                        onClick = {
                            onChangeScreensaverPrefs(
                                !settings.screensaverEnabled,
                                settings.screensaverDelayMinutes
                            )
                        }
                    )
                }
            }
        }

        // 3. Display, Scaling & Performance Optimisation
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Display, Safe-Area & Performance", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = if (settings.reducedAnimations) "Reduced Motion: ON" else "Reduced Motion: OFF",
                        primary = settings.reducedAnimations,
                        onClick = {
                            onChangePerformancePrefs(!settings.reducedAnimations, settings.lowPerformanceMode)
                        }
                    )
                    FocusableActionButton(
                        label = if (settings.lowPerformanceMode) "Low-Memory TV Mode: ON" else "Low-Memory TV Mode: OFF",
                        primary = settings.lowPerformanceMode,
                        onClick = {
                            onChangePerformancePrefs(settings.reducedAnimations, !settings.lowPerformanceMode)
                        }
                    )
                    FocusableActionButton(
                        label = "UI Scale: ${(settings.uiScale * 100).toInt()}%",
                        primary = false,
                        onClick = {
                            val nextScale = when {
                                settings.uiScale < 1.0f -> 1.0f
                                settings.uiScale == 1.0f -> 1.1f
                                else -> 0.9f
                            }
                            onChangeDisplayPrefs(nextScale, settings.safeAreaMarginDp)
                        }
                    )
                }
            }
        }

        // 4. Hidden Applications Management
        if (hiddenApps.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hidden Applications (${hiddenApps.size})",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        hiddenApps.forEach { hiddenApp ->
                            FocusableActionButton(
                                label = "Unhide ${hiddenApp.appName}",
                                primary = false,
                                onClick = { onUnhideApp(hiddenApp) }
                            )
                        }
                    }
                }
            }
        }

        // 5. About & Reset Controls
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("About & Reset", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusableActionButton(
                        label = "About TV LAUNCHER (Developer: Shanawaz)",
                        primary = true,
                        onClick = onOpenAbout
                    )
                    FocusableActionButton(
                        label = "Reset Appearance",
                        primary = false,
                        onClick = onResetAppearance
                    )
                    FocusableActionButton(
                        label = "Reset Favourites",
                        primary = false,
                        onClick = onResetFavourites
                    )
                    if (!confirmFullReset) {
                        FocusableActionButton(
                            label = "Complete Settings Reset",
                            primary = false,
                            onClick = { confirmFullReset = true }
                        )
                    } else {
                        FocusableActionButton(
                            label = "Confirm Full Factory Reset",
                            primary = true,
                            onClick = {
                                onResetAll()
                                confirmFullReset = false
                            }
                        )
                    }
                }
            }
        }
    }
}
