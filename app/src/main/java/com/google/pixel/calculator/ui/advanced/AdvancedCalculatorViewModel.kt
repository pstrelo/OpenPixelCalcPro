package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class AdvancedCalculatorViewModel(
    private val engine: AdvancedMathEngine = AdvancedMathEngine(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    val availableColors = listOf(
        Color(0xFF4E75F6), // Google Pixel Blue
        Color(0xFF10B981), // Emerald Mint Green
        Color(0xFFF59E0B), // Radiant Amber
        Color(0xFFEC4899), // Crimson Fuchsia
        Color(0xFF8B5CF6), // Vivid Violet
        Color(0xFF06B6D4)  // Cyan Teal
    )

    private val _uiState = MutableStateFlow(AdvancedCalculatorState())
    val uiState: StateFlow<AdvancedCalculatorState> = _uiState.asStateFlow()

    init {
        updateGraphForActiveEquation()
    }

    // ==========================================
    // 1. TABS & THEME ACCENTS
    // ==========================================

    fun selectTab(tab: MathSolverTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // ==========================================
    // 2. GESTIONE MULTI-EQUAZIONE (Max 4)
    // ==========================================

    fun selectEquation(index: Int) {
        if (index in _uiState.value.equations.indices) {
            _uiState.update { it.copy(activeEquationIndex = index) }
        }
    }

    fun selectEquationById(id: String) {
        val index = _uiState.value.equations.indexOfFirst { it.id == id }
        if (index != -1) {
            selectEquation(index)
        }
    }

    fun addEquation() {
        val current = _uiState.value.equations
        if (current.size >= 4) return

        val nextNum = (current.mapNotNull { it.id.toString().removePrefix("eq_").toIntOrNull() }.maxOrNull() ?: current.size) + 1
        val color = availableColors[current.size % availableColors.size]
        val newEntry = EquationItem(id = "eq_$nextNum", expression = "", cursorPosition = 0, color = color)

        _uiState.update {
            it.copy(
                equations = current + newEntry,
                activeEquationIndex = current.size
            )
        }
    }

    fun removeEquation(index: Int) {
        val current = _uiState.value.equations
        if (current.size <= 1) {
            // If there is only one equation, clear it without removing
            updateEquationAt(0) {
                it.copy(
                    expression = "",
                    cursorPosition = 0,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    sampledPoints = emptyList(),
                    isError = false
                )
            }
            updateAllGraphPoints()
            return
        }

        if (index !in current.indices) return
        val updated = current.toMutableList()
        updated.removeAt(index)
        val newActiveIndex = if (index >= updated.size) updated.size - 1 else index

        _uiState.update {
            it.copy(
                equations = updated,
                activeEquationIndex = newActiveIndex
            )
        }
        updateAllGraphPoints()
    }

    fun removeEquationById(id: String) {
        val index = _uiState.value.equations.indexOfFirst { it.id == id }
        if (index != -1) {
            removeEquation(index)
        }
    }

    // Backward compatibility aliases for existing UI composables
    fun selectFormula(index: Int) = selectEquation(index)
    fun addFormula() = addEquation()
    fun removeFormula(index: Int) = removeEquation(index)

    fun cycleEquationColor(index: Int) {
        if (index !in _uiState.value.equations.indices) return
        val currentEq = _uiState.value.equations[index]
        val curIndex = availableColors.indexOf(currentEq.color)
        val nextIndex = if (curIndex >= 0) (curIndex + 1) % availableColors.size else 0
        val newColor = availableColors[nextIndex]
        updateEquationAt(index) { it.copy(color = newColor) }
        updateAllGraphPoints()
    }

    // ==========================================
    // 3. CURSORE ED EDITING IN-PLACE
    // ==========================================

    fun moveCursorLeft() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val newPos = (eq.cursorPosition - 1).coerceAtLeast(0)
        updateEquationAt(curIndex) { it.copy(cursorPosition = newPos) }
    }

    fun moveCursorRight() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val newPos = (eq.cursorPosition + 1).coerceAtMost(eq.expression.length)
        updateEquationAt(curIndex) { it.copy(cursorPosition = newPos) }
    }

    fun setCursorPosition(pos: Int) {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val clamped = pos.coerceIn(0, eq.expression.length)
        updateEquationAt(curIndex) { it.copy(cursorPosition = clamped) }
    }

    fun insertCharacter(text: String) {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val expr = eq.expression
        val pos = eq.cursorPosition.coerceIn(0, expr.length)

        val newExpr = expr.substring(0, pos) + text + expr.substring(pos)
        val newPos = pos + text.length

        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newPos,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                isError = false
            )
        }
        updateGraphForActiveEquation()
    }

    fun insertText(text: String) = insertCharacter(text)

    fun deleteAtCursor() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val expr = eq.expression
        val pos = eq.cursorPosition.coerceIn(0, expr.length)
        if (pos <= 0) return

        // Multi-character token handling: if preceded by " = ", delete all 3 characters
        val deleteLen = when {
            pos >= 3 && expr.substring(pos - 3, pos) == " = " -> 3
            else -> 1
        }

        val newExpr = expr.substring(0, pos - deleteLen) + expr.substring(pos)
        val newPos = pos - deleteLen

        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newPos,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                isError = false
            )
        }
        updateGraphForActiveEquation()
    }

    fun onBackspace() = deleteAtCursor()

    fun onClearAll() {
        _uiState.update {
            it.copy(
                equations = listOf(EquationItem(id = "eq_1", expression = "", cursorPosition = 0, color = availableColors[0])),
                activeEquationIndex = 0,
                selectedGraphPoint = null,
                selectedPoint = null,
                shadedIntervals = emptyList()
            )
        }
    }

    fun clearActiveEquation() {
        val curIndex = _uiState.value.activeEquationIndex
        updateEquationAt(curIndex) {
            it.copy(
                expression = "",
                cursorPosition = 0,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                sampledPoints = emptyList(),
                isError = false
            )
        }
        updateGraphForActiveEquation()
    }

    fun updateCurrentExpression(newExpr: String) {
        val curIndex = _uiState.value.activeEquationIndex
        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newExpr.length,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                isError = false
            )
        }
        updateGraphForActiveEquation()
    }

    // ==========================================
    // 4. DISPATCHER TASTIERA UNIFICATO
    // ==========================================

    fun onKeyClick(key: String) {
        val curIndex = _uiState.value.activeEquationIndex
        val curExpr = _uiState.value.equations.getOrNull(curIndex)?.expression ?: ""

        when (key) {
            // Navigazione Cursore
            "←", "LEFT" -> moveCursorLeft()
            "→", "RIGHT" -> moveCursorRight()

            // Cancellazione
            "⌫", "DEL" -> deleteAtCursor()
            "CE" -> clearActiveEquation()
            "AC", "C" -> onClearAll()

            // Memoria
            "MC" -> _uiState.update { it.copy(memoryValue = 0.0) }
            "MR" -> insertCharacter(_uiState.value.memoryValue.toString())
            "MS" -> {
                val num = curExpr.toDoubleOrNull() ?: 0.0
                _uiState.update { it.copy(memoryValue = num) }
            }
            "M+" -> {
                val num = curExpr.toDoubleOrNull() ?: 0.0
                _uiState.update { it.copy(memoryValue = it.memoryValue + num) }
            }
            "M-" -> {
                val num = curExpr.toDoubleOrNull() ?: 0.0
                _uiState.update { it.copy(memoryValue = it.memoryValue - num) }
            }

            // Segno e funzioni rapide
            "+/-" -> {
                if (curExpr.startsWith("-")) {
                    val newExpr = curExpr.removePrefix("-")
                    updateEquationAt(curIndex) { it.copy(expression = newExpr, cursorPosition = (it.cursorPosition - 1).coerceAtLeast(0)) }
                } else {
                    val newExpr = "-$curExpr"
                    updateEquationAt(curIndex) { it.copy(expression = newExpr, cursorPosition = it.cursorPosition + 1) }
                }
                updateGraphForActiveEquation()
            }
            "xⁿ", "x□", "□^□" -> insertCharacter("^")
            "x²", "□²" -> insertCharacter("^2")
            "ⁿ√", "ⁿ√□" -> insertCharacter("ʸ√")
            "√", "√□" -> insertCharacter("sqrt(")
            "□/□" -> insertCharacter("/")
            "|x|", "|□|" -> insertCharacter("abs(")
            "n!", "x!", "□!" -> insertCharacter("!")
            "log", "log_□" -> insertCharacter("log(")
            "ln" -> insertCharacter("ln(")
            "f(x)" -> openCatalog()
            "CATALOG" -> openCatalog()
            "d/dx", "d/d□" -> insertCharacter("d/dx(")
            "∫", "∫□" -> insertCharacter("∫(")
            "∫_a^b", "∫_□^□" -> insertCharacter("∫(")
            "lim", "lim_□→□" -> insertCharacter("lim(")
            "lim+", "lim_□→□+" -> insertCharacter("lim(")
            "lim-", "lim_□→□-" -> insertCharacter("lim(")
            "Σ" -> insertCharacter("Σ(")
            "∞" -> insertCharacter("∞")
            "sin" -> insertCharacter("sin(")
            "cos" -> insertCharacter("cos(")
            "tan" -> insertCharacter("tan(")
            "arcsin" -> insertCharacter("arcsin(")
            "arccos" -> insertCharacter("arccos(")
            "arctan" -> insertCharacter("arctan(")
            "csc" -> insertCharacter("csc(")
            "sec" -> insertCharacter("sec(")
            "cot" -> insertCharacter("cot(")
            "π" -> insertCharacter("π")
            "°", "□°" -> insertCharacter("°")
            "C(n,k)" -> insertCharacter("C(")
            "P(n,k)" -> insertCharacter("P(")
            "e" -> insertCharacter("e")
            "i" -> insertCharacter("i")

            // KEY '=': Strictly inserts equals symbol into formula (does NOT calculate!)
            "=" -> insertCharacter(" = ")

            // KEY '➤' or 'EXE': Executes actual solution
            "➤", "EXE" -> executeSolve()

            // KEY 'FORMAT': Instantly cycles result representation
            "FORMAT" -> cycleFormat()

            // Key 'DEG / RAD' embedded in the keyboard
            "DEG", "RAD", "DEG/RAD" -> toggleRadians()

            // Variables
            "x" -> insertCharacter("x")
            "y" -> insertCharacter("y")

            else -> insertCharacter(key)
        }
    }

    // ==========================================
    // 5. SOLVE EXECUTION EXCLUSIVELY BY '➤'
    // ==========================================

    fun getCustomVariables(): Map<String, java.math.BigDecimal> {
        val vars = mutableMapOf<String, java.math.BigDecimal>()
        val isRad = _uiState.value.isRadians
        for (eq in _uiState.value.equations) {
            val expr = eq.expression.trim()
            if (engine.isVariableAssignment(expr)) {
                val parts = expr.split("=")
                if (parts.size == 2) {
                    val varName = parts[0].trim()
                    val rhs = parts[1].trim()
                    try {
                        val ast = com.google.pixel.calculator.core.math.ExpressionParser().parse(rhs)
                        val ctx = com.google.pixel.calculator.core.math.EvaluationContext(
                            variables = com.google.pixel.calculator.core.math.EvaluationContext.DEFAULT_VARIABLES + vars,
                            isRadians = isRad,
                            mathContext = java.math.MathContext(18, java.math.RoundingMode.HALF_UP)
                        )
                        val evaluated = ast.evaluate(ctx)
                        vars[varName] = evaluated
                    } catch (_: Exception) {}
                }
            }
        }
        return vars
    }

    fun executeSolve() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        if (eq.expression.isBlank()) return

        viewModelScope.launch(dispatcher) {
            val isRad = _uiState.value.isRadians
            val customVars = getCustomVariables()
            val result = engine.solve(eq.expression, isRad, customVars)
            val format = eq.formatMode

            when (result) {
                is AdvancedMathEngine.SolveResult.VariableAssignment -> {
                    val formatted = "${result.variableName} = ${engine.formatNumber(result.value, format, isRad)}"
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = formatted,
                            lastSolveResult = result,
                            roots = emptyList(),
                            isError = false
                        )
                    }
                    updateAllGraphPoints()
                }
                is AdvancedMathEngine.SolveResult.SingleValue -> {
                    val formatted = if (result.formatted.startsWith("f(0)") || result.formatted.startsWith("f'(") || result.formatted.startsWith("lim(")) {
                        result.formatted
                    } else {
                        "= ${engine.formatNumber(result.raw, format, isRad)}"
                    }
                    val isFuncSummary = result.formatted.startsWith("f(0)") || result.formatted.startsWith("f(x)")
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = formatted,
                            lastSolveResult = result,
                            roots = if (isFuncSummary) emptyList() else listOf(result.raw),
                            isError = false
                        )
                    }
                    _uiState.update { it.copy(shadedIntervals = emptyList()) }
                }
                is AdvancedMathEngine.SolveResult.EquationRoots -> {
                    val formatted = formatRoots(result.roots, format, isRad)
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = formatted,
                            lastSolveResult = result,
                            roots = result.roots,
                            isError = false
                        )
                    }
                    _uiState.update { it.copy(shadedIntervals = emptyList()) }
                }
                is AdvancedMathEngine.SolveResult.InequalityIntervals -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.text,
                            lastSolveResult = result,
                            isError = false
                        )
                    }
                    _uiState.update { it.copy(shadedIntervals = result.intervals) }
                }
                is AdvancedMathEngine.SolveResult.SystemSolution -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.formattedText,
                            lastSolveResult = result,
                            isError = false
                        )
                    }
                }
                is AdvancedMathEngine.SolveResult.Error -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.message,
                            lastSolveResult = result,
                            isError = true
                        )
                    }
                }
            }
            updateGraphForActiveEquation()
        }
    }

    fun onExecute() = executeSolve()

    // ==========================================
    // 6. INSTANT FORMAT CYCLING
    // ==========================================

    fun cycleFormat() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return

        val formats = AdvancedMathEngine.ResultFormat.values()
        val nextIndex = (eq.formatMode.ordinal + 1) % formats.size
        val newFormat = formats[nextIndex]
        val isRad = _uiState.value.isRadians

        val newFormatted = when (val res = eq.lastSolveResult) {
            is AdvancedMathEngine.SolveResult.VariableAssignment -> {
                "${res.variableName} = ${engine.formatNumber(res.value, newFormat, isRad)}"
            }
            is AdvancedMathEngine.SolveResult.SingleValue -> {
                "= ${engine.formatNumber(res.raw, newFormat, isRad)}"
            }
            is AdvancedMathEngine.SolveResult.EquationRoots -> {
                formatRoots(res.roots, newFormat, isRad)
            }
            else -> eq.formattedResult
        }

        updateEquationAt(curIndex) {
            it.copy(
                formatMode = newFormat,
                formattedResult = newFormatted
            )
        }
    }

    fun onCycleFormat() = cycleFormat()

    // ==========================================
    // 7. FORMATTAZIONE STAFFE A MATRICE RADICI
    // ==========================================

    fun formatRoots(
        roots: List<Double>,
        format: AdvancedMathEngine.ResultFormat,
        isRadians: Boolean
    ): String {
        if (roots.isEmpty()) return "Nessuna radice reale"
        if (roots.size == 1) {
            val formatted = engine.formatNumber(roots[0], format, isRadians)
            return "x = $formatted"
        }
        val formattedRoots = roots.map { r ->
            "x = ${engine.formatNumber(r, format, isRadians)}"
        }
        return "[ ${formattedRoots.joinToString(" ; ")} ]"
    }

    // ==========================================
    // 8. CATALOG & CONTROLLI VISTA
    // ==========================================

    fun openCatalog() {
        _uiState.update { it.copy(isCatalogOpen = true) }
    }

    fun closeCatalog() {
        _uiState.update { it.copy(isCatalogOpen = false) }
    }

    fun onSelectCatalogItem(insertion: String) {
        insertCharacter(insertion)
        closeCatalog()
    }

    fun setScreenViewMode(viewMode: ScreenViewMode) {
        _uiState.update { it.copy(screenViewMode = viewMode) }
        updateAllGraphPoints()
    }

    fun toggleGraphView() {
        _uiState.update {
            val newMode = when (it.screenViewMode) {
                ScreenViewMode.CALCULATOR_ONLY -> ScreenViewMode.SPLIT_SCREEN
                ScreenViewMode.SPLIT_SCREEN -> ScreenViewMode.CALCULATOR_ONLY
                ScreenViewMode.FULLSCREEN_GRAPH -> ScreenViewMode.SPLIT_SCREEN
            }
            it.copy(screenViewMode = newMode)
        }
        updateAllGraphPoints()
    }

    fun cycleGraphViewMode() {
        _uiState.update {
            val newMode = when (it.screenViewMode) {
                ScreenViewMode.CALCULATOR_ONLY -> ScreenViewMode.SPLIT_SCREEN
                ScreenViewMode.SPLIT_SCREEN -> ScreenViewMode.FULLSCREEN_GRAPH
                ScreenViewMode.FULLSCREEN_GRAPH -> ScreenViewMode.CALCULATOR_ONLY
            }
            it.copy(screenViewMode = newMode)
        }
        updateAllGraphPoints()
    }

    fun toggleRadians() {
        _uiState.update { it.copy(isRadians = !it.isRadians) }
        updateAllGraphPoints()
        val curIndex = _uiState.value.activeEquationIndex
        if (_uiState.value.equations.getOrNull(curIndex)?.lastSolveResult != null) {
            executeSolve()
        }
    }

    fun onGraphTransform(zoomChange: Float, panChange: Offset) {
        _uiState.update { current ->
            val oldVp = current.graphViewport
            val newScale = (oldVp.scaleX * zoomChange).coerceIn(10f, 300f)
            val newCenterMathX = oldVp.centerMathX + (panChange.x / newScale)
            val newCenterMathY = oldVp.centerMathY + (panChange.y / newScale)
            current.copy(
                graphViewport = oldVp.copy(
                    centerMathX = newCenterMathX,
                    centerMathY = newCenterMathY,
                    scaleX = newScale,
                    scaleY = newScale
                )
            )
        }
        updateAllGraphPoints()
    }

    fun onGraphTransform(centroid: Offset, pan: Offset, zoom: Float) {
        _uiState.update { current ->
            val oldVp = current.graphViewport
            val newScale = (oldVp.scaleX * zoom).coerceIn(10f, 300f)
            val effectiveZoom = newScale / oldVp.scaleX

            // Centroid-anchored zoom formula
            val newCenterMathX = oldVp.centerMathX + (pan.x / newScale)
            val newCenterMathY = oldVp.centerMathY + (pan.y / newScale)

            current.copy(
                graphViewport = oldVp.copy(
                    centerMathX = newCenterMathX,
                    centerMathY = newCenterMathY,
                    scaleX = newScale,
                    scaleY = newScale
                )
            )
        }
        updateAllGraphPoints()
    }

    fun onResetGraph() {
        _uiState.update {
            it.copy(
                graphViewport = GraphViewport(),
                selectedGraphPoint = null,
                selectedPoint = null
            )
        }
        updateAllGraphPoints()
    }

    fun onResetGraphViewport() = onResetGraph()

    fun selectGraphPoint(point: Pair<Float, Float>?) {
        _uiState.update {
            it.copy(
                selectedPoint = point,
                selectedGraphPoint = point?.let { p ->
                    SelectedGraphPoint(mathX = p.first, mathY = p.second, label = "(%.2f; %.2f)".format(p.first, p.second))
                }
            )
        }
    }

    fun selectGraphPoint(point: SelectedGraphPoint?) {
        _uiState.update {
            it.copy(
                selectedGraphPoint = point,
                selectedPoint = point?.let { p -> Pair(p.mathX, p.mathY) }
            )
        }
    }

    // ==========================================
    // 9. INTERNAL HELPERS FOR GRAPH
    // ==========================================

    private fun updateEquationAt(index: Int, transform: (EquationItem) -> EquationItem) {
        val current = _uiState.value.equations.toMutableList()
        if (index in current.indices) {
            current[index] = transform(current[index])
            _uiState.update { it.copy(equations = current) }
        }
    }

    private fun updateGraphForActiveEquation() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        if (eq.expression.isBlank()) return

        if (engine.isVariableAssignment(eq.expression)) {
            updateAllGraphPoints()
            return
        }

        viewModelScope.launch(dispatcher) {
            val state = _uiState.value
            val scale = state.graphViewport.scaleX
            // Generous buffer of at least 2.5x screen width to prevent clipping
            val halfSpan = maxOf((1600f / scale) * 1.5f, 40f)
            val centerX = state.graphViewport.centerMathX.toFloat()
            val xMin = centerX - halfSpan
            val xMax = centerX + halfSpan

            val customVars = getCustomVariables()
            val plotExpr = preparePlotExpression(eq.expression)
            val points = if (plotExpr.isNotBlank()) {
                engine.sampleFunction(plotExpr, xMin, xMax, 550, state.isRadians, customVars)
            } else {
                emptyList()
            }

            updateEquationAt(curIndex) { it.copy(sampledPoints = points) }
        }
    }

    private fun updateAllGraphPoints() {
        viewModelScope.launch(dispatcher) {
            val state = _uiState.value
            val scale = state.graphViewport.scaleX
            // Generous buffer of at least 2.5x screen width to prevent clipping
            val halfSpan = maxOf((1600f / scale) * 1.5f, 40f)
            val centerX = state.graphViewport.centerMathX.toFloat()
            val xMin = centerX - halfSpan
            val xMax = centerX + halfSpan

            val customVars = getCustomVariables()
            val updated = state.equations.map { entry ->
                val plotExpr = preparePlotExpression(entry.expression)
                if (plotExpr.isNotBlank()) {
                    val pts = engine.sampleFunction(plotExpr, xMin, xMax, 550, state.isRadians, customVars)
                    entry.copy(sampledPoints = pts)
                } else {
                    entry.copy(sampledPoints = emptyList())
                }
            }
            _uiState.update { it.copy(equations = updated) }
        }
    }

    private fun preparePlotExpression(expr: String): String {
        if (engine.isVariableAssignment(expr)) {
            return ""
        }
        return if (expr.contains("=")) {
            val parts = expr.split("=")
            if (parts.size == 2) {
                val lhs = parts[0].trim()
                val rhs = parts[1].trim()
                if (lhs.equals("y", ignoreCase = true) || Regex("^y[0-9]+$", RegexOption.IGNORE_CASE).matches(lhs)) {
                    rhs
                } else {
                    "(${lhs}) - (${rhs})"
                }
            } else expr
        } else {
            expr
        }
    }
}
