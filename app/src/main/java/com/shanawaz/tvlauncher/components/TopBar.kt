package com.shanawaz.tvlauncher.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.navigation.LauncherRoute
import com.shanawaz.tvlauncher.theme.LiquidGlassColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopBar(
    currentRoute: String,
    use24HourClock: Boolean,
    showClockSeconds: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTimeText by remember { mutableStateOf("") }
    var currentDateText by remember { mutableStateOf("") }
    var wifiConnected by remember { mutableStateOf(true) }
    var batteryPercent by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(use24HourClock, showClockSeconds) {
        while (true) {
            val now = Date()
            val timePattern = when {
                use24HourClock && showClockSeconds -> "HH:mm:ss"
                use24HourClock -> "HH:mm"
                showClockSeconds -> "hh:mm:ss a"
                else -> "hh:mm a"
            }
            currentTimeText = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
            currentDateText = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(now)
            wifiConnected = checkNetworkAvailable(context)
            batteryPercent = readBatteryLevelOrNull(context)
            delay(1000L)
        }
    }

    GlassCard(
        isFocused = false,
        cornerRadius = 22.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            // Zone 1: Brand Title
            Text(
                text = "TV LAUNCHER",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = LiquidGlassColors.TextPrimary
            )

            // Zone 2: D-Pad Focusable Navigation Links
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                TopBarNavLink("Home", LauncherRoute.Home.route, currentRoute, onNavigate)
                TopBarNavLink("Apps", LauncherRoute.AppLibrary.route, currentRoute, onNavigate)
                TopBarNavLink("Search", LauncherRoute.Search.route, currentRoute, onNavigate)
                TopBarNavLink("Wallpapers", LauncherRoute.Wallpapers.route, currentRoute, onNavigate)
                TopBarNavLink("Launcher Mode", LauncherRoute.LauncherMode.route, currentRoute, onNavigate)
                TopBarNavLink("Settings", LauncherRoute.Settings.route, currentRoute, onNavigate)
            }

            // Zone 3: System Status (Wi-Fi, Optional Battery, Date & Clock)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (wifiConnected) "Wi-Fi Connected" else "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (wifiConnected) LiquidGlassColors.TextSecondary else LiquidGlassColors.StatusWarning
                )
                batteryPercent?.let { pct ->
                    Text(
                        text = "· $pct%",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidGlassColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$currentDateText · $currentTimeText",
                    style = MaterialTheme.typography.labelMedium,
                    color = LiquidGlassColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TopBarNavLink(
    label: String,
    route: String,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val isSelected = currentRoute == route

    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        color = when {
            isFocused -> LiquidGlassColors.AccentIcyBlue
            isSelected -> LiquidGlassColors.TextPrimary
            else -> LiquidGlassColors.TextSecondary
        },
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onNavigate(route) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private fun checkNetworkAvailable(context: Context): Boolean {
    return runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)
}

private fun readBatteryLevelOrNull(context: Context): Int? {
    return runCatching {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return null
        val present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        if (!present) return null
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) (level * 100) / scale else null
    }.getOrNull()
}
