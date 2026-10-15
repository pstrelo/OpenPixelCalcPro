package com.google.pixel.calculator.ui.keypad

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
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
    val effectiveProMode = if (isLandscape) true else isProMode
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Proportional dimensions: keypad takes ~50% of screen height with square squircle keys (btnHeight ~= btnWidth)
    val hPadding = if (isLandscape) 10.dp else 16.dp
    val spacingBasic = (screenWidth * 0.026f).coerceIn(8.dp, 12.dp)
    val btnWidthBasic = (screenWidth - (hPadding * 2) - (spacingBasic * 3)) / 4

    // Height of button in basic mode matches width for square icons
    val maxBasicHeightByScreen = (screenHeight * 0.52f - 38.dp - 12.dp - (spacingBasic * 4)) / 5
    val targetBtnHeightBasic = if (isLandscape) 48.dp else minOf(btnWidthBasic * 0.94f, maxBasicHeightByScreen).coerceIn(46.dp, 82.dp)
    val targetKeypadHeightBasic = (targetBtnHeightBasic * 5) + (spacingBasic * 4) + 38.dp + 12.dp

    // Pro mode (8 rows): compact scientific rows, keypad takes ~50-52% of screen height
    val spacingPro = (screenWidth * 0.015f).coerceIn(4.dp, 6.dp)
    val maxProHeightByScreen = (screenHeight * 0.54f - 38.dp - 12.dp - (spacingPro * 7)) / 8
    val targetBtnHeightPro = if (isLandscape) 48.dp else minOf(btnWidthBasic * 0.58f, maxProHeightByScreen).coerceIn(38.dp, 52.dp)
    val targetKeypadHeightPro = (targetBtnHeightPro * 8) + (spacingPro * 7) + 38.dp + 12.dp

    val targetBtnHeight = if (isLandscape) 48.dp else if (effectiveProMode) targetBtnHeightPro else targetBtnHeightBasic
    val targetSpacing = if (isLandscape) 6.dp else if (effectiveProMode) spacingPro else spacingBasic
    val targetKeypadHeight = if (isLandscape) 0.dp else if (effectiveProMode) targetKeypadHeightPro else targetKeypadHeightBasic

    val keyAnimationSpec = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    val btnHeight by animateDpAsState(
        targetValue = targetBtnHeight,
        animationSpec = keyAnimationSpec,
        label = "KeypadBtnHeight"
    )

    val spacing by animateDpAsState(
        targetValue = targetSpacing,
        animationSpec = keyAnimationSpec,
        label = "KeypadSpacing"
    )

    val keypadHeight by animateDpAsState(
        targetValue = targetKeypadHeight,
        animationSpec = keyAnimationSpec,
        label = "KeypadTotalHeight"
    )

    val scientificBtnHeight = if (isLandscape) 48.dp else btnHeight
    val scientificSpacing = spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isLandscape) Modifier.height(keypadHeight) else Modifier)
            .padding(horizontal = if (isLandscape) 10.dp else 16.dp, vertical = if (isLandscape) 2.dp else 6.dp),
        verticalArrangement = if (!isLandscape) Arrangement.spacedBy(spacing, Alignment.Bottom) else Arrangement.spacedBy(spacing)
    ) {
        // Scientific expansion lever/arrow (only in portrait, hidden in landscape)
        if (!isLandscape) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            onToggleProMode()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        val arrowRotation by animateFloatAsState(
                            targetValue = if (effectiveProMode) 180f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "ProModeArrowRotation"
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = if (effectiveProMode) stringResource(R.string.reduce_calc) else stringResource(R.string.expand_scientific),
                            tint = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                // Scientific section (3 rows visible in Pro / Expanded mode)
                // Expands smoothly from top to bottom while lower keys shrink
                AnimatedVisibility(
                    visible = effectiveProMode,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        expandFrom = Alignment.Top,
                        clip = true
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        shrinkTowards = Alignment.Top,
                        clip = true
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = scientificSpacing),
                        verticalArrangement = Arrangement.spacedBy(scientificSpacing)
                    ) {
                        // SCIENTIFIC ROW 1: [⇧ Shift] [Rad/Deg] [^] [( )]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(scientificSpacing)
                        ) {
                            PixelIconButton(
                                type = if (isShiftActive) PixelButtonType.SHIFT_ACTIVE else PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
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
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    onToggleAngleUnit()
                                }
                            )

                            PixelButton(
                                text = "^",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    onOperator("^")
                                }
                            )

                            PixelButton(
                                text = "( )",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
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
                            horizontalArrangement = Arrangement.spacedBy(scientificSpacing)
                        ) {
                            PixelButton(
                                text = if (isShiftActive) "ln" else "e",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onFunction("ln") else onConstant("e")
                                }
                            )

                            PixelButton(
                                text = if (isShiftActive) "10ˣ" else "log",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onOperator("10^") else onFunction("log")
                                }
                            )

                            PixelButton(
                                text = if (isShiftActive) "ʸ√x" else "√",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onFunction("root") else onFunction("sqrt")
                                }
                            )

                            PixelButton(
                                text = "!",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
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
                            horizontalArrangement = Arrangement.spacedBy(scientificSpacing)
                        ) {
                            PixelButton(
                                text = if (isShiftActive) "sin⁻¹" else "sin",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onFunction("sin⁻¹") else onFunction("sin")
                                }
                            )

                            PixelButton(
                                text = if (isShiftActive) "cos⁻¹" else "cos",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onFunction("cos⁻¹") else onFunction("cos")
                                }
                            )

                            PixelButton(
                                text = if (isShiftActive) "tan⁻¹" else "tan",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    if (isShiftActive) onFunction("tan⁻¹") else onFunction("tan")
                                }
                            )

                            PixelButton(
                                text = "π",
                                type = PixelButtonType.SCIENTIFIC,
                                height = scientificBtnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    triggerHaptic()
                                    onConstant("π")
                                }
                            )
                        }
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
                val backspaceIconSize by animateDpAsState(
                    targetValue = if (effectiveProMode) 24.dp else 28.dp,
                    animationSpec = keyAnimationSpec,
                    label = "BackspaceIconSize"
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    modifier = Modifier.size(backspaceIconSize)
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
                    visible = effectiveProMode,
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
    val buttonShape = RoundedCornerShape(18.dp)

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
            val fraction = ((height - 48.dp) / 40.dp).coerceIn(0f, 1f)
            val fontSize = when {
                type == PixelButtonType.SCIENTIFIC -> {
                    if (text.length > 3) (13f + 2f * fraction).sp else if (height <= 48.dp) 15.sp else (15f + 3.5f * fraction).sp
                }
                height <= 48.dp -> {
                    when (type) {
                        PixelButtonType.NUMERIC -> 22.sp
                        PixelButtonType.OPERATOR, PixelButtonType.EQUALS -> 24.sp
                        else -> if (text.length > 2) 18.sp else 20.sp
                    }
                }
                else -> {
                    when (type) {
                        PixelButtonType.NUMERIC -> (26f + 8f * fraction).sp
                        PixelButtonType.OPERATOR, PixelButtonType.EQUALS -> (28f + 8f * fraction).sp
                        else -> if (text.length > 2) (18f + 3f * fraction).sp else (20f + 7f * fraction).sp
                    }
                }
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
    val buttonShape = RoundedCornerShape(18.dp)

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
