package com.google.pixel.calculator.ui.toolbox

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.MathEvaluator
import com.google.pixel.calculator.core.utils.CurrencyRateRepository
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.data.ConverterPreferences
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.display.ActiveConverterField
import com.google.pixel.calculator.ui.display.ConvertDisplay
import com.google.pixel.calculator.ui.keypad.BaseKeypad
import com.google.pixel.calculator.ui.theme.CalculatorTheme

/**
 * Unit Converter Screen for Pixel Calculator:
 * - Two clickable boxes: active box has high-contrast regular text, passive box is faded.
 * - Clicking a box focuses it for input while the other box computes real-time conversion.
 * - Swap button exchanges units while preserving the entered numerical value in the active box.
 * - Clean typographic design without emojis in titles or menus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterCalculatorView(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isToolBarExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (isToolBarExpanded) {
            isToolBarExpanded = false
        } else {
            onBack()
        }
    }

    val currencyRepo = remember { CurrencyRateRepository(context) }
    val converter = remember(currencyRepo) { UnitConverter(currencyRepo) }
    val converterPrefs = remember { ConverterPreferences(context) }
    val evaluator = remember { MathEvaluator() }

    // Collect currency rate updates
    val currencyRates by currencyRepo.rates.collectAsState()
    val isCurrencyUpdating by currencyRepo.isUpdating.collectAsState()

    // Load persisted unit selection or fallback to defaults
    val initialSelection = remember { converterPrefs.loadSelection(converter) }

    // Unit conversion state (persisted across app launches)
    var selectedCategory by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initialSelection.category) }
    var topUnitCode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initialSelection.topUnitCode) }
    var bottomUnitCode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initialSelection.bottomUnitCode) }

    // Active field state (TOP or BOTTOM)
    var activeField by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(ActiveConverterField.TOP) }
    var expression by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("0") }
    var shouldClearOnNextDigit by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    var isProMode by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var isShiftActive by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var isRadians by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var isHapticEnabled by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var isAutoSyncEnabled by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(converterPrefs.isAutoSyncEnabled()) }
    var showMenu by remember { mutableStateOf(false) }

    // Unit picker bottom sheet state
    var showPicker by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf(UnitPickerTarget.FROM) }

    // Check on launch if rates need refreshing (> 24h) and auto-sync is enabled
    LaunchedEffect(isAutoSyncEnabled) {
        if (isAutoSyncEnabled) {
            currencyRepo.updateRatesIfNeeded(coroutineScope, force = false)
        }
    }

    // Real-time evaluation of active expression
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

    // Converted value for the passive box
    val passiveConvertedValueStr by remember(activeNumericValue, topUnitCode, bottomUnitCode, activeField, selectedCategory, currencyRates) {
        derivedStateOf {
            val fromCode = if (activeField == ActiveConverterField.TOP) topUnitCode else bottomUnitCode
            val toCode = if (activeField == ActiveConverterField.TOP) bottomUnitCode else topUnitCode
            val convertedNum = converter.convert(activeNumericValue, fromCode, toCode, selectedCategory)
            converter.formatValue(convertedNum)
        }
    }

    // Strings displayed for each box
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

    // Swap action: swaps units while keeping current input value in active box
    val onPerformSwap: () -> Unit = {
        val oldTop = topUnitCode
        val oldBottom = bottomUnitCode
        topUnitCode = oldBottom
        bottomUnitCode = oldTop
        shouldClearOnNextDigit = false
        converterPrefs.saveSelection(selectedCategory, oldBottom, oldTop)
    }

    // Cleans formatted string for numeric input
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
                            contentDescription = stringResource(R.string.back),
                            tint = CalculatorTheme.colors.onSurface
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.tool_converter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CalculatorTheme.colors.onSurface
                    )
                },
                actions = {
                    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing)
                        ),
                        label = "SyncAngle"
                    )

                    // Refresh button for currency exchange rates when in currency category
                    if (selectedCategory == UnitConverter.Category.CURRENCY) {
                        IconButton(
                            onClick = {
                                currencyRepo.updateRatesIfNeeded(coroutineScope, force = true)
                            },
                            enabled = !isCurrencyUpdating
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.update_exchange_rates),
                                tint = CalculatorTheme.colors.onSurface,
                                modifier = if (isCurrencyUpdating) Modifier.rotate(angle) else Modifier
                            )
                        }
                    }

                    // 3-dots options menu for converter synchronization
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options),
                                tint = CalculatorTheme.colors.onSurface
                            )
                        }

                        MaterialTheme(
                            shapes = MaterialTheme.shapes.copy(
                                extraSmall = RoundedCornerShape(20.dp)
                            )
                        ) {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.converter_auto_sync),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        isAutoSyncEnabled = !isAutoSyncEnabled
                                        converterPrefs.setAutoSyncEnabled(isAutoSyncEnabled)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isAutoSyncEnabled) Icons.Default.Sync else Icons.Default.SyncDisabled,
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = {
                                        Switch(
                                            checked = isAutoSyncEnabled,
                                            onCheckedChange = {
                                                isAutoSyncEnabled = it
                                                converterPrefs.setAutoSyncEnabled(it)
                                            }
                                        )
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.converter_sync_now),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        currencyRepo.updateRatesIfNeeded(coroutineScope, force = true)
                                        showMenu = false
                                    },
                                    enabled = !isCurrencyUpdating,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = if (isCurrencyUpdating) Modifier.rotate(angle) else Modifier
                                        )
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currencyRepo.getLastUpdateText(context),
                                        fontSize = 11.sp,
                                        fontFamily = QuicksandFontFamily,
                                        color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Two clickable display boxes (active is high contrast, passive is faded)
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
                        isProMode = isProMode,
                        isHapticEnabled = isHapticEnabled,
                        onFocusField = { clickedField ->
                            if (activeField != clickedField) {
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
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Full calculator keypad (BaseKeypad)
                BaseKeypad(
                    isProMode = isProMode,
                    isShiftActive = isShiftActive,
                    isRadians = isRadians,
                    isHapticEnabled = isHapticEnabled,
                    currentScreen = ActiveScreen.TOOLBOX_CONVERTER,
                    isToolBarExpanded = isToolBarExpanded,
                    onToggleToolBar = { isToolBarExpanded = !isToolBarExpanded },
                    onSelectTool = { screen ->
                        isToolBarExpanded = false
                        onNavigate(screen)
                    },
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
                    onToggleAngleUnit = { isRadians = !isRadians }
                )
            }

            // Outside dismiss when toolbar is open
            if (isToolBarExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 76.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isToolBarExpanded = false
                        }
                )
            }
        }
    }

    // Modal Bottom Sheet unit picker
    if (showPicker) {
        UnitPickerSheet(
            converter = converter,
            initialCategory = selectedCategory,
            currentUnitCode = if (pickerTarget == UnitPickerTarget.FROM) topUnitCode else bottomUnitCode,
            target = pickerTarget,
            onDismiss = { showPicker = false },
            onSelectUnit = { category, unitCode ->
                val newTop: String
                val newBottom: String
                if (category != selectedCategory) {
                    selectedCategory = category
                    val units = converter.getUnitsForCategory(category)
                    val companion = units.firstOrNull { it.code != unitCode }?.code ?: unitCode
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        newTop = unitCode
                        newBottom = companion
                    } else {
                        newTop = companion
                        newBottom = unitCode
                    }
                } else {
                    if (pickerTarget == UnitPickerTarget.FROM) {
                        newTop = unitCode
                        newBottom = bottomUnitCode
                    } else {
                        newTop = topUnitCode
                        newBottom = unitCode
                    }
                }
                topUnitCode = newTop
                bottomUnitCode = newBottom
                converterPrefs.saveSelection(selectedCategory, newTop, newBottom)
            }
        )
    }
}
