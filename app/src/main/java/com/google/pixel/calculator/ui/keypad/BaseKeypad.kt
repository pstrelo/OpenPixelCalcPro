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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.res.stringResource
import com.google.pixel.calculator.R
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

    val btnHeight = if (isLandscape) 48.dp else if (isProMode) 60.dp else 82.dp
    val spacing = if (isLandscape) 6.dp else if (isProMode) 8.dp else 12.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLandscape) 10.dp else 16.dp, vertical = if (isLandscape) 2.dp else 6.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Scientific expansion lever/arrow (placed above the first row on the left)
        // In landscape it rotates 90 degrees for horizontal expansion
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
                modifier = Modifier.size(if (isLandscape) 32.dp else 40.dp)
            ) {
                Icon(
                    imageVector = if (isProMode) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isProMode) stringResource(R.string.reduce_calc) else stringResource(R.string.expand_scientific),
                    tint = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(if (isLandscape) 22.dp else 28.dp)
                        .rotate(if (isLandscape) -90f else 0f)
                )
            }
        }

        if (!isLandscape) {
            // Scientific section (3 rows visible in Pro / Expanded mode)
            AnimatedVisibility(
                visible = isProMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // SCIENTIFIC ROW 1: [⇧ Shift] [Rad/Deg] [^] [( )]
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
                            modifier = Modifier.size(24.dp)
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

                // SCIENTIFIC ROW 2: [e / ln] [log / 10ˣ] [√ / ʸ√x] [!]
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

                // SCIENTIFIC ROW 3: [sin / sin⁻¹] [cos / cos⁻¹] [tan / tan⁻¹] [π]
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

        // ROW 1 BASE: [AC] [%] [÷] [⌫]
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
                    contentDescription = stringResource(R.string.delete_digit),
                    modifier = Modifier.size(if (isProMode) 24.dp else 28.dp)
                )
            }
        }

        // ROW 2 BASE: [7] [8] [9] [×]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "7", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("7") })
            PixelButton(text = "8", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("8") })
            PixelButton(text = "9", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("9") })
            PixelButton(text = "×", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("×") })
        }

        // ROW 3 BASE: [4] [5] [6] [+]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "4", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("4") })
            PixelButton(text = "5", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("5") })
            PixelButton(text = "6", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("6") })
            PixelButton(text = "+", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("+") })
        }

        // ROW 4 BASE: [1] [2] [3] [-]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            PixelButton(text = "1", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("1") })
            PixelButton(text = "2", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("2") })
            PixelButton(text = "3", type = PixelButtonType.NUMERIC, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onDigit("3") })
            PixelButton(text = "-", type = PixelButtonType.OPERATOR, height = btnHeight, modifier = Modifier.weight(1f), onClick = { triggerHaptic(); onOperator("-") })
        }

        // ROW 5 BASE: [ Tool Picker ] [0] [.] [=]
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
            // ==================== LANDSCAPE MODE ====================
            // Landscape layout: [Extra symbols (if active)] [ numbers ] [math operators]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // 1. [Extra symbols (if active)] (3 Columns x 4 Rows: 12 scientific keys)
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
                        // Extra row 1: [⇧ Shift] [Rad/Deg] [( )]
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
                                    modifier = Modifier.size(22.dp)
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

                        // Extra row 2: [sin / sin⁻¹] [cos / cos⁻¹] [tan / tan⁻¹]
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

                        // Extra row 3: [ln / e] [log / 10ˣ] [^]
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

                        // Extra row 4: [√ / ʸ√x] [!] [π]
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

                // 2. [ numbers ] (3 Columns x 4 Rows: 7 8 9 / 4 5 6 / 1 2 3 / 🖩 0 .)
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

                // 3. [math operators] (2 Columns x 4 Rows: AC ⌫ / % ÷ / × - / + =)
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
                                contentDescription = stringResource(R.string.delete_digit),
                                modifier = Modifier.size(22.dp)
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
    height: Dp = 82.dp,
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
    val buttonShape = if (colors.isSquircle) RoundedCornerShape(18.dp) else RoundedCornerShape(height / 2)

    Surface(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            onClick()
        },
        modifier = modifier.height(height),
        shape = buttonShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val fontSize = when {
                type == PixelButtonType.SCIENTIFIC -> if (text.length > 3) 15.sp else if (height <= 48.dp) 15.sp else 18.sp
                type == PixelButtonType.NUMERIC -> if (height <= 48.dp) 22.sp else if (height < 65.dp) 28.sp else 33.sp
                type == PixelButtonType.OPERATOR || type == PixelButtonType.EQUALS -> if (height <= 48.dp) 24.sp else if (height < 65.dp) 30.sp else 35.sp
                else -> if (text.length > 2) 18.sp else if (height <= 48.dp) 20.sp else 25.sp
            }
            val isNumeric = type == PixelButtonType.NUMERIC || text.all { it.isDigit() || it == '.' || it == ',' }
            val isOperator = type == PixelButtonType.OPERATOR
            Text(
                text = text,
                fontSize = fontSize,
                fontFamily = if (isNumeric) QuicksandFontFamily else if (isOperator) QuicksandFontFamily else SystemFontFamily,
                fontWeight = if (isNumeric) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun PixelIconButton(
    modifier: Modifier = Modifier,
    type: PixelButtonType = PixelButtonType.CONTROL,
    height: Dp = 82.dp,
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
    val buttonShape = if (colors.isSquircle) RoundedCornerShape(18.dp) else RoundedCornerShape(height / 2)

    Surface(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            onClick()
        },
        modifier = modifier.height(height),
        shape = buttonShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            icon()
        }
    }
}
