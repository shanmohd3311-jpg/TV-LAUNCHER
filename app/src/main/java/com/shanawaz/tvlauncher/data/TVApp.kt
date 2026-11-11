package com.shanawaz.tvlauncher.data

import android.graphics.drawable.Drawable

/**
 * Domain representation of an installed Android TV / Google TV application.
 */
data class TVApp(
    val packageName: String,
    val activityName: String,
    val appName: String,
    val isLeanbackNative: Boolean,
    val isSystemApp: Boolean,
    val versionName: String,
    val iconDrawable: Drawable? = null,
    val bannerDrawable: Drawable? = null,
    val isFavourite: Boolean = false,
    val favouriteOrder: Int = Int.MAX_VALUE,
    val customOrder: Int = Int.MAX_VALUE,
    val isHidden: Boolean = false,
    val lastLaunchedTimestamp: Long = 0L
)

enum class AppSortOrder {
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    RECENTLY_USED,
    CUSTOM_ORDER
}
