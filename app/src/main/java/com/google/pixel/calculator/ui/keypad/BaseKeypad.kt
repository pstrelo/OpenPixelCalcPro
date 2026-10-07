package com.google.pixel.calculator.ui.keypad

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.icons.SquareFoot
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily
import com.google.pixel.calculator.ui.toolbox.PixelKeypadToolRow

enum class PixelButtonType {
    NUMERIC,
    OPERATOR,
    CONTROL,
    EQUALS,
    SCIENTIFIC,
    SHIFT_ACTIVE
}

@Composable
fun BaseKeypad(
    isProMode: Boolean,
    isShiftActive: Boolean,
    isRadians: Boolean,
    isHapticEnabled: Boolean = true,
    currentScreen: ActiveScreen = ActiveScreen.MAIN_CALCULATOR,
    isToolBarExpanded: Boolean = false,
    onToggleToolBar: () -> Unit = {},
    onSelectTool: (ActiveScreen) -> Unit = {},
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onSmartParenthesis: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
    onToggleProMode: () -> Unit,
    onToggleShift: () -> Unit,
    onToggleAngleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val triggerHaptic: () -> Unit = {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val btnHeight = if (isLandscape) 40.dp else if (isProMode) 50.dp else 68.dp
    val spacing = if (isLandscape) 6.dp else if (isProMode) 8.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLandscape) 10.dp else 16.dp, vertical = if (isLandscape) 2.dp else 8.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // LEVA / FRECCIA DI ESPANSIONE SCIENTIFICA STILE GOOGLE PIXEL (posta sopra la prima fila a sinistra)
        // In orizzontale ruota di 90 gradi per espansione laterale
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = if (isLandscape) 0.dp else 2.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    triggerHaptic()
                    onToggleProMode()
                },
                modifier = Modifier.size(if (isLandscape) 28.dp else 36.dp)
            ) {
                Icon(
                    imageVector = if (isProMode) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isProMode) "Riduci calcolatrice" else "Espandi funzioni scientifiche",
                    tint = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(if (isLandscape) 20.dp else 24.dp)
                        .rotate(if (isLandscape) -90f else 0f)
                )
            }
        }

        if (!isLandscape) {
            // SEZIONE SCIENTIFICA (3 file visibili in modalità Pro / Estesa)
            AnimatedVisibility(
                visible = isProMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // FILA SCIENTIFICA 1: [⇧ Shift] [Rad/Deg] [^] [( )]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    PixelIconButton(
                        type = if (isShiftActive) PixelButtonType.SHIFT_ACTIVE else PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onToggleShift()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Shift",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    PixelButton(
                        text = if (isRadians) "Rad" else "Deg",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onToggleAngleUnit()
                        }
                    )

                    PixelButton(
                        text = "^",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onOperator("^")
                        }
                    )

                    PixelButton(
                        text = "( )",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onSmartParenthesis()
                        }
                    )
                }

                // FILA SCIENTIFICA 2: [e / ln] [log / 10ˣ] [√ / ʸ√x] [!]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    PixelButton(
                        text = if (isShiftActive) "ln" else "e",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onFunction("ln") else onConstant("e")
                        }
                    )

                    PixelButton(
                        text = if (isShiftActive) "10ˣ" else "log",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onOperator("10^") else onFunction("log")
                        }
                    )

                    PixelButton(
                        text = if (isShiftActive) "ʸ√x" else "√",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onFunction("root") else onFunction("sqrt")
                        }
                    )

                    PixelButton(
                        text = "!",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onOperator("!")
                        }
                    )
                }

                // FILA SCIENTIFICA 3: [sin / sin⁻¹] [cos / cos⁻¹] [tan / tan⁻¹] [π]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    PixelButton(
                        text = if (isShiftActive) "sin⁻¹" else "sin",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onFunction("sin⁻¹") else onFunction("sin")
                        }
                    )

                    PixelButton(
                        text = if (isShiftActive) "cos⁻¹" else "cos",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onFunction("cos⁻¹") else onFunction("cos")
                        }
                    )

                    PixelButton(
                        text = if (isShiftActive) "tan⁻¹" else "tan",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            if (isShiftActive) onFunction("tan⁻¹") else onFunction("tan")
                        }
                    )

                    PixelButton(
                        text = "π",
                        type = PixelButtonType.SCIENTIFIC,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onConstant("π")
                        }
                    )
                }
            }
        }

        // FILA 1 BASE: [AC] [%] [÷] [⌫]  (disposizione originale dell'utente)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(
                text = "AC",
                type = PixelButtonType.CONTROL,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onClear()
                }
            )
            PixelButton(
                text = "%",
                type = PixelButtonType.OPERATOR,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("%")
                }
            )
            PixelButton(
                text = "÷",
                type = PixelButtonType.OPERATOR,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onOperator("÷")
                }
            )
            PixelIconButton(
                type = PixelButtonType.CONTROL,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                onClick = {
                    triggerHaptic()
                    onBackspace()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Cancella cifra",
                    modifier = Modifier.size(if (isProMode) 20.dp else 24.dp)
                )
            }
        }

        // FILA 2 BASE: [7] [8] [9] [×]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "7", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("7") })
            PixelButton(text = "8", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("8") })
            PixelButton(text = "9", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("9") })
            PixelButton(text = "×", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("×") })
        }

        // FILA 3 BASE: [4] [5] [6] [+] (disposizione originale dell'utente con +)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "4", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("4") })
            PixelButton(text = "5", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("5") })
            PixelButton(text = "6", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("6") })
            PixelButton(text = "+", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("+") })
        }

        // FILA 4 BASE: [1] [2] [3] [-] (disposizione originale dell'utente con -)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "1", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("1") })
            PixelButton(text = "2", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("2") })
            PixelButton(text = "3", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("3") })
            PixelButton(text = "-", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("-") })
        }

        // FILA 5 BASE: [ Selettore Strumenti ] [0] [.] [=]
        PixelKeypadToolRow(
            currentScreen = currentScreen,
            isExpanded = isToolBarExpanded,
            onToggleExpand = onToggleToolBar,
            onNavigate = onSelectTool,
            btnHeight = btnHeight,
            spacing = spacing,
            isLandscape = false,
            onDigit = onDigit,
            onEquals = onEquals
        )
        } else {
            // ==================== MODALITÀ ORIZZONTALE (LANDSCAPE) ====================
            // Struttura orizzontale: [Simboli extra (se attivati)] [  numeri  ] [operatori matematici]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 1. [Simboli extra (se attivati)] (3 Colonne x 4 Righe: 12 tasti scientifici)
                AnimatedVisibility(
                    visible = isProMode,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut(),
                    modifier = Modifier.weight(3f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        // Riga 1 extra: [⇧ Shift] [Rad/Deg] [( )]
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            PixelIconButton(
                                type = if (isShiftActive) PixelButtonType.SHIFT_ACTIVE else PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onToggleShift() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Shift",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            PixelButton(
                                text = if (isRadians) "Rad" else "Deg",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onToggleAngleUnit() }
                            )
                            PixelButton(
                                text = "( )",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onSmartParenthesis() }
                            )
                        }

                        // Riga 2 extra: [sin / sin⁻¹] [cos / cos⁻¹] [tan / tan⁻¹]
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            PixelButton(
                                text = if (isShiftActive) "sin⁻¹" else "sin",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onFunction("sin⁻¹") else onFunction("sin") }
                            )
                            PixelButton(
                                text = if (isShiftActive) "cos⁻¹" else "cos",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onFunction("cos⁻¹") else onFunction("cos") }
                            )
                            PixelButton(
                                text = if (isShiftActive) "tan⁻¹" else "tan",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onFunction("tan⁻¹") else onFunction("tan") }
                            )
                        }

                        // Riga 3 extra: [ln / e] [log / 10ˣ] [^]
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            PixelButton(
                                text = if (isShiftActive) "ln" else "e",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onFunction("ln") else onConstant("e") }
                            )
                            PixelButton(
                                text = if (isShiftActive) "10ˣ" else "log",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onOperator("10^") else onFunction("log") }
                            )
                            PixelButton(
                                text = "^",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onOperator("^") }
                            )
                        }

                        // Riga 4 extra: [√ / ʸ√x] [!] [π]
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            PixelButton(
                                text = if (isShiftActive) "ʸ√x" else "√",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); if (isShiftActive) onFunction("root") else onFunction("sqrt") }
                            )
                            PixelButton(
                                text = "!",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onOperator("!") }
                            )
                            PixelButton(
                                text = "π",
                                type = PixelButtonType.SCIENTIFIC,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = { triggerHaptic(); onConstant("π") }
                            )
                        }
                    }
                }

                // 2. [  numeri  ] (3 Colonne x 4 Righe: 7 8 9 / 4 5 6 / 1 2 3 / 🖩 0 .)
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "7", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("7") })
                        PixelButton(text = "8", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("8") })
                        PixelButton(text = "9", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("9") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "4", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("4") })
                        PixelButton(text = "5", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("5") })
                        PixelButton(text = "6", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("6") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "1", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("1") })
                        PixelButton(text = "2", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("2") })
                        PixelButton(text = "3", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("3") })
                    }
                    PixelKeypadToolRow(
                        currentScreen = currentScreen,
                        isExpanded = isToolBarExpanded,
                        onToggleExpand = onToggleToolBar,
                        onNavigate = onSelectTool,
                        btnHeight = btnHeight,
                        spacing = spacing,
                        isLandscape = true,
                        onDigit = onDigit
                    )
                }

                // 3. [operatori matematici] (2 Colonne x 4 Righe: AC ⌫ / % ÷ / × - / + =)
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "AC", type = PixelButtonType.CONTROL, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onClear() })
                        PixelIconButton(
                            type = PixelButtonType.CONTROL,
                            height = btnHeight,
                            modifier = Modifier.weight(1f),
                            onClick = { triggerHaptic(); onBackspace() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Cancella cifra",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "%", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("%") })
                        PixelButton(text = "÷", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("÷") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "×", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("×") })
                        PixelButton(text = "-", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("-") })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                        PixelButton(text = "+", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("+") })
                        PixelButton(text = "=", type = PixelButtonType.EQUALS, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onEquals() })
                    }
                }
            }
        }
    }
}

@Composable
fun PixelButton(
    text: String,
    modifier: Modifier = Modifier,
    type: PixelButtonType = PixelButtonType.NUMERIC,
    height: Dp = 68.dp,
    onClick: () -> Unit
) {
    val colors = CalculatorTheme.colors

    val containerColor = when (type) {
        PixelButtonType.NUMERIC -> colors.numericContainer
        PixelButtonType.OPERATOR -> colors.operatorContainer
        PixelButtonType.CONTROL -> colors.controlContainer
        PixelButtonType.EQUALS -> colors.equalsContainer
        PixelButtonType.SCIENTIFIC -> colors.scientificContainer
        PixelButtonType.SHIFT_ACTIVE -> colors.equalsContainer
    }

    val contentColor = when (type) {
        PixelButtonType.NUMERIC -> colors.numericContent
        PixelButtonType.OPERATOR -> colors.operatorContent
        PixelButtonType.CONTROL -> colors.controlContent
        PixelButtonType.EQUALS -> colors.equalsContent
        PixelButtonType.SCIENTIFIC -> colors.scientificContent
        PixelButtonType.SHIFT_ACTIVE -> colors.equalsContent
    }

    val view = LocalView.current

    Button(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            onClick()
        },
        modifier = modifier.height(height),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        val fontSize = when {
            type == PixelButtonType.SCIENTIFIC -> if (text.length > 3) 13.sp else if (height <= 44.dp) 14.sp else 16.sp
            type == PixelButtonType.NUMERIC -> if (height <= 44.dp) 20.sp else if (height < 55.dp) 24.sp else 28.sp
            type == PixelButtonType.OPERATOR || type == PixelButtonType.EQUALS -> if (height <= 44.dp) 22.sp else if (height < 55.dp) 26.sp else 30.sp
            else -> if (text.length > 2) 16.sp else if (height <= 44.dp) 18.sp else 22.sp
        }
        val isNumeric = type == PixelButtonType.NUMERIC || text.all { it.isDigit() || it == '.' || it == ',' }
        val isOperator = type == PixelButtonType.OPERATOR
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = if (isNumeric) QuicksandFontFamily else if( isOperator) QuicksandFontFamily else SystemFontFamily,
            fontWeight = if (isNumeric) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun PixelIconButton(
    modifier: Modifier = Modifier,
    type: PixelButtonType = PixelButtonType.CONTROL,
    height: Dp = 68.dp,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    val colors = CalculatorTheme.colors

    val containerColor = when (type) {
        PixelButtonType.NUMERIC -> colors.numericContainer
        PixelButtonType.OPERATOR -> colors.operatorContainer
        PixelButtonType.CONTROL -> colors.controlContainer
        PixelButtonType.EQUALS -> colors.equalsContainer
        PixelButtonType.SCIENTIFIC -> colors.scientificContainer
        PixelButtonType.SHIFT_ACTIVE -> colors.equalsContainer
    }

    val contentColor = when (type) {
        PixelButtonType.NUMERIC -> colors.numericContent
        PixelButtonType.OPERATOR -> colors.operatorContent
        PixelButtonType.CONTROL -> colors.controlContent
        PixelButtonType.EQUALS -> colors.equalsContent
        PixelButtonType.SCIENTIFIC -> colors.scientificContent
        PixelButtonType.SHIFT_ACTIVE -> colors.equalsContent
    }

    val view = LocalView.current

    Button(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            onClick()
        },
        modifier = modifier.height(height),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        icon()
    }
}
