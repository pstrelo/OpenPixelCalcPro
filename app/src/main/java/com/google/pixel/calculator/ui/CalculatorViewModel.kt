package com.google.pixel.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.MathEvaluator
import com.google.pixel.calculator.data.AppThemeMode
import com.google.pixel.calculator.data.HistoryStorage
import com.google.pixel.calculator.data.ThemePreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    historyStorage: HistoryStorage? = null,
    private val themePreferences: ThemePreferences? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val activeHistoryStorage: HistoryStorage? =
        historyStorage ?: HistoryStorage.getInstanceOrNull()

    val historyFlow: StateFlow<List<com.google.pixel.calculator.data.HistoryItem>> =
        activeHistoryStorage?.historyFlow ?: MutableStateFlow(emptyList())

    private val evaluator = MathEvaluator()

    private val _uiState = MutableStateFlow(
        CalculatorState(
            themeMode = themePreferences?.themeMode?.value ?: AppThemeMode.SYSTEM_AUTO,
            appIconMode = themePreferences?.appIconMode?.value ?: AppThemeMode.SYSTEM_AUTO,
            syncIconWithTheme = themePreferences?.syncIconWithTheme?.value ?: false,
            isHapticEnabled = themePreferences?.isHapticEnabled?.value ?: true
        )
    )
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    init {
        val initialHistory = activeHistoryStorage?.historyFlow?.value ?: emptyList()
        if (initialHistory.isNotEmpty()) {
            val recent = initialHistory.take(2).reversed().map { Pair(it.expression, it.result) }
            _uiState.update { it.copy(sessionHistory = recent) }
        }

        themePreferences?.let { prefs ->
            viewModelScope.launch(dispatcher) {
                prefs.themeMode.collect { mode ->
                    _uiState.update { it.copy(themeMode = mode) }
                }
            }
            viewModelScope.launch(dispatcher) {
                prefs.appIconMode.collect { iconMode ->
                    _uiState.update { it.copy(appIconMode = iconMode) }
                }
            }
            viewModelScope.launch(dispatcher) {
                prefs.syncIconWithTheme.collect { sync ->
                    _uiState.update { it.copy(syncIconWithTheme = sync) }
                }
            }
            viewModelScope.launch(dispatcher) {
                prefs.isHapticEnabled.collect { haptic ->
                    _uiState.update { it.copy(isHapticEnabled = haptic) }
                }
            }
        }
    }

    private fun toSuperscript(c: Char): Char = when (c) {
        '0' -> '⁰'
        '1' -> '¹'
        '2' -> '²'
        '3' -> '³'
        '4' -> '⁴'
        '5' -> '⁵'
        '6' -> '⁶'
        '7' -> '⁷'
        '8' -> '⁸'
        '9' -> '⁹'
        else -> c
    }

    // Append digit or dot
    fun onDigit(digit: String) {
        val state = _uiState.value
        val current = state.expression

        // Gestisci virgola / punto decimale
        val normalizedDigit = if (digit == ",") "." else digit

        if (state.isAwaitingRootIndex) {
            if (normalizedDigit.all { it.isDigit() }) {
                val supDigits = normalizedDigit.map { toSuperscript(it) }.joinToString("")
                val updated = if (current.endsWith("ʸ√")) {
                    current.removeSuffix("ʸ√") + "${supDigits}√("
                } else {
                    "${current}${supDigits}√("
                }
                _uiState.update { it.copy(isAwaitingRootIndex = false) }
                updateExpression(updated)
                return
            } else {
                _uiState.update { it.copy(isAwaitingRootIndex = false) }
            }
        }

        // Evita doppi punti decimali nello stesso numero corrente
        if (normalizedDigit == ".") {
            val lastTokens = current.split(Regex("[+\\-×÷/*^() ]"))
            val currentNumber = lastTokens.lastOrNull() ?: ""
            if ('.' in currentNumber) return
            val lastChar = current.lastOrNull()
            if (current.isEmpty() || (lastChar != null && lastChar in "+-×÷/*^(")) {
                updateExpression("${current}0.")
                return
            }
        }

        val updated = current + normalizedDigit
        updateExpression(updated)
    }

    // Append operator (+, -, ×, ÷, %, ^, !)
    fun onOperator(op: String) {
        val state = _uiState.value
        if (state.isAwaitingRootIndex) {
            _uiState.update { it.copy(isAwaitingRootIndex = false) }
        }
        val current = if (state.expression.endsWith("ʸ√")) state.expression.removeSuffix("ʸ√") else state.expression
        if (current.isEmpty() && op != "-" && op != "10^") return

        val updated = if (op == "%" || op == "!") {
            // Operatori postfissi: si attaccano all'operando precedente
            if (current.isEmpty() || current.last() in "+-×÷/*^(") current else current + op
        } else if (op == "10^") {
            "$current 10^("
        } else {
            val lastChar = current.lastOrNull()
            // Sostituisci solo se l'ultimo carattere era un operatore binario classico (+, -, ×, ÷, ^, /)
            if (lastChar != null && lastChar in "+-×÷/*^") {
                current.dropLast(1) + op
            } else {
                current + op
            }
        }
        updateExpression(updated)
    }

    // Append function: sin(, cos(, √, etc.
    fun onFunction(funcName: String) {
        val current = _uiState.value.expression
        when (funcName) {
            "sqrt", "√" -> {
                _uiState.update { it.copy(isAwaitingRootIndex = false) }
                updateExpression("${current}√(")
            }
            "root", "ʸ√" -> {
                val digitRegex = Regex("([0-9]+)$")
                val match = digitRegex.find(current)
                if (match != null) {
                    val digits = match.value
                    val supDigits = digits.map { toSuperscript(it) }.joinToString("")
                    val newExpr = current.substring(0, match.range.first) + "${supDigits}√("
                    _uiState.update { it.copy(isAwaitingRootIndex = false) }
                    updateExpression(newExpr)
                } else {
                    _uiState.update { it.copy(isAwaitingRootIndex = true) }
                    updateExpression("${current}ʸ√")
                }
            }
            else -> {
                _uiState.update { it.copy(isAwaitingRootIndex = false) }
                updateExpression("${current}${funcName}(")
            }
        }
    }

    fun onParenthesis(paren: String) {
        val current = _uiState.value.expression
        updateExpression(current + paren)
    }

    fun onSmartParenthesis() {
        val current = _uiState.value.expression
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        val lastChar = current.lastOrNull()
        val shouldClose = openCount > closeCount && lastChar != null && 
                (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!' || lastChar == '%')
        if (shouldClose) {
            updateExpression("$current)")
        } else {
            updateExpression("$current(")
        }
    }

    fun onConstant(constant: String) {
        val current = _uiState.value.expression
        updateExpression(current + constant)
    }

    fun onBackspace() {
        val state = _uiState.value
        val current = state.expression
        if (current.isEmpty()) return

        if (state.isAwaitingRootIndex) {
            _uiState.update { it.copy(isAwaitingRootIndex = false) }
            if (current.endsWith("ʸ√")) {
                updateExpression(current.removeSuffix("ʸ√"))
                return
            }
        }

        // Se termina con un blocco radice come ³√( o √(
        val rootPrefixRegex = Regex("([⁰¹²³⁴⁵⁶⁷⁸⁹]*)√\\($")
        val rootMatch = rootPrefixRegex.find(current)
        if (rootMatch != null) {
            updateExpression(current.substring(0, rootMatch.range.first))
            return
        }

        // Se termina con funzioni a più lettere
        for (fn in listOf("asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "ln(", "log(", "10^(")) {
            if (current.endsWith(fn)) {
                updateExpression(current.removeSuffix(fn))
                return
            }
        }

        val updated = current.dropLast(1)
        updateExpression(updated)
    }

    fun onClearAll() {
        _uiState.update {
            it.copy(
                expression = "",
                resultPreview = "",
                fullResult = null,
                ast = null,
                errorMessage = null,
                isAwaitingRootIndex = false,
                sessionHistory = emptyList(),
                lastEvaluatedExpression = null
            )
        }
    }

    fun onEquals() {
        val state = _uiState.value
        val expr = state.expression
        if (expr.isBlank()) return

        val cleanExpr = expr.replace("\\s+".toRegex(), "")
        if (cleanExpr == "26+04" || cleanExpr == "26+4") {
            navigateTo(ActiveScreen.EASTER_EGG)
            return
        }

        val closedExpr = evaluator.autoCloseParentheses(expr)
        val evalResult = evaluator.evaluate(
            closedExpr,
            EvaluationContext(
                isRadians = state.isRadians
            )
        )

        if (evalResult.isSuccess) {
            val updatedSession = (state.sessionHistory + Pair(closedExpr, evalResult.formattedResult)).takeLast(5)
            _uiState.update {
                it.copy(
                    fullResult = evalResult.formattedResult,
                    resultPreview = "",
                    expression = evalResult.formattedResult.replace(".", "").replace(",", "."),
                    ast = evalResult.ast,
                    errorMessage = null,
                    sessionHistory = updatedSession,
                    lastEvaluatedExpression = closedExpr
                )
            }
            // Salva nella cronologia persistente
            viewModelScope.launch(dispatcher) {
                activeHistoryStorage?.addItem(closedExpr, evalResult.formattedResult)
            }
        } else {
            _uiState.update {
                it.copy(
                    errorMessage = evalResult.errorMessage ?: "Errore di calcolo"
                )
            }
        }
    }

    fun toggleShift() {
        _uiState.update { it.copy(isShiftActive = !it.isShiftActive) }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch(dispatcher) {
            activeHistoryStorage?.deleteItem(id)
        }
    }

    fun clearAllHistory() {
        _uiState.update {
            it.copy(
                sessionHistory = emptyList(),
                lastEvaluatedExpression = null
            )
        }
        viewModelScope.launch(dispatcher) {
            activeHistoryStorage?.clearHistory()
        }
    }

    fun toggleProMode() {
        _uiState.update { it.copy(isProMode = !it.isProMode) }
    }

    fun toggleAngleUnit() {
        val newRad = !_uiState.value.isRadians
        _uiState.update { it.copy(isRadians = newRad) }

        val currentState = _uiState.value
        val lastExpr = currentState.lastEvaluatedExpression
        val currentExpr = currentState.expression

        if (lastExpr != null && currentExpr == currentState.fullResult?.replace(".", "")?.replace(",", ".")) {
            val reEval = evaluator.evaluate(lastExpr, EvaluationContext(isRadians = newRad))
            if (reEval.isSuccess) {
                _uiState.update {
                    it.copy(
                        fullResult = reEval.formattedResult,
                        expression = reEval.formattedResult.replace(".", "").replace(",", "."),
                        ast = reEval.ast,
                        resultPreview = ""
                    )
                }
                return
            }
        }

        if (currentExpr.isNotEmpty()) {
            val evalResult = evaluator.evaluate(currentExpr, EvaluationContext(isRadians = newRad))
            _uiState.update {
                it.copy(
                    resultPreview = if (evalResult.isSuccess && evalResult.formattedResult != currentExpr) {
                        evalResult.formattedResult
                    } else "",
                    ast = evalResult.ast,
                    errorMessage = null
                )
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        themePreferences?.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAppIconMode(mode: AppThemeMode) {
        themePreferences?.setAppIconMode(mode)
        _uiState.update { it.copy(appIconMode = mode) }
    }

    fun setSyncIconWithTheme(sync: Boolean) {
        themePreferences?.setSyncIconWithTheme(sync)
        _uiState.update { it.copy(syncIconWithTheme = sync) }
    }

    fun toggleHaptic() {
        val newHaptic = !_uiState.value.isHapticEnabled
        themePreferences?.setHapticEnabled(newHaptic)
        _uiState.update { it.copy(isHapticEnabled = newHaptic) }
    }

    fun toggleSettingsDropdown(show: Boolean) {
        _uiState.update { it.copy(showSettingsDropdown = show) }
    }

    fun toggleThemeDialog(show: Boolean) {
        _uiState.update { it.copy(showThemeDialog = show) }
    }

    fun toggleClearHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showClearHistoryDialog = show) }
    }

    fun navigateTo(screen: ActiveScreen) {
        _uiState.update {
            it.copy(
                activeScreen = screen,
                showToolboxMenu = false,
                showSettingsDropdown = false
            )
        }
    }

    fun selectHistoryItem(expression: String) {
        updateExpression(expression)
        navigateTo(ActiveScreen.MAIN_CALCULATOR)
    }

    private fun updateExpression(newExpr: String) {
        val isRad = _uiState.value.isRadians
        val evalResult = evaluator.evaluate(newExpr, EvaluationContext(isRadians = isRad))

        _uiState.update {
            it.copy(
                expression = newExpr,
                resultPreview = if (evalResult.isSuccess && newExpr.isNotEmpty() && evalResult.formattedResult != newExpr) {
                    evalResult.formattedResult
                } else "",
                ast = evalResult.ast,
                errorMessage = null
            )
        }
    }
}
