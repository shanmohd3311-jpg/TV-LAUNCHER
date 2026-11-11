package com.shanawaz.tvlauncher.data

import com.shanawaz.tvlauncher.database.AppPreferenceEntity
import com.shanawaz.tvlauncher.database.FavouriteAppDao
import com.shanawaz.tvlauncher.manager.AppDiscoveryManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Coordinates installed package discovery with Room database persistence for
 * favourites, custom app ordering, hidden apps, and recently launched timestamps.
 */
class AppRepository(
    private val discoveryManager: AppDiscoveryManager,
    private val favouriteAppDao: FavouriteAppDao
) {
    private val installedAppsFlow = MutableStateFlow<List<TVApp>>(emptyList())

    val allAppsFlow: Flow<List<TVApp>> = combine(
        installedAppsFlow,
        favouriteAppDao.observeAllAppPreferences()
    ) { installed, prefs ->
        val prefMap = prefs.associateBy { it.packageName }
        installed.mapIndexed { index, app ->
            val pref = prefMap[app.packageName]
            app.copy(
                isFavourite = pref?.isFavourite ?: (index < 5), // Default top 5 as initial dock favourites
                favouriteOrder = pref?.favouriteOrder ?: index,
                customOrder = pref?.customOrder ?: index,
                isHidden = pref?.isHidden ?: false,
                lastLaunchedTimestamp = pref?.lastLaunchedTimestamp ?: 0L
            )
        }
    }

    suspend fun refreshInstalledApps(includeStandardApps: Boolean = true): Result<List<TVApp>> {
        val result = discoveryManager.discoverInstalledApps(includeStandardApps)
        result.onSuccess { apps ->
            installedAppsFlow.value = apps
        }
        return result
    }

    suspend fun toggleFavourite(packageName: String, makeFavourite: Boolean, nextOrder: Int) {
        val existing = favouriteAppDao.getPreferenceForPackage(packageName)
        val updated = (existing ?: AppPreferenceEntity(packageName = packageName)).copy(
            isFavourite = makeFavourite,
            favouriteOrder = if (makeFavourite) nextOrder else Int.MAX_VALUE
        )
        favouriteAppDao.upsertPreference(updated)
    }

    suspend fun reorderFavourites(orderedPackages: List<String>) {
        val updates = orderedPackages.mapIndexed { index, pkg ->
            val existing = favouriteAppDao.getPreferenceForPackage(pkg)
            (existing ?: AppPreferenceEntity(packageName = pkg)).copy(
                isFavourite = true,
                favouriteOrder = index
            )
        }
        favouriteAppDao.upsertAll(updates)
    }

    suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        val existing = favouriteAppDao.getPreferenceForPackage(packageName)
        val updated = (existing ?: AppPreferenceEntity(packageName = packageName)).copy(
            isHidden = hidden
        )
        favouriteAppDao.upsertPreference(updated)
    }

    suspend fun recordAppLaunched(packageName: String) {
        val existing = favouriteAppDao.getPreferenceForPackage(packageName)
        val updated = (existing ?: AppPreferenceEntity(packageName = packageName)).copy(
            lastLaunchedTimestamp = System.currentTimeMillis()
        )
        favouriteAppDao.upsertPreference(updated)
    }

    suspend fun resetFavourites() {
        favouriteAppDao.clearAllFavourites()
    }

    suspend fun resetAllAppPreferences() {
        favouriteAppDao.deleteAll()
    }
}
