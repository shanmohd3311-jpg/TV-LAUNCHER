package com.shanawaz.tvlauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.AppIcon
import com.shanawaz.tvlauncher.components.FocusableAppCard
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.components.GlassDock
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.manager.DefaultHomeStatus
import com.shanawaz.tvlauncher.navigation.LauncherRoute
import com.shanawaz.tvlauncher.theme.LiquidGlassColors
import com.shanawaz.tvlauncher.viewmodel.HomeUiState

/**
 * Complete TV LAUNCHER Home Dashboard:
 * - Error notification banner (if any launch/discovery issue occurred)
 * - Welcome Hero section with featured wallpaper, featured app spotlight, and Dual Mode shortcut
 * - Liquid Glass Favourite Applications Dock
 * - Recently Used Applications row (respecting Android permissions)
 * - Installed TV Applications preview row
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    defaultHomeStatus: DefaultHomeStatus,
    onLaunchApp: (TVApp) -> Unit,
    onToggleFavourite: (TVApp) -> Unit,
    onNavigate: (String) -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Error banner when an app fails to launch or package scan encounters an issue
        uiState.errorMessage?.let { msg ->
            GlassCard(
                isFocused = true,
                cornerRadius = 20.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.titleMedium,
                        color = LiquidGlassColors.StatusError
                    )
                    FocusableActionButton(
                        label = "Dismiss",
                        primary = false,
                        onClick = onDismissError
                    )
                }
            }
        }

        // Welcome Hero Section + Featured App Spotlight
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GlassCard(
                isFocused = false,
                cornerRadius = 28.dp,
                modifier = Modifier.weight(1.65f)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text(
                        text = if (defaultHomeStatus.isCurrentlyDefaultHome)
                            "DEFAULT HOME LAUNCHER MODE · LIQUID GLASS OS"
                        else
                            "NORMAL APPLICATION MODE · LIQUID GLASS OS",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidGlassColors.AccentIcyBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.settings.promoTitle,
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.settings.promoSubtitle,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FocusableActionButton(
                            label = "All Applications (${uiState.visibleApps.size})",
                            primary = true,
                            onClick = { onNavigate(LauncherRoute.AppLibrary.route) }
                        )
                        FocusableActionButton(
                            label = "Launcher Mode",
                            primary = false,
                            onClick = { onNavigate(LauncherRoute.LauncherMode.route) }
                        )
                        FocusableActionButton(
                            label = "Wallpapers",
                            primary = false,
                            onClick = { onNavigate(LauncherRoute.Wallpapers.route) }
                        )
                    }
                }
            }

            // Featured Application Card
            uiState.featuredApp?.let { featured ->
                GlassCard(
                    isFocused = false,
                    cornerRadius = 28.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FEATURED APPLICATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidGlassColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(app = featured, size = 60.dp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = featured.appName,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = featured.packageName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        FocusableActionButton(
                            label = "Launch ${featured.appName}",
                            primary = true,
                            onClick = { onLaunchApp(featured) }
                        )
                    }
                }
            }
        }

        // Favourite Applications Liquid Glass Dock
        GlassDock(
            favouriteApps = uiState.favouriteApps,
            onLaunchApp = onLaunchApp,
            onToggleFavourite = onToggleFavourite
        )

        // Recently Used Applications (populated from actual launches within TV LAUNCHER)
        if (uiState.recentApps.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Recently Used Applications",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Local launcher session history",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.recentApps,
                        key = { "recent_${it.packageName}" }
                    ) { app ->
                        FocusableAppCard(
                            app = app,
                            compact = true,
                            onLaunch = onLaunchApp,
                            onToggleFavourite = onToggleFavourite
                        )
                    }
                }
            }
        }

        // Installed Applications Library Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "All Installed Applications (${uiState.visibleApps.size})",
                    style = MaterialTheme.typography.headlineMedium
                )
                FocusableActionButton(
                    label = "Open Full Library",
                    primary = false,
                    onClick = { onNavigate(LauncherRoute.AppLibrary.route) }
                )
            }
            LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = uiState.visibleApps,
                    key = { "all_${it.packageName}" }
                ) { app ->
                    FocusableAppCard(
                        app = app,
                        onLaunch = onLaunchApp,
                        onToggleFavourite = onToggleFavourite
                    )
                }
            }
        }
    }
}
