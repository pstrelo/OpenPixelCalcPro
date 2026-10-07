package com.google.pixel.calculator.ui

import com.google.pixel.calculator.core.math.MathNode
import com.google.pixel.calculator.data.AppThemeMode
import java.math.BigDecimal

enum class ActiveScreen {
    MAIN_CALCULATOR,
    HISTORY,
    SETTINGS,
    TOOLBOX_CONVERTER,
    TOOLBOX_LEVEL,
    TOOLBOX_COMPASS,
    TOOLBOX_RULER,
    ADVANCED_CALCULATOR,
    EASTER_EGG
}

data class CalculatorState(
    val expression: String = "",
    val resultPreview: String = "",
    val fullResult: String? = null,
    val ast: MathNode? = null,
    val isProMode: Boolean = false,
    val isRadians: Boolean = true,
    val isShiftActive: Boolean = false,
    val isAwaitingRootIndex: Boolean = false,
    val isHapticEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM_AUTO,
    val appIconMode: AppThemeMode = AppThemeMode.SYSTEM_AUTO,
    val syncIconWithTheme: Boolean = false,
    val decimalPrecision: Int = 10,
    val memoryValue: BigDecimal? = null,
    val activeScreen: ActiveScreen = ActiveScreen.MAIN_CALCULATOR,
    val showToolboxMenu: Boolean = false,
    val showSettingsDropdown: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showClearHistoryDialog: Boolean = false,
    val errorMessage: String? = null,
    val sessionHistory: List<Pair<String, String>> = emptyList(),
    val lastEvaluatedExpression: String? = null
)
