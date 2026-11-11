package com.shanawaz.tvlauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.data.UserLauncherSettings
import com.shanawaz.tvlauncher.manager.DefaultHomeStatus
import com.shanawaz.tvlauncher.manager.OperatingModePreference
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * Dedicated Mode Selection & Compatibility Screen:
 * Title: LAUNCHER MODE
 * - Option 1: DEFAULT LAUNCHER ("Use TV LAUNCHER as your television's Home interface.")
 * - Option 2: NORMAL APPLICATION ("Open TV LAUNCHER manually without changing your current Home application.")
 * - Includes: Current mode status, "SET AS DEFAULT LAUNCHER" button, System settings shortcut,
 *   Compatibility status, and step-by-step instructions for unsupported/restricted devices.
 */
@Composable
fun LauncherModeScreen(
    settings: UserLauncherSettings,
    defaultHomeStatus: DefaultHomeStatus,
    onSelectMode: (OperatingModePreference) -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "LAUNCHER MODE",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = "Configure how TV LAUNCHER operates on your Android TV or Google TV device. Both modes share the full Liquid Glass dashboard.",
            style = MaterialTheme.typography.bodyLarge
        )

        // Current Mode & Platform Detection Status Card
        GlassCard(
            isFocused = false,
            cornerRadius = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Current Operating & System Default Status",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (defaultHomeStatus.isCurrentlyDefaultHome) {
                        "Active Mode: DEFAULT LAUNCHER (Verified as System Default Home)"
                    } else {
                        "Active Mode: ${
                            if (settings.operatingMode == OperatingModePreference.DEFAULT_LAUNCHER)
                                "DEFAULT LAUNCHER (Pending System Home Selection — Running in Normal App Fallback)"
                            else
                                "NORMAL APPLICATION (Opened Manually)"
                        }"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (defaultHomeStatus.isCurrentlyDefaultHome)
                        LiquidGlassColors.StatusSuccess
                    else
                        LiquidGlassColors.AccentIcyBlue
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Detected System Home Package: ${defaultHomeStatus.currentDefaultPackage ?: "Undetermined (Protected by OS)"}",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Compatibility Check: ${defaultHomeStatus.compatibilitySummary}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Option 1 & Option 2 Selection Cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ModeOptionCard(
                title = "DEFAULT LAUNCHER",
                description = "Use TV LAUNCHER as your television's Home interface.",
                badgeText = if (defaultHomeStatus.isCurrentlyDefaultHome) "Active System Home" else "Option 1",
                isSelected = settings.operatingMode == OperatingModePreference.DEFAULT_LAUNCHER,
                onClick = { onSelectMode(OperatingModePreference.DEFAULT_LAUNCHER) },
                modifier = Modifier.weight(1f)
            )

            ModeOptionCard(
                title = "NORMAL APPLICATION",
                description = "Open TV LAUNCHER manually without changing your current Home application.",
                badgeText = if (!defaultHomeStatus.isCurrentlyDefaultHome && settings.operatingMode == OperatingModePreference.NORMAL_APPLICATION)
                    "Currently Active"
                else
                    "Option 2",
                isSelected = settings.operatingMode == OperatingModePreference.NORMAL_APPLICATION,
                onClick = { onSelectMode(OperatingModePreference.NORMAL_APPLICATION) },
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons: SET AS DEFAULT LAUNCHER + System Settings Shortcut + Re-verify Status
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusableActionButton(
                label = "SET AS DEFAULT LAUNCHER",
                primary = true,
                onClick = onSetDefaultLauncher
            )
            FocusableActionButton(
                label = "OPEN SYSTEM SETTINGS",
                primary = false,
                onClick = onOpenSystemSettings
            )
            FocusableActionButton(
                label = "RE-CHECK HOME STATUS",
                primary = false,
                onClick = onRefreshStatus
            )
        }

        // Manual Setup & Restricted Firmware Instructions
        GlassCard(
            isFocused = false,
            cornerRadius = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Manual Setup & Restricted Device Instructions",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "1. Standard Android TV Boxes & Televisions: Select 'SET AS DEFAULT LAUNCHER' above, or open Android TV Settings -> Apps -> Default Apps -> Home App, and choose TV LAUNCHER.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "2. Google TV Devices with Restricted Home Settings: Some Google TV models hide the Default Home App selector in system firmware. On these devices, TV LAUNCHER automatically and safely operates in NORMAL APPLICATION mode.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "3. Safe & Compliant Operation: TV LAUNCHER never uses root exploits, accessibility overrides, or private APIs to forcibly disable your manufacturer's launcher. You can launch TV LAUNCHER anytime from your Apps row.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    description: String,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    GlassCard(
        isFocused = isFocused || isSelected,
        cornerRadius = 24.dp,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) LiquidGlassColors.AccentIcyBlue else LiquidGlassColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun FocusableActionButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    GlassCard(
        isFocused = isFocused || primary,
        cornerRadius = 18.dp,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isFocused || primary) LiquidGlassColors.TextPrimary else LiquidGlassColors.TextSecondary
            )
        }
    }
}
