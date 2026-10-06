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
import com.google.pixel.calculator.ui.display.ConvertDisplay
import com.google.pixel.calculator.ui.keypad.BaseKeypad
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import kotlinx.coroutines.launch

/**
 * Schermata principale Convertitore di Unità in perfetto stile iPhone (iOS 18) per Calcolatrice-Pixel:
 * - Sostituisce la vecchia ConverterView.
 * - Utilizza il display a due righe ConvertDisplay con tasto Swap ⇅ e chip unità.
 * - Mantiene il tastierino nativo BaseKeypad per consentire calcoli aritmetici in tempo reale.
 * - Supporta 17 categorie offline con sincronizzazione tassi di cambio ultra-leggera (~2KB).
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
    val lastUpdateTs by currencyRepo.lastUpdateTimestamp.collectAsState()

    // Stato Unità di Conversione
    var selectedCategory by remember { mutableStateOf(UnitConverter.Category.CURRENCY) }
    var fromUnitCode by remember { mutableStateOf("USD") }
    var toUnitCode by remember { mutableStateOf("EUR") }

    // Stato Calcolatrice / Espressione
    var expression by remember { mutableStateOf("1000") }
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

    // Calcolo numerico del valore in tempo reale
    val inputNumericValue by remember(expression, isRadians) {
        derivedStateOf {
            if (expression.isBlank()) {
                0.0
            } else {
                // Rimuovi eventuali operatori finali in corso di digitazione per calcolare il valore parziale
                val cleanExpr = expression.trimEnd('+', '-', '×', '÷', '^', '(', ',')
                if (cleanExpr.isEmpty()) {
                    0.0
                } else {
                    val evalResult = evaluator.evaluate(cleanExpr, EvaluationContext(isRadians = isRadians))
                    if (evalResult.isSuccess && evalResult.rawValue != null) {
                        evalResult.rawValue.toDouble()
                    } else {
                        // Fallback parsing diretto
                        cleanExpr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }
    }

    // Risultato convertito ricalcolato istantaneamente
    val convertedValueStr by remember(inputNumericValue, fromUnitCode, toUnitCode, selectedCategory, currencyRates) {
        derivedStateOf {
            val convertedNum = converter.convert(inputNumericValue, fromUnitCode, toUnitCode, selectedCategory)
            converter.formatValue(convertedNum)
        }
    }

    val fromUnitDef = remember(fromUnitCode, selectedCategory) {
        converter.findUnit(fromUnitCode, selectedCategory)
            ?: converter.getUnitsForCategory(selectedCategory).first()
    }

    val toUnitDef = remember(toUnitCode, selectedCategory) {
        converter.findUnit(toUnitCode, selectedCategory)
            ?: converter.getUnitsForCategory(selectedCategory).getOrNull(1)
            ?: fromUnitDef
    }

    // Funzione di Swap intelligente: scambia le unità e trasforma il valore convertito nel nuovo input da convertire
    val onPerformSwap: () -> Unit = {
        val oldFrom = fromUnitCode
        val oldTo = toUnitCode

        fromUnitCode = oldTo
        toUnitCode = oldFrom

        // Il risultato convertito diventa il nuovo valore da convertire
        if (convertedValueStr.isNotEmpty() && convertedValueStr != "0") {
            expression = convertedValueStr.replace(".", "").replace(",", ".")
        }
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
            // AREA DISPLAY STILE IPHONE (A due righe con Swap ⇅ e Chip Unità)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ConvertDisplay(
                    expression = expression,
                    convertedValueStr = convertedValueStr,
                    fromUnit = fromUnitDef,
                    toUnit = toUnitDef,
                    isHapticEnabled = isHapticEnabled,
                    onSwap = onPerformSwap,
                    onSelectFromUnit = {
                        pickerTarget = UnitPickerTarget.FROM
                        showPicker = true
                    },
                    onSelectToUnit = {
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
                        // Se l'espressione era "0", sostituisci
                        expression = if (expression == "0") normalized else expression + normalized
                    }
                },
                onOperator = { op ->
                    if (expression.isEmpty() && op != "-") return@BaseKeypad
                    val lastChar = expression.lastOrNull()
                    expression = if (lastChar != null && lastChar in "+-×÷/*^") {
                        expression.dropLast(1) + op
                    } else {
                        expression + op
                    }
                },
                onFunction = { func ->
                    expression = "$expression$func("
                },
                onConstant = { const ->
                    expression += const
                },
                onSmartParenthesis = {
                    val openCount = expression.count { it == '(' }
                    val closeCount = expression.count { it == ')' }
                    val lastChar = expression.lastOrNull()
                    val shouldClose = openCount > closeCount && lastChar != null &&
                            (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')
                    expression = if (shouldClose) "$expression)" else "$expression("
                },
                onClear = {
                    expression = ""
                },
                onBackspace = {
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                },
                onEquals = {
                    // Risolve l'espressione matematica e imposta il valore calcolato nella riga di input
                    if (expression.isNotBlank()) {
                        val evalResult = evaluator.evaluate(expression, EvaluationContext(isRadians = isRadians))
                        if (evalResult.isSuccess && evalResult.rawValue != null) {
                            val cleanVal = evalResult.formattedResult.replace(".", "").replace(",", ".")
                            expression = cleanVal
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
            currentUnitCode = if (pickerTarget == UnitPickerTarget.FROM) fromUnitCode else toUnitCode,
            target = pickerTarget,
            onDismiss = { showPicker = false },
            onSelectUnit = { category, unitCode ->
                if (category != selectedCategory) {
                    // Cambio categoria intelligente: adatta automaticamente anche l'altra unità
                    selectedCategory = category
                    val units = converter.getUnitsForCategory(category)
                    val companion = units.firstOrNull { it.code != unitCode }?.code ?: unitCode
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        fromUnitCode = unitCode
                        toUnitCode = companion
                    } else {
                        toUnitCode = unitCode
                        fromUnitCode = companion
                    }
                } else {
                    // Stessa categoria
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        fromUnitCode = unitCode
                    } else {
                        toUnitCode = unitCode
                    }
                }
            }
        )
    }
}
