package com.google.pixel.calculator.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM_AUTO,    // Dynamic system (Auto: follows light/dark system with Monet accents)
    SYSTEM_LIGHT,   // Dynamic system (Always light with Monet accents)
    SYSTEM_DARK,    // Dynamic system (Always dark with Monet accents)
    STANDARD_LIGHT, // Classic standard light theme
    STANDARD_DARK,  // Classic standard dark theme
    PIXEL_SQUIRCLE, // Extra theme with squircle / square keypad buttons
    CANDY_POP,      // Vibrant Light (Pastel)
    RETRO_CYBER     // Vibrant Dark (Cyber/Neon)
}

class ThemePreferences(private val context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pixel_calc_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(loadHapticEnabled())
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _appIconMode = MutableStateFlow(loadAppIconMode())
    val appIconMode: StateFlow<AppThemeMode> = _appIconMode.asStateFlow()

    private val _syncIconWithTheme = MutableStateFlow(loadSyncIconWithTheme())
    val syncIconWithTheme: StateFlow<Boolean> = _syncIconWithTheme.asStateFlow()

    private fun parseThemeMode(name: String): AppThemeMode {
        return when (name) {
            "SYSTEM", "SYSTEM_AUTO" -> AppThemeMode.SYSTEM_AUTO
            "SYSTEM_LIGHT" -> AppThemeMode.SYSTEM_LIGHT
            "SYSTEM_DARK" -> AppThemeMode.SYSTEM_DARK
            "LIGHT", "STANDARD_LIGHT" -> AppThemeMode.STANDARD_LIGHT
            "DARK", "STANDARD_DARK" -> AppThemeMode.STANDARD_DARK
            "PIXEL_SQUIRCLE", "SQUIRCLE" -> AppThemeMode.PIXEL_SQUIRCLE
            "CANDY_POP" -> AppThemeMode.CANDY_POP
            "RETRO_CYBER" -> AppThemeMode.RETRO_CYBER
            else -> AppThemeMode.SYSTEM_AUTO
        }
    }

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString("selected_theme", AppThemeMode.SYSTEM_AUTO.name) ?: AppThemeMode.SYSTEM_AUTO.name
        return parseThemeMode(name)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("selected_theme", mode.name).apply()
        _themeMode.value = mode

        // If auto-sync between icon and theme is enabled, update the icon as well
        if (_syncIconWithTheme.value) {
            setAppIconMode(mode)
        }
    }

    private fun loadAppIconMode(): AppThemeMode {
        val saved = prefs.getString("selected_app_icon", null)
        return if (saved != null) {
            parseThemeMode(saved)
        } else {
            // If never saved, read active alias from PackageManager
            AppIconManager.getCurrentIcon(context)
        }
    }

    fun setAppIconMode(mode: AppThemeMode) {
        prefs.edit().putString("selected_app_icon", mode.name).apply()
        _appIconMode.value = mode
        AppIconManager.setAppIcon(context, mode)
    }

    private fun loadSyncIconWithTheme(): Boolean {
        return prefs.getBoolean("sync_icon_with_theme", false)
    }

    fun setSyncIconWithTheme(enabled: Boolean) {
        prefs.edit().putBoolean("sync_icon_with_theme", enabled).apply()
        _syncIconWithTheme.value = enabled
        if (enabled) {
            setAppIconMode(_themeMode.value)
        }
    }

    private fun loadHapticEnabled(): Boolean {
        return prefs.getBoolean("haptic_feedback", true)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _isHapticEnabled.value = enabled
    }
}
