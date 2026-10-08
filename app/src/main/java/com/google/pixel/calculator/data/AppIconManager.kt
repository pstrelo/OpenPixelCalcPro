package com.google.pixel.calculator.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.pixel.calculator.R

object AppIconManager {
    private const val TAG = "AppIconManager"

    private const val PACKAGE_NAME = "com.google.pixel.calculator"

    private val ALIAS_MAP = mapOf(
        AppThemeMode.SYSTEM_AUTO to "$PACKAGE_NAME.MainActivityAliasSystemAuto",
        AppThemeMode.SYSTEM_LIGHT to "$PACKAGE_NAME.MainActivityAliasSystemLight",
        AppThemeMode.SYSTEM_DARK to "$PACKAGE_NAME.MainActivityAliasSystemDark",
        AppThemeMode.STANDARD_LIGHT to "$PACKAGE_NAME.MainActivityAliasStandardLight",
        AppThemeMode.STANDARD_DARK to "$PACKAGE_NAME.MainActivityAliasStandardDark",
        AppThemeMode.PIXEL_SQUIRCLE to "$PACKAGE_NAME.MainActivityAliasStandardDark",
        AppThemeMode.CANDY_POP to "$PACKAGE_NAME.MainActivityAliasCandyPop",
        AppThemeMode.RETRO_CYBER to "$PACKAGE_NAME.MainActivityAliasRetroCyber"
    )

    /**
     * Applies the application icon matching the selected theme.
     * Enables the desired activity-alias and disables all others.
     */
    fun setAppIcon(context: Context, themeMode: AppThemeMode): Boolean {
        return try {
            val packageManager = context.packageManager
            val targetAlias = ALIAS_MAP[themeMode] ?: ALIAS_MAP[AppThemeMode.SYSTEM_AUTO]!!

            // Enable selected alias
            packageManager.setComponentEnabledSetting(
                ComponentName(context, targetAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disable other aliases
            ALIAS_MAP.values.forEach { alias ->
                if (alias != targetAlias) {
                    packageManager.setComponentEnabledSetting(
                        ComponentName(context, alias),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            Log.d(TAG, "Application icon successfully updated for theme: $themeMode ($targetAlias)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error changing application icon for theme $themeMode", e)
            false
        }
    }

    /**
     * Returns the currently active theme for the app icon from PackageManager.
     */
    fun getCurrentIcon(context: Context): AppThemeMode {
        return try {
            val packageManager = context.packageManager
            for ((themeMode, alias) in ALIAS_MAP) {
                val state = packageManager.getComponentEnabledSetting(ComponentName(context, alias))
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return themeMode
                }
            }
            AppThemeMode.SYSTEM_AUTO
        } catch (e: Exception) {
            Log.e(TAG, "Error reading active icon from PackageManager", e)
            AppThemeMode.SYSTEM_AUTO
        }
    }

    /**
     * Returns the foreground drawable resource for the theme
     */
    fun getForegroundDrawableRes(themeMode: AppThemeMode): Int = when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> R.drawable.ic_launcher_foreground_system_auto
        AppThemeMode.SYSTEM_LIGHT -> R.drawable.ic_launcher_foreground_system_light
        AppThemeMode.SYSTEM_DARK -> R.drawable.ic_launcher_foreground_system_dark
        AppThemeMode.STANDARD_LIGHT -> R.drawable.ic_launcher_foreground_standard_light
        AppThemeMode.STANDARD_DARK -> R.drawable.ic_launcher_foreground_standard_dark
        AppThemeMode.PIXEL_SQUIRCLE -> R.drawable.ic_launcher_foreground_standard_dark
        AppThemeMode.CANDY_POP -> R.drawable.ic_launcher_foreground_candy_pop
        AppThemeMode.RETRO_CYBER -> R.drawable.ic_launcher_foreground_retro_cyber
    }

    /**
     * Returns the background drawable resource for the theme
     */
    fun getBackgroundDrawableRes(themeMode: AppThemeMode): Int = when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> R.drawable.ic_launcher_background_system_auto
        AppThemeMode.SYSTEM_LIGHT -> R.drawable.ic_launcher_background_system_light
        AppThemeMode.SYSTEM_DARK -> R.drawable.ic_launcher_background_system_dark
        AppThemeMode.STANDARD_LIGHT -> R.drawable.ic_launcher_background_standard_light
        AppThemeMode.STANDARD_DARK -> R.drawable.ic_launcher_background_standard_dark
        AppThemeMode.PIXEL_SQUIRCLE -> R.drawable.ic_launcher_background_standard_dark
        AppThemeMode.CANDY_POP -> R.drawable.ic_launcher_background_candy_pop
        AppThemeMode.RETRO_CYBER -> R.drawable.ic_launcher_background_retro_cyber
    }
}
