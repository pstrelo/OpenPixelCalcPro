package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.toolbox.PixelToolPillBar
import com.google.pixel.calculator.ui.toolbox.PixelToolToggleButton
import com.google.pixel.calculator.ui.toolbox.rememberToolBarDimensions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Backspace
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
 * - Tab 1 (Algebra): Tasti di navigazione cursore (←, →), RAD/DEG, DEL, AC, =, tastierino numerico completo e operazioni base.
 * - Tab 2 (Trigonometria): Tutte le funzioni goniometriche dirette, inverse, iperboliche, inverse iperboliche, unità d'angolo e costanti.
 * - Tab 3 (Calcolo & Probabilità): Derivate, integrali, limiti, serie, coefficiente binomiale, distribuzioni (binompdf/cdf, normpdf/cdf), statistica (mean, var, std) e random.
 * - Tab 4 (Alfabeto & Costanti): Selettore a 3 schede per Lettere greche/latine, Costanti fisiche/matematiche (c, G, h, ħ, k_B, N_A, R, q_e, m_e...) e Prefissi metrici SI (k, M, G, T, m, μ, n, p...).
 * - Barra persistente inferiore: [ FORMAT: DEC/ENG/FRAC... ] e [ RISOLVI ➤ ].
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
    val buttonHeight = if (isLandscape) 30.dp else if (isSplitScreen) 36.dp else 40.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. Barra orizzontale a 4 Tab tematiche (Algebra, Trigonometry, Calculus, Alphabet)
        MathSolverTabBar(
            selectedTab = activeTab,
            onTabSelected = { tab ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTabSelected(tab)
            },
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Griglia tasti della Tab attiva
        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
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
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.TRIGONOMETRY -> TrigonometryKeypadGrid(
                    btnHeight = buttonHeight,
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.CALCULUS -> CalculusKeypadGrid(
                    btnHeight = buttonHeight,
                    onDeleteChar = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteChar()
                    },
                    onKey = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertChar(it)
                    }
                )
                MathSolverTab.ALPHABET -> AlphabetKeypadGrid(
                    btnHeight = if (isSplitScreen) 32.dp else 36.dp,
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
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Barra persistente inferiore ([ FORMAT: DEC ], [ RISOLVI ➤ ]) con spazio dedicato per il tasto strumenti
        PersistentBottomEditBar(
            activeTab = activeTab,
            activeFormatLabel = activeFormatLabel,
            onCycleFormat = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCycleFormat()
            },
            onExecute = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onExecute()
            },
            isToolBarExpanded = isToolBarExpanded,
            onToggleToolBar = onToggleToolBar,
            onNavigate = onNavigate
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
// ROW 1 COMUNE PER ALGEBRA: [ ← ] [ → ] [ RAD/DEG ] [ DEL ] [ AC ] [ = ]
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

        // Row 4: [ < ], [ > ], [ 4 ], [ 5 ], [ 6 ], [ × ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("<", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey(" < ") }
            SquircleKey(">", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey(" > ") }
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

        // Row 6: [ x ], [ π ], [ 0 ], [ . ], [ % ], [ + ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("π", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f)) { onKey("%") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (6 Colonne x 6 Righe - 100% Funzioni)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    btnHeight: Dp,
    onDeleteChar: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.TRIGONOMETRY.accentColor
    val fnBg = if (isDark) MathSolverTheme.trigFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.trigFunctionContent else Color(0xFF047857)
    val delBg = if (isDark) MathSolverTheme.deleteContainer else Color(0xFFFFE4E6)
    val delColor = if (isDark) MathSolverTheme.deleteContent else Color(0xFFE11D48)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: Funzioni Goniometriche Dirette e Reciproche
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sin", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sin(") }
            SquircleKey("cos", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cos(") }
            SquircleKey("tan", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("tan(") }
            SquircleKey("csc", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("csc(") }
            SquircleKey("sec", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sec(") }
            SquircleKey("cot", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cot(") }
        }

        // Row 2: Funzioni Goniometriche Inverse
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("arcsin", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("arccos", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccos(") }
            SquircleKey("arctan", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arctan(") }
            SquircleKey("arccsc", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccsc(") }
            SquircleKey("arcsec", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsec(") }
            SquircleKey("arccot", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccot(") }
        }

        // Row 3: Funzioni Iperboliche Dirette e Reciproche
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sinh", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sinh(") }
            SquircleKey("cosh", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cosh(") }
            SquircleKey("tanh", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("tanh(") }
            SquircleKey("csch", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("csch(") }
            SquircleKey("sech", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sech(") }
            SquircleKey("coth", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("coth(") }
        }

        // Row 4: Funzioni Iperboliche Inverse
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("asinh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("asinh(") }
            SquircleKey("acosh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("acosh(") }
            SquircleKey("atanh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("atanh(") }
            SquircleKey("acsch", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("acsch(") }
            SquircleKey("asech", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("asech(") }
            SquircleKey("acoth", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("acoth(") }
        }

        // Row 5: Unità d'Angolo, Costanti & Potenze
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("π", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("π") }
            SquircleKey("°", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("°") }
            SquircleKey("rad", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("*(1)") }
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
        }

        // Row 6: Variabili, Parentesi & Cancellazione
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("e", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("θ", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("x") }
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(delBg)
                    .clickable { onDeleteChar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Cancella carattere",
                    tint = delColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (6 Colonne x 6 Righe - 100% Funzioni)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    btnHeight: Dp,
    onDeleteChar: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.CALCULUS.accentColor
    val fnBg = if (isDark) MathSolverTheme.calculusFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.calculusFunctionContent else Color(0xFFBE185D)
    val delBg = if (isDark) MathSolverTheme.deleteContainer else Color(0xFFFFE4E6)
    val delColor = if (isDark) MathSolverTheme.deleteContent else Color(0xFFE11D48)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: Derivate, Integrali, Limiti, Somme, Produttorie
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("d/dx", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onKey("d/dx(") }
            SquircleKey("∫", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("∫ₐᵇ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 13.sp) { onKey("defint(") }
            SquircleKey("lim", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 13.sp) { onKey("lim(") }
            SquircleKey("Σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("Π", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("prod(") }
        }

        // Row 2: Costanti, Logaritmi & Potenze
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("∞", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("e") }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
        }

        // Row 3: Combinatoria & Probabilità (Binomiale nCk, Permutazioni nPk, Fattoriale, Random, Gamma)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("binom", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("binom(") }
            SquircleKey("nPr", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onKey("perm(") }
            SquircleKey("n!", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 14.sp) { onKey("!") }
            SquircleKey("rand", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("rand()") }
            SquircleKey("randInt", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("randint(") }
            SquircleKey("Γ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 15.sp) { onKey("gamma(") }
        }

        // Row 4: Distribuzioni Statistiche (Binomiale & Normale PDF/CDF), Frazione & Valore Assoluto
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("binompdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("binompdf(") }
            SquircleKey("binomcdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("binomcdf(") }
            SquircleKey("normpdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("normpdf(") }
            SquircleKey("normcdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("normcdf(") }
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
        }

        // Row 5: Statistica Descrittiva (Media, Varianza, Deviazione Standard, Mediana) & Relazioni
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("mean", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("mean(") }
            SquircleKey("var", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("var(") }
            SquircleKey("std", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("std(") }
            SquircleKey("median", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("median(") }
            SquircleKey("<", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey(" < ") }
            SquircleKey(">", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey(" > ") }
        }

        // Row 6: Variabili, Separatore Argomenti (,), Parentesi & Cancellazione
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("t", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("t") }
            SquircleKey(",", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey(", ") }
            SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
            SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(btnHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(delBg)
                    .clickable { onDeleteChar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Cancella carattere",
                    tint = delColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// =========================================================================
// 4. ALPHABET, CONSTANTS & SI PREFIXES KEYPAD GRID
// =========================================================================

enum class AlphabetSubTab(val label: String) {
    LETTERS("Lettere"),
    CONSTANTS("Costanti"),
    SI_PREFIXES("Prefissi SI")
}

@Composable
private fun AlphabetKeypadGrid(
    btnHeight: Dp,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onKey: (String) -> Unit
) {
    var subTab by remember { mutableStateOf(AlphabetSubTab.LETTERS) }
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.ALPHABET.accentColor
    val fnBg = if (isDark) MathSolverTheme.alphabetFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.alphabetFunctionContent else Color(0xFFC2410C)
    val delBg = if (isDark) MathSolverTheme.deleteContainer else Color(0xFFFFE4E6)
    val delColor = if (isDark) MathSolverTheme.deleteContent else Color(0xFFE11D48)
    val clearBg = if (isDark) MathSolverTheme.clearContainer else Color(0xFFFEE2E2)
    val clearColor = if (isDark) MathSolverTheme.clearContent else Color(0xFFDC2626)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Selettore a 3 schede per sotto-sezioni (Lettere, Costanti, Prefissi SI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AlphabetSubTab.values().forEach { tab ->
                val isSelected = tab == subTab
                val chipBg = if (isSelected) tabAccent else if (isDark) Color(0xFF262A38) else Color(0xFFF1F5F9)
                val chipTextColor = if (isSelected) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(chipBg)
                        .clickable { subTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = chipTextColor
                    )
                }
            }
        }

        when (subTab) {
            AlphabetSubTab.LETTERS -> {
                // Lettere latine e greche complete (30 simboli + Backspace)
                val letterRows = listOf(
                    listOf("a", "b", "c", "d", "e", "f"),
                    listOf("g", "h", "i", "j", "k", "l"),
                    listOf("m", "n", "o", "p", "q", "r"),
                    listOf("s", "t", "u", "v", "w", "x"),
                    listOf("y", "z", "α", "β", "θ", "λ")
                )
                letterRows.forEach { rowKeys ->
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
                // Riga 6 lettere greche avanzate e cancellazione
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("μ", "π", "φ", "ω", "Δ").forEach { key ->
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(delBg)
                            .clickable { onDeleteChar() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Cancella carattere",
                            tint = delColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AlphabetSubTab.CONSTANTS -> {
                // Costanti Fisiche e Matematiche universali
                // Row 1: Velocità della luce, Gravitazione, Gravità terrestre, Costante di Planck, ħ, Boltzmann
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("c", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("c") }
                    SquircleKey("G", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("G") }
                    SquircleKey("g", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("g") }
                    SquircleKey("h", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("h") }
                    SquircleKey("ħ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("ħ") }
                    SquircleKey("k_B", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("k_B") }
                }
                // Row 2: Numero di Avogadro, Costante dei Gas, Carica elettrone, Massa elettrone, Massa protone, Unità massa atomica
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("N_A", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("N_A") }
                    SquircleKey("R", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("R") }
                    SquircleKey("q_e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("q_e") }
                    SquircleKey("m_e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("m_e") }
                    SquircleKey("m_p", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("m_p") }
                    SquircleKey("u", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("u") }
                }
                // Row 3: Costanti Dielettriche, Stefan-Boltzmann, Sezione Aurea, Eulero, Pi Greco
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("ε₀", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("ε₀") }
                    SquircleKey("μ₀", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("μ₀") }
                    SquircleKey("σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("σ") }
                    SquircleKey("φ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("φ") }
                    SquircleKey("e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("e") }
                    SquircleKey("π", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("π") }
                }
                // Row 4: Costanti Matematiche Frequenti & Potenze
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("√2", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sqrt(2)") }
                    SquircleKey("√3", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sqrt(3)") }
                    SquircleKey("ln2", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("ln(2)") }
                    SquircleKey("ln10", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("ln(10)") }
                    SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
                    SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
                }
                // Row 5: Operatori e Parentesi
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
                    SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
                    SquircleKey("×", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("*") }
                    SquircleKey("÷", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("/") }
                    SquircleKey("+", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("+") }
                    SquircleKey("-", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("-") }
                }
                // Row 6: Variabili, Uguale & Cancellazione
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("x") }
                    SquircleKey("e", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("e") }
                    SquircleKey("=", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" = ") }
                    SquircleKey("DEL", delBg, delColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onDeleteChar() }
                    SquircleKey("AC", clearBg, clearColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onClearAll() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(delBg)
                            .clickable { onDeleteChar() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Cancella carattere",
                            tint = delColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AlphabetSubTab.SI_PREFIXES -> {
                // Prefissi Metrici del Sistema Internazionale (inserimento moltiplicatore scientifico immediato)
                // Row 1: Grandi Multipli: Tera (10¹²), Giga (10⁹), Mega (10⁶), kilo (10³), etto (10²), deca (10¹)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("T (10¹²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^12") }
                    SquircleKey("G (10⁹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("*10^9") }
                    SquircleKey("M (10⁶)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("*10^6") }
                    SquircleKey("k (10³)", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 10.sp) { onKey("*10^3") }
                    SquircleKey("h (10²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("*10^2") }
                    SquircleKey("da (10¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^1") }
                }
                // Row 2: Sottomultipli: deci (10⁻¹), centi (10⁻²), milli (10⁻³), micro (10⁻⁶), nano (10⁻⁹), pico (10⁻¹²)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("d (10⁻¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^-1") }
                    SquircleKey("c (10⁻²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^-2") }
                    SquircleKey("m (10⁻³)", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 9.sp) { onKey("*10^-3") }
                    SquircleKey("μ (10⁻⁶)", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 9.sp) { onKey("*10^-6") }
                    SquircleKey("n (10⁻⁹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^-9") }
                    SquircleKey("p (10⁻¹²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*10^-12") }
                }
                // Row 3: Estremi & Notazione Scientifica: femto (10⁻¹⁵), atto (10⁻¹⁸), Peta (10¹⁵), Exa (10¹⁸), ×10ⁿ, □/□
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("f (10⁻¹⁵)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*10^-15") }
                    SquircleKey("a (10⁻¹⁸)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*10^-18") }
                    SquircleKey("P (10¹⁵)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^15") }
                    SquircleKey("E (10¹⁸)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*10^18") }
                    SquircleKey("×10ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("*10^") }
                    SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
                }
                // Row 4: Potenze, Radici, Parentesi & Moltiplicazione
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
                    SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
                    SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
                    SquircleKey("(", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("(") }
                    SquircleKey(")", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey(")") }
                    SquircleKey("×", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("*") }
                }
                // Row 5: Potenze Base di 10 e separatore decimale
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("1", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("1") }
                    SquircleKey("10", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("10") }
                    SquircleKey("100", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("100") }
                    SquircleKey("1000", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onKey("1000") }
                    SquircleKey(".", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey(".") }
                    SquircleKey("+", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("+") }
                }
                // Row 6: Variabili, Uguale & Cancellazione
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("x") }
                    SquircleKey("π", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("π") }
                    SquircleKey("=", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" = ") }
                    SquircleKey("DEL", delBg, delColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onDeleteChar() }
                    SquircleKey("AC", clearBg, clearColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onClearAll() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(btnHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(delBg)
                            .clickable { onDeleteChar() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Cancella carattere",
                            tint = delColor,
                            modifier = Modifier.size(16.dp)
                        )
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
    btnHeight: Dp? = null,
    onCycleFormat: () -> Unit,
    onExecute: () -> Unit,
    isToolBarExpanded: Boolean = false,
    onToggleToolBar: () -> Unit = {},
    onNavigate: (ActiveScreen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dims = rememberToolBarDimensions()
    val barHeight = btnHeight ?: dims.height

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
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dims.horizontalPadding,
                end = dims.horizontalPadding,
                bottom = dims.bottomPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(dims.spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tasto Strumenti a sinistra (da tondo a quadrato M3)
        // Stessa identica dimensione e posizione della calcolatrice principale
        PixelToolToggleButton(
            currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
            isExpanded = isToolBarExpanded,
            height = barHeight,
            onClick = onToggleToolBar,
            modifier = Modifier.size(width = dims.btnWidth, height = barHeight)
        )

        // Zona comandi con la pillola che fluttua sopra di essi alla stessa identica dimensione
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [ FORMAT ] Tasto per ciclare formato risultato (DEC, FRAC, ENG, DMS, POL)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
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
                        .height(barHeight)
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

            // Pillola fluttuante Material 3 SEMPRE fluttuante e della STESSA dimensione fissa
            androidx.compose.animation.AnimatedVisibility(
                visible = isToolBarExpanded,
                enter = fadeIn(tween(180)) + scaleIn(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.62f,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    ),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                ),
                exit = fadeOut(tween(140)) + scaleOut(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.85f,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    ),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                PixelToolPillBar(
                    currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                    height = barHeight,
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
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
    val view = LocalView.current
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
            .clickable {
                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onToggle()
            },
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
