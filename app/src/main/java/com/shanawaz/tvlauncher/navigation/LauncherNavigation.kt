package com.shanawaz.tvlauncher.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shanawaz.tvlauncher.ui.AboutScreen
import com.shanawaz.tvlauncher.ui.AppLibraryScreen
import com.shanawaz.tvlauncher.ui.HomeScreen
import com.shanawaz.tvlauncher.ui.LauncherModeScreen
import com.shanawaz.tvlauncher.ui.SearchScreen
import com.shanawaz.tvlauncher.ui.SettingsScreen
import com.shanawaz.tvlauncher.ui.WallpaperScreen
import com.shanawaz.tvlauncher.viewmodel.HomeViewModel
import com.shanawaz.tvlauncher.viewmodel.SettingsViewModel

sealed class LauncherRoute(val route: String) {
    data object Home : LauncherRoute("home")
    data object AppLibrary : LauncherRoute("app_library")
    data object Search : LauncherRoute("search")
    data object Wallpapers : LauncherRoute("wallpapers")
    data object LauncherMode : LauncherRoute("launcher_mode")
    data object Settings : LauncherRoute("settings")
    data object About : LauncherRoute("about")
}

@Composable
fun LauncherNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel
) {
    val homeState by homeViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val defaultHomeStatus by settingsViewModel.defaultHomeStatus.collectAsState()

    NavHost(
        navController = navController,
        startDestination = LauncherRoute.Home.route
    ) {
        composable(LauncherRoute.Home.route) {
            HomeScreen(
                uiState = homeState,
                defaultHomeStatus = defaultHomeStatus,
                onLaunchApp = homeViewModel::launchApplication,
                onToggleFavourite = homeViewModel::toggleFavourite,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onDismissError = homeViewModel::clearError
            )
        }

        composable(LauncherRoute.AppLibrary.route) {
            AppLibraryScreen(
                uiState = homeState,
                onLaunchApp = homeViewModel::launchApplication,
                onToggleFavourite = homeViewModel::toggleFavourite,
                onToggleHidden = { app -> homeViewModel.setAppHidden(app, !app.isHidden) },
                onChangeSortOrder = settingsViewModel::setSortOrder,
                onRefreshApps = homeViewModel::refreshInstalledApplications
            )
        }

        composable(LauncherRoute.Search.route) {
            SearchScreen(
                uiState = homeState,
                onQueryChange = homeViewModel::updateSearchQuery,
                onLaunchApp = homeViewModel::launchApplication,
                onToggleFavourite = homeViewModel::toggleFavourite
            )
        }

        composable(LauncherRoute.Wallpapers.route) {
            WallpaperScreen(
                settings = settingsState,
                presets = settingsViewModel.builtInWallpapers,
                onSelectPreset = settingsViewModel::selectBuiltInWallpaper,
                onSelectCustomUri = settingsViewModel::selectCustomSafWallpaper
            )
        }

        composable(LauncherRoute.LauncherMode.route) {
            LauncherModeScreen(
                settings = settingsState,
                defaultHomeStatus = defaultHomeStatus,
                onSelectMode = settingsViewModel::setOperatingMode,
                onSetDefaultLauncher = settingsViewModel::openSystemDefaultHomeSelector,
                onOpenSystemSettings = settingsViewModel::openSystemSettingsShortcut,
                onRefreshStatus = settingsViewModel::refreshDefaultHomeStatus
            )
        }

        composable(LauncherRoute.Settings.route) {
            SettingsScreen(
                settings = settingsState,
                hiddenApps = homeState.hiddenApps,
                onUnhideApp = { app -> homeViewModel.setAppHidden(app, false) },
                onSelectTheme = settingsViewModel::setThemeMode,
                onChangeGlassIntensity = settingsViewModel::setGlassIntensity,
                onSelectAccent = settingsViewModel::setAccentColor,
                onChangeClockPrefs = settingsViewModel::setClockPreferences,
                onChangePerformancePrefs = settingsViewModel::setPerformancePreferences,
                onChangeDisplayPrefs = settingsViewModel::setDisplayPreferences,
                onChangeScreensaverPrefs = settingsViewModel::setScreensaverPreferences,
                onOpenLauncherMode = { navController.navigate(LauncherRoute.LauncherMode.route) },
                onOpenDefaultLauncherSettings = settingsViewModel::openSystemDefaultHomeSelector,
                onOpenAbout = { navController.navigate(LauncherRoute.About.route) },
                onResetAppearance = settingsViewModel::resetAppearance,
                onResetFavourites = settingsViewModel::resetFavourites,
                onResetAll = settingsViewModel::resetAllSettingsAndData
            )
        }

        composable(LauncherRoute.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
