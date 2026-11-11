package com.shanawaz.tvlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shanawaz.tvlauncher.components.TopBar
import com.shanawaz.tvlauncher.data.AppRepository
import com.shanawaz.tvlauncher.data.SettingsRepository
import com.shanawaz.tvlauncher.database.LauncherDatabase
import com.shanawaz.tvlauncher.manager.AppDiscoveryManager
import com.shanawaz.tvlauncher.manager.AppLaunchManager
import com.shanawaz.tvlauncher.manager.WallpaperManager
import com.shanawaz.tvlauncher.navigation.LauncherNavigation
import com.shanawaz.tvlauncher.navigation.LauncherRoute
import com.shanawaz.tvlauncher.theme.LiquidGlassColors
import com.shanawaz.tvlauncher.theme.LiquidGlassStyleConfig
import com.shanawaz.tvlauncher.theme.TVLauncherTheme
import com.shanawaz.tvlauncher.viewmodel.HomeViewModel
import com.shanawaz.tvlauncher.viewmodel.SettingsViewModel

/**
 * MainActivity for TV LAUNCHER (com.shanawaz.tvlauncher) by Shanawaz.
 * Supports Dual Mode operation:
 * 1. Default Home Launcher Mode (MAIN + HOME + DEFAULT)
 * 2. Normal Application Mode (MAIN + LEANBACK_LAUNCHER + LAUNCHER)
 */
class MainActivity : ComponentActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var wallpaperManager: WallpaperManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = LauncherDatabase.getInstance(applicationContext)
        val discoveryManager = AppDiscoveryManager(applicationContext)
        val launchManager = AppLaunchManager(applicationContext)
        wallpaperManager = WallpaperManager(applicationContext)

        val appRepository = AppRepository(discoveryManager, database.favouriteAppDao())
        val settingsRepository = SettingsRepository(applicationContext)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                        HomeViewModel(appRepository, settingsRepository, launchManager) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel(settingsRepository, appRepository, wallpaperManager, launchManager) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }

        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setContent {
            val settings by settingsViewModel.settingsState.collectAsState()
            val activeWallpaper = wallpaperManager.findById(
                settings.wallpaperId,
                settings.customWallpaperUri
            )

            val styleConfig = LiquidGlassStyleConfig(
                themeMode = settings.themeMode,
                glassIntensity = settings.glassIntensity,
                accentColor = LiquidGlassColors.fromHex(settings.accentHex),
                reducedAnimations = settings.reducedAnimations,
                lowPerformanceMode = settings.lowPerformanceMode,
                uiScale = settings.uiScale,
                safeAreaHorizontalDp = settings.safeAreaMarginDp,
                safeAreaVerticalDp = (settings.safeAreaMarginDp * 0.6f).toInt()
            )

            TVLauncherTheme(config = styleConfig) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: LauncherRoute.Home.route

                // When acting as Home Launcher, pressing Back on the Home destination stays on Home
                BackHandler(enabled = currentRoute == LauncherRoute.Home.route) {
                    // Consume Back on root Home dashboard per Android TV Home Launcher conventions
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    activeWallpaper.primaryColor,
                                    activeWallpaper.secondaryColor,
                                    activeWallpaper.tertiaryColor.copy(alpha = 0.28f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = styleConfig.safeAreaHorizontalDp.dp,
                                vertical = styleConfig.safeAreaVerticalDp.dp
                            )
                    ) {
                        TopBar(
                            currentRoute = currentRoute,
                            use24HourClock = settings.use24HourClock,
                            showClockSeconds = settings.showClockSeconds,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        )

                        LauncherNavigation(
                            navController = navController,
                            homeViewModel = homeViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh installed packages & Default Home status when returning from external TV apps or System Settings
        if (::homeViewModel.isInitialized) {
            homeViewModel.refreshInstalledApplications()
        }
        if (::settingsViewModel.isInitialized) {
            settingsViewModel.refreshDefaultHomeStatus()
        }
    }
}
