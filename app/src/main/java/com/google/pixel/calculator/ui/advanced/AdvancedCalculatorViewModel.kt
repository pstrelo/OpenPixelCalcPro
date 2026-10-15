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

    private var graphSamplingJob: kotlinx.coroutines.Job? = null

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
        updateAllGraphPoints()
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
                    shadedIntervals = emptyList(),
                    inequality2D = null,
                    isError = false
                )
            }
            _uiState.update { it.copy(shadedIntervals = emptyList()) }
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
                activeEquationIndex = newActiveIndex,
                shadedIntervals = updated.flatMap { eq -> eq.shadedIntervals }
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
        val newPos = Formula2DParser.getPrevCursorPosition(eq.expression, eq.cursorPosition)
        updateEquationAt(curIndex) { it.copy(cursorPosition = newPos) }
    }

    fun moveCursorRight() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        val newPos = Formula2DParser.getNextCursorPosition(eq.expression, eq.cursorPosition)
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

        val (newExpr, newPos) = when {
            text == "/" || text == "□/□" -> {
                // If cursor is at the end of numerator in an existing fraction, typing '/' drops down to denominator
                val segments = Formula2DParser.parse(expr)
                val inFraction = segments.filterIsInstance<FormulaSegment.FractionSegment>().firstOrNull { pos == it.numEnd }
                if (inFraction != null) {
                    Pair(expr, inFraction.denStart)
                } else if (pos > 0 && (expr[pos - 1].isLetterOrDigit() || expr[pos - 1] in ")πe")) {
                    // Preceded by a token: insert '/' and place cursor in denominator
                    val updated = expr.substring(0, pos) + "/" + expr.substring(pos)
                    Pair(updated, pos + 1)
                } else if (expr.isEmpty()) {
                    Pair("()/( )", 1)
                } else {
                    val updated = expr.substring(0, pos) + "/" + expr.substring(pos)
                    Pair(updated, pos + 1)
                }
            }
            text == "^" || text == "□ⁿ" || text == "xⁿ" -> {
                // Power template: insert ^() with cursor inside
                val updated = expr.substring(0, pos) + "^()" + expr.substring(pos)
                Pair(updated, pos + 2)
            }
            text == "²" || text == "□²" || text == "x²" -> {
                // Square template: insert ^(2) with cursor placed after ')' on baseline
                val updated = expr.substring(0, pos) + "^(2)" + expr.substring(pos)
                Pair(updated, pos + 4)
            }
            text == "sqrt(" || text == "√" || text == "√x" || text == "√□" -> {
                // Radical template: insert sqrt() with cursor inside
                val updated = expr.substring(0, pos) + "sqrt()" + expr.substring(pos)
                Pair(updated, pos + 5)
            }
            text == "±" -> {
                val updated = expr.substring(0, pos) + "±" + expr.substring(pos)
                Pair(updated, pos + 1)
            }
            text == "log_" || text == "log_□" || text == "log₍□₎" -> {
                // Inserts log_()( ) template with cursor inside the base
                val updated = expr.substring(0, pos) + "log_()( )" + expr.substring(pos)
                Pair(updated, pos + 5) // cursor inside the base: log_( | )( )
            }
            text == "ⁿ√□" || text == "ʸ√" || text == "□√□" -> {
                // Inserts ^()√( ) template with cursor inside the root index [apice n]
                val updated = expr.substring(0, pos) + "^()√( )" + expr.substring(pos)
                Pair(updated, pos + 2) // cursor inside index: ^( | )√( )
            }
            text == "x_□" || text == "_" -> {
                // Inserts _() subscript template with cursor inside
                val updated = expr.substring(0, pos) + "_()" + expr.substring(pos)
                Pair(updated, pos + 2) // cursor inside subscript: _( | )
            }
            text == "(x)" -> {
                // Quick function argument insertion: insert "(x)" and place cursor after ')'
                val updated = expr.substring(0, pos) + "(x)" + expr.substring(pos)
                Pair(updated, pos + 3)
            }
            text == "()" || text == "( )" || text == "(" -> {
                // Auto-close: insert "()" and place cursor inside
                val updated = expr.substring(0, pos) + "()" + expr.substring(pos)
                Pair(updated, pos + 1)
            }
            text == ")" -> {
                // If next character is already ')', skip over it without duplicating
                if (pos < expr.length && expr[pos] == ')') {
                    Pair(expr, pos + 1)
                } else {
                    val updated = expr.substring(0, pos) + ")" + expr.substring(pos)
                    Pair(updated, pos + 1)
                }
            }
            // If cursor is right on or before a placeholder '□', replace '□' with the entered text!
            pos < expr.length && expr[pos] == '□' -> {
                val updated = expr.substring(0, pos) + text + expr.substring(pos + 1)
                Pair(updated, pos + text.length)
            }
            // Smart step-over for binary operators if cursor is right after an argument like "f(x|)"
            pos < expr.length && expr[pos] == ')' && text in listOf("+", "-", "*", "/", "×", "÷", "^", "=") &&
                Regex("[a-zA-Z]\\([a-zA-Z0-9]+$").containsMatchIn(expr.substring(0, pos)) -> {
                // Jump over the closing parenthesis and insert operator outside: "f(x) + "
                val updated = expr.substring(0, pos + 1) + text + expr.substring(pos + 1)
                Pair(updated, pos + 1 + text.length)
            }
            text == "|" -> {
                // If next character is already '|', skip over it
                if (pos < expr.length && expr[pos] == '|') {
                    Pair(expr, pos + 1)
                } else {
                    // Auto-close: insert "||" and place cursor inside
                    val updated = expr.substring(0, pos) + "||" + expr.substring(pos)
                    Pair(updated, pos + 1)
                }
            }
            else -> {
                val updated = expr.substring(0, pos) + text + expr.substring(pos)
                Pair(updated, pos + text.length)
            }
        }

        val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newPos,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                inequality2D = if (hasIneq) it.inequality2D else null,
                shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                isError = false
            )
        }
        if (!hasIneq) {
            _uiState.update { current ->
                current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
            }
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

        // Smart delete between pairs: if cursor is inside () or ||, delete both
        if (pos < expr.length) {
            val prevChar = expr[pos - 1]
            val nextChar = expr[pos]
            if ((prevChar == '(' && nextChar == ')') || (prevChar == '|' && nextChar == '|')) {
                val newExpr = expr.substring(0, pos - 1) + expr.substring(pos + 1)
                val newPos = pos - 1
                val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
                updateEquationAt(curIndex) {
                    it.copy(
                        expression = newExpr,
                        cursorPosition = newPos,
                        formattedResult = "",
                        lastSolveResult = null,
                        roots = emptyList(),
                        inequality2D = if (hasIneq) it.inequality2D else null,
                        shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                        isError = false
                    )
                }
                if (!hasIneq) {
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                updateGraphForActiveEquation()
                return
            }
        }

        // Smart template deletion: if inside empty ^()√( ) or log_()( ) or _()
        if (pos >= 2 && expr.substring(pos - 2, pos) == "^(" && expr.startsWith(")√( )", pos)) {
            val newExpr = expr.substring(0, pos - 2) + expr.substring(pos + 5)
            val newPos = pos - 2
            val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
            updateEquationAt(curIndex) {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newPos,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    inequality2D = if (hasIneq) it.inequality2D else null,
                    shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                    isError = false
                )
            }
            updateGraphForActiveEquation()
            return
        }
        if (pos >= 5 && expr.substring(pos - 5, pos) == "log_(" && expr.startsWith(")( )", pos)) {
            val newExpr = expr.substring(0, pos - 5) + expr.substring(pos + 4)
            val newPos = pos - 5
            val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
            updateEquationAt(curIndex) {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newPos,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    inequality2D = if (hasIneq) it.inequality2D else null,
                    shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                    isError = false
                )
            }
            updateGraphForActiveEquation()
            return
        }
        if (pos >= 2 && expr.substring(pos - 2, pos) == "_(" && pos < expr.length && expr[pos] == ')') {
            val newExpr = expr.substring(0, pos - 2) + expr.substring(pos + 1)
            val newPos = pos - 2
            val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
            updateEquationAt(curIndex) {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newPos,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    inequality2D = if (hasIneq) it.inequality2D else null,
                    shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                    isError = false
                )
            }
            updateGraphForActiveEquation()
            return
        }

        // Smart template deletion: if inside empty ^() or sqrt()
        if (pos >= 2 && expr.substring(pos - 2, pos) == "^(" && pos < expr.length && expr[pos] == ')') {
            val newExpr = expr.substring(0, pos - 2) + expr.substring(pos + 1)
            val newPos = pos - 2
            val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
            updateEquationAt(curIndex) {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newPos,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    inequality2D = if (hasIneq) it.inequality2D else null,
                    shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                    isError = false
                )
            }
            updateGraphForActiveEquation()
            return
        }
        if (pos >= 5 && expr.substring(pos - 5, pos) == "sqrt(" && pos < expr.length && expr[pos] == ')') {
            val newExpr = expr.substring(0, pos - 5) + expr.substring(pos + 1)
            val newPos = pos - 5
            val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
            updateEquationAt(curIndex) {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newPos,
                    formattedResult = "",
                    lastSolveResult = null,
                    roots = emptyList(),
                    inequality2D = if (hasIneq) it.inequality2D else null,
                    shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                    isError = false
                )
            }
            updateGraphForActiveEquation()
            return
        }

        // Multi-character token handling: if preceded by " = ", delete all 3 characters
        // Or if deleting right after log_ or log_□, delete the whole template or prefix
        // Or if preceded by legacy "^2", delete both characters together
        val deleteLen = when {
            pos >= 3 && expr.substring(pos - 3, pos) == " = " -> 3
            pos >= 4 && expr.substring(pos - 4, pos) == "log_" -> 4
            pos >= 5 && expr.substring(pos - 5, pos) == "log_□" -> 5
            pos >= 2 && expr.substring(pos - 2, pos) == "^2" -> 2
            else -> 1
        }

        val newExpr = expr.substring(0, pos - deleteLen) + expr.substring(pos)
        val newPos = pos - deleteLen
        val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }

        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newPos,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                inequality2D = if (hasIneq) it.inequality2D else null,
                shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                isError = false
            )
        }
        if (!hasIneq) {
            _uiState.update { current ->
                current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
            }
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
                shadedIntervals = emptyList(),
                inequality2D = null,
                isError = false
            )
        }
        _uiState.update { current ->
            current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
        }
        updateGraphForActiveEquation()
    }

    fun updateCurrentExpression(newExpr: String) {
        val curIndex = _uiState.value.activeEquationIndex
        val hasIneq = listOf("<=", ">=", "≤", "≥", "<", ">").any { newExpr.contains(it) }
        updateEquationAt(curIndex) {
            it.copy(
                expression = newExpr,
                cursorPosition = newExpr.length,
                formattedResult = "",
                lastSolveResult = null,
                roots = emptyList(),
                inequality2D = if (hasIneq) it.inequality2D else null,
                shadedIntervals = if (hasIneq) it.shadedIntervals else emptyList(),
                isError = false
            )
        }
        if (!hasIneq) {
            _uiState.update { current ->
                current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
            }
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
            "x²", "□²", "²" -> insertCharacter("²")
            "ⁿ√", "ⁿ√□", "□√□", "ʸ√" -> insertCharacter("ⁿ√□")
            "√", "√□" -> insertCharacter("sqrt(")
            "□/□" -> insertCharacter("/")
            "|x|", "|□|" -> insertCharacter("|")
            "n!", "x!", "□!" -> insertCharacter("!")
            "log" -> insertCharacter("log(")
            "log_□", "log_", "logₙ", "logn", "log₍□₎" -> insertCharacter("log_□")
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

    companion object {
        private val FUNCTION_LETTERS = listOf("f", "g", "h", "p", "q", "r", "u", "v")

        fun getFunctionLetter(index: Int): String {
            return if (index in FUNCTION_LETTERS.indices) FUNCTION_LETTERS[index] else "y${index + 1}"
        }

        fun getFunctionName(index: Int): String {
            return "${getFunctionLetter(index)}(x)"
        }
    }

    fun getFunctionLetterForIndex(index: Int): String = getFunctionLetter(index)
    fun getFunctionNameForIndex(index: Int): String = getFunctionName(index)

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

    fun getCustomFunctions(excludeIndex: Int = -1): Map<String, (List<java.math.BigDecimal>, com.google.pixel.calculator.core.math.EvaluationContext) -> java.math.BigDecimal> {
        val functions = mutableMapOf<String, (List<java.math.BigDecimal>, com.google.pixel.calculator.core.math.EvaluationContext) -> java.math.BigDecimal>()
        val parser = com.google.pixel.calculator.core.math.ExpressionParser()

        _uiState.value.equations.forEachIndexed { idx, item ->
            if (idx != excludeIndex && item.expression.isNotBlank()) {
                val fnName = getFunctionLetter(idx).lowercase()
                val body = engine.stripFunctionDefinition(item.expression).trim()
                if (body.isNotBlank()) {
                    try {
                        val ast = parser.parse(body)
                        functions[fnName] = { args, evalCtx ->
                            val xVal = args.firstOrNull() ?: java.math.BigDecimal.ZERO
                            val subVars = evalCtx.variables + ("x" to xVal)
                            val subCtx = evalCtx.copy(variables = subVars)
                            ast.evaluate(subCtx)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        return functions
    }

    fun getCustomFunctionsFast(
        excludeIndex: Int = -1,
        isRadians: Boolean = true,
        customVars: Map<String, Double> = emptyMap()
    ): Map<String, (Double) -> Double> {
        val functions = mutableMapOf<String, (Double) -> Double>()
        val parser = com.google.pixel.calculator.core.math.ExpressionParser()

        _uiState.value.equations.forEachIndexed { idx, item ->
            if (idx != excludeIndex && item.expression.isNotBlank()) {
                val fnName = getFunctionLetter(idx).lowercase()
                val body = engine.stripFunctionDefinition(item.expression).trim()
                if (body.isNotBlank()) {
                    try {
                        val ast = parser.parse(body)
                        functions[fnName] = { xArg ->
                            com.google.pixel.calculator.core.math.FastDoubleEvaluator.evaluate(
                                node = ast,
                                x = xArg,
                                isRadians = isRadians,
                                customVars = customVars,
                                customFns = emptyMap() // Prevents mutual recursive lock
                            )
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        return functions
    }

    fun executeSolve() {
        val curIndex = _uiState.value.activeEquationIndex
        val eq = _uiState.value.equations.getOrNull(curIndex) ?: return
        if (eq.expression.isBlank()) return

        viewModelScope.launch(dispatcher) {
            val isRad = _uiState.value.isRadians
            val customVars = getCustomVariables()
            val customFns = getCustomFunctions(excludeIndex = curIndex)
            val result = engine.solve(eq.expression, isRad, customVars, customFns)
            val format = eq.formatMode

            when (result) {
                is AdvancedMathEngine.SolveResult.VariableAssignment -> {
                    val formatted = "${result.variableName} = ${engine.formatNumber(result.value, format, isRad)}"
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = formatted,
                            lastSolveResult = result,
                            roots = emptyList(),
                            inequality2D = null,
                            shadedIntervals = emptyList(),
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
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
                            inequality2D = null,
                            shadedIntervals = emptyList(),
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                is AdvancedMathEngine.SolveResult.EquationRoots -> {
                    val formatted = formatRoots(result.roots, format, isRad)
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = formatted,
                            lastSolveResult = result,
                            roots = result.roots,
                            inequality2D = null,
                            shadedIntervals = emptyList(),
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                is AdvancedMathEngine.SolveResult.InequalityIntervals -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.text,
                            lastSolveResult = result,
                            inequality2D = null,
                            shadedIntervals = result.intervals,
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                is AdvancedMathEngine.SolveResult.Inequality2D -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.text,
                            lastSolveResult = result,
                            inequality2D = Inequality2DData(
                                boundaryExpr = result.boundaryExpr,
                                isAbove = result.isAbove,
                                isStrict = result.isStrict
                            ),
                            shadedIntervals = emptyList(),
                            roots = emptyList(),
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                is AdvancedMathEngine.SolveResult.SystemSolution -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.formattedText,
                            lastSolveResult = result,
                            inequality2D = null,
                            shadedIntervals = emptyList(),
                            isError = false
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
                    }
                }
                is AdvancedMathEngine.SolveResult.Error -> {
                    updateEquationAt(curIndex) {
                        it.copy(
                            formattedResult = result.message,
                            lastSolveResult = result,
                            inequality2D = null,
                            shadedIntervals = emptyList(),
                            isError = true
                        )
                    }
                    _uiState.update { current ->
                        current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
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
            val newCenterMathX = oldVp.centerMathX - (panChange.x / newScale)
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

            // Centroid-anchored zoom formula
            val newCenterMathX = oldVp.centerMathX - (pan.x / newScale)
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
        if (eq.expression.isBlank()) {
            updateEquationAt(curIndex) {
                it.copy(
                    sampledPoints = emptyList(),
                    inequality2D = null,
                    shadedIntervals = emptyList(),
                    roots = emptyList(),
                    formattedResult = "",
                    lastSolveResult = null
                )
            }
            _uiState.update { current ->
                current.copy(shadedIntervals = current.equations.flatMap { it.shadedIntervals })
            }
            updateAllGraphPoints()
            return
        }

        updateAllGraphPoints()
    }

    private data class ComputedEquationData(
        val points: List<AdvancedMathEngine.Point?>,
        val inequality2D: Inequality2DData?,
        val shadedIntervals: List<AdvancedMathEngine.ShadedInterval>
    )

    private fun updateAllGraphPoints() {
        graphSamplingJob?.cancel()
        graphSamplingJob = viewModelScope.launch(dispatcher) {
            val state = _uiState.value
            val scale = state.graphViewport.scaleX
            // Generous buffer of 2.2x screen width to avoid pop-in on pan
            val halfSpan = maxOf((1600f / scale) * 2.2f, 50f)
            val centerX = state.graphViewport.centerMathX.toFloat()
            val xMin = centerX - halfSpan
            val xMax = centerX + halfSpan

            val customVars = getCustomVariables()
            val doubleVars = customVars.mapValues { it.value.toDouble() }
            val ineqOps = listOf("<=", ">=", "≤", "≥", "<", ">")

            val computedMap = mutableMapOf<Any, ComputedEquationData>()

            for ((idx, entry) in state.equations.withIndex()) {
                val rawExpr = entry.expression.trim()
                if (rawExpr.isBlank()) {
                    computedMap[entry.id] = ComputedEquationData(emptyList(), null, emptyList())
                    continue
                }

                // If user typed e.g. "> 2" or ">= x", treat as 2D inequality "y > 2"
                val normalizedExpr = if (ineqOps.any { rawExpr.startsWith(it) }) {
                    "y $rawExpr"
                } else {
                    rawExpr
                }

                val cleanExpr = engine.stripFunctionDefinition(normalizedExpr.replace("θ", "x")).trim()
                val foundIneq = ineqOps.firstOrNull { cleanExpr.contains(it) }

                if (foundIneq != null) {
                    val ineq2DInfo = engine.parseInequality2D(cleanExpr, foundIneq, customVars)
                    if (ineq2DInfo != null) {
                        // 2D inequality (e.g. y > x, y >= 2, x + y > 3)
                        val ineq2DData = Inequality2DData(
                            boundaryExpr = ineq2DInfo.boundaryExpr,
                            isAbove = ineq2DInfo.isAbove,
                            isStrict = ineq2DInfo.isStrict
                        )
                        val fastCustomFns = getCustomFunctionsFast(excludeIndex = idx, isRadians = state.isRadians, customVars = doubleVars)
                        val pts = engine.sampleFunction(
                            expr = ineq2DInfo.boundaryExpr,
                            xMin = xMin,
                            xMax = xMax,
                            steps = 1100,
                            isRadians = state.isRadians,
                            customVariables = customVars,
                            fastCustomFunctions = fastCustomFns
                        )
                        computedMap[entry.id] = ComputedEquationData(pts, ineq2DData, emptyList())
                    } else {
                        // 1D inequality in x (e.g. x > 2, x <= -1, x^2 - 4 <= 0)
                        val intervals = try {
                            val ineqRes = engine.solveInequality(cleanExpr, foundIneq, isRadians = state.isRadians, customVariables = customVars)
                            if (ineqRes is AdvancedMathEngine.SolveResult.InequalityIntervals) {
                                ineqRes.intervals
                            } else {
                                emptyList()
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }
                        computedMap[entry.id] = ComputedEquationData(emptyList(), null, intervals)
                    }
                } else {
                    // Standard function / equation
                    val plotExpr = preparePlotExpression(cleanExpr)
                    if (plotExpr.isBlank()) {
                        computedMap[entry.id] = ComputedEquationData(emptyList(), null, emptyList())
                        continue
                    }

                    val fastCustomFns = getCustomFunctionsFast(excludeIndex = idx, isRadians = state.isRadians, customVars = doubleVars)
                    val pts = engine.sampleFunction(
                        expr = plotExpr,
                        xMin = xMin,
                        xMax = xMax,
                        steps = 1100,
                        isRadians = state.isRadians,
                        customVariables = customVars,
                        fastCustomFunctions = fastCustomFns
                    )
                    computedMap[entry.id] = ComputedEquationData(pts, null, emptyList())
                }
            }

            // Safe update by Equation ID to prevent wiping concurrent user edits
            _uiState.update { current ->
                val updated = current.equations.map { eq ->
                    val computed = computedMap[eq.id]
                    if (computed != null) {
                        eq.copy(
                            sampledPoints = computed.points,
                            inequality2D = computed.inequality2D,
                            shadedIntervals = computed.shadedIntervals
                        )
                    } else {
                        eq
                    }
                }
                val allIntervals = updated.flatMap { it.shadedIntervals }
                current.copy(equations = updated, shadedIntervals = allIntervals)
            }
        }
    }

    private fun preparePlotExpression(expr: String): String {
        if (engine.isVariableAssignment(expr)) {
            return ""
        }
        val cleanExpr = engine.stripFunctionDefinition(expr.replace("θ", "x")).trim()
        val ineqOps = listOf("<=", ">=", "≤", "≥", "<", ">")
        val foundIneq = ineqOps.firstOrNull { cleanExpr.contains(it) }
        if (foundIneq != null) {
            val ineq2D = engine.parseInequality2D(cleanExpr, foundIneq, getCustomVariables())
            if (ineq2D != null) {
                return ineq2D.boundaryExpr
            }
            // For 1D inequalities (e.g. x > 2), do not sample a fake diagonal y = x - 2 line;
            // only the shaded intervals are rendered on the Cartesian plane.
            return ""
        }
        return if (cleanExpr.contains("=")) {
            val parts = cleanExpr.split("=")
            if (parts.size == 2) {
                val lhs = parts[0].trim()
                val rhs = parts[1].trim()
                if (lhs.equals("y", ignoreCase = true) || Regex("^y[0-9]+$", RegexOption.IGNORE_CASE).matches(lhs)) {
                    rhs
                } else {
                    "(${lhs}) - (${rhs})"
                }
            } else cleanExpr
        } else {
            cleanExpr
        }
    }
}
