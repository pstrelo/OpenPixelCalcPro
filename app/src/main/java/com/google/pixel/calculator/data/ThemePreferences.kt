package com.google.pixel.calculator.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,      // Dinamico (Material You / Monet) che segue il sistema operativo
    LIGHT,       // Pixel Light classico
    DARK,        // Pixel Dark classico
    CANDY_POP,   // Vivace Chiaro (Pastello, Ispirato alla foto)
    RETRO_CYBER  // Vivace Scuro (Cyber/Neon, Ispirato alla foto)
}

class ThemePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("pixel_calc_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(loadHapticEnabled())
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString("selected_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("selected_theme", mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadHapticEnabled(): Boolean {
        return prefs.getBoolean("haptic_feedback", true)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_feedback", enabled).apply()
        _isHapticEnabled.value = enabled
    }
}
