package com.shanawaz.tvlauncher.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * Remote D-pad Focusable Liquid Glass Application Card.
 * Responds to D-pad Focus, OK/Select/Enter to launch, and Long-Press/Menu key for favourite/hide options.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusableAppCard(
    app: TVApp,
    onLaunch: (TVApp) -> Unit,
    onToggleFavourite: ((TVApp) -> Unit)? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    GlassCard(
        isFocused = isFocused,
        cornerRadius = if (compact) 20.dp else 24.dp,
        modifier = modifier
            .width(if (compact) 150.dp else 196.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp) {
                    when (event.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            onLaunch(app)
                            true
                        }
                        Key.Menu, Key.Bookmark -> {
                            onToggleFavourite?.invoke(app)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable()
            .combinedClickable(
                onClick = { onLaunch(app) },
                onLongClick = { onToggleFavourite?.invoke(app) }
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (compact) 16.dp else 20.dp)
        ) {
            AppIcon(
                app = app,
                size = if (compact) 52.dp else 64.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (app.isLeanbackNative) "TV Native" else "Standard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFocused) LiquidGlassColors.AccentIcyBlue else LiquidGlassColors.TextSecondary
                )
                if (app.isFavourite) {
                    Text(
                        text = " · Fav",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlassColors.AccentIcyBlue
                    )
                }
            }
        }
    }
}
