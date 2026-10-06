package com.google.pixel.calculator.ui.toolbox

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.MathEvaluator
import com.google.pixel.calculator.core.utils.CurrencyRateRepository
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.ui.display.ActiveConverterField
import com.google.pixel.calculator.ui.display.ConvertDisplay
import com.google.pixel.calculator.ui.keypad.BaseKeypad
import com.google.pixel.calculator.ui.theme.CalculatorTheme

/**
 * Schermata Convertitore di Unità per Calcolatrice-Pixel:
 * - Due caselle cliccabili: quella attiva ha testo in colore normale, l'altra è sbiadita.
 * - Cliccando una casella se ne modifica il valore mentre l'altra mostra la conversione automatica.
 * - Tasto swap che scambia le unità mantenendo il valore inserito nella casella attiva.
 * - Nessuna emoji nelle scritte o nei menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterCalculatorView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currencyRepo = remember { CurrencyRateRepository(context) }
    val converter = remember(currencyRepo) { UnitConverter(currencyRepo) }
    val evaluator = remember { MathEvaluator() }

    // Raccoglie gli aggiornamenti tassi valuta
    val currencyRates by currencyRepo.rates.collectAsState()
    val isCurrencyUpdating by currencyRepo.isUpdating.collectAsState()

    // Stato Unità di Conversione (default: Massa kg e g, o Valuta USD ed EUR)
    var selectedCategory by remember { mutableStateOf(UnitConverter.Category.MASS) }
    var topUnitCode by remember { mutableStateOf("g") }
    var bottomUnitCode by remember { mutableStateOf("kg") }

    // Stato Casella Attiva (TOP o BOTTOM)
    var activeField by remember { mutableStateOf(ActiveConverterField.TOP) }
    var expression by remember { mutableStateOf("100") }
    var shouldClearOnNextDigit by remember { mutableStateOf(false) }

    var isProMode by remember { mutableStateOf(false) }
    var isShiftActive by remember { mutableStateOf(false) }
    var isRadians by remember { mutableStateOf(true) }
    var isHapticEnabled by remember { mutableStateOf(true) }

    // Stato Picker Bottom Sheet
    var showPicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf(UnitPickerTarget.FROM) }

    // All'avvio se è valuta, controlla se aggiornare i tassi (> 24h)
    LaunchedEffect(Unit) {
        currencyRepo.updateRatesIfNeeded(coroutineScope, force = false)
    }

    // Calcolo numerico del valore della casella attiva in tempo reale
    val activeNumericValue by remember(expression, isRadians) {
        derivedStateOf {
            if (expression.isBlank()) {
                0.0
            } else {
                val cleanExpr = expression.trimEnd('+', '-', '×', '÷', '^', '(', ',')
                if (cleanExpr.isEmpty()) {
                    0.0
                } else {
                    val evalResult = evaluator.evaluate(cleanExpr, EvaluationContext(isRadians = isRadians))
                    if (evalResult.isSuccess && evalResult.rawValue != null) {
                        evalResult.rawValue.toDouble()
                    } else {
                        cleanExpr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }
    }

    // Risultato convertito per la casella passiva
    val passiveConvertedValueStr by remember(activeNumericValue, topUnitCode, bottomUnitCode, activeField, selectedCategory, currencyRates) {
        derivedStateOf {
            val fromCode = if (activeField == ActiveConverterField.TOP) topUnitCode else bottomUnitCode
            val toCode = if (activeField == ActiveConverterField.TOP) bottomUnitCode else topUnitCode
            val convertedNum = converter.convert(activeNumericValue, fromCode, toCode, selectedCategory)
            converter.formatValue(convertedNum)
        }
    }

    // Stringhe visualizzate per ciascuna casella
    val topDisplayStr = if (activeField == ActiveConverterField.TOP) {
        if (expression.isEmpty()) "0" else expression
    } else {
        passiveConvertedValueStr
    }

    val bottomDisplayStr = if (activeField == ActiveConverterField.BOTTOM) {
        if (expression.isEmpty()) "0" else expression
    } else {
        passiveConvertedValueStr
    }

    val topUnitDef = remember(topUnitCode, selectedCategory) {
        converter.findUnit(topUnitCode, selectedCategory)
            ?: converter.getUnitsForCategory(selectedCategory).first()
    }

    val bottomUnitDef = remember(bottomUnitCode, selectedCategory) {
        converter.findUnit(bottomUnitCode, selectedCategory)
            ?: converter.getUnitsForCategory(selectedCategory).getOrNull(1)
            ?: topUnitDef
    }

    // Funzione Swap: scambia le due unità mantenendo il valore nella casella attiva
    val onPerformSwap: () -> Unit = {
        val oldTop = topUnitCode
        val oldBottom = bottomUnitCode
        topUnitCode = oldBottom
        bottomUnitCode = oldTop
        // Il valore inserito nella casella attiva rimane lo stesso, quindi la conversione
        // viene immediatamente ricalcolata con le nuove unità invertite (es. 100g -> 100kg = x g)
        shouldClearOnNextDigit = false
    }

    // Funzione di pulizia formattazione per inserimento numerico
    fun cleanNumberForInput(str: String): String {
        return str.replace(".", "").replace(",", ".")
    }

    Scaffold(
        containerColor = CalculatorTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = CalculatorTheme.colors.onSurface
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Convertitore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CalculatorTheme.colors.onSurface
                        )
                        if (selectedCategory == UnitConverter.Category.CURRENCY) {
                            Text(
                                text = currencyRepo.getLastUpdateText(),
                                fontSize = 11.sp,
                                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = "${selectedCategory.icon} ${selectedCategory.displayName}",
                                fontSize = 12.sp,
                                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    // Pulsante sincronizzazione ultra-leggera tassi se in modalità valuta
                    if (selectedCategory == UnitConverter.Category.CURRENCY) {
                        val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing)
                            ),
                            label = "SyncAngle"
                        )

                        IconButton(
                            onClick = {
                                currencyRepo.updateRatesIfNeeded(coroutineScope, force = true)
                            },
                            enabled = !isCurrencyUpdating
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Aggiorna tassi di cambio",
                                tint = CalculatorTheme.colors.onSurface,
                                modifier = if (isCurrencyUpdating) Modifier.rotate(angle) else Modifier
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // AREA DISPLAY CON DUE CASELLE CLICCABILI (Attiva normale, Inattiva sbiadita)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ConvertDisplay(
                    topValueStr = topDisplayStr,
                    bottomValueStr = bottomDisplayStr,
                    topUnit = topUnitDef,
                    bottomUnit = bottomUnitDef,
                    activeField = activeField,
                    isHapticEnabled = isHapticEnabled,
                    onFocusField = { clickedField ->
                        if (activeField != clickedField) {
                            // Cambia casella attiva: imposta il valore corrente della casella
                            // e abilita la sovrascrittura alla prima cifra digitata
                            val currentPassiveStr = if (clickedField == ActiveConverterField.TOP) topDisplayStr else bottomDisplayStr
                            activeField = clickedField
                            expression = cleanNumberForInput(currentPassiveStr)
                            shouldClearOnNextDigit = true
                        }
                    },
                    onSwap = onPerformSwap,
                    onSelectTopUnit = {
                        pickerTarget = UnitPickerTarget.FROM
                        showPicker = true
                    },
                    onSelectBottomUnit = {
                        pickerTarget = UnitPickerTarget.TO
                        showPicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // TASTIERINO CALCOLATRICE COMPLETO (BaseKeypad)
            BaseKeypad(
                isProMode = isProMode,
                isShiftActive = isShiftActive,
                isRadians = isRadians,
                isHapticEnabled = isHapticEnabled,
                onDigit = { digit ->
                    val normalized = if (digit == ",") "." else digit

                    if (shouldClearOnNextDigit) {
                        expression = if (normalized == ".") "0." else normalized
                        shouldClearOnNextDigit = false
                        return@BaseKeypad
                    }

                    if (normalized == ".") {
                        val lastTokens = expression.split(Regex("[+\\-×÷/*^() ]"))
                        val currentNum = lastTokens.lastOrNull() ?: ""
                        if ('.' !in currentNum) {
                            val lastChar = expression.lastOrNull()
                            expression = if (expression.isEmpty() || (lastChar != null && lastChar in "+-×÷/*^(")) {
                                "${expression}0."
                            } else {
                                "$expression."
                            }
                        }
                    } else {
                        expression = if (expression == "0") normalized else expression + normalized
                    }
                },
                onOperator = { op ->
                    shouldClearOnNextDigit = false
                    if (expression.isEmpty() && op != "-") return@BaseKeypad
                    val lastChar = expression.lastOrNull()
                    expression = if (lastChar != null && lastChar in "+-×÷/*^") {
                        expression.dropLast(1) + op
                    } else {
                        expression + op
                    }
                },
                onFunction = { func ->
                    if (shouldClearOnNextDigit) {
                        expression = "$func("
                        shouldClearOnNextDigit = false
                    } else {
                        expression = "$expression$func("
                    }
                },
                onConstant = { const ->
                    if (shouldClearOnNextDigit) {
                        expression = const
                        shouldClearOnNextDigit = false
                    } else {
                        expression += const
                    }
                },
                onSmartParenthesis = {
                    shouldClearOnNextDigit = false
                    val openCount = expression.count { it == '(' }
                    val closeCount = expression.count { it == ')' }
                    val lastChar = expression.lastOrNull()
                    val shouldClose = openCount > closeCount && lastChar != null &&
                            (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')
                    expression = if (shouldClose) "$expression)" else "$expression("
                },
                onClear = {
                    expression = ""
                    shouldClearOnNextDigit = false
                },
                onBackspace = {
                    shouldClearOnNextDigit = false
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                },
                onEquals = {
                    // Risolve l'espressione matematica nella casella attiva
                    if (expression.isNotBlank()) {
                        val evalResult = evaluator.evaluate(expression, EvaluationContext(isRadians = isRadians))
                        if (evalResult.isSuccess && evalResult.rawValue != null) {
                            val cleanVal = cleanNumberForInput(evalResult.formattedResult)
                            expression = cleanVal
                            shouldClearOnNextDigit = true
                        }
                    }
                },
                onToggleProMode = { isProMode = !isProMode },
                onToggleShift = { isShiftActive = !isShiftActive },
                onToggleAngleUnit = { isRadians = !isRadians },
                onOpenToolbox = onBack
            )
        }
    }

    // MODAL BOTTOM SHEET SELETTORE UNITA'
    if (showPicker) {
        UnitPickerSheet(
            converter = converter,
            initialCategory = selectedCategory,
            currentUnitCode = if (pickerTarget == UnitPickerTarget.FROM) topUnitCode else bottomUnitCode,
            target = pickerTarget,
            onDismiss = { showPicker = false },
            onSelectUnit = { category, unitCode ->
                if (category != selectedCategory) {
                    // Cambio categoria intelligente: adatta automaticamente anche l'altra unità
                    selectedCategory = category
                    val units = converter.getUnitsForCategory(category)
                    val companion = units.firstOrNull { it.code != unitCode }?.code ?: unitCode
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        topUnitCode = unitCode
                        bottomUnitCode = companion
                    } else {
                        bottomUnitCode = unitCode
                        topUnitCode = companion
                    }
                } else {
                    // Stessa categoria
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        topUnitCode = unitCode
                    } else {
                        bottomUnitCode = unitCode
                    }
                }
            }
        )
    }
}
