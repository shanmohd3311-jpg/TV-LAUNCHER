package com.shanawaz.tvlauncher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.theme.LiquidGlassColors
import com.shanawaz.tvlauncher.viewmodel.HomeUiState

private val REMOTE_KEYBOARD_KEYS = listOf(
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
    "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
    "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3",
    "4", "5", "6", "7", "8", "9"
)

/**
 * Remote-Friendly Application Search Interface:
 * - D-pad navigable on-screen keyboard
 * - Instant filtering of installed applications by name or package ID
 * - Clear no-results state and 1-click Clear button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onLaunchApp: (TVApp) -> Unit,
    onToggleFavourite: (TVApp) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Search Installed Applications",
            style = MaterialTheme.typography.displayMedium
        )

        // Search Query Box + Clear / Backspace Controls
        GlassCard(
            isFocused = false,
            cornerRadius = 22.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = if (uiState.searchQuery.isEmpty())
                        "Use the D-pad keyboard below to filter installed TV apps..."
                    else
                        "Query: \"${uiState.searchQuery}\"",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (uiState.searchQuery.isEmpty())
                        LiquidGlassColors.TextSecondary
                    else
                        LiquidGlassColors.AccentIcyBlue
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FocusableActionButton(
                        label = "Space",
                        primary = false,
                        onClick = { onQueryChange(uiState.searchQuery + " ") }
                    )
                    FocusableActionButton(
                        label = "Backspace",
                        primary = false,
                        onClick = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                onQueryChange(uiState.searchQuery.dropLast(1))
                            }
                        }
                    )
                    FocusableActionButton(
                        label = "Clear Search",
                        primary = false,
                        onClick = { onQueryChange("") }
                    )
                }
            }
        }

        // D-Pad On-Screen Keyboard Grid
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            REMOTE_KEYBOARD_KEYS.forEach { keyChar ->
                FocusableActionButton(
                    label = keyChar,
                    primary = false,
                    onClick = { onQueryChange(uiState.searchQuery + keyChar.lowercase()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filtered Search Results or Empty State
        if (uiState.searchResults.isEmpty()) {
            GlassCard(
                isFocused = false,
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text(
                        text = "No matching applications found for \"${uiState.searchQuery}\"",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing the search query or searching by a shorter keyword.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 190.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uiState.searchResults,
                    key = { "search_${it.packageName}" }
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
