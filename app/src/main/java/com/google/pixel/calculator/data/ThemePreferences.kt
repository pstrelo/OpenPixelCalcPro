package com.google.pixel.calculator.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM_AUTO,    // Dinamico di sistema (Auto: segue chiaro/scuro di sistema con accenti Monet)
    SYSTEM_LIGHT,   // Dinamico di sistema (Sempre chiaro con accenti Monet)
    SYSTEM_DARK,    // Dinamico di sistema (Sempre scuro con accenti Monet)
    STANDARD_LIGHT, // Tema chiaro standard classico
    STANDARD_DARK,  // Tema scuro standard classico
    CANDY_POP,      // Vivace Chiaro (Pastello, dalla foto)
    RETRO_CYBER     // Vivace Scuro (Cyber/Neon, dalla foto)
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

        // Se l'opzione di sincronizzazione automatica icona-tema è attiva, aggiorna anche l'icona
        if (_syncIconWithTheme.value) {
            setAppIconMode(mode)
        }
    }

    private fun loadAppIconMode(): AppThemeMode {
        val saved = prefs.getString("selected_app_icon", null)
        return if (saved != null) {
            parseThemeMode(saved)
        } else {
            // Se mai salvata, leggi dal PackageManager l'alias attivo
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
