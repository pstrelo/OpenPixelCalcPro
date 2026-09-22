package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdvancedCalculatorViewModel(
    private val engine: AdvancedMathEngine = AdvancedMathEngine()
) : ViewModel() {

    private val availableColors = listOf(
        Color(0xFF4E75F6), // Blu brillante (Pixel style)
        Color(0xFF10B981), // Verde smeraldo
        Color(0xFFF59E0B), // Ambra
        Color(0xFFEC4899)  // Magenta
    )

    private val _uiState = MutableStateFlow(AdvancedCalculatorState())
    val uiState: StateFlow<AdvancedCalculatorState> = _uiState.asStateFlow()

    init {
        // Calcola e campiona la formula iniziale
        updateGraphForActiveFormula()
    }

    // ==========================================
    // GESTIONE FORMULE MULTIPLE
    // ==========================================

    fun selectFormula(index: Int) {
        if (index in _uiState.value.formulas.indices) {
            _uiState.update { it.copy(activeFormulaIndex = index) }
        }
    }

    fun addFormula() {
        val currentFormulas = _uiState.value.formulas
        if (currentFormulas.size >= 4) return // Massimo 4 funzioni contemporanee
        val nextId = (currentFormulas.maxOfOrNull { it.id } ?: 0) + 1
        val color = availableColors[currentFormulas.size % availableColors.size]
        val newEntry = FormulaEntry(id = nextId, expression = "", color = color)
        _uiState.update {
            it.copy(
                formulas = currentFormulas + newEntry,
                activeFormulaIndex = currentFormulas.size
            )
        }
    }

    fun removeFormula(index: Int) {
        val currentFormulas = _uiState.value.formulas
        if (currentFormulas.size <= 1) {
            updateCurrentExpression("")
            return
        }
        val updated = currentFormulas.toMutableList()
        updated.removeAt(index)
        val newActiveIndex = if (index >= updated.size) updated.size - 1 else index
        _uiState.update {
            it.copy(
                formulas = updated,
                activeFormulaIndex = newActiveIndex
            )
        }
        updateAllGraphPoints()
    }

    // ==========================================
    // DIGITAZIONE ED INPUT TASTIERA
    // ==========================================

    fun onKeyClick(key: String) {
        val curIndex = _uiState.value.activeFormulaIndex
        val curExpr = _uiState.value.formulas.getOrNull(curIndex)?.expression ?: ""

        when (key) {
            // Memoria
            "MC" -> _uiState.update { it.copy(memoryValue = 0.0) }
            "MR" -> insertText(_uiState.value.memoryValue.toString())
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

            // Cancellazione
            "⌫", "DEL" -> onBackspace()
            "CE" -> updateCurrentExpression("")
            "AC", "C" -> onClearAll()

            // Segno e radici
            "+/-" -> {
                if (curExpr.startsWith("-")) {
                    updateCurrentExpression(curExpr.removePrefix("-"))
                } else {
                    updateCurrentExpression("-$curExpr")
                }
            }
            "xⁿ", "x□" -> insertText("^")
            "x²" -> insertText("^2")
            "ⁿ√", "ⁿ√□" -> insertText("ʸ√")
            "√", "√□" -> insertText("sqrt(")
            "□/□" -> insertText("/")

            // TASTO '=': Inserisce l'uguale nella formula (NON calcola!)
            "=" -> insertText(" = ")

            // TASTO 'EXE': Esegue il calcolo/risoluzione
            "EXE" -> onExecute()

            // TASTO 'FORMAT': Cambia formato di visualizzazione
            "FORMAT" -> onCycleFormat()

            // Catalog
            "CATALOG" -> openCatalog()

            // Variabili rapide
            "x" -> insertText("x")
            "y" -> insertText("y")

            else -> insertText(key)
        }
    }

    fun insertText(text: String) {
        val curIndex = _uiState.value.activeFormulaIndex
        val curExpr = _uiState.value.formulas.getOrNull(curIndex)?.expression ?: ""
        val newExpr = curExpr + text
        updateCurrentExpression(newExpr)
    }

    fun onBackspace() {
        val curIndex = _uiState.value.activeFormulaIndex
        val curExpr = _uiState.value.formulas.getOrNull(curIndex)?.expression ?: ""
        if (curExpr.isNotEmpty()) {
            val newExpr = curExpr.dropLast(1)
            updateCurrentExpression(newExpr)
        }
    }

    fun onClearAll() {
        _uiState.update {
            it.copy(
                formulas = listOf(FormulaEntry(id = 1, expression = "", color = availableColors[0])),
                activeFormulaIndex = 0,
                selectedPoint = null,
                shadedIntervals = emptyList()
            )
        }
    }

    fun updateCurrentExpression(newExpr: String) {
        val curIndex = _uiState.value.activeFormulaIndex
        val currentFormulas = _uiState.value.formulas.toMutableList()
        if (curIndex in currentFormulas.indices) {
            currentFormulas[curIndex] = currentFormulas[curIndex].copy(
                expression = newExpr,
                resultText = "",
                lastSolveResult = null
            )
            _uiState.update { it.copy(formulas = currentFormulas) }
            updateGraphForActiveFormula()
        }
    }

    // ==========================================
    // RISOLUZIONE ESEGUITA DA 'EXE'
    // ==========================================

    fun onExecute() {
        val curIndex = _uiState.value.activeFormulaIndex
        val formula = _uiState.value.formulas.getOrNull(curIndex) ?: return
        if (formula.expression.isBlank()) return

        viewModelScope.launch {
            val isRad = _uiState.value.isRadians
            val result = engine.solve(formula.expression, isRad)

            val updatedFormulas = _uiState.value.formulas.toMutableList()
            val format = formula.activeFormat

            when (result) {
                is AdvancedMathEngine.SolveResult.SingleValue -> {
                    val formatted = result.formatAs(format, isRad, engine)
                    updatedFormulas[curIndex] = formula.copy(
                        resultText = "= $formatted",
                        lastSolveResult = result,
                        roots = listOf(result.raw),
                        isError = false
                    )
                    _uiState.update { it.copy(formulas = updatedFormulas, shadedIntervals = emptyList()) }
                }
                is AdvancedMathEngine.SolveResult.EquationRoots -> {
                    val formatted = result.formatAs(format, isRad, engine)
                    updatedFormulas[curIndex] = formula.copy(
                        resultText = formatted,
                        lastSolveResult = result,
                        roots = result.roots,
                        isError = false
                    )
                    _uiState.update { it.copy(formulas = updatedFormulas, shadedIntervals = emptyList()) }
                }
                is AdvancedMathEngine.SolveResult.InequalityIntervals -> {
                    updatedFormulas[curIndex] = formula.copy(
                        resultText = result.text,
                        lastSolveResult = result,
                        isError = false
                    )
                    _uiState.update {
                        it.copy(
                            formulas = updatedFormulas,
                            shadedIntervals = result.intervals
                        )
                    }
                }
                is AdvancedMathEngine.SolveResult.SystemSolution -> {
                    updatedFormulas[curIndex] = formula.copy(
                        resultText = result.formattedText,
                        lastSolveResult = result,
                        isError = false
                    )
                    _uiState.update { it.copy(formulas = updatedFormulas) }
                }
                is AdvancedMathEngine.SolveResult.Error -> {
                    updatedFormulas[curIndex] = formula.copy(
                        resultText = result.message,
                        lastSolveResult = result,
                        isError = true
                    )
                    _uiState.update { it.copy(formulas = updatedFormulas) }
                }
            }
        }
    }

    // ==========================================
    // TASTO 'FORMAT': CAMBIA LA RAPPRESENTAZIONE DEL RISULTATO
    // ==========================================

    fun onCycleFormat() {
        val curIndex = _uiState.value.activeFormulaIndex
        val formula = _uiState.value.formulas.getOrNull(curIndex) ?: return

        val formats = AdvancedMathEngine.ResultFormat.values()
        val nextIndex = (formula.activeFormat.ordinal + 1) % formats.size
        val newFormat = formats[nextIndex]

        val updatedFormulas = _uiState.value.formulas.toMutableList()
        val isRad = _uiState.value.isRadians

        val newResultText = formula.lastSolveResult?.let {
            if (it is AdvancedMathEngine.SolveResult.SingleValue) {
                "= ${it.formatAs(newFormat, isRad, engine)}"
            } else {
                it.formatAs(newFormat, isRad, engine)
            }
        } ?: formula.resultText

        updatedFormulas[curIndex] = formula.copy(
            activeFormat = newFormat,
            resultText = newResultText
        )

        _uiState.update { it.copy(formulas = updatedFormulas) }
    }

    // ==========================================
    // CATALOG / LIBRERIA
    // ==========================================

    fun openCatalog() {
        _uiState.update { it.copy(isCatalogOpen = true) }
    }

    fun closeCatalog() {
        _uiState.update { it.copy(isCatalogOpen = false) }
    }

    fun onSelectCatalogItem(insertion: String) {
        insertText(insertion)
        closeCatalog()
    }

    // ==========================================
    // CONTROLLI VISTA E GRAFICO
    // ==========================================

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

    fun toggleRadians() {
        _uiState.update { it.copy(isRadians = !it.isRadians) }
        updateAllGraphPoints()
        // Aggiorna anche eventuale risultato polare
        val curIndex = _uiState.value.activeFormulaIndex
        val formula = _uiState.value.formulas.getOrNull(curIndex)
        if (formula?.lastSolveResult != null) {
            onExecute()
        }
    }

    fun onGraphTransform(zoomChange: Float, panChange: Offset) {
        _uiState.update { current ->
            val newScale = (current.graphScale * zoomChange).coerceIn(10f, 300f)
            val newOffsetX = current.graphOffsetX + panChange.x
            val newOffsetY = current.graphOffsetY + panChange.y
            current.copy(
                graphScale = newScale,
                graphOffsetX = newOffsetX,
                graphOffsetY = newOffsetY
            )
        }
        updateAllGraphPoints()
    }

    fun onResetGraph() {
        _uiState.update {
            it.copy(
                graphScale = 40f,
                graphOffsetX = 0f,
                graphOffsetY = 0f,
                selectedPoint = null
            )
        }
        updateAllGraphPoints()
    }

    fun selectGraphPoint(point: Pair<Float, Float>?) {
        _uiState.update { it.copy(selectedPoint = point) }
    }

    private fun updateGraphForActiveFormula() {
        val curIndex = _uiState.value.activeFormulaIndex
        val formula = _uiState.value.formulas.getOrNull(curIndex) ?: return
        if (formula.expression.isBlank()) return

        viewModelScope.launch {
            val state = _uiState.value
            val halfSpan = (300f / state.graphScale) + 10f
            val centerX = -state.graphOffsetX / state.graphScale
            val xMin = centerX - halfSpan
            val xMax = centerX + halfSpan

            // Se è un'equazione "lhs = rhs", per il grafico disegniamo la funzione differenza "lhs - rhs"
            val plotExpr = if (formula.expression.contains("=")) {
                val parts = formula.expression.split("=")
                if (parts.size == 2) "(${parts[0]}) - (${parts[1]})" else formula.expression
            } else {
                formula.expression
            }

            val points = engine.sampleFunction(plotExpr, xMin, xMax, 350, state.isRadians)
            val updatedFormulas = _uiState.value.formulas.toMutableList()
            if (curIndex in updatedFormulas.indices) {
                updatedFormulas[curIndex] = updatedFormulas[curIndex].copy(sampledPoints = points)
                _uiState.update { it.copy(formulas = updatedFormulas) }
            }
        }
    }

    private fun updateAllGraphPoints() {
        viewModelScope.launch {
            val state = _uiState.value
            val halfSpan = (300f / state.graphScale) + 10f
            val centerX = -state.graphOffsetX / state.graphScale
            val xMin = centerX - halfSpan
            val xMax = centerX + halfSpan

            val updatedFormulas = state.formulas.map { entry ->
                if (entry.expression.isNotBlank()) {
                    val plotExpr = if (entry.expression.contains("=")) {
                        val parts = entry.expression.split("=")
                        if (parts.size == 2) "(${parts[0]}) - (${parts[1]})" else entry.expression
                    } else {
                        entry.expression
                    }
                    val pts = engine.sampleFunction(plotExpr, xMin, xMax, 350, state.isRadians)
                    entry.copy(sampledPoints = pts)
                } else {
                    entry.copy(sampledPoints = emptyList())
                }
            }
            _uiState.update { it.copy(formulas = updatedFormulas) }
        }
    }
}
