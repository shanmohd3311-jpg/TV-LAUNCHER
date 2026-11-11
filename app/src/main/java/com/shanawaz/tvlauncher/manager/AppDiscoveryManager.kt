package com.shanawaz.tvlauncher.manager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.shanawaz.tvlauncher.data.TVApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Discovers installed Android TV (Leanback) and standard launchable applications
 * using PackageManager off the main UI thread.
 */
class AppDiscoveryManager(private val context: Context) {

    suspend fun discoverInstalledApps(includeStandardApps: Boolean = true): Result<List<TVApp>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pm = context.packageManager
                val selfPackage = context.packageName
                val discoveredMap = linkedMapOf<String, TVApp>()

                // 1. Query native Android TV Leanback Launcher activities first
                val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                }
                val leanbackActivities = queryIntentActivitiesCompat(pm, leanbackIntent)

                for (resolveInfo in leanbackActivities) {
                    val activityInfo = resolveInfo.activityInfo ?: continue
                    val pkg = activityInfo.packageName ?: continue
                    if (pkg == selfPackage) continue

                    discoveredMap[pkg] = mapResolveInfoToTVApp(
                        pm = pm,
                        resolveInfo = resolveInfo,
                        isLeanback = true
                    )
                }

                // 2. Query standard Android Launcher activities as fallback for sideloaded TV apps
                if (includeStandardApps) {
                    val mainLauncherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    val standardActivities = queryIntentActivitiesCompat(pm, mainLauncherIntent)

                    for (resolveInfo in standardActivities) {
                        val activityInfo = resolveInfo.activityInfo ?: continue
                        val pkg = activityInfo.packageName ?: continue
                        if (pkg == selfPackage || discoveredMap.containsKey(pkg)) continue

                        discoveredMap[pkg] = mapResolveInfoToTVApp(
                            pm = pm,
                            resolveInfo = resolveInfo,
                            isLeanback = false
                        )
                    }
                }

                discoveredMap.values.sortedBy { it.appName.lowercase() }
            }
        }

    private fun queryIntentActivitiesCompat(
        pm: PackageManager,
        intent: Intent
    ): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
    }

    private fun mapResolveInfoToTVApp(
        pm: PackageManager,
        resolveInfo: ResolveInfo,
        isLeanback: Boolean
    ): TVApp {
        val activityInfo = resolveInfo.activityInfo
        val pkg = activityInfo.packageName
        val label = runCatching { resolveInfo.loadLabel(pm).toString() }
            .getOrDefault(pkg)
            .ifBlank { pkg }

        val icon = runCatching { resolveInfo.loadIcon(pm) }.getOrNull()
        val banner = runCatching { activityInfo.loadBanner(pm) ?: activityInfo.applicationInfo.loadBanner(pm) }.getOrNull()
        val isSystem = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val versionName = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L)).versionName ?: "1.0"
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0).versionName ?: "1.0"
            }
        }.getOrDefault("1.0")

        return TVApp(
            packageName = pkg,
            activityName = activityInfo.name ?: "",
            appName = label,
            isLeanbackNative = isLeanback,
            isSystemApp = isSystem,
            versionName = versionName,
            iconDrawable = icon,
            bannerDrawable = banner
        )
    }
}
