package com.shanawaz.tvlauncher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shanawaz.tvlauncher.manager.OperatingModePreference
import com.shanawaz.tvlauncher.theme.LauncherThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tv_launcher_settings")

data class UserLauncherSettings(
    val operatingMode: OperatingModePreference = OperatingModePreference.NORMAL_APPLICATION,
    val themeMode: LauncherThemeMode = LauncherThemeMode.DARK,
    val glassIntensity: Float = 0.75f,
    val accentHex: String = "#38BDF8",
    val wallpaperId: String = "liquid_glass_icy_blue",
    val customWallpaperUri: String? = null,
    val sortOrder: AppSortOrder = AppSortOrder.ALPHABETICAL_ASC,
    val use24HourClock: Boolean = false,
    val showClockSeconds: Boolean = false,
    val reducedAnimations: Boolean = false,
    val lowPerformanceMode: Boolean = false,
    val uiScale: Float = 1.0f,
    val safeAreaMarginDp: Int = 48,
    val screensaverEnabled: Boolean = true,
    val screensaverDelayMinutes: Int = 5,
    val promoTitle: String = "Welcome to TV LAUNCHER",
    val promoSubtitle: String = "Designed by Shanawaz • macOS Liquid Glass Home for Android TV"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val OPERATING_MODE = stringPreferencesKey("operating_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
        val ACCENT_HEX = stringPreferencesKey("accent_hex")
        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val USE_24H_CLOCK = booleanPreferencesKey("use_24h_clock")
        val SHOW_CLOCK_SECONDS = booleanPreferencesKey("show_clock_seconds")
        val REDUCED_ANIMATIONS = booleanPreferencesKey("reduced_animations")
        val LOW_PERF_MODE = booleanPreferencesKey("low_perf_mode")
        val UI_SCALE = floatPreferencesKey("ui_scale")
        val SAFE_AREA_DP = intPreferencesKey("safe_area_dp")
        val SCREENSAVER_ENABLED = booleanPreferencesKey("screensaver_enabled")
        val SCREENSAVER_DELAY_MIN = intPreferencesKey("screensaver_delay_min")
        val PROMO_TITLE = stringPreferencesKey("promo_title")
        val PROMO_SUBTITLE = stringPreferencesKey("promo_subtitle")
    }

    val settingsFlow: Flow<UserLauncherSettings> = context.dataStore.data.map { prefs ->
        UserLauncherSettings(
            operatingMode = runCatching {
                OperatingModePreference.valueOf(
                    prefs[Keys.OPERATING_MODE] ?: OperatingModePreference.NORMAL_APPLICATION.name
                )
            }.getOrDefault(OperatingModePreference.NORMAL_APPLICATION),
            themeMode = runCatching {
                LauncherThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: LauncherThemeMode.DARK.name)
            }.getOrDefault(LauncherThemeMode.DARK),
            glassIntensity = prefs[Keys.GLASS_INTENSITY] ?: 0.75f,
            accentHex = prefs[Keys.ACCENT_HEX] ?: "#38BDF8",
            wallpaperId = prefs[Keys.WALLPAPER_ID] ?: "liquid_glass_icy_blue",
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI],
            sortOrder = runCatching {
                AppSortOrder.valueOf(prefs[Keys.SORT_ORDER] ?: AppSortOrder.ALPHABETICAL_ASC.name)
            }.getOrDefault(AppSortOrder.ALPHABETICAL_ASC),
            use24HourClock = prefs[Keys.USE_24H_CLOCK] ?: false,
            showClockSeconds = prefs[Keys.SHOW_CLOCK_SECONDS] ?: false,
            reducedAnimations = prefs[Keys.REDUCED_ANIMATIONS] ?: false,
            lowPerformanceMode = prefs[Keys.LOW_PERF_MODE] ?: false,
            uiScale = prefs[Keys.UI_SCALE] ?: 1.0f,
            safeAreaMarginDp = prefs[Keys.SAFE_AREA_DP] ?: 48,
            screensaverEnabled = prefs[Keys.SCREENSAVER_ENABLED] ?: true,
            screensaverDelayMinutes = prefs[Keys.SCREENSAVER_DELAY_MIN] ?: 5,
            promoTitle = prefs[Keys.PROMO_TITLE] ?: "Welcome to TV LAUNCHER",
            promoSubtitle = prefs[Keys.PROMO_SUBTITLE] ?: "Designed by Shanawaz • macOS Liquid Glass Home for Android TV"
        )
    }

    suspend fun updateOperatingMode(mode: OperatingModePreference) {
        context.dataStore.edit { it[Keys.OPERATING_MODE] = mode.name }
    }

    suspend fun updateThemeMode(mode: LauncherThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateGlassIntensity(intensity: Float) {
        context.dataStore.edit { it[Keys.GLASS_INTENSITY] = intensity.coerceIn(0.2f, 1.0f) }
    }

    suspend fun updateAccentHex(hex: String) {
        context.dataStore.edit { it[Keys.ACCENT_HEX] = hex }
    }

    suspend fun updateWallpaper(wallpaperId: String, customUri: String? = null) {
        context.dataStore.edit {
            it[Keys.WALLPAPER_ID] = wallpaperId
            if (customUri != null) {
                it[Keys.CUSTOM_WALLPAPER_URI] = customUri
            }
        }
    }

    suspend fun updateSortOrder(order: AppSortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun updateClockPreferences(use24Hour: Boolean, showSeconds: Boolean) {
        context.dataStore.edit {
            it[Keys.USE_24H_CLOCK] = use24Hour
            it[Keys.SHOW_CLOCK_SECONDS] = showSeconds
        }
    }

    suspend fun updatePerformancePreferences(reducedAnimations: Boolean, lowPerformanceMode: Boolean) {
        context.dataStore.edit {
            it[Keys.REDUCED_ANIMATIONS] = reducedAnimations
            it[Keys.LOW_PERF_MODE] = lowPerformanceMode
        }
    }

    suspend fun updateDisplayPreferences(uiScale: Float, safeAreaDp: Int) {
        context.dataStore.edit {
            it[Keys.UI_SCALE] = uiScale.coerceIn(0.85f, 1.15f)
            it[Keys.SAFE_AREA_DP] = safeAreaDp.coerceIn(16, 96)
        }
    }

    suspend fun updateScreensaver(enabled: Boolean, delayMinutes: Int) {
        context.dataStore.edit {
            it[Keys.SCREENSAVER_ENABLED] = enabled
            it[Keys.SCREENSAVER_DELAY_MIN] = delayMinutes.coerceIn(1, 60)
        }
    }

    suspend fun updatePromoBanner(title: String, subtitle: String) {
        context.dataStore.edit {
            it[Keys.PROMO_TITLE] = title
            it[Keys.PROMO_SUBTITLE] = subtitle
        }
    }

    suspend fun resetAppearanceSettings() {
        context.dataStore.edit {
            it[Keys.THEME_MODE] = LauncherThemeMode.DARK.name
            it[Keys.GLASS_INTENSITY] = 0.75f
            it[Keys.ACCENT_HEX] = "#38BDF8"
            it[Keys.WALLPAPER_ID] = "liquid_glass_icy_blue"
            it.remove(Keys.CUSTOM_WALLPAPER_URI)
        }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { it.clear() }
    }
}
