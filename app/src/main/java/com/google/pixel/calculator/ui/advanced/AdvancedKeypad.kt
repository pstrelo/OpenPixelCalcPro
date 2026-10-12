package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * Material 3 Expressive keypad for Advanced Calculator / Google Math Solver.
 * 6 Columns x 6 Rows layout with fixed navigation controls across all tabs:
 * - Row 4 Col 6: [ ⌫ ] Square delete key with standard calculator shape
 * - Row 5 Col 6: Empty spacer aligning with the layout sketch
 * - Row 6: [ Tool ] [ ( / < ] [ ) / > ] [ ← ] [ → ] [ ↵ ]
 *   Where [ ← ], [ → ], and [ ↵ ] take the active menu tab color.
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
    val buttonHeight = if (isLandscape) 30.dp else if (isSplitScreen) 34.dp else 42.dp

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

        // 2. Active Tab Keypad Grid with floating toolbox support
        Box(modifier = Modifier.padding(horizontal = 6.dp)) {
            when (activeTab) {
                MathSolverTab.ALGEBRA -> AlgebraKeypadGrid(
                    btnHeight = buttonHeight,
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
                    btnHeight = buttonHeight,
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
                    btnHeight = buttonHeight,
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
                    btnHeight = if (isLandscape) 28.dp else if (isSplitScreen) 30.dp else 37.dp,
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
                    .padding(bottom = buttonHeight + 6.dp)
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

// Backward-compatible overload
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
// FIXED BOTTOM NAVIGATION ROW (ROW 6)
// =========================================================================

@Composable
fun CommonBottomNavRow(
    btnHeight: Dp,
    tabAccent: Color,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onKey: (String) -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)
    val navSquareContent = tabAccent
    val exeSquareBg = tabAccent
    val exeSquareContent = Color.White
    val dualBg = if (isDark) MathSolverTheme.algebraFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val dualColor = if (isDark) MathSolverTheme.algebraFunctionContent else tabAccent

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Col 1: Tool picker circle button
        PixelToolToggleButton(
            currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
            isExpanded = isToolBarExpanded,
            height = btnHeight,
            onClick = onToggleToolBar,
            modifier = Modifier.weight(1f)
        )

        // Col 2: Stacked '(' and '<'
        StackedDualKey(
            topLabel = "(",
            bottomLabel = "<",
            height = btnHeight,
            modifier = Modifier.weight(1f),
            bgColor = dualBg,
            textColor = dualColor,
            onTopClick = { onKey("(") },
            onBottomClick = { onKey(" < ") }
        )

        // Col 3: Stacked ')' and '>'
        StackedDualKey(
            topLabel = ")",
            bottomLabel = ">",
            height = btnHeight,
            modifier = Modifier.weight(1f),
            bgColor = dualBg,
            textColor = dualColor,
            onTopClick = { onKey(")") },
            onBottomClick = { onKey(" > ") }
        )

        // Col 4: [ ← ] Square button with tab menu color
        SquareSystemKey(
            height = btnHeight,
            modifier = Modifier.weight(1f),
            bgColor = navSquareBg,
            contentColor = navSquareContent,
            onClick = onMoveCursorLeft
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Cursor left",
                tint = navSquareContent,
                modifier = Modifier.size(19.dp)
            )
        }

        // Col 5: [ → ] Square button with tab menu color
        SquareSystemKey(
            height = btnHeight,
            modifier = Modifier.weight(1f),
            bgColor = navSquareBg,
            contentColor = navSquareContent,
            onClick = onMoveCursorRight
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Cursor right",
                tint = navSquareContent,
                modifier = Modifier.size(19.dp)
            )
        }

        // Col 6: [ ↵ ] Square button with tab menu color (Execute / Solve)
        SquareSystemKey(
            height = btnHeight,
            modifier = Modifier.weight(1f),
            bgColor = exeSquareBg,
            contentColor = exeSquareContent,
            onClick = onExecute
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                contentDescription = stringResource(R.string.solve),
                tint = exeSquareContent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =========================================================================
// 1. ALGEBRA KEYPAD GRID (6 Columns x 6 Rows)
// =========================================================================

@Composable
private fun AlgebraKeypadGrid(
    btnHeight: Dp,
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ x ], [ y ], [ π ], [ e ], [ % ], [ × ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("y", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("π", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("e", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("%", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 17.sp) { onKey("%") }
            SquircleKey("×", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("*") }
        }

        // Row 2: [ □² ], [ □/□ ], [ 7 ], [ 8 ], [ 9 ], [ - ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("7", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("7") }
            SquircleKey("8", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("8") }
            SquircleKey("9", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("9") }
            SquircleKey("-", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("-") }
        }

        // Row 3: [ □ⁿ ], [ √x ], [ 4 ], [ 5 ], [ 6 ], [ + ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
            SquircleKey("√x", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("4", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("4") }
            SquircleKey("5", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("5") }
            SquircleKey("6", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("6") }
            SquircleKey("+", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp) { onKey("+") }
        }

        // Row 4: [ log ], [ ⁿ√□ ], [ 1 ], [ 2 ], [ 3 ], [ ⌫ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("ⁿ√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ʸ√") }
            SquircleKey("1", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("1") }
            SquircleKey("2", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("2") }
            SquircleKey("3", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("3") }
            SquareSystemKey(
                height = btnHeight,
                modifier = Modifier.weight(1f),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Row 5: [ ln ], [ |x| ], [ 0 ], [ . ], [ = ], [ Spacer ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
            SquircleKey("0", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey("0") }
            SquircleKey(".", numBg, numColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(".") }
            SquircleKey("=", opBg, opColor, btnHeight, Modifier.weight(1f), fontSize = 18.sp, isBold = true) { onKey(" = ") }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Row 6: Common bottom nav row
        CommonBottomNavRow(
            btnHeight = btnHeight,
            tabAccent = tabAccent,
            isToolBarExpanded = isToolBarExpanded,
            onToggleToolBar = onToggleToolBar,
            onKey = onKey,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onExecute = onExecute
        )
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (6 Columns x 6 Rows)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    btnHeight: Dp,
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ x ], [ θ ], [ e ], [ π ], [ ° ], [ rad ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("θ", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("θ") }
            SquircleKey("e", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("π", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("°", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("°") }
            SquircleKey("rad", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("*(1)") }
        }

        // Row 2: [ sin ], [ cos ], [ tan ], [ csc ], [ sec ], [ cot ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("sin", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sin(") }
            SquircleKey("cos", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cos(") }
            SquircleKey("tan", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("tan(") }
            SquircleKey("csc", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("csc(") }
            SquircleKey("sec", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sec(") }
            SquircleKey("cot", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("cot(") }
        }

        // Row 3: [ arcsin ], [ arccos ], [ arctan ], [ arccsc ], [ arcsec ], [ arccot ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("arcsin", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("arccos", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccos(") }
            SquircleKey("arctan", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arctan(") }
            SquircleKey("arccsc", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccsc(") }
            SquircleKey("arcsec", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arcsec(") }
            SquircleKey("arccot", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("arccot(") }
        }

        // Row 4: [ asinh ], [ acosh ], [ atanh ], [ acsch ], [ asech ], [ ⌫ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("asinh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("asinh(") }
            SquircleKey("acosh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("acosh(") }
            SquircleKey("atanh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("atanh(") }
            SquircleKey("acsch", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("acsch(") }
            SquircleKey("asech", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("asech(") }
            SquareSystemKey(
                height = btnHeight,
                modifier = Modifier.weight(1f),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Row 5: [ □/□ ], [ √□ ], [ □² ], [ sinh ], [ cosh ], [ Spacer ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
            SquircleKey("□²", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^2") }
            SquircleKey("sinh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sinh(") }
            SquircleKey("cosh", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("cosh(") }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Row 6: Common bottom nav row
        CommonBottomNavRow(
            btnHeight = btnHeight,
            tabAccent = tabAccent,
            isToolBarExpanded = isToolBarExpanded,
            onToggleToolBar = onToggleToolBar,
            onKey = onKey,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onExecute = onExecute
        )
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (6 Columns x 6 Rows)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    btnHeight: Dp,
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Row 1: [ x ], [ t ], [ d/dx ], [ ∫ ], [ ∫ₐᵇ ], [ lim ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("t", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("t") }
            SquircleKey("d/dx", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onKey("d/dx(") }
            SquircleKey("∫", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("∫ₐᵇ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 13.sp) { onKey("defint(") }
            SquircleKey("lim", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 13.sp) { onKey("lim(") }
        }

        // Row 2: [ Σ ], [ Π ], [ ∞ ], [ ln ], [ log ], [ □ⁿ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("Σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("Π", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("prod(") }
            SquircleKey("∞", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("ln", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("ln(") }
            SquircleKey("log", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("log(") }
            SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
        }

        // Row 3: [ binom ], [ nPr ], [ n! ], [ rand ], [ randInt ], [ Γ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("binom", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("binom(") }
            SquircleKey("nPr", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 12.sp) { onKey("perm(") }
            SquircleKey("n!", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 14.sp) { onKey("!") }
            SquircleKey("rand", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("rand()") }
            SquircleKey("randInt", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("randint(") }
            SquircleKey("Γ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 15.sp) { onKey("gamma(") }
        }

        // Row 4: [ binompdf ], [ binomcdf ], [ normpdf ], [ normcdf ], [ mean ], [ ⌫ ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("binompdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("binompdf(") }
            SquircleKey("binomcdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("binomcdf(") }
            SquircleKey("normpdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("normpdf(") }
            SquircleKey("normcdf", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("normcdf(") }
            SquircleKey("mean", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("mean(") }
            SquareSystemKey(
                height = btnHeight,
                modifier = Modifier.weight(1f),
                bgColor = delBg,
                contentColor = delColor,
                onClick = onDeleteChar
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_digit),
                    tint = delColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Row 5: [ var ], [ std ], [ median ], [ □/□ ], [ |x| ], [ Spacer ]
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SquircleKey("var", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("var(") }
            SquircleKey("std", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("std(") }
            SquircleKey("median", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("median(") }
            SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
            Spacer(modifier = Modifier.weight(1f))
        }

        // Row 6: Common bottom nav row
        CommonBottomNavRow(
            btnHeight = btnHeight,
            tabAccent = tabAccent,
            isToolBarExpanded = isToolBarExpanded,
            onToggleToolBar = onToggleToolBar,
            onKey = onKey,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onExecute = onExecute
        )
    }
}

// =========================================================================
// 4. ALPHABET, CONSTANTS & SI PREFIXES KEYPAD GRID (6 Columns x 6 Rows)
// =========================================================================

enum class AlphabetSubTab(val labelRes: Int) {
    LETTERS(R.string.subtab_letters),
    CONSTANTS(R.string.subtab_constants),
    SI_PREFIXES(R.string.subtab_si_prefixes)
}

@Composable
private fun AlphabetKeypadGrid(
    btnHeight: Dp,
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
                // Row 1: [ a ], [ b ], [ c ], [ d ], [ k ], [ m ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("a", "b", "c", "d", "k", "m").forEach { key ->
                        SquircleKey(key, fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(key) }
                    }
                }
                // Row 2: [ n ], [ p ], [ r ], [ s ], [ t ], [ u ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("n", "p", "r", "s", "t", "u").forEach { key ->
                        SquircleKey(key, fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(key) }
                    }
                }
                // Row 3: [ v ], [ w ], [ z ], [ α ], [ β ], [ γ ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("v", "w", "z", "α", "β", "γ").forEach { key ->
                        SquircleKey(key, fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(key) }
                    }
                }
                // Row 4: [ δ ], [ ε ], [ λ ], [ μ ], [ φ ], [ ⌫ ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("δ", "ε", "λ", "μ", "φ").forEach { key ->
                        SquircleKey(key, fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(key) }
                    }
                    SquareSystemKey(
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        bgColor = delBg,
                        contentColor = delColor,
                        onClick = onDeleteChar
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.delete_digit),
                            tint = delColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                // Row 5: [ ω ], [ Δ ], [ Σ ], [ Ω ], [ i ], [ Spacer ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ω", "Δ", "Σ", "Ω", "i").forEach { key ->
                        SquircleKey(key, fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(key) }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            AlphabetSubTab.CONSTANTS -> {
                // Row 1: [ c ], [ G ], [ g ], [ h ], [ ħ ], [ k_B ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("c", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("c") }
                    SquircleKey("G", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("G") }
                    SquircleKey("g", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("g") }
                    SquircleKey("h", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("h") }
                    SquircleKey("ħ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("ħ") }
                    SquircleKey("k_B", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("k_B") }
                }
                // Row 2: [ N_A ], [ R ], [ q_e ], [ m_e ], [ m_p ], [ u ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("N_A", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("N_A") }
                    SquircleKey("R", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("R") }
                    SquircleKey("q_e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("q_e") }
                    SquircleKey("m_e", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("m_e") }
                    SquircleKey("m_p", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 11.sp) { onKey("m_p") }
                    SquircleKey("u", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true) { onKey("u") }
                }
                // Row 3: [ ε₀ ], [ μ₀ ], [ σ ], [ φ ], [ e ], [ π ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("ε₀", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("ε₀") }
                    SquircleKey("μ₀", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("μ₀") }
                    SquircleKey("σ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("σ") }
                    SquircleKey("φ", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 15.sp) { onKey("φ") }
                    SquircleKey("e", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("e") }
                    SquircleKey("π", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("π") }
                }
                // Row 4: [ √2 ], [ √3 ], [ ln2 ], [ ln10 ], [ □ⁿ ], [ ⌫ ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("√2", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sqrt(2)") }
                    SquircleKey("√3", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("sqrt(3)") }
                    SquircleKey("ln2", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 12.sp) { onKey("ln(2)") }
                    SquircleKey("ln10", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("ln(10)") }
                    SquircleKey("□ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("^") }
                    SquareSystemKey(
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        bgColor = delBg,
                        contentColor = delColor,
                        onClick = onDeleteChar
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.delete_digit),
                            tint = delColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                // Row 5: [ √□ ], [ □/□ ], [ |x| ], [ x ], [ y ], [ Spacer ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("√□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("sqrt(") }
                    SquircleKey("□/□", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("/") }
                    SquircleKey("|x|", fnBg, fnColor, btnHeight, Modifier.weight(1f)) { onKey("abs(") }
                    SquircleKey("x", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("x") }
                    SquircleKey("y", fnBg, tabAccent, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 16.sp) { onKey("y") }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            AlphabetSubTab.SI_PREFIXES -> {
                // Row 1: [ Y ], [ Z ], [ E ], [ P ], [ T ], [ G ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("Y (10²⁴)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e24)") }
                    SquircleKey("Z (10²¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e21)") }
                    SquircleKey("E (10¹⁸)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e18)") }
                    SquircleKey("P (10¹⁵)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e15)") }
                    SquircleKey("T (10¹²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e12)") }
                    SquircleKey("G (10⁹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e9)") }
                }
                // Row 2: [ M ], [ k ], [ h ], [ da ], [ d ], [ c ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("M (10⁶)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e6)") }
                    SquircleKey("k (10³)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("*(1000)") }
                    SquircleKey("h (10²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 10.sp) { onKey("*(100)") }
                    SquircleKey("da (10¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(10)") }
                    SquircleKey("d (10⁻¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(0.1)") }
                    SquircleKey("c (10⁻²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(0.01)") }
                }
                // Row 3: [ m ], [ μ ], [ n ], [ p ], [ f ], [ a ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("m (10⁻³)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(0.001)") }
                    SquircleKey("μ (10⁻⁶)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e-6)") }
                    SquircleKey("n (10⁻⁹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 9.sp) { onKey("*(1e-9)") }
                    SquircleKey("p (10⁻¹²)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*(1e-12)") }
                    SquircleKey("f (10⁻¹⁵)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*(1e-15)") }
                    SquircleKey("a (10⁻¹⁸)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*(1e-18)") }
                }
                // Row 4: [ z ], [ y ], [ 10ⁿ ], [ × ], [ ÷ ], [ ⌫ ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("z (10⁻²¹)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*(1e-21)") }
                    SquircleKey("y (10⁻²⁴)", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 8.sp) { onKey("*(1e-24)") }
                    SquircleKey("10ⁿ", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 11.sp) { onKey("*10^") }
                    SquircleKey("×", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("*") }
                    SquircleKey("÷", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("/") }
                    SquareSystemKey(
                        height = btnHeight,
                        modifier = Modifier.weight(1f),
                        bgColor = delBg,
                        contentColor = delColor,
                        onClick = onDeleteChar
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.delete_digit),
                            tint = delColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                // Row 5: [ + ], [ - ], [ = ], [ 0 ], [ . ], [ Spacer ]
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SquircleKey("+", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("+") }
                    SquircleKey("-", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("-") }
                    SquircleKey("=", fnBg, fnColor, btnHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" = ") }
                    SquircleKey("0", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey("0") }
                    SquircleKey(".", fnBg, fnColor, btnHeight, Modifier.weight(1f), fontSize = 16.sp) { onKey(".") }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Row 6: Common bottom nav row
        CommonBottomNavRow(
            btnHeight = btnHeight,
            tabAccent = tabAccent,
            isToolBarExpanded = isToolBarExpanded,
            onToggleToolBar = onToggleToolBar,
            onKey = onKey,
            onMoveCursorLeft = onMoveCursorLeft,
            onMoveCursorRight = onMoveCursorRight,
            onExecute = onExecute
        )
    }
}

// =========================================================================
// REUSABLE KEYPAD BUTTONS (SQUARE & PILL & DUAL)
// =========================================================================

/**
 * Square button with the exact shape and size of the system calculator (RoundedCornerShape 18dp).
 * Used for [ ← ], [ → ], [ ↵ ], and [ ⌫ ].
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
 * Stacked dual pill button (occupies 1 column width with two vertically stacked half-height keys).
 * Used for '(' over '<' and ')' over '>'.
 */
@Composable
fun StackedDualKey(
    topLabel: String,
    bottomLabel: String,
    height: Dp,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    onTopClick: () -> Unit,
    onBottomClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val halfHeight = (height - 2.dp) / 2
    Column(
        modifier = modifier.height(height),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(bgColor)
                .clickable {
                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTopClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = topLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(halfHeight)
                .clip(RoundedCornerShape(percent = 50))
                .background(bgColor)
                .clickable {
                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBottomClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bottomLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Standard pill/squircle key with rounded ends.
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
