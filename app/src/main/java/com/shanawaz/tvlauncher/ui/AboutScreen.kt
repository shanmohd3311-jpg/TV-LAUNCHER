package com.shanawaz.tvlauncher.ui

import android.os.Build
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * About TV LAUNCHER Screen:
 * - Application: TV LAUNCHER
 * - Developer: Shanawaz
 * - Package: com.shanawaz.tvlauncher
 * - Version: 1.0.0 (Build 1)
 * - Android OS & Hardware Telemetry
 * - Open-source Licences
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "About TV LAUNCHER",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Native Android TV & Google TV Liquid Glass Home Application",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            FocusableActionButton(
                label = "Back to Settings",
                primary = false,
                onClick = onBack
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TV LAUNCHER",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiquidGlassColors.AccentIcyBlue
                )
                Text("Developer: Shanawaz", style = MaterialTheme.typography.titleMedium)
                Text("Package Name: com.shanawaz.tvlauncher", style = MaterialTheme.typography.labelMedium)
                Text("Version: 1.0.0 (Version Code: 1)", style = MaterialTheme.typography.bodyLarge)
                Text("Minimum SDK: Android 6.0 (API 23) · Target SDK: API 35", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Running Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Open-Source Licences & Privacy Guarantee", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Android Jetpack Compose & Compose for TV — Apache License 2.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "• AndroidX Room & Preferences DataStore — Apache License 2.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "• Coil Image Loader & Kotlin Coroutines — Apache License 2.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Privacy Commitment: TV LAUNCHER operates 100% locally on your television. No external analytics, no cloud tracking, and no unauthorized background network requests.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
