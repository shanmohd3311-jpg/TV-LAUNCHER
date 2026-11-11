package com.shanawaz.tvlauncher.manager

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.shanawaz.tvlauncher.data.TVApp

sealed class AppLaunchResult {
    data object Success : AppLaunchResult()
    data class Error(val userMessage: String) : AppLaunchResult()
}

enum class OperatingModePreference {
    DEFAULT_LAUNCHER,
    NORMAL_APPLICATION
}

data class DefaultHomeStatus(
    val isCurrentlyDefaultHome: Boolean,
    val currentDefaultPackage: String?,
    val isHomeRoleSupported: Boolean,
    val canOpenHomeSettingsIntent: Boolean,
    val isManufacturerRestricted: Boolean,
    val compatibilitySummary: String
)

/**
 * Handles launching Android TV Leanback activities, standard activities,
 * and Dual Mode (Default Home Launcher vs Normal Application) detection and system settings intents.
 */
class AppLaunchManager(private val context: Context) {

    fun launchApp(app: TVApp): AppLaunchResult {
        val pm = context.packageManager

        return try {
            // 1. Prefer Leanback Launcher Intent on Android TV / Google TV devices
            val leanbackIntent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
            if (leanbackIntent != null) {
                leanbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(leanbackIntent)
                return AppLaunchResult.Success
            }

            // 2. Fallback to standard Package Launch Intent
            val standardIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (standardIntent != null) {
                standardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(standardIntent)
                return AppLaunchResult.Success
            }

            // 3. Explicit ComponentName fallback if activityName was resolved during discovery
            if (app.activityName.isNotBlank()) {
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(app.packageName, app.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(explicitIntent)
                return AppLaunchResult.Success
            }

            AppLaunchResult.Error("No launchable TV activity found for ${app.appName} (${app.packageName}).")
        } catch (e: ActivityNotFoundException) {
            AppLaunchResult.Error("${app.appName} is no longer installed or does not expose a launch activity.")
        } catch (e: SecurityException) {
            AppLaunchResult.Error("Android security policy prevented launching ${app.appName}.")
        } catch (e: Exception) {
            AppLaunchResult.Error("Could not open ${app.appName}: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Inspects whether TV LAUNCHER is currently the active system Default Home application,
     * and checks device compatibility for third-party Home selection without fake success claims.
     */
    fun inspectDefaultHomeStatus(): DefaultHomeStatus {
        val pm = context.packageManager
        val selfPackage = context.packageName

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        val resolvedHome = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.resolveActivity(
                    homeIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
        }.getOrNull()

        val resolvedPkg = resolvedHome?.activityInfo?.packageName
        val heldViaRoleManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val roleManager = context.getSystemService(RoleManager::class.java)
                roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
            }.getOrDefault(false)
        } else {
            false
        }

        val isDefault = heldViaRoleManager || (resolvedPkg == selfPackage)

        val homeRoleAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val roleManager = context.getSystemService(RoleManager::class.java)
                roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true
            }.getOrDefault(true)
        } else {
            true
        }

        val homeSettingsIntent = Intent(Settings.ACTION_HOME_SETTINGS)
        val canResolveHomeSettings = homeSettingsIntent.resolveActivity(pm) != null

        // Some Google TV firmwares lock the default home launcher to com.google.android.apps.tv.launcherx
        // and disable ACTION_HOME_SETTINGS. We detect this gracefully and report accurate status.
        val isRestricted = !homeRoleAvailable || (!canResolveHomeSettings && resolvedPkg == "com.google.android.apps.tv.launcherx")

        val summary = when {
            isDefault -> "TV LAUNCHER is active as your default Android TV Home interface."
            isRestricted -> "This Google TV / manufacturer firmware restricts third-party Home replacement. Normal Application Mode is fully active."
            canResolveHomeSettings -> "Compatible Android TV device: Default Home selection is available via System Settings."
            else -> "Standard Home Settings shortcut is hidden by the manufacturer; manual selection or Normal Application Mode is supported."
        }

        return DefaultHomeStatus(
            isCurrentlyDefaultHome = isDefault,
            currentDefaultPackage = resolvedPkg,
            isHomeRoleSupported = homeRoleAvailable,
            canOpenHomeSettingsIntent = canResolveHomeSettings,
            isManufacturerRestricted = isRestricted,
            compatibilitySummary = summary
        )
    }

    /**
     * Opens the official Android system Home-app selection settings where supported.
     * Never claims false success or uses root/private hacks.
     */
    fun openDefaultLauncherSettings(): AppLaunchResult {
        return try {
            val homeSettingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (homeSettingsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(homeSettingsIntent)
                return AppLaunchResult.Success
            }

            val defaultAppsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (defaultAppsIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(defaultAppsIntent)
                return AppLaunchResult.Success
            }

            // Trigger Home chooser dialog if no default is locked
            val homeChooserIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(homeChooserIntent, "Select TV LAUNCHER as Home"))
            AppLaunchResult.Success
        } catch (e: Exception) {
            openSystemSettings()
        }
    }

    /**
     * Opens the top-level Android TV System Settings screen so users can navigate manually
     * to Apps -> Default Apps -> Home App when direct Home settings navigation is restricted.
     */
    fun openSystemSettings(): AppLaunchResult {
        return try {
            val generalSettings = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(generalSettings)
            AppLaunchResult.Success
        } catch (ex: Exception) {
            AppLaunchResult.Error("Unable to open Android system settings on this television device.")
        }
    }
}
