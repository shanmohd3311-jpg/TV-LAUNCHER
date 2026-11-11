package com.shanawaz.tvlauncher.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.theme.LiquidGlassColors

/**
 * Renders the installed application icon from PackageManager Drawable,
 * with a clean monogram fallback if the package icon cannot be loaded.
 */
@Composable
fun AppIcon(
    app: TVApp,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val imageBitmap: ImageBitmap? = remember(app.packageName, app.iconDrawable) {
        app.iconDrawable?.let { drawableToImageBitmap(it) }
    }

    val shape = RoundedCornerShape(16.dp)

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = app.appName,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape)
        )
    } else {
        // Resilient fallback icon when a package drawable is unavailable
        val initials = app.appName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "TV" }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            LiquidGlassColors.AccentIcyBlue.copy(alpha = 0.35f),
                            LiquidGlassColors.BackgroundSecondary
                        )
                    )
                )
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap? {
    return runCatching {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = drawable.intrinsicWidth.coerceAtLeast(96)
        val height = drawable.intrinsicHeight.coerceAtLeast(96)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }.getOrNull()
}
