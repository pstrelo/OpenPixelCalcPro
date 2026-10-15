package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine

/**
 * 4 Thematic Tabs for Google Math Solver redesign with distinct Material 3 Expressive accent colors:
 * 1. Algebra: Lilac / Purple (#A855F7)
 * 2. Trigonometry: Mint Green (#10B981)
 * 3. Calculus: Crimson / Pink (#EC4899)
 * 4. Alphabet: Peach / Orange (#F97316)
 */
enum class MathSolverTab(val title: String, val accentColor: Color) {
    ALGEBRA("Algebra", Color(0xFFA855F7)),
    TRIGONOMETRY("Trigonometry", Color(0xFF10B981)),
    CALCULUS("Calculus", Color(0xFFEC4899)),
    ALPHABET("Alphabet", Color(0xFFF97316))
}

/**
 * Centralized theme colors for Google Math Solver (M3 Expressive Dark Theme #1E212D).
 */
object MathSolverTheme {
    val background = Color(0xFF1E212D)
    val surfaceContainer = Color(0xFF25293A)
    val surfaceVariant = Color(0xFF2C3042)
    val outline = Color(0xFF3E455E)
    val tabUnselected = Color(0xFF94A3B8)

    // Thematic Tab Accents
    val accentAlgebra = Color(0xFFA855F7)        // Lilac / Purple
    val accentTrigonometry = Color(0xFF10B981)   // Mint Green
    val accentCalculus = Color(0xFFEC4899)       // Crimson / Pink
    val accentAlphabet = Color(0xFFF97316)       // Peach / Orange

    // Key Containers & Contents
    val numericContainer = Color(0xFF2E3346)
    val numericContent = Color(0xFFF8FAFC)

    val functionContainer = Color(0xFF252A3D)
    val functionContent = Color(0xFF93C5FD)

    // Tab-specific tinted containers (Material 3 Expressive)
    val algebraFunctionContainer = Color(0xFF2D253B)
    val algebraFunctionContent = Color(0xFFE9D5FF)

    val trigFunctionContainer = Color(0xFF1E322A)
    val trigFunctionContent = Color(0xFFA7F3D0)

    val calculusFunctionContainer = Color(0xFF382332)
    val calculusFunctionContent = Color(0xFFFBCFE8)

    val alphabetFunctionContainer = Color(0xFF34261F)
    val alphabetFunctionContent = Color(0xFFFED7AA)

    val operatorContainer = Color(0xFF2A2E40)
    val operatorContent = Color(0xFFCBD5E1)

    val deleteContainer = Color(0xFF3F2B35)
    val deleteContent = Color(0xFFFCA5A5)

    val clearContainer = Color(0xFF5C2636)
    val clearContent = Color(0xFFFECDD3)

    val formatContainer = Color(0xFF2A3048)
    val formatBorder = Color(0xFF4B5568)
    val formatLabel = Color(0xFF60A5FA)
}

enum class KeypadMode {
    OPERATIONS,
    FUNCTIONS
}

enum class ScreenViewMode {
    CALCULATOR_ONLY,
    SPLIT_SCREEN,
    FULLSCREEN_GRAPH
}

/**
 * Invariant Mathematical Viewport for 2D Cartesian Graph Canvas.
 * centerMathX/Y: Origin center in mathematical coordinate units.
 * scaleX/Y: Screen pixels per mathematical unit.
 */
data class GraphViewport(
    val centerMathX: Double = 0.0,
    val centerMathY: Double = 0.0,
    val scaleX: Float = 40f,
    val scaleY: Float = 40f
) {
    fun mathToScreen(mathX: Double, mathY: Double, canvasSize: Size): Offset {
        val screenX = ((canvasSize.width.toDouble() / 2.0) + (mathX - centerMathX) * scaleX.toDouble()).toFloat()
        val screenY = ((canvasSize.height.toDouble() / 2.0) - (mathY - centerMathY) * scaleY.toDouble()).toFloat()
        return Offset(screenX, screenY)
    }

    fun screenToMath(screenOffset: Offset, canvasSize: Size): Pair<Double, Double> {
        val mathX = centerMathX + (screenOffset.x.toDouble() - (canvasSize.width.toDouble() / 2.0)) / scaleX.toDouble()
        val mathY = centerMathY - (screenOffset.y.toDouble() - (canvasSize.height.toDouble() / 2.0)) / scaleY.toDouble()
        return Pair(mathX, mathY)
    }

    fun getVisibleDomainX(canvasWidth: Float, bufferFactor: Float = 0.15f): Pair<Double, Double> {
        val halfSpan = (canvasWidth / (2f * scaleX)).toDouble() * (1.0 + bufferFactor)
        return Pair(centerMathX - halfSpan, centerMathX + halfSpan)
    }
}

/**
 * Inspected point on the 2D Cartesian graph with coordinates, snapping status, and color.
 */
data class SelectedGraphPoint(
    val mathX: Float,
    val mathY: Float,
    val label: String,
    val isRoot: Boolean = false,
    val isYIntercept: Boolean = false,
    val color: Color = Color(0xFF60A5FA)
)

/**
 * Data describing a 2D inequality region on the Cartesian plane (e.g. y >= x or y + x > 2).
 */
data class Inequality2DData(
    val boundaryExpr: String,
    val isAbove: Boolean,
    val isStrict: Boolean
)

/**
 * Single equation item in the Desmos/GeoGebra style multi-equation list.
 * id is Any to transparently support both String IDs ("eq_1") and legacy Int IDs (1, 2, 3).
 */
data class EquationItem(
    val id: Any = "eq_1",
    val expression: String = "",
    val cursorPosition: Int = 0,
    val color: Color = Color(0xFF4E75F6),
    val lastSolveResult: AdvancedMathEngine.SolveResult? = null,
    val formatMode: AdvancedMathEngine.ResultFormat = AdvancedMathEngine.ResultFormat.DECIMAL,
    val formattedResult: String = "",
    val roots: List<Double> = emptyList(),
    val sampledPoints: List<AdvancedMathEngine.Point?> = emptyList(),
    val isError: Boolean = false,
    val inequality2D: Inequality2DData? = null,
    val shadedIntervals: List<AdvancedMathEngine.ShadedInterval> = emptyList(),
    val isVisible: Boolean = true
) {
    // Backward compatibility constructor for legacy FormulaEntry references with Int id
    constructor(
        id: Int,
        expression: String = "",
        cursorPosition: Int = 0,
        color: Color = Color(0xFF4E75F6),
        resultText: String = "",
        lastSolveResult: AdvancedMathEngine.SolveResult? = null,
        activeFormat: AdvancedMathEngine.ResultFormat = AdvancedMathEngine.ResultFormat.DECIMAL,
        sampledPoints: List<AdvancedMathEngine.Point?> = emptyList(),
        roots: List<Double> = emptyList(),
        isError: Boolean = false
    ) : this(
        id = id,
        expression = expression,
        cursorPosition = cursorPosition,
        color = color,
        lastSolveResult = lastSolveResult,
        formatMode = activeFormat,
        formattedResult = resultText,
        roots = roots,
        sampledPoints = sampledPoints,
        isError = isError,
        inequality2D = null,
        shadedIntervals = emptyList(),
        isVisible = true
    )

    // Legacy property aliases
    val resultText: String get() = formattedResult
    val activeFormat: AdvancedMathEngine.ResultFormat get() = formatMode
}

typealias FormulaEntry = EquationItem

/**
 * State for Google Math Solver Advanced Calculator Screen.
 */
data class AdvancedCalculatorState(
    val equations: List<EquationItem> = listOf(
        EquationItem(
            id = "eq_1",
            expression = "",
            cursorPosition = 0,
            color = Color(0xFF4E75F6)
        )
    ),
    val activeEquationIndex: Int = 0,
    val activeTab: MathSolverTab = MathSolverTab.ALGEBRA,
    val screenViewMode: ScreenViewMode = ScreenViewMode.CALCULATOR_ONLY,
    val isRadians: Boolean = true,
    val graphViewport: GraphViewport = GraphViewport(),
    val selectedGraphPoint: SelectedGraphPoint? = null,
    val selectedPoint: Pair<Float, Float>? = null,
    val shadedIntervals: List<AdvancedMathEngine.ShadedInterval> = emptyList(),
    val isCatalogOpen: Boolean = false,
    val activeCatalogTab: Int = 0,
    val memoryValue: Double = 0.0,
    val keypadMode: KeypadMode = KeypadMode.OPERATIONS,
    val showModeDropdown: Boolean = false,
    val showAlgebraStrip: Boolean = false,
    val arcSubMenuVisible: Boolean = false
) {
    // Secondary constructor for backward compatibility with legacy tests and composables
    constructor(
        formulas: List<EquationItem>,
        activeFormulaIndex: Int = 0,
        keypadMode: KeypadMode = KeypadMode.OPERATIONS,
        screenViewMode: ScreenViewMode = ScreenViewMode.CALCULATOR_ONLY,
        isRadians: Boolean = true,
        showAlgebraStrip: Boolean = false,
        showModeDropdown: Boolean = false,
        isCatalogOpen: Boolean = false,
        activeCatalogTab: Int = 0,
        graphScale: Float = 40f,
        graphOffsetX: Float = 0f,
        graphOffsetY: Float = 0f,
        selectedPoint: Pair<Float, Float>? = null,
        shadedIntervals: List<AdvancedMathEngine.ShadedInterval> = emptyList(),
        memoryValue: Double = 0.0,
        arcSubMenuVisible: Boolean = false
    ) : this(
        equations = formulas,
        activeEquationIndex = activeFormulaIndex,
        activeTab = MathSolverTab.ALGEBRA,
        screenViewMode = screenViewMode,
        isRadians = isRadians,
        graphViewport = GraphViewport(
            centerMathX = if (graphScale != 0f) (-graphOffsetX / graphScale).toDouble() else 0.0,
            centerMathY = if (graphScale != 0f) (graphOffsetY / graphScale).toDouble() else 0.0,
            scaleX = graphScale,
            scaleY = graphScale
        ),
        selectedGraphPoint = selectedPoint?.let {
            SelectedGraphPoint(mathX = it.first, mathY = it.second, label = "(%.2f; %.2f)".format(it.first, it.second))
        },
        selectedPoint = selectedPoint,
        shadedIntervals = shadedIntervals,
        isCatalogOpen = isCatalogOpen,
        activeCatalogTab = activeCatalogTab,
        memoryValue = memoryValue,
        keypadMode = keypadMode,
        showModeDropdown = showModeDropdown,
        showAlgebraStrip = showAlgebraStrip,
        arcSubMenuVisible = arcSubMenuVisible
    )

    val activeEquation: EquationItem?
        get() = equations.getOrNull(activeEquationIndex)

    val activeTabAccentColor: Color
        get() = activeTab.accentColor

    // Compatibility getters for legacy code and existing UI
    val formulas: List<EquationItem> get() = equations
    val activeFormulaIndex: Int get() = activeEquationIndex
    val graphScale: Float get() = graphViewport.scaleX
    val graphOffsetX: Float get() = (-graphViewport.centerMathX * graphViewport.scaleX).toFloat()
    val graphOffsetY: Float get() = (graphViewport.centerMathY * graphViewport.scaleY).toFloat()
    val viewport: GraphViewport get() = graphViewport
    val cursorPosition: Int get() = activeEquation?.cursorPosition ?: 0
}
