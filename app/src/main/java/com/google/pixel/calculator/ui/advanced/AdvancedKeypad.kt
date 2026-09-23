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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Keypad Material 3 Expressive per Google Math Solver (6 Colonne x 6 Righe + Barra Inferiore di Editing).
 * Riproduce fedelmente la disposizione e l'aspetto di Google Math Solver.
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
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val buttonHeight = if (isSplitScreen) 37.dp else 42.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
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

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Griglia tasti 6 Colonne x 6 Righe della Tab attiva
        when (activeTab) {
            MathSolverTab.ALGEBRA -> AlgebraKeypadGrid(
                btnHeight = buttonHeight,
                isRadians = isRadians,
                onToggleRadians = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleRadians()
                },
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.TRIGONOMETRY -> TrigonometryKeypadGrid(
                btnHeight = buttonHeight,
                isRadians = isRadians,
                onToggleRadians = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleRadians()
                },
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.CALCULUS -> CalculusKeypadGrid(
                btnHeight = buttonHeight,
                isRadians = isRadians,
                onToggleRadians = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleRadians()
                },
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

        Spacer(modifier = Modifier.height(2.dp))

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

// Overload basato su stato
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
        isRadians = state.isRadians,
        onToggleRadians = { onKeyClick("DEG") },
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
        containerColor = Color.Transparent,
        contentColor = Color.White,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                height = 3.dp,
                color = selectedTab.accentColor
            )
        },
        divider = {
            HorizontalDivider(color = Color(0xFF2E3448), thickness = 1.dp)
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
// 1. ALGEBRA KEYPAD GRID (6 Colonne x 6 Righe)
// =========================================================================

@Composable
private fun AlgebraKeypadGrid(
    btnHeight: Dp,
    isRadians: Boolean = true,
    onToggleRadians: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val fnBg = MathSolverTheme.algebraFunctionContainer
    val fnColor = MathSolverTheme.algebraFunctionContent
    val numBg = MathSolverTheme.numericContainer
    val numColor = MathSolverTheme.numericContent
    val opBg = MathSolverTheme.operatorContainer
    val opColor = MathSolverTheme.operatorContent

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [□/□], [|x|], [n!], [log], [ln], [ = ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
            SquircleKey("n!", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("!") }
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey(" = ") }
        }
        // Row 2: [√□], [ⁿ√□], [<], [≤], [>], [≥]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("ⁿ√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ʸ√") }
            SquircleKey("<", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(" < ") }
            SquircleKey("≤", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(" <= ") }
            SquircleKey(">", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(" > ") }
            SquircleKey("≥", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(" >= ") }
        }
        // Row 3: [xⁿ], [x²], [7], [8], [9], [÷]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("xⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("x²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }
        // Row 4: [f(x)], [DEG/RAD], [4], [5], [6], [×]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("f(x)", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("f(x)") }
            DegRadKey(
                isRadians = isRadians,
                btnHeight = btnHeight,
                modifier = Modifier.weight(1f),
                accentColor = MathSolverTheme.accentAlgebra,
                onToggle = onToggleRadians
            )
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }
        // Row 5: [(], [)], [1], [2], [3], [-]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }
        // Row 6: [x], [y], [0], [.], [%], [+]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, MathSolverTheme.accentAlgebra, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f)) { onKey("%") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (6 Colonne x 6 Righe)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    btnHeight: Dp,
    isRadians: Boolean = true,
    onToggleRadians: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val fnBg = MathSolverTheme.trigFunctionContainer
    val fnColor = MathSolverTheme.trigFunctionContent
    val numBg = MathSolverTheme.numericContainer
    val numColor = MathSolverTheme.numericContent
    val opBg = MathSolverTheme.operatorContainer
    val opColor = MathSolverTheme.operatorContent

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [□/□], [sin], [cos], [tan], [arcsin], [ = ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("sin", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sin(") }
            SquircleKey("cos", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cos(") }
            SquircleKey("tan", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("tan(") }
            SquircleKey("arcsin", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey(" = ") }
        }
        // Row 2: [√□], [csc], [sec], [cot], [arctan], [arccos]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("csc", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("csc(") }
            SquircleKey("sec", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sec(") }
            SquircleKey("cot", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("cot(") }
            SquircleKey("arctan", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arctan(") }
            SquircleKey("arccos", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccos(") }
        }
        // Row 3: [xⁿ], [x²], [7], [8], [9], [÷]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("xⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("x²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }
        // Row 4: [π], [DEG/RAD], [4], [5], [6], [×]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("π", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("π") }
            DegRadKey(
                isRadians = isRadians,
                btnHeight = btnHeight,
                modifier = Modifier.weight(1f),
                accentColor = MathSolverTheme.accentTrigonometry,
                onToggle = onToggleRadians
            )
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }
        // Row 5: [(], [)], [1], [2], [3], [-]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }
        // Row 6: [x], [y], [0], [.], [%], [+]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, MathSolverTheme.accentTrigonometry, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f)) { onKey("%") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (6 Colonne x 6 Righe)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    btnHeight: Dp,
    isRadians: Boolean = true,
    onToggleRadians: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val fnBg = MathSolverTheme.calculusFunctionContainer
    val fnColor = MathSolverTheme.calculusFunctionContent
    val numBg = MathSolverTheme.numericContainer
    val numColor = MathSolverTheme.numericContent
    val opBg = MathSolverTheme.operatorContainer
    val opColor = MathSolverTheme.operatorContent

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [d/dx], [C(n,k)], [P(n,k)], [ln], [log], [ = ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("d/dx", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 13.sp) { onKey("d/dx(") }
            SquircleKey("C(n,k)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("C(") }
            SquircleKey("P(n,k)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("P(") }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("=", MathSolverTheme.surfaceVariant, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey(" = ") }
        }
        // Row 2: [ⁿ√□], [∫], [∫_a^b], [lim], [lim+], [lim-]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("ⁿ√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ʸ√") }
            SquircleKey("∫", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("∫ₐᵇ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 13.sp) { onKey("∫(") }
            SquircleKey("lim", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("lim(") }
            SquircleKey("lim⁺", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("lim(") }
            SquircleKey("lim⁻", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("lim(") }
        }
        // Row 3: [Σ], [∞], [7], [8], [9], [÷]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("Σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("∞", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }
        // Row 4: [f(x)], [DEG/RAD], [4], [5], [6], [×]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("f(x)", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("f(x)") }
            DegRadKey(
                isRadians = isRadians,
                btnHeight = btnHeight,
                modifier = Modifier.weight(1f),
                accentColor = MathSolverTheme.accentCalculus,
                onToggle = onToggleRadians
            )
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }
        // Row 5: [(], [)], [1], [2], [3], [-]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }
        // Row 6: [x], [y], [0], [.], [%], [+]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, MathSolverTheme.accentCalculus, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f)) { onKey("%") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }
    }
}

// =========================================================================
// 4. ALPHABET KEYPAD GRID (6 Colonne x 6 Righe - Latin / Greek)
// =========================================================================

@Composable
private fun AlphabetKeypadGrid(
    btnHeight: Dp,
    onKey: (String) -> Unit
) {
    val fnBg = MathSolverTheme.alphabetFunctionContainer
    val fnColor = MathSolverTheme.alphabetFunctionContent

    // Disposizione esatta 6x6 come in media_1790089593416.png
    val alphabetRows = listOf(
        listOf("a", "b", "c", "d", "e", "f"),
        listOf("g", "h", "i", "j", "k", "l"),
        listOf("m", "n", "o", "p", "q", "r"),
        listOf("s", "t", "u", "v", "w", "x"),
        listOf("y", "z", "α", "β", "γ", "ε"),
        listOf("θ", "λ", "μ", "π", "ρ", "φ")
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        alphabetRows.forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowKeys.forEach { key ->
                    SquircleKey(
                        label = key,
                        bgColor = fnBg,
                        textColor = fnColor,
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp
                    ) {
                        onKey(key)
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
        // 1. [ ← ] Cursor Left
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MathSolverTheme.operatorContainer)
                .clickable { onMoveCursorLeft() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursore a sinistra",
                tint = MathSolverTheme.operatorContent,
                modifier = Modifier.size(19.dp)
            )
        }

        // 2. [ → ] Cursor Right
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MathSolverTheme.operatorContainer)
                .clickable { onMoveCursorRight() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursore a destra",
                tint = MathSolverTheme.operatorContent,
                modifier = Modifier.size(19.dp)
            )
        }

        // 3. [ ⌫ DEL ] Backspace
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MathSolverTheme.deleteContainer)
                .clickable { onDeleteChar() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MathSolverTheme.deleteContent
            )
        }

        // 4. [ AC ] All Clear
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MathSolverTheme.clearContainer)
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

        // 5. [ FORMAT ] Instant Result Formatter (DEC, FRAC, ENG, DMS, POL)
        Box(
            modifier = Modifier
                .weight(1.1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(MathSolverTheme.formatContainer)
                .border(1.dp, MathSolverTheme.formatBorder, RoundedCornerShape(percent = 50))
                .clickable { onCycleFormat() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FORMAT",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MathSolverTheme.tabUnselected
                )
                Text(
                    text = activeFormatLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MathSolverTheme.formatLabel
                )
            }
        }

        // 6. [ ➤ ] Execution Send Pill (Prominent Pill with Send Icon in Active Tab Accent Color)
        Box(
            modifier = Modifier
                .weight(1.3f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(executionBgColor)
                .clickable { onExecute() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Calcola ed Esegui",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =========================================================================
// REUSABLE SQUIRCLE / PILL BUTTON
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
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
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

/**
 * Tasto DEG / RAD incorporato direttamente nel tastierino matematico.
 * Si illumina e si contorna quando la modalità DEG è attiva.
 */
@Composable
fun DegRadKey(
    isRadians: Boolean,
    btnHeight: Dp,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF10B981),
    onToggle: () -> Unit
) {
    val isDegActive = !isRadians
    val bgColor = if (isDegActive) accentColor.copy(alpha = 0.22f) else MathSolverTheme.functionContainer
    val textColor = if (isDegActive) accentColor else MathSolverTheme.functionContent
    val borderColor = if (isDegActive) accentColor.copy(alpha = 0.8f) else Color.Transparent

    Box(
        modifier = modifier
            .height(btnHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
            .border(if (isDegActive) 1.dp else 0.dp, borderColor, RoundedCornerShape(percent = 50))
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRadians) "RAD" else "DEG",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
