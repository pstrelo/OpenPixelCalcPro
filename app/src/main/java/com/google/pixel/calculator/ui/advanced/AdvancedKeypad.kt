package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.toolbox.PixelToolPillBar
import com.google.pixel.calculator.ui.toolbox.PixelToolToggleButton

/**
 * Material 3 Keypad for Advanced Calculator.
 * 6 Columns x 7 Row-units layout:
 * - Columns 1-5 have 5 regular keys (1 row-unit each).
 * - In rows 6 & 7:
 *   - Col 1: Tool menu button (double-height, circular to square animation).
 *   - Col 2: '(' (row 6) and '<' (row 7) single keys.
 *   - Col 3: ')' (row 6) and '>' (row 7) single keys.
 *   - Col 4: [ ← ] Square key (double-height, active tab color).
 *   - Col 5: [ → ] Square key (double-height, active tab color).
 * - Column 6 has 5 keys total:
 *   - Rows 1, 2, 3: single keys.
 *   - Rows 4 & 5: [ ⌫ ] Square backspace key (double-height, standard calculator shape).
 *   - Rows 6 & 7: [ ↵ ] Square enter/solve key (double-height, active tab color).
 */
@Composable
fun AdvancedKeypad(
    activeTab: MathSolverTab,
    activeFormatLabel: String,
    onTabSelected: (MathSolverTab) -> Unit,
    onInsertChar: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onCycleFormat: () -> Unit,
    onExecute: () -> Unit,
    isSplitScreen: Boolean = false,
    isRadians: Boolean = true,
    onToggleRadians: () -> Unit = {},
    onOpenCatalog: () -> Unit = {},
    isToolBarExpanded: Boolean = false,
    onToggleToolBar: () -> Unit = {},
    onNavigate: (ActiveScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val unitHeight = if (isLandscape) 26.dp else if (isSplitScreen) 30.dp else 36.dp
    val spacing = 4.dp
    val doubleHeight = unitHeight * 2 + spacing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. Horizontal tab bar (Algebra, Trigonometria, Calcolo, Alfabeto)
        MathSolverTabBar(
            selectedTab = activeTab,
            onTabSelected = { tab ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTabSelected(tab)
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Keypad grid
        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
            when (activeTab) {
                MathSolverTab.ALGEBRA -> AlgebraKeypadGrid(
                    unitHeight = unitHeight,
                    doubleHeight = doubleHeight,
                    spacing = spacing,
                    isToolBarExpanded = isToolBarExpanded,
                    onToggleToolBar = onToggleToolBar,
                    onMoveCursorLeft = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorLeft()
                    },
                    onMoveCursorRight = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorRight()
                    },
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onExecute = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExecute()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.TRIGONOMETRY -> TrigonometryKeypadGrid(
                    unitHeight = unitHeight,
                    doubleHeight = doubleHeight,
                    spacing = spacing,
                    isToolBarExpanded = isToolBarExpanded,
                    onToggleToolBar = onToggleToolBar,
                    onMoveCursorLeft = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorLeft()
                    },
                    onMoveCursorRight = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorRight()
                    },
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onExecute = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExecute()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.CALCULUS -> CalculusKeypadGrid(
                    unitHeight = unitHeight,
                    doubleHeight = doubleHeight,
                    spacing = spacing,
                    isToolBarExpanded = isToolBarExpanded,
                    onToggleToolBar = onToggleToolBar,
                    onMoveCursorLeft = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorLeft()
                    },
                    onMoveCursorRight = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorRight()
                    },
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onExecute = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExecute()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.ALPHABET -> AlphabetKeypadGrid(
                    unitHeight = if (isLandscape) 24.dp else if (isSplitScreen) 28.dp else 32.dp,
                    doubleHeight = if (isLandscape) 24.dp * 2 + spacing else if (isSplitScreen) 28.dp * 2 + spacing else 32.dp * 2 + spacing,
                    spacing = spacing,
                    isToolBarExpanded = isToolBarExpanded,
                    onToggleToolBar = onToggleToolBar,
                    onMoveCursorLeft = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorLeft()
                    },
                    onMoveCursorRight = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMoveCursorRight()
                    },
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onExecute = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExecute()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
            }

            // Material 3 floating pill bar when tool button is pressed
            androidx.compose.animation.AnimatedVisibility(
                visible = isToolBarExpanded,
                enter = fadeIn(tween(180)) + scaleIn(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.62f,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    ),
                    transformOrigin = TransformOrigin(0f, 1f)
                ),
                exit = fadeOut(tween(140)) + scaleOut(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    ),
                    transformOrigin = TransformOrigin(0f, 1f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = doubleHeight + 6.dp)
            ) {
                PixelToolPillBar(
                    currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                    height = 42.dp,
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                )
            }
        }
    }
}

// State-based overload
@Composable
fun AdvancedKeypad(
    state: AdvancedCalculatorState,
    onTabSelected: (MathSolverTab) -> Unit,
    onInsertChar: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onCycleFormat: () -> Unit,
    onExecute: () -> Unit,
    onToggleRadians: () -> Unit = {},
    onOpenCatalog: () -> Unit = {},
    isToolBarExpanded: Boolean = false,
    onToggleToolBar: () -> Unit = {},
    onNavigate: (ActiveScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeTab = state.activeTab
    val activeFormatLabel = state.equations.getOrNull(state.activeEquationIndex)?.formatMode?.label ?: "DEC"
    val isSplitScreen = state.screenViewMode == ScreenViewMode.SPLIT_SCREEN
    val isRadians = state.isRadians

    AdvancedKeypad(
        activeTab = activeTab,
        activeFormatLabel = activeFormatLabel,
        onTabSelected = onTabSelected,
        onInsertChar = onInsertChar,
        onMoveCursorLeft = onMoveCursorLeft,
        onMoveCursorRight = onMoveCursorRight,
        onDeleteChar = onDeleteChar,
        onClearAll = onClearAll,
        onCycleFormat = onCycleFormat,
        onExecute = onExecute,
        isSplitScreen = isSplitScreen,
        isRadians = isRadians,
        onToggleRadians = onToggleRadians,
        onOpenCatalog = onOpenCatalog,
        isToolBarExpanded = isToolBarExpanded,
        onToggleToolBar = onToggleToolBar,
        onNavigate = onNavigate,
        modifier = modifier
    )
}

// Backward-compatible legacy overload
@Composable
fun AdvancedKeypad(
    state: AdvancedCalculatorState,
    onKeyClick: (String) -> Unit,
    onExecute: () -> Unit,
    onCycleFormat: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onOpenCatalog: () -> Unit = {},
    onToggleGraph: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localTab by remember { mutableStateOf(state.activeTab) }
    val effectiveTab = if (state.activeTab != localTab) state.activeTab else localTab

    AdvancedKeypad(
        activeTab = effectiveTab,
        activeFormatLabel = state.equations.getOrNull(state.activeEquationIndex)?.formatMode?.label ?: "DEC",
        onTabSelected = { localTab = it },
        onInsertChar = onKeyClick,
        onMoveCursorLeft = { onKeyClick("←") },
        onMoveCursorRight = { onKeyClick("→") },
        onDeleteChar = onBackspace,
        onClearAll = onClearAll,
        onCycleFormat = onCycleFormat,
        onExecute = onExecute,
        isSplitScreen = state.screenViewMode == ScreenViewMode.SPLIT_SCREEN,
        isRadians = state.isRadians,
        onToggleRadians = { onKeyClick("DEG") },
        onOpenCatalog = onOpenCatalog,
        modifier = modifier
    )
}

// =========================================================================
// TAB BAR COMPOSABLE
// =========================================================================

@Composable
fun MathSolverTabBar(
    selectedTab: MathSolverTab,
    onTabSelected: (MathSolverTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val dividerColor = if (isDark) Color(0xFF2E3448) else CalculatorTheme.colors.operatorContainer
    val unselectedColor = if (isDark) MathSolverTheme.tabUnselected else CalculatorTheme.colors.onSurfaceVariant

    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = if (isDark) Color.White else CalculatorTheme.colors.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                height = 3.dp,
                color = selectedTab.accentColor
            )
        },
        divider = {
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        MathSolverTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isSelected) tab.accentColor else unselectedColor
                    )
                }
            )
        }
    }
}

// =========================================================================
// 1. ALGEBRA KEYPAD GRID (6 Columns x 7 Row-units)
// =========================================================================

@Composable
private fun AlgebraKeypadGrid(
    unitHeight: Dp,
    doubleHeight: Dp,
    spacing: Dp,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onExecute: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.ALGEBRA.accentColor
    val fnBg = if (isDark) MathSolverTheme.algebraFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.algebraFunctionContent else Color(0xFF7E22CE)
    val numBg = if (isDark) MathSolverTheme.numericContainer else CalculatorTheme.colors.numericContainer
    val numColor = if (isDark) MathSolverTheme.numericContent else CalculatorTheme.colors.numericContent
    val opBg = if (isDark) MathSolverTheme.operatorContainer else CalculatorTheme.colors.operatorContainer
    val opColor = if (isDark) MathSolverTheme.operatorContent else CalculatorTheme.colors.operatorContent
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ □² ], [ □ⁿ ], [ log ], [ ln ], [ ToolButton (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("□²", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^2") }
            SquircleKey("□ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^") }
            SquircleKey("log", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("log(") }
            SquircleKey("ln", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("ln(") }
            PixelToolToggleButton(
                currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                isExpanded = isToolBarExpanded,
                height = doubleHeight,
                onClick = onToggleToolBar,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Col 2: [ y ], [ □/□ ], [ √x ], [ ⁿ√□ ], [ |x| ], [ ( ], [ < ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("y", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
            SquircleKey("√x", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
            SquircleKey("ⁿ√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("ʸ√") }
            SquircleKey("|x|", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("abs(") }
            SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
            SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
        }

        // Col 3: [ π ], [ 7 ], [ 4 ], [ 1 ], [ 0 ], [ ) ], [ > ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("7", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("7") }
            SquircleKey("4", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("4") }
            SquircleKey("1", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("1") }
            SquircleKey("0", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("0") }
            SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
            SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
        }

        // Col 4: [ e ], [ 8 ], [ 5 ], [ 2 ], [ . ], [ ← (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("8", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("8") }
            SquircleKey("5", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("5") }
            SquircleKey("2", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("2") }
            SquircleKey(".", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey(".") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorLeft
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cursor left",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 5: [ % ], [ 9 ], [ 6 ], [ 3 ], [ = ], [ → (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("%", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 17.sp) { onKey("%") }
            SquircleKey("9", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("9") }
            SquircleKey("6", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("6") }
            SquircleKey("3", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("3") }
            SquircleKey("=", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey(" = ") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorRight
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Cursor right",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 6: [ × ], [ - ], [ + ], [ ⌫ (double height) ], [ ↵ (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("×", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 18.sp) { onKey("*") }
            SquircleKey("-", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 18.sp) { onKey("-") }
            SquircleKey("+", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 18.sp) { onKey("+") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = tabAccent,
                contentColor = Color.White,
                onClick = onExecute
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = stringResource(R.string.solve),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (6 Columns x 7 Row-units)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    unitHeight: Dp,
    doubleHeight: Dp,
    spacing: Dp,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onExecute: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.TRIGONOMETRY.accentColor
    val fnBg = if (isDark) MathSolverTheme.trigFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.trigFunctionContent else Color(0xFF047857)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ sin ], [ arcsin ], [ asinh ], [ □/□ ], [ ToolButton (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("sin", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sin(") }
            SquircleKey("arcsin", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("asinh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("asinh(") }
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
            PixelToolToggleButton(
                currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                isExpanded = isToolBarExpanded,
                height = doubleHeight,
                onClick = onToggleToolBar,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Col 2: [ θ ], [ cos ], [ arccos ], [ acosh ], [ √□ ], [ ( ], [ < ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("θ", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("θ") }
            SquircleKey("cos", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("cos(") }
            SquircleKey("arccos", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccos(") }
            SquircleKey("acosh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("acosh(") }
            SquircleKey("√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
            SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
            SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
        }

        // Col 3: [ e ], [ tan ], [ arctan ], [ atanh ], [ □² ], [ ) ], [ > ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("tan", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("tan(") }
            SquircleKey("arctan", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arctan(") }
            SquircleKey("atanh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("atanh(") }
            SquircleKey("□²", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^2") }
            SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
            SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
        }

        // Col 4: [ π ], [ csc ], [ arccsc ], [ acsch ], [ sinh ], [ ← (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("csc", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("csc(") }
            SquircleKey("arccsc", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccsc(") }
            SquircleKey("acsch", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("acsch(") }
            SquircleKey("sinh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("sinh(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorLeft
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cursor left",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 5: [ ° ], [ sec ], [ arcsec ], [ asech ], [ cosh ], [ → (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("°", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("°") }
            SquircleKey("sec", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sec(") }
            SquircleKey("arcsec", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arcsec(") }
            SquircleKey("asech", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("asech(") }
            SquircleKey("cosh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("cosh(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorRight
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Cursor right",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 6: [ rad ], [ cot ], [ arccot ], [ ⌫ (double height) ], [ ↵ (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("rad", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("*(1)") }
            SquircleKey("cot", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("cot(") }
            SquircleKey("arccot", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccot(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = tabAccent,
                contentColor = Color.White,
                onClick = onExecute
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = stringResource(R.string.solve),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (6 Columns x 7 Row-units)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    unitHeight: Dp,
    doubleHeight: Dp,
    spacing: Dp,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onExecute: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.CALCULUS.accentColor
    val fnBg = if (isDark) MathSolverTheme.calculusFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.calculusFunctionContent else Color(0xFFBE185D)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ d/dx ], [ Σ ], [ binom ], [ binompdf ], [ ToolButton (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("d/dx", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 12.sp) { onKey("d/dx(") }
            SquircleKey("Σ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("binom", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("binom(") }
            SquircleKey("binompdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("binompdf(") }
            PixelToolToggleButton(
                currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                isExpanded = isToolBarExpanded,
                height = doubleHeight,
                onClick = onToggleToolBar,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Col 2: [ t ], [ ∫ ], [ Π ], [ nPr ], [ binomcdf ], [ ( ], [ < ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("t", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("t") }
            SquircleKey("∫", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("Π", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("prod(") }
            SquircleKey("nPr", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 12.sp) { onKey("perm(") }
            SquircleKey("binomcdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("binomcdf(") }
            SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
            SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
        }

        // Col 3: [ ∞ ], [ ∫ₐᵇ ], [ ln ], [ n! ], [ normpdf ], [ ) ], [ > ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("∞", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("∫ₐᵇ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 13.sp) { onKey("defint(") }
            SquircleKey("ln", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("ln(") }
            SquircleKey("n!", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("!") }
            SquircleKey("normpdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("normpdf(") }
            SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
            SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
        }

        // Col 4: [ e ], [ lim ], [ log ], [ rand ], [ normcdf ], [ ← (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("lim", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 13.sp) { onKey("lim(") }
            SquircleKey("log", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("log(") }
            SquircleKey("rand", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("rand()") }
            SquircleKey("normcdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("normcdf(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorLeft
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cursor left",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 5: [ π ], [ □ⁿ ], [ √□ ], [ randInt ], [ mean ], [ → (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("□ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^") }
            SquircleKey("√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
            SquircleKey("randInt", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 10.sp) { onKey("randint(") }
            SquircleKey("mean", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("mean(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = navSquareBg,
                contentColor = tabAccent,
                onClick = onMoveCursorRight
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Cursor right",
                    tint = tabAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Col 6: [ □/□ ], [ |x| ], [ Γ ], [ ⌫ (double height) ], [ ↵ (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("abs(") }
            SquircleKey("Γ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey("gamma(") }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            SquareSystemKey(
                height = doubleHeight,
                modifier = Modifier.fillMaxWidth(),
                bgColor = tabAccent,
                contentColor = Color.White,
                onClick = onExecute
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = stringResource(R.string.solve),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// =========================================================================
// 4. ALPHABET, CONSTANTS & SI PREFIXES KEYPAD GRID (6 Columns x 7 Row-units)
// =========================================================================

enum class AlphabetSubTab(val labelRes: Int) {
    LETTERS(R.string.subtab_letters),
    CONSTANTS(R.string.subtab_constants),
    SI_PREFIXES(R.string.subtab_si_prefixes)
}

@Composable
private fun AlphabetKeypadGrid(
    unitHeight: Dp,
    doubleHeight: Dp,
    spacing: Dp,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onExecute: () -> Unit,
    onKey: (String) -> Unit
) {
    var subTab by remember { mutableStateOf(AlphabetSubTab.LETTERS) }
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.ALPHABET.accentColor
    val fnBg = if (isDark) MathSolverTheme.alphabetFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.alphabetFunctionContent else Color(0xFFC2410C)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        // Sub-selector chip row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlphabetSubTab.values().forEach { tab ->
                val isSelected = tab == subTab
                val chipBg = if (isSelected) tabAccent else if (isDark) Color(0xFF282D3D) else Color(0xFFE2E8F0)
                val chipText = if (isSelected) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(chipBg)
                        .clickable { subTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(tab.labelRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = chipText
                    )
                }
            }
        }

        when (subTab) {
            AlphabetSubTab.LETTERS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Col 1: a, b, c, d, k, ToolButton
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("a", "b", "c", "d", "k").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        PixelToolToggleButton(
                            currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                            isExpanded = isToolBarExpanded,
                            height = doubleHeight,
                            onClick = onToggleToolBar,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Col 2: m, n, p, r, s, (, <
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("m", "n", "p", "r", "s").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
                        SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
                    }
                    // Col 3: t, u, v, w, z, ), >
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("t", "u", "v", "w", "z").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
                        SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
                    }
                    // Col 4: α, β, γ, δ, ε, [ ← ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("α", "β", "γ", "δ", "ε").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorLeft
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cursor left",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 5: λ, μ, φ, ω, i, [ → ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("λ", "μ", "φ", "ω", "i").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorRight
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Cursor right",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 6: Δ, Σ, Ω, [ ⌫ ], [ ↵ ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("Δ", "Σ", "Ω").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onDeleteChar
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_digit),
                                tint = delColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = tabAccent,
                            contentColor = Color.White,
                            onClick = onExecute
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = stringResource(R.string.solve),
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            AlphabetSubTab.CONSTANTS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Col 1: c, G, g, h, ħ, ToolButton
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("c", "G", "g", "h", "ħ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 15.sp) { onKey(key) }
                        }
                        PixelToolToggleButton(
                            currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                            isExpanded = isToolBarExpanded,
                            height = doubleHeight,
                            onClick = onToggleToolBar,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Col 2: k_B, N_A, R, q_e, m_e, (, <
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("k_B", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("k_B") }
                        SquircleKey("N_A", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("N_A") }
                        SquircleKey("R", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("R") }
                        SquircleKey("q_e", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("q_e") }
                        SquircleKey("m_e", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("m_e") }
                        SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
                        SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
                    }
                    // Col 3: m_p, u, ε₀, μ₀, σ, ), >
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("m_p", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("m_p") }
                        SquircleKey("u", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("u") }
                        SquircleKey("ε₀", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("ε₀") }
                        SquircleKey("μ₀", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("μ₀") }
                        SquircleKey("σ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("σ") }
                        SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
                        SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
                    }
                    // Col 4: φ, e, π, √2, √3, [ ← ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("φ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 15.sp) { onKey("φ") }
                        SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("e") }
                        SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("π") }
                        SquircleKey("√2", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("sqrt(2)") }
                        SquircleKey("√3", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("sqrt(3)") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorLeft
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cursor left",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 5: ln2, ln10, □ⁿ, √□, □/□, [ → ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("ln2", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("ln(2)") }
                        SquircleKey("ln10", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("ln(10)") }
                        SquircleKey("□ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^") }
                        SquircleKey("√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
                        SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorRight
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Cursor right",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 6: |x|, x, y, [ ⌫ ], [ ↵ ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("|x|", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("abs(") }
                        SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("x") }
                        SquircleKey("y", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("y") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onDeleteChar
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_digit),
                                tint = delColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = tabAccent,
                            contentColor = Color.White,
                            onClick = onExecute
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = stringResource(R.string.solve),
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            AlphabetSubTab.SI_PREFIXES -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Col 1: Y, Z, E, P, T, ToolButton
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("Y (10²⁴)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e24)") }
                        SquircleKey("Z (10²¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e21)") }
                        SquircleKey("E (10¹⁸)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e18)") }
                        SquircleKey("P (10¹⁵)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e15)") }
                        SquircleKey("T (10¹²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e12)") }
                        PixelToolToggleButton(
                            currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                            isExpanded = isToolBarExpanded,
                            height = doubleHeight,
                            onClick = onToggleToolBar,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Col 2: G, M, k, h, da, (, <
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("G (10⁹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e9)") }
                        SquircleKey("M (10⁶)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e6)") }
                        SquircleKey("k (10³)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 10.sp) { onKey("*(1000)") }
                        SquircleKey("h (10²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 10.sp) { onKey("*(100)") }
                        SquircleKey("da (10¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(10)") }
                        SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey("(") }
                        SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" < ") }
                    }
                    // Col 3: d, c, m, μ, n, ), >
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("d (10⁻¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(0.1)") }
                        SquircleKey("c (10⁻²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(0.01)") }
                        SquircleKey("m (10⁻³)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(0.001)") }
                        SquircleKey("μ (10⁻⁶)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e-6)") }
                        SquircleKey("n (10⁻⁹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("*(1e-9)") }
                        SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(")") }
                        SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true) { onKey(" > ") }
                    }
                    // Col 4: p, f, a, z, y, [ ← ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("p (10⁻¹²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-12)") }
                        SquircleKey("f (10⁻¹⁵)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-15)") }
                        SquircleKey("a (10⁻¹⁸)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-18)") }
                        SquircleKey("z (10⁻²¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-21)") }
                        SquircleKey("y (10⁻²⁴)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-24)") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorLeft
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cursor left",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 5: 10ⁿ, ×, ÷, +, -, [ → ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("10ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("*10^") }
                        SquircleKey("×", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("*") }
                        SquircleKey("÷", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("/") }
                        SquircleKey("+", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("+") }
                        SquircleKey("-", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("-") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = navSquareBg,
                            contentColor = tabAccent,
                            onClick = onMoveCursorRight
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Cursor right",
                                tint = tabAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Col 6: =, 0, ., [ ⌫ ], [ ↵ ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("=", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey(" = ") }
                        SquircleKey("0", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("0") }
                        SquircleKey(".", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey(".") }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onDeleteChar
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_digit),
                                tint = delColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        SquareSystemKey(
                            height = doubleHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = tabAccent,
                            contentColor = Color.White,
                            onClick = onExecute
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = stringResource(R.string.solve),
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// REUSABLE KEYPAD BUTTONS (SQUARE & PILL)
// =========================================================================

/**
 * Square button with the exact shape and size of the system calculator (RoundedCornerShape 18dp).
 * Used for double-height [ ← ], [ → ], [ ↵ ], and [ ⌫ ].
 */
@Composable
fun SquareSystemKey(
    height: Dp,
    modifier: Modifier = Modifier,
    bgColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    Surface(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.height(height),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

/**
 * Standard pill/squircle key with rounded ends (RoundedCornerShape 50%).
 */
@Composable
fun SquircleKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
            .clickable {
                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
