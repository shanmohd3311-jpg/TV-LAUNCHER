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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.components.FocusableAppCard
import com.shanawaz.tvlauncher.components.GlassCard
import com.shanawaz.tvlauncher.data.AppSortOrder
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.viewmodel.HomeUiState

/**
 * Full All-Applications Grid Screen:
 * - Displays all discovered Leanback & compatible Android applications
 * - Supports sorting (A–Z, Z–A, Recently Used, Custom Order)
 * - Supports refreshing package list from PackageManager
 * - Allows pinning/unpinning Favourites and hiding apps from the launcher grid
 */
@Composable
fun AppLibraryScreen(
    uiState: HomeUiState,
    onLaunchApp: (TVApp) -> Unit,
    onToggleFavourite: (TVApp) -> Unit,
    onToggleHidden: (TVApp) -> Unit,
    onChangeSortOrder: (AppSortOrder) -> Unit,
    onRefreshApps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Application Library (${uiState.visibleApps.size})",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "Press OK to launch · Long-press OK or press Menu to pin/unpin favourite",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FocusableActionButton(
                    label = "Sort: A–Z",
                    primary = uiState.settings.sortOrder == AppSortOrder.ALPHABETICAL_ASC,
                    onClick = { onChangeSortOrder(AppSortOrder.ALPHABETICAL_ASC) }
                )
                FocusableActionButton(
                    label = "Sort: Recent",
                    primary = uiState.settings.sortOrder == AppSortOrder.RECENTLY_USED,
                    onClick = { onChangeSortOrder(AppSortOrder.RECENTLY_USED) }
                )
                FocusableActionButton(
                    label = "Refresh Apps",
                    primary = false,
                    onClick = onRefreshApps
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.visibleApps.isEmpty()) {
            GlassCard(
                isFocused = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Text(
                        text = "No applications visible",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Click Refresh Apps to scan PackageManager or unhide hidden applications in Settings.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 190.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uiState.visibleApps,
                    key = { it.packageName }
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
