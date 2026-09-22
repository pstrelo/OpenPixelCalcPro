package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val buttonHeight = if (isSplitScreen) 38.dp else 42.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. Barra orizzontale a 4 Tab tematiche (Algebra, Trigonometry, Calculus, Alphabet)
        MathSolverTabBar(
            selectedTab = activeTab,
            onTabSelected = { tab ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTabSelected(tab)
            }
        )

        // 2. Griglia tasti della Tab attiva (5 Colonne x 6 Righe)
        when (activeTab) {
            MathSolverTab.ALGEBRA -> AlgebraKeypadGrid(
                btnHeight = buttonHeight,
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.TRIGONOMETRY -> TrigonometryKeypadGrid(
                btnHeight = buttonHeight,
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.CALCULUS -> CalculusKeypadGrid(
                btnHeight = buttonHeight,
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.ALPHABET -> AlphabetKeypadGrid(
                btnHeight = buttonHeight,
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
        }

        // 3. Barra persistente inferiore a 6 tasti ([ ← ], [ → ], [ ⌫ DEL ], [ AC ], [ FORMAT ], [ ➤ ])
        PersistentBottomEditBar(
            activeTab = activeTab,
            activeFormatLabel = activeFormatLabel,
            btnHeight = buttonHeight,
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
            onClearAll = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClearAll()
            },
            onCycleFormat = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCycleFormat()
            },
            onExecute = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onExecute()
            }
        )
    }
}

// Overload orientato allo stato
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
    modifier: Modifier = Modifier
) {
    val activeTab = state.activeTab
    val activeFormatLabel = state.equations.getOrNull(state.activeEquationIndex)?.formatMode?.label ?: "DEC"
    val isSplitScreen = state.screenViewMode == ScreenViewMode.SPLIT_SCREEN

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
        modifier = modifier
    )
}

// Overload legacy retrocompatibile con onKeyClick
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
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MathSolverTheme.background,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                height = 3.dp,
                color = selectedTab.accentColor
            )
        },
        divider = {
            HorizontalDivider(color = Color(0xFF2A2E40), thickness = 1.dp)
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
                        color = if (isSelected) tab.accentColor else MathSolverTheme.tabUnselected
                    )
                }
            )
        }
    }
}

// =========================================================================
// 1. ALGEBRA KEYPAD GRID (5x6)
// =========================================================================

@Composable
private fun AlgebraKeypadGrid(
    btnHeight: Dp,
    onKey: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: x², xⁿ, √□, ⁿ√□, □/□
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x²", MathSolverTheme.functionContainer, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("xⁿ", MathSolverTheme.functionContainer, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("√□", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("ⁿ√□", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("ʸ√") }
            SquircleKey("□/□", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("/") }
        }
        // Row 2: log, ln, |x|, π, e
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("log", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("ln", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("|x|", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
            SquircleKey("π", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("π") }
            SquircleKey("e", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("e") }
        }
        // Row 3: 7, 8, 9, ÷, (
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("7", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("7") }
            SquircleKey("8", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("8") }
            SquircleKey("9", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("9") }
            SquircleKey("÷", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
            SquircleKey("(", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("(") }
        }
        // Row 4: 4, 5, 6, ×, )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("4", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("4") }
            SquircleKey("5", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("5") }
            SquircleKey("6", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("6") }
            SquircleKey("×", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
            SquircleKey(")", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(")") }
        }
        // Row 5: 1, 2, 3, -, x
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("1", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("1") }
            SquircleKey("2", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("2") }
            SquircleKey("3", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("3") }
            SquircleKey("-", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
            SquircleKey("x", MathSolverTheme.functionContainer, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("x") }
        }
        // Row 6: 0, ., =, +, y (Nota: '=' inserisce " = " senza calcolare!)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("0", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("0") }
            SquircleKey(".", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(".") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 20.sp) { onKey(" = ") }
            SquircleKey("+", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
            SquircleKey("y", MathSolverTheme.functionContainer, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("y") }
        }
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (5x6)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    btnHeight: Dp,
    onKey: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: sin, cos, tan, π, θ
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sin", MathSolverTheme.functionContainer, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f)) { onKey("sin(") }
            SquircleKey("cos", MathSolverTheme.functionContainer, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f)) { onKey("cos(") }
            SquircleKey("tan", MathSolverTheme.functionContainer, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f)) { onKey("tan(") }
            SquircleKey("π", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("π") }
            SquircleKey("θ", MathSolverTheme.functionContainer, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f)) { onKey("θ") }
        }
        // Row 2: sin⁻¹, cos⁻¹, tan⁻¹, sinh, cosh
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sin⁻¹", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("asin(") }
            SquircleKey("cos⁻¹", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("acos(") }
            SquircleKey("tan⁻¹", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("atan(") }
            SquircleKey("sinh", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sinh(") }
            SquircleKey("cosh", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("cosh(") }
        }
        // Row 3: 7, 8, 9, ÷, (
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("7", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("7") }
            SquircleKey("8", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("8") }
            SquircleKey("9", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("9") }
            SquircleKey("÷", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
            SquircleKey("(", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("(") }
        }
        // Row 4: 4, 5, 6, ×, )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("4", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("4") }
            SquircleKey("5", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("5") }
            SquircleKey("6", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("6") }
            SquircleKey("×", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
            SquircleKey(")", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(")") }
        }
        // Row 5: 1, 2, 3, -, x
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("1", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("1") }
            SquircleKey("2", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("2") }
            SquircleKey("3", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("3") }
            SquircleKey("-", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
            SquircleKey("x", MathSolverTheme.functionContainer, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("x") }
        }
        // Row 6: 0, ., =, +, tanh
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("0", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("0") }
            SquircleKey(".", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(".") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 20.sp) { onKey(" = ") }
            SquircleKey("+", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
            SquircleKey("tanh", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("tanh(") }
        }
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (5x6)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    btnHeight: Dp,
    onKey: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: d/dx, ∫, lim, Σ, ∞
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("d/dx", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true) { onKey("d/dx(") }
            SquircleKey("∫", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("lim", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f)) { onKey("lim(") }
            SquircleKey("Σ", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("Σ(") }
            SquircleKey("∞", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("∞") }
        }
        // Row 2: xⁿ, √□, e, i, , (separatore per parametri: d/dx(x^2, 2))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("xⁿ", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("√□", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("e", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("e") }
            SquircleKey("i", MathSolverTheme.functionContainer, MathSolverTheme.functionContent, btnHeight, Modifier.weight(1f)) { onKey("i") }
            SquircleKey(",", MathSolverTheme.surfaceVariant, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey(", ") }
        }
        // Row 3: 7, 8, 9, ÷, (
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("7", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("7") }
            SquircleKey("8", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("8") }
            SquircleKey("9", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("9") }
            SquircleKey("÷", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
            SquircleKey("(", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("(") }
        }
        // Row 4: 4, 5, 6, ×, )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("4", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("4") }
            SquircleKey("5", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("5") }
            SquircleKey("6", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("6") }
            SquircleKey("×", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
            SquircleKey(")", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(")") }
        }
        // Row 5: 1, 2, 3, -, x
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("1", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("1") }
            SquircleKey("2", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("2") }
            SquircleKey("3", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("3") }
            SquircleKey("-", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
            SquircleKey("x", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("x") }
        }
        // Row 6: 0, ., =, +, t
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("0", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("0") }
            SquircleKey(".", MathSolverTheme.numericContainer, MathSolverTheme.numericContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey(".") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 20.sp) { onKey(" = ") }
            SquircleKey("+", MathSolverTheme.operatorContainer, MathSolverTheme.operatorContent, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
            SquircleKey("t", MathSolverTheme.functionContainer, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("t") }
        }
    }
}

// =========================================================================
// 4. ALPHABET KEYPAD GRID (Latin / Greek Toggle)
// =========================================================================

@Composable
private fun AlphabetKeypadGrid(
    btnHeight: Dp,
    onKey: (String) -> Unit
) {
    var isGreekMode by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Sub-Bar: Latin [ a-z ] vs Greek [ α-ω ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { isGreekMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isGreekMode) MathSolverTheme.accentAlphabet else MathSolverTheme.functionContainer
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            ) {
                Text(
                    text = "Latin (a-z)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isGreekMode) Color.White else MathSolverTheme.tabUnselected
                )
            }
            Button(
                onClick = { isGreekMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGreekMode) MathSolverTheme.accentAlphabet else MathSolverTheme.functionContainer
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            ) {
                Text(
                    text = "Greek (α-ω)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGreekMode) Color.White else MathSolverTheme.tabUnselected
                )
            }
        }

        if (!isGreekMode) {
            // Latin Mode Matrix (30 Keys)
            val rows = listOf(
                listOf("a", "b", "c", "d", "e"),
                listOf("f", "g", "h", "i", "j"),
                listOf("k", "l", "m", "n", "o"),
                listOf("p", "q", "r", "s", "t"),
                listOf("u", "v", "w", "x", "y"),
                listOf("z", "_", "(", ")", "=")
            )
            rows.forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowKeys.forEach { key ->
                        val isEquals = key == "="
                        val insertValue = if (isEquals) " = " else key
                        val container = if (isEquals) MathSolverTheme.surfaceVariant else MathSolverTheme.functionContainer
                        val content = if (isEquals) MathSolverTheme.accentAlphabet else Color.White
                        SquircleKey(key, container, content, btnHeight, Modifier.weight(1f), fontSize = 16.sp) {
                            onKey(insertValue)
                        }
                    }
                }
            }
        } else {
            // Greek Mode Matrix (30 Keys)
            val rows = listOf(
                listOf("α", "β", "γ", "δ", "ε"),
                listOf("θ", "λ", "μ", "ν", "ξ"),
                listOf("π", "ρ", "σ", "τ", "φ"),
                listOf("χ", "ψ", "ω", "Δ", "Ω"),
                listOf("(", ")", "+", "-", "="),
                listOf("0", "1", "2", "x", "y")
            )
            rows.forEach { rowKeys ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowKeys.forEach { key ->
                        val isEquals = key == "="
                        val insertValue = if (isEquals) " = " else key
                        val container = if (isEquals) MathSolverTheme.surfaceVariant else MathSolverTheme.functionContainer
                        val content = MathSolverTheme.accentAlphabet
                        SquircleKey(key, container, content, btnHeight, Modifier.weight(1f), fontSize = 16.sp) {
                            onKey(insertValue)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. PERSISTENT BOTTOM EDIT BAR (6 Keys)
// =========================================================================

@Composable
fun PersistentBottomEditBar(
    activeTab: MathSolverTab,
    activeFormatLabel: String,
    btnHeight: Dp,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onCycleFormat: () -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic Accent Color for the Execute Pill
    val executionBgColor by animateColorAsState(
        targetValue = activeTab.accentColor,
        animationSpec = tween(durationMillis = 250),
        label = "ExecutionPillColorAnim"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [ ← ] Cursor Left
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .background(MathSolverTheme.operatorContainer, RoundedCornerShape(14.dp))
                .clickable { onMoveCursorLeft() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursore a sinistra",
                tint = MathSolverTheme.operatorContent,
                modifier = Modifier.size(20.dp)
            )
        }

        // [ → ] Cursor Right
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .background(MathSolverTheme.operatorContainer, RoundedCornerShape(14.dp))
                .clickable { onMoveCursorRight() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursore a destra",
                tint = MathSolverTheme.operatorContent,
                modifier = Modifier.size(20.dp)
            )
        }

        // [ ⌫ DEL ] Backspace
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .background(MathSolverTheme.deleteContainer, RoundedCornerShape(14.dp))
                .clickable { onDeleteChar() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DEL",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MathSolverTheme.deleteContent
            )
        }

        // [ AC ] All Clear
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .background(MathSolverTheme.clearContainer, RoundedCornerShape(14.dp))
                .clickable { onClearAll() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AC",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MathSolverTheme.clearContent
            )
        }

        // [ FORMAT ] Instant Result Formatter
        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(btnHeight)
                .background(MathSolverTheme.formatContainer, RoundedCornerShape(14.dp))
                .border(1.dp, MathSolverTheme.formatBorder, RoundedCornerShape(14.dp))
                .clickable { onCycleFormat() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FORMAT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MathSolverTheme.tabUnselected
                )
                Text(
                    text = activeFormatLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MathSolverTheme.formatLabel
                )
            }
        }

        // [ ➤ ] Execution Pill (Prominent Call to Action with Dynamic Color)
        Box(
            modifier = Modifier
                .weight(1.4f)
                .height(btnHeight)
                .background(executionBgColor, RoundedCornerShape(22.dp))
                .clickable { onExecute() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "➤",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

// =========================================================================
// REUSABLE SQUIRCLE BUTTON
// =========================================================================

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
    Box(
        modifier = modifier
            .height(height)
            .background(bgColor, RoundedCornerShape(14.dp))
            .clickable { onClick() },
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
