package com.shanawaz.tvlauncher.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * macOS-inspired Horizontal Liquid Glass Favourites Dock.
 * Supports remote D-pad navigation, launching, adding/removing favourites, and reordering.
 */
@Composable
fun GlassDock(
    favouriteApps: List<TVApp>,
    onLaunchApp: (TVApp) -> Unit,
    onToggleFavourite: (TVApp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "Favourite Applications Dock",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${favouriteApps.size} pinned · Press OK to open or Menu to unpin",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidGlassColors.TextSecondary
            )
        }

        GlassCard(
            isFocused = false,
            cornerRadius = 28.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (favouriteApps.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = "No favourite apps pinned yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Highlight any installed application below and press the Menu button (or long-press OK) to pin it to your Liquid Glass dock.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = favouriteApps,
                        key = { it.packageName }
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
    }
}
