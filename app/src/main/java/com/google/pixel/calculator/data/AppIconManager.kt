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
        AppThemeMode.CANDY_POP to "$PACKAGE_NAME.MainActivityAliasCandyPop",
        AppThemeMode.RETRO_CYBER to "$PACKAGE_NAME.MainActivityAliasRetroCyber"
    )

    /**
     * Applica l'icona dell'applicazione corrispondente al tema scelto.
     * Abilita l'activity-alias desiderato e disabilita tutti gli altri.
     */
    fun setAppIcon(context: Context, themeMode: AppThemeMode): Boolean {
        return try {
            val packageManager = context.packageManager
            val targetAlias = ALIAS_MAP[themeMode] ?: ALIAS_MAP[AppThemeMode.SYSTEM_AUTO]!!

            // Abilita l'alias selezionato
            packageManager.setComponentEnabledSetting(
                ComponentName(context, targetAlias),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Disabilita gli altri alias
            ALIAS_MAP.values.forEach { alias ->
                if (alias != targetAlias) {
                    packageManager.setComponentEnabledSetting(
                        ComponentName(context, alias),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
            }

            Log.d(TAG, "Icona applicazione aggiornata con successo al tema: $themeMode ($targetAlias)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Errore durante il cambio icona applicazione per il tema $themeMode", e)
            false
        }
    }

    /**
     * Restituisce il tema attualmente impostato per l'icona dell'applicazione leggendo il PackageManager.
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
            Log.e(TAG, "Errore nella lettura dell'icona attiva dal PackageManager", e)
            AppThemeMode.SYSTEM_AUTO
        }
    }

    /**
     * Restituisce la risorsa drawable foreground per il tema
     */
    fun getForegroundDrawableRes(themeMode: AppThemeMode): Int = when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> R.drawable.ic_launcher_foreground_system_auto
        AppThemeMode.SYSTEM_LIGHT -> R.drawable.ic_launcher_foreground_system_light
        AppThemeMode.SYSTEM_DARK -> R.drawable.ic_launcher_foreground_system_dark
        AppThemeMode.STANDARD_LIGHT -> R.drawable.ic_launcher_foreground_standard_light
        AppThemeMode.STANDARD_DARK -> R.drawable.ic_launcher_foreground_standard_dark
        AppThemeMode.CANDY_POP -> R.drawable.ic_launcher_foreground_candy_pop
        AppThemeMode.RETRO_CYBER -> R.drawable.ic_launcher_foreground_retro_cyber
    }

    /**
     * Restituisce la risorsa drawable background per il tema
     */
    fun getBackgroundDrawableRes(themeMode: AppThemeMode): Int = when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> R.drawable.ic_launcher_background_system_auto
        AppThemeMode.SYSTEM_LIGHT -> R.drawable.ic_launcher_background_system_light
        AppThemeMode.SYSTEM_DARK -> R.drawable.ic_launcher_background_system_dark
        AppThemeMode.STANDARD_LIGHT -> R.drawable.ic_launcher_background_standard_light
        AppThemeMode.STANDARD_DARK -> R.drawable.ic_launcher_background_standard_dark
        AppThemeMode.CANDY_POP -> R.drawable.ic_launcher_background_candy_pop
        AppThemeMode.RETRO_CYBER -> R.drawable.ic_launcher_background_retro_cyber
    }
}
