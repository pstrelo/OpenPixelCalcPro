package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.graphics.Color
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine

enum class KeypadMode {
    OPERATIONS,
    FUNCTIONS
}

enum class ScreenViewMode {
    CALCULATOR_ONLY,
    SPLIT_SCREEN,
    FULLSCREEN_GRAPH
}

data class FormulaEntry(
    val id: Int,
    val expression: String = "",
    val color: Color = Color(0xFF4E75F6),
    val resultText: String = "",
    val lastSolveResult: AdvancedMathEngine.SolveResult? = null,
    val activeFormat: AdvancedMathEngine.ResultFormat = AdvancedMathEngine.ResultFormat.DECIMAL,
    val sampledPoints: List<AdvancedMathEngine.Point?> = emptyList(),
    val roots: List<Double> = emptyList(),
    val isError: Boolean = false
)

data class AdvancedCalculatorState(
    val formulas: List<FormulaEntry> = listOf(
        FormulaEntry(
            id = 1,
            expression = "x^2 + 2x - 3 = 2x + 6",
            color = Color(0xFF4E75F6)
        )
    ),
    val activeFormulaIndex: Int = 0,
    val keypadMode: KeypadMode = KeypadMode.OPERATIONS,
    val screenViewMode: ScreenViewMode = ScreenViewMode.CALCULATOR_ONLY,
    val isRadians: Boolean = true,
    val showAlgebraStrip: Boolean = false,
    val showModeDropdown: Boolean = false,
    val isCatalogOpen: Boolean = false,
    val activeCatalogTab: Int = 0,
    val graphScale: Float = 40f,
    val graphOffsetX: Float = 0f,
    val graphOffsetY: Float = 0f,
    val selectedPoint: Pair<Float, Float>? = null,
    val shadedIntervals: List<AdvancedMathEngine.ShadedInterval> = emptyList(),
    val memoryValue: Double = 0.0,
    val arcSubMenuVisible: Boolean = false
)
