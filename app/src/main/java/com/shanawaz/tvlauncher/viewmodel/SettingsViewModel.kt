package com.shanawaz.tvlauncher.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanawaz.tvlauncher.data.AppRepository
import com.shanawaz.tvlauncher.data.AppSortOrder
import com.shanawaz.tvlauncher.data.SettingsRepository
import com.shanawaz.tvlauncher.data.UserLauncherSettings
import com.shanawaz.tvlauncher.manager.AppLaunchManager
import com.shanawaz.tvlauncher.manager.DefaultHomeStatus
import com.shanawaz.tvlauncher.manager.OperatingModePreference
import com.shanawaz.tvlauncher.manager.WallpaperManager
import com.shanawaz.tvlauncher.theme.LauncherThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val appRepository: AppRepository,
    private val wallpaperManager: WallpaperManager,
    private val appLaunchManager: AppLaunchManager
) : ViewModel() {

    val settingsState: StateFlow<UserLauncherSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserLauncherSettings()
        )

    private val _defaultHomeStatus = MutableStateFlow(appLaunchManager.inspectDefaultHomeStatus())
    val defaultHomeStatus: StateFlow<DefaultHomeStatus> = _defaultHomeStatus.asStateFlow()

    val builtInWallpapers = wallpaperManager.builtInWallpapers

    fun refreshDefaultHomeStatus() {
        _defaultHomeStatus.value = appLaunchManager.inspectDefaultHomeStatus()
    }

    fun setOperatingMode(mode: OperatingModePreference) {
        viewModelScope.launch {
            settingsRepository.updateOperatingMode(mode)
            refreshDefaultHomeStatus()
        }
    }

    fun setThemeMode(mode: LauncherThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun setGlassIntensity(intensity: Float) {
        viewModelScope.launch {
            settingsRepository.updateGlassIntensity(intensity)
        }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            settingsRepository.updateAccentHex(hex)
        }
    }

    fun selectBuiltInWallpaper(wallpaperId: String) {
        viewModelScope.launch {
            settingsRepository.updateWallpaper(wallpaperId, null)
        }
    }

    fun selectCustomSafWallpaper(uri: Uri) {
        viewModelScope.launch {
            wallpaperManager.persistCustomWallpaperUri(uri).onSuccess { persistedUriString ->
                settingsRepository.updateWallpaper("custom_user_uri", persistedUriString)
            }
        }
    }

    fun setSortOrder(order: AppSortOrder) {
        viewModelScope.launch {
            settingsRepository.updateSortOrder(order)
        }
    }

    fun setClockPreferences(use24Hour: Boolean, showSeconds: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateClockPreferences(use24Hour, showSeconds)
        }
    }

    fun setPerformancePreferences(reducedAnimations: Boolean, lowPerformanceMode: Boolean) {
        viewModelScope.launch {
            settingsRepository.updatePerformancePreferences(reducedAnimations, lowPerformanceMode)
        }
    }

    fun setDisplayPreferences(uiScale: Float, safeAreaDp: Int) {
        viewModelScope.launch {
            settingsRepository.updateDisplayPreferences(uiScale, safeAreaDp)
        }
    }

    fun setScreensaverPreferences(enabled: Boolean, delayMinutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateScreensaver(enabled, delayMinutes)
        }
    }

    fun openSystemDefaultHomeSelector() {
        appLaunchManager.openDefaultLauncherSettings()
        refreshDefaultHomeStatus()
    }

    fun openSystemSettingsShortcut() {
        appLaunchManager.openSystemSettings()
    }

    fun resetAppearance() {
        viewModelScope.launch {
            settingsRepository.resetAppearanceSettings()
        }
    }

    fun resetFavourites() {
        viewModelScope.launch {
            appRepository.resetFavourites()
        }
    }

    fun resetAllSettingsAndData() {
        viewModelScope.launch {
            settingsRepository.resetAllSettings()
            appRepository.resetAllAppPreferences()
        }
    }
}
