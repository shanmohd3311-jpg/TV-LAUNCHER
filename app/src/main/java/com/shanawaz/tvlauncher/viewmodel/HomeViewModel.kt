package com.shanawaz.tvlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shanawaz.tvlauncher.data.AppRepository
import com.shanawaz.tvlauncher.data.AppSortOrder
import com.shanawaz.tvlauncher.data.SettingsRepository
import com.shanawaz.tvlauncher.data.TVApp
import com.shanawaz.tvlauncher.data.UserLauncherSettings
import com.shanawaz.tvlauncher.manager.AppLaunchManager
import com.shanawaz.tvlauncher.manager.AppLaunchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val allApps: List<TVApp> = emptyList(),
    val visibleApps: List<TVApp> = emptyList(),
    val favouriteApps: List<TVApp> = emptyList(),
    val recentApps: List<TVApp> = emptyList(),
    val hiddenApps: List<TVApp> = emptyList(),
    val featuredApp: TVApp? = null,
    val searchQuery: String = "",
    val searchResults: List<TVApp> = emptyList(),
    val settings: UserLauncherSettings = UserLauncherSettings(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val appLaunchManager: AppLaunchManager
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val errorBannerFlow = MutableStateFlow<String?>(null)
    private val isLoadingFlow = MutableStateFlow(true)

    val uiState: StateFlow<HomeUiState> = combine(
        appRepository.allAppsFlow,
        settingsRepository.settingsFlow,
        searchQueryFlow,
        errorBannerFlow,
        isLoadingFlow
    ) { apps, settings, query, errorMsg, loading ->
        val visible = apps.filter { !it.isHidden }
        val sortedVisible = when (settings.sortOrder) {
            AppSortOrder.ALPHABETICAL_ASC -> visible.sortedBy { it.appName.lowercase() }
            AppSortOrder.ALPHABETICAL_DESC -> visible.sortedByDescending { it.appName.lowercase() }
            AppSortOrder.RECENTLY_USED -> visible.sortedByDescending { it.lastLaunchedTimestamp }
            AppSortOrder.CUSTOM_ORDER -> visible.sortedBy { it.customOrder }
        }

        val favourites = visible
            .filter { it.isFavourite }
            .sortedBy { it.favouriteOrder }

        val recents = visible
            .filter { it.lastLaunchedTimestamp > 0L }
            .sortedByDescending { it.lastLaunchedTimestamp }
            .take(8)

        val hidden = apps.filter { it.isHidden }

        val filteredSearch = if (query.isBlank()) {
            sortedVisible
        } else {
            val q = query.trim().lowercase()
            sortedVisible.filter {
                it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        HomeUiState(
            isLoading = loading,
            allApps = apps,
            visibleApps = sortedVisible,
            favouriteApps = favourites,
            recentApps = recents,
            hiddenApps = hidden,
            featuredApp = favourites.firstOrNull() ?: sortedVisible.firstOrNull(),
            searchQuery = query,
            searchResults = filteredSearch,
            settings = settings,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomeUiState()
    )

    init {
        refreshInstalledApplications()
    }

    fun refreshInstalledApplications() {
        viewModelScope.launch {
            isLoadingFlow.value = true
            val result = appRepository.refreshInstalledApps(includeStandardApps = true)
            result.onFailure { err ->
                errorBannerFlow.value = "Failed to scan installed TV applications: ${err.localizedMessage}"
            }
            isLoadingFlow.value = false
        }
    }

    fun launchApplication(app: TVApp) {
        when (val result = appLaunchManager.launchApp(app)) {
            is AppLaunchResult.Success -> {
                viewModelScope.launch {
                    appRepository.recordAppLaunched(app.packageName)
                }
            }
            is AppLaunchResult.Error -> {
                errorBannerFlow.value = result.userMessage
            }
        }
    }

    fun toggleFavourite(app: TVApp) {
        viewModelScope.launch {
            val nextOrder = uiState.value.favouriteApps.size
            appRepository.toggleFavourite(
                packageName = app.packageName,
                makeFavourite = !app.isFavourite,
                nextOrder = nextOrder
            )
        }
    }

    fun moveFavourite(packageName: String, direction: Int) {
        viewModelScope.launch {
            val currentList = uiState.value.favouriteApps.map { it.packageName }.toMutableList()
            val idx = currentList.indexOf(packageName)
            if (idx == -1) return@launch
            val targetIdx = (idx + direction).coerceIn(0, currentList.lastIndex)
            if (idx != targetIdx) {
                val item = currentList.removeAt(idx)
                currentList.add(targetIdx, item)
                appRepository.reorderFavourites(currentList)
            }
        }
    }

    fun setAppHidden(app: TVApp, hidden: Boolean) {
        viewModelScope.launch {
            appRepository.setAppHidden(app.packageName, hidden)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun clearError() {
        errorBannerFlow.value = null
    }
}
