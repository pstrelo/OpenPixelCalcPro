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
import com.google.pixel.calculator.ui.theme.CalculatorTheme

/**
 * Keypad Material 3 Expressive per Google Math Solver (6 Colonne x 6 Righe + Barra Inferiore di Risoluzione).
 * Row 1 comune: [ ← ] [ → ] [ RAD/DEG ] [ DEL ] [ AC ] [ = ]
 * Esponenti con segnaposto: [ □ⁿ ], [ □² ]
 * Tasto f(x) collegato all'apertura del catalogo funzioni scientifiche complete.
 * Integrazione cromatica dinamica con CalculatorTheme.colors mantenendo gli accenti per ciascuna tab.
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
                onOpenCatalog = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenCatalog()
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
                onOpenCatalog = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenCatalog()
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
                onOpenCatalog = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenCatalog()
                },
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
            MathSolverTab.ALPHABET -> AlphabetKeypadGrid(
                btnHeight = buttonHeight,
                isRadians = isRadians,
                onToggleRadians = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleRadians()
                },
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
                onKey = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsertChar(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Barra persistente inferiore ([ FORMAT: DEC ], [ RISOLVI ➤ ])
        PersistentBottomEditBar(
            activeTab = activeTab,
            activeFormatLabel = activeFormatLabel,
            btnHeight = buttonHeight,
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

// Overload basato su stato completo
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
        modifier = modifier
    )
}

// Overload legacy retrocompatibile
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
// ROW 1 COMUNE: [ ← ] [ → ] [ RAD/DEG ] [ DEL ] [ AC ] [ = ]
// =========================================================================

@Composable
fun NavigationAndControlRow(
    btnHeight: Dp,
    isRadians: Boolean,
    tabAccentColor: Color,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onToggleRadians: () -> Unit,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onInsertChar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val navBg = if (isDark) MathSolverTheme.operatorContainer else CalculatorTheme.colors.operatorContainer
    val navColor = if (isDark) MathSolverTheme.operatorContent else CalculatorTheme.colors.operatorContent

    val delBg = if (isDark) MathSolverTheme.deleteContainer else Color(0xFFFFE4E6)
    val delColor = if (isDark) MathSolverTheme.deleteContent else Color(0xFFE11D48)

    val clearBg = if (isDark) MathSolverTheme.clearContainer else Color(0xFFFEE2E2)
    val clearColor = if (isDark) MathSolverTheme.clearContent else Color(0xFFDC2626)

    val eqBg = if (isDark) MathSolverTheme.surfaceVariant else CalculatorTheme.colors.operatorContainer
    val eqColor = tabAccentColor

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. [ ← ] Sposta cursore a sinistra
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(navBg)
                .clickable { onMoveCursorLeft() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursore a sinistra",
                tint = navColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // 2. [ → ] Sposta cursore a destra
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(navBg)
                .clickable { onMoveCursorRight() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursore a destra",
                tint = navColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // 3. [ RAD / DEG ] Tasto cambio unità angolare incorporato in tastiera
        DegRadKey(
            isRadians = isRadians,
            btnHeight = btnHeight,
            modifier = Modifier.weight(1f),
            accentColor = tabAccentColor,
            onToggle = onToggleRadians
        )

        // 4. [ DEL ] Cancella carattere
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(delBg)
                .clickable { onDeleteChar() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DEL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = delColor
            )
        }

        // 5. [ AC ] Pulisce tutto
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(clearBg)
                .clickable { onClearAll() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AC",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = clearColor
            )
        }

        // 6. [ = ] Inserisce simbolo di uguaglianza per equazioni
        SquircleKey(
            label = "=",
            bgColor = eqBg,
            textColor = eqColor,
            height = btnHeight,
            modifier = Modifier.weight(1f),
            isBold = true,
            fontSize = 18.sp
        ) {
            onInsertChar(" = ")
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
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onDeleteChar: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onOpenCatalog: () -> Unit = {},
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ ← ], [ → ], [ RAD/DEG ], [ DEL ], [ AC ], [ = ]
        NavigationAndControlRow(
            btnHeight = btnHeight,
            isRadians = isRadians,
            tabAccentColor = tabAccent,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onToggleRadians = onToggleRadians,
            onDeleteChar = onDeleteChar,
            onClearAll = onClearAll,
            onInsertChar = onKey
        )

        // Row 2: [ □/□ ], [ |x| ], [ √□ ], [ ⁿ√□ ], [ log ], [ ln ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("ⁿ√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ʸ√") }
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
        }

        // Row 3: [ □ⁿ ], [ □² ], [ 7 ], [ 8 ], [ 9 ], [ ÷ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }

        // Row 4: [ f(x) ], [ n! ], [ 4 ], [ 5 ], [ 6 ], [ × ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey(
                label = "f(x)",
                bgColor = if (isDark) tabAccent.copy(alpha = 0.22f) else tabAccent.copy(alpha = 0.15f),
                textColor = tabAccent,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                isBold = true,
                fontSize = 13.sp
            ) {
                onOpenCatalog()
            }
            SquircleKey("n!", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("!") }
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }

        // Row 5: [ ( ], [ ) ], [ 1 ], [ 2 ], [ 3 ], [ - ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }

        // Row 6: [ x ], [ y ], [ 0 ], [ . ], [ % ], [ + ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
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
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onDeleteChar: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onOpenCatalog: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.TRIGONOMETRY.accentColor
    val fnBg = if (isDark) MathSolverTheme.trigFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.trigFunctionContent else Color(0xFF047857)
    val numBg = if (isDark) MathSolverTheme.numericContainer else CalculatorTheme.colors.numericContainer
    val numColor = if (isDark) MathSolverTheme.numericContent else CalculatorTheme.colors.numericContent
    val opBg = if (isDark) MathSolverTheme.operatorContainer else CalculatorTheme.colors.operatorContainer
    val opColor = if (isDark) MathSolverTheme.operatorContent else CalculatorTheme.colors.operatorContent

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ ← ], [ → ], [ RAD/DEG ], [ DEL ], [ AC ], [ = ]
        NavigationAndControlRow(
            btnHeight = btnHeight,
            isRadians = isRadians,
            tabAccentColor = tabAccent,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onToggleRadians = onToggleRadians,
            onDeleteChar = onDeleteChar,
            onClearAll = onClearAll,
            onInsertChar = onKey
        )

        // Row 2: [ sin ], [ cos ], [ tan ], [ arcsin ], [ arccos ], [ arctan ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sin", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sin(") }
            SquircleKey("cos", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cos(") }
            SquircleKey("tan", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("tan(") }
            SquircleKey("arcsin", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("arccos", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccos(") }
            SquircleKey("arctan", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arctan(") }
        }

        // Row 3: [ □ⁿ ], [ □² ], [ 7 ], [ 8 ], [ 9 ], [ ÷ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }

        // Row 4: [ f(x) ], [ π ], [ 4 ], [ 5 ], [ 6 ], [ × ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey(
                label = "f(x)",
                bgColor = if (isDark) tabAccent.copy(alpha = 0.22f) else tabAccent.copy(alpha = 0.15f),
                textColor = tabAccent,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                isBold = true,
                fontSize = 13.sp
            ) {
                onOpenCatalog()
            }
            SquircleKey("π", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("π") }
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }

        // Row 5: [ ( ], [ ) ], [ 1 ], [ 2 ], [ 3 ], [ - ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }

        // Row 6: [ x ], [ y ], [ 0 ], [ . ], [ % ], [ + ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
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
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onDeleteChar: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onOpenCatalog: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.CALCULUS.accentColor
    val fnBg = if (isDark) MathSolverTheme.calculusFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.calculusFunctionContent else Color(0xFFBE185D)
    val numBg = if (isDark) MathSolverTheme.numericContainer else CalculatorTheme.colors.numericContainer
    val numColor = if (isDark) MathSolverTheme.numericContent else CalculatorTheme.colors.numericContent
    val opBg = if (isDark) MathSolverTheme.operatorContainer else CalculatorTheme.colors.operatorContainer
    val opColor = if (isDark) MathSolverTheme.operatorContent else CalculatorTheme.colors.operatorContent

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ ← ], [ → ], [ RAD/DEG ], [ DEL ], [ AC ], [ = ]
        NavigationAndControlRow(
            btnHeight = btnHeight,
            isRadians = isRadians,
            tabAccentColor = tabAccent,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onToggleRadians = onToggleRadians,
            onDeleteChar = onDeleteChar,
            onClearAll = onClearAll,
            onInsertChar = onKey
        )

        // Row 2: [ d/dx ], [ ∫ ], [ lim ], [ Σ ], [ ∞ ], [ □/□ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("d/dx", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 13.sp) { onKey("d/dx(") }
            SquircleKey("∫", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("lim", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("lim(") }
            SquircleKey("Σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("∞", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
        }

        // Row 3: [ □ⁿ ], [ □² ], [ 7 ], [ 8 ], [ 9 ], [ ÷ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("÷", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("/") }
        }

        // Row 4: [ f(x) ], [ ln ], [ 4 ], [ 5 ], [ 6 ], [ × ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey(
                label = "f(x)",
                bgColor = if (isDark) tabAccent.copy(alpha = 0.22f) else tabAccent.copy(alpha = 0.15f),
                textColor = tabAccent,
                height = btnHeight,
                modifier = Modifier.weight(1f),
                isBold = true,
                fontSize = 13.sp
            ) {
                onOpenCatalog()
            }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }

        // Row 5: [ ( ], [ ) ], [ 1 ], [ 2 ], [ 3 ], [ - ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }

        // Row 6: [ x ], [ y ], [ 0 ], [ . ], [ % ], [ + ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f)) { onKey("%") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }
    }
}

// =========================================================================
// 4. ALPHABET KEYPAD GRID (6 Colonne x 6 Righe)
// =========================================================================

@Composable
private fun AlphabetKeypadGrid(
    btnHeight: Dp,
    isRadians: Boolean = true,
    onToggleRadians: () -> Unit = {},
    onMoveCursorLeft: () -> Unit = {},
    onMoveCursorRight: () -> Unit = {},
    onDeleteChar: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.ALPHABET.accentColor
    val fnBg = if (isDark) MathSolverTheme.alphabetFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.alphabetFunctionContent else Color(0xFFC2410C)

    // 5 righe da 6 lettere latine e greche = 30 simboli
    val alphabetRows = listOf(
        listOf("a", "b", "c", "d", "e", "f"),
        listOf("g", "h", "i", "j", "k", "l"),
        listOf("m", "n", "o", "p", "q", "r"),
        listOf("s", "t", "u", "v", "w", "x"),
        listOf("y", "z", "α", "β", "π", "θ")
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ ← ], [ → ], [ RAD/DEG ], [ DEL ], [ AC ], [ = ]
        NavigationAndControlRow(
            btnHeight = btnHeight,
            isRadians = isRadians,
            tabAccentColor = tabAccent,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onToggleRadians = onToggleRadians,
            onDeleteChar = onDeleteChar,
            onClearAll = onClearAll,
            onInsertChar = onKey
        )

        // Rows 2..6: Lettere e caratteri
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
// 5. PERSISTENT BOTTOM EDIT BAR ([ FORMAT: DEC ], [ RISOLVI ➤ ])
// =========================================================================

@Composable
fun PersistentBottomEditBar(
    activeTab: MathSolverTab,
    activeFormatLabel: String,
    btnHeight: Dp,
    onCycleFormat: () -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val executionBgColor by animateColorAsState(
        targetValue = activeTab.accentColor,
        animationSpec = tween(durationMillis = 250),
        label = "ExecutionPillColorAnim"
    )
    val isDark = CalculatorTheme.colors.isDark
    val formatContainer = if (isDark) MathSolverTheme.formatContainer else CalculatorTheme.colors.operatorContainer
    val formatBorder = if (isDark) MathSolverTheme.formatBorder else CalculatorTheme.colors.operatorContainer
    val formatLabelColor = if (isDark) MathSolverTheme.formatLabel else activeTab.accentColor
    val formatSubtextColor = if (isDark) MathSolverTheme.tabUnselected else CalculatorTheme.colors.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [ FORMAT ] Tasto per ciclare formato risultato (DEC, FRAC, ENG, DMS, POL)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(formatContainer)
                .border(1.dp, formatBorder, RoundedCornerShape(percent = 50))
                .clickable { onCycleFormat() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "FORMAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = formatSubtextColor
                )
                Text(
                    text = activeFormatLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = formatLabelColor
                )
            }
        }

        // [ RISOLVI ➤ ] Pulsante pillola di esecuzione / calcolo prominente con accento della tab attiva
        Box(
            modifier = Modifier
                .weight(1.8f)
                .height(btnHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(executionBgColor)
                .clickable { onExecute() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RISOLVI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Calcola ed Esegui",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
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
    val isDark = CalculatorTheme.colors.isDark
    val isDegActive = !isRadians
    val defaultBg = if (isDark) MathSolverTheme.functionContainer else CalculatorTheme.colors.operatorContainer
    val defaultText = if (isDark) MathSolverTheme.functionContent else CalculatorTheme.colors.operatorContent

    val bgColor = if (isDegActive) accentColor.copy(alpha = if (isDark) 0.25f else 0.2f) else defaultBg
    val textColor = if (isDegActive) accentColor else defaultText
    val borderColor = if (isDegActive) accentColor else Color.Transparent

    Box(
        modifier = modifier
            .height(btnHeight)
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
            .border(if (isDegActive) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(percent = 50))
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
