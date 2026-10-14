package com.google.pixel.calculator.ui.advanced

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.google.pixel.calculator.ui.toolbox.rememberToolBarDimensions

/**
 * Material 3 Keypad for Advanced Calculator.
 * Structure:
 * 1. Top Fixed Control Row (6 keys, uniform across all tabs):
 *    [ < ] [ > ] [ ( ] [ ) ] [ FORMAT ] [ AC ]
 * 2. 5 Thematic Rows (6 columns) according to the active tab:
 *    - Columns 1-5 have 5 regular keys each.
 *    - Column 6 has 3 regular keys + 1 double-height [ ⌫ ] backspace key (rows 4-5).
 * 3. Bottom System / Navigation Row:
 *    - Left: PixelToolToggleButton with identical dimensions and circular-to-squircle animation
 *      as the standard calculator and toolbox screens.
 *    - Right: Empty space left by moved keys (columns 2 & 3) + [ ← ] [ → ] [ ↵ ] navigation keys,
 *      which smoothly disappear with M3 scale/fade animation when the tool pill bar expands.
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val toolBarDims = rememberToolBarDimensions()

    val spacing = if (isLandscape) 4.dp else 6.dp
    val unitHeight = if (isLandscape) 22.dp else if (isSplitScreen) 28.dp else 36.dp
    val bottomRowHeight = if (isLandscape) 44.dp else if (isSplitScreen) 50.dp else 56.dp

    val tabAccent = activeTab.accentColor

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isLandscape) 8.dp else 12.dp,
                    vertical = if (isLandscape) 2.dp else 4.dp
                ),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            // Tab Selector Bar (Algebra, Trigonometry, Calculus, Alphabet)
            MathSolverTabBar(
                selectedTab = activeTab,
                onTabSelected = onTabSelected
            )

            // 1. TOP FIXED CONTROL ROW: [ < ] [ > ] [ ≤ ] [ ≥ ] [ ( ] [ ) ] (Nascosto nella tab Alphabet)
            if (activeTab != MathSolverTab.ALPHABET) {
                TopFixedControlRow(
                    unitHeight = unitHeight,
                    spacing = spacing,
                    onKey = { onInsertChar(it) }
                )
            }

            // 2. 5 THEMATIC ROWS (Columns 1 to 6)
            when (activeTab) {
                MathSolverTab.ALGEBRA -> AlgebraKeypadGrid(
                    unitHeight = unitHeight,
                    spacing = spacing,
                    onDeleteChar = onDeleteChar,
                    onKey = { onInsertChar(it) }
                )
                MathSolverTab.TRIGONOMETRY -> TrigonometryKeypadGrid(
                    unitHeight = unitHeight,
                    spacing = spacing,
                    onDeleteChar = onDeleteChar,
                    onKey = { onInsertChar(it) }
                )
                MathSolverTab.CALCULUS -> CalculusKeypadGrid(
                    unitHeight = unitHeight,
                    spacing = spacing,
                    onDeleteChar = onDeleteChar,
                    onKey = { onInsertChar(it) }
                )
                MathSolverTab.ALPHABET -> AlphabetKeypadGrid(
                    unitHeight = unitHeight,
                    spacing = spacing,
                    onDeleteChar = onDeleteChar,
                    onKey = { onInsertChar(it) }
                )
            }

            // 3. BOTTOM SYSTEM / NAVIGATION ROW (Tool Toggle Button + [ FORMAT ] [ AC ] [ ← ] [ → ] [ ↵ ])
            BottomSystemNavRow(
                height = bottomRowHeight,
                unitHeight = unitHeight,
                spacing = spacing,
                tabAccent = tabAccent,
                isToolBarExpanded = isToolBarExpanded,
                onToggleToolBar = onToggleToolBar,
                onNavigate = onNavigate,
                onCycleFormat = onCycleFormat,
                onClearAll = onClearAll,
                onMoveCursorLeft = onMoveCursorLeft,
                onMoveCursorRight = onMoveCursorRight,
                onExecute = onExecute
            )
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
                        text = when (tab) {
                            MathSolverTab.TRIGONOMETRY -> "Trig"
                            else -> tab.title
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 1,
                        color = if (isSelected) tab.accentColor else unselectedColor
                    )
                }
            )
        }
    }
}

// =========================================================================
// TOP FIXED CONTROL ROW: [ < ] [ > ] [ ≤ ] [ ≥ ] [ ( ] [ ) ]
// =========================================================================

@Composable
private fun TopFixedControlRow(
    unitHeight: Dp,
    spacing: Dp,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val fnBg = if (isDark) Color(0xFF242738) else Color(0xFFECEFF5)
    val fnColor = if (isDark) Color(0xFFCBD5E1) else CalculatorTheme.colors.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquircleKey("<", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" < ") }
        SquircleKey(">", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" > ") }
        SquircleKey("≤", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" <= ") }
        SquircleKey("≥", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(" >= ") }
        SquircleKey("(", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey("(") }
        SquircleKey(")", fnBg, fnColor, unitHeight, Modifier.weight(1f), isBold = true, fontSize = 17.sp) { onKey(")") }
    }
}

// =========================================================================
// BOTTOM SYSTEM / NAVIGATION ROW (Tool Toggle Button + [ FORMAT ] [ AC ] [ ← ] [ → ] [ ↵ ])
// =========================================================================

@Composable
private fun BottomSystemNavRow(
    height: Dp,
    unitHeight: Dp,
    spacing: Dp,
    tabAccent: Color,
    isToolBarExpanded: Boolean,
    onToggleToolBar: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit,
    onCycleFormat: () -> Unit,
    onClearAll: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onExecute: () -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val navSquareBg = if (isDark) tabAccent.copy(alpha = 0.28f) else tabAccent.copy(alpha = 0.18f)
    val fnBg = if (isDark) Color(0xFF242738) else Color(0xFFECEFF5)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)

    if (isToolBarExpanded) {
        BackHandler { onToggleToolBar() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colonna 1: Tasto strumenti (allineato con colonna 1 della tastiera)
            PixelToolToggleButton(
                currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                isExpanded = isToolBarExpanded,
                height = height,
                onClick = onToggleToolBar,
                modifier = Modifier.weight(1f)
            )

            // Contenitore per le restanti 5 colonne (tasti di controllo o menu a pillola)
            Box(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Tasti di navigazione, format, AC e invio (scompaiono fluidamente quando la toolbar si apre)
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isToolBarExpanded,
                    enter = fadeIn(tween(180)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    ),
                    exit = fadeOut(tween(140)) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colonna 2: [ ← ] (squadrato, altezza piena)
                        SquareSystemKey(
                            height = height,
                            modifier = Modifier.weight(1f),
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

                        // Colonna 3: [ → ] (squadrato, altezza piena)
                        SquareSystemKey(
                            height = height,
                            modifier = Modifier.weight(1f),
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

                        // Colonna 4: [ FORMAT ] (squadrato, altezza piena)
                        SquareSystemKey(
                            height = height,
                            modifier = Modifier.weight(1f),
                            bgColor = fnBg,
                            contentColor = tabAccent,
                            onClick = onCycleFormat
                        ) {
                            Text(
                                text = "FORMAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tabAccent
                            )
                        }

                        // Colonna 5: [ AC ] (squadrato, altezza piena)
                        SquareSystemKey(
                            height = height,
                            modifier = Modifier.weight(1f),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onClearAll
                        ) {
                            Text(
                                text = "AC",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = delColor
                            )
                        }

                        // Colonna 6: [ ↵ ] (squadrato, altezza piena)
                        SquareSystemKey(
                            height = height,
                            modifier = Modifier.weight(1f),
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

                // Menu a pillola degli strumenti (compare al posto dei tasti sottostanti)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isToolBarExpanded,
                    enter = fadeIn(tween(180)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    ),
                    exit = fadeOut(tween(140)) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    )
                ) {
                    PixelToolPillBar(
                        currentScreen = ActiveScreen.ADVANCED_CALCULATOR,
                        height = height,
                        onNavigate = { screen ->
                            onToggleToolBar()
                            onNavigate(screen)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 1. ALGEBRA KEYPAD GRID (6 Columns x 5 Rows)
// =========================================================================

@Composable
private fun AlgebraKeypadGrid(
    unitHeight: Dp,
    spacing: Dp,
    onDeleteChar: () -> Unit,
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
    val doubleHeight = unitHeight * 2 + spacing

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ □² ], [ □ⁿ ], [ log ], [ logₙ ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("□²", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("²") }
            SquircleKey("□ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^") }
            SquircleKey("log", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("log(") }
            SquircleKey("log_□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("log_□") }
        }

        // Col 2: [ y ], [ □/□ ], [ √x ], [ ⁿ√□ ], [ |x| ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("y", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("y") }
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
            SquircleKey("√x", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
            SquircleKey("ⁿ√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 13.sp) { onKey("ⁿ√□") }
            SquircleKey("|x|", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("|") }
        }

        // Col 3: [ π ], [ 7 ], [ 4 ], [ 1 ], [ 0 ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("7", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("7") }
            SquircleKey("4", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("4") }
            SquircleKey("1", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("1") }
            SquircleKey("0", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("0") }
        }

        // Col 4: [ e ], [ 8 ], [ 5 ], [ 2 ], [ . ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("8", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("8") }
            SquircleKey("5", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("5") }
            SquircleKey("2", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("2") }
            SquircleKey(".", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey(".") }
        }

        // Col 5: [ % ], [ 9 ], [ 6 ], [ 3 ], [ = ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("%", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 17.sp) { onKey("%") }
            SquircleKey("9", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("9") }
            SquircleKey("6", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("6") }
            SquircleKey("3", numBg, numColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("3") }
            SquircleKey("=", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey(" = ") }
        }

        // Col 6: [ × ], [ - ], [ + ], [ ⌫ (double height) ]
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
        }
    }
}

// =========================================================================
// 2. TRIGONOMETRY KEYPAD GRID (6 Columns x 5 Rows)
// =========================================================================

@Composable
private fun TrigonometryKeypadGrid(
    unitHeight: Dp,
    spacing: Dp,
    onDeleteChar: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.TRIGONOMETRY.accentColor
    val fnBg = if (isDark) MathSolverTheme.trigFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.trigFunctionContent else Color(0xFF047857)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val doubleHeight = unitHeight * 2 + spacing

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ sin ], [ arcsin ], [ asinh ], [ □/□ ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("sin", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sin(") }
            SquircleKey("arcsin", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arcsin(") }
            SquircleKey("asinh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("asinh(") }
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
        }

        // Col 2: [ θ ], [ cos ], [ arccos ], [ acosh ], [ √□ ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("θ", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("θ") }
            SquircleKey("cos", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("cos(") }
            SquircleKey("arccos", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccos(") }
            SquircleKey("acosh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("acosh(") }
            SquircleKey("√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
        }

        // Col 3: [ e ], [ tan ], [ arctan ], [ atanh ], [ □² ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("tan", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("tan(") }
            SquircleKey("arctan", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arctan(") }
            SquircleKey("atanh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("atanh(") }
            SquircleKey("□²", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("²") }
        }

        // Col 4: [ π ], [ csc ], [ arccsc ], [ acsch ], [ sinh ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("csc", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("csc(") }
            SquircleKey("arccsc", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccsc(") }
            SquircleKey("acsch", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("acsch(") }
            SquircleKey("sinh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sinh(") }
        }

        // Col 5: [ sec ], [ arcsec ], [ asech ], [ cosh ], [ tanh ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("sec", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sec(") }
            SquircleKey("arcsec", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arcsec(") }
            SquircleKey("asech", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("asech(") }
            SquircleKey("cosh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("cosh(") }
            SquircleKey("tanh", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("tanh(") }
        }

        // Col 6: [ cot ], [ arccot ], [ acoth ], [ ⌫ (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("cot", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("cot(") }
            SquircleKey("arccot", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("arccot(") }
            SquircleKey("acoth", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("acoth(") }
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
        }
    }
}

// =========================================================================
// 3. CALCULUS KEYPAD GRID (6 Columns x 5 Rows)
// =========================================================================

@Composable
private fun CalculusKeypadGrid(
    unitHeight: Dp,
    spacing: Dp,
    onDeleteChar: () -> Unit,
    onKey: (String) -> Unit
) {
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.CALCULUS.accentColor
    val fnBg = if (isDark) MathSolverTheme.calculusFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.calculusFunctionContent else Color(0xFFBE185D)
    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val doubleHeight = unitHeight * 2 + spacing

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Col 1: [ x ], [ d/dx ], [ Σ ], [ binom ], [ binompdf ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("x", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("x") }
            SquircleKey("d/dx", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 12.sp) { onKey("d/dx(") }
            SquircleKey("Σ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("Σ(") }
            SquircleKey("binom", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("binom(") }
            SquircleKey("binompdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("binompdf(") }
        }

        // Col 2: [ t ], [ ∫ ], [ Π ], [ nPr ], [ binomcdf ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("t", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("t") }
            SquircleKey("∫", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 18.sp) { onKey("∫(") }
            SquircleKey("Π", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("prod(") }
            SquircleKey("nPr", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 12.sp) { onKey("perm(") }
            SquircleKey("binomcdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("binomcdf(") }
        }

        // Col 3: [ ∞ ], [ ∫ₐᵇ ], [ ln ], [ n! ], [ normpdf ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("∞", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("∞") }
            SquircleKey("∫ₐᵇ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 13.sp) { onKey("defint(") }
            SquircleKey("ln", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("ln(") }
            SquircleKey("n!", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("!") }
            SquircleKey("normpdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("normpdf(") }
        }

        // Col 4: [ e ], [ lim ], [ logₙ ], [ rand ], [ normcdf ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("e") }
            SquircleKey("lim", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 13.sp) { onKey("lim(") }
            SquircleKey("log_□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("log_□") }
            SquircleKey("rand", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("rand()") }
            SquircleKey("normcdf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 9.sp) { onKey("normcdf(") }
        }

        // Col 5: [ π ], [ □ⁿ ], [ √□ ], [ randInt ], [ mean ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 17.sp) { onKey("π") }
            SquircleKey("□ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("^") }
            SquircleKey("√□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("sqrt(") }
            SquircleKey("randInt", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 10.sp) { onKey("randint(") }
            SquircleKey("mean", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("mean(") }
        }

        // Col 6: [ □/□ ], [ |x| ], [ Γ ], [ ⌫ (double height) ]
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
            SquircleKey("□/□", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("/") }
            SquircleKey("|x|", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth()) { onKey("|") }
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
        }
    }
}

// =========================================================================
// 4. ALPHABET, CONSTANTS & SI PREFIXES KEYPAD GRID (6 Columns x 5 Rows)
// =========================================================================

enum class AlphabetSubTab(val labelRes: Int) {
    LETTERS(R.string.subtab_letters),
    GREEK(R.string.subtab_greek),
    CONSTANTS(R.string.subtab_constants),
    SI_PREFIXES(R.string.subtab_si_prefixes)
}

@Composable
private fun AlphabetKeypadGrid(
    unitHeight: Dp,
    spacing: Dp,
    onDeleteChar: () -> Unit,
    onKey: (String) -> Unit
) {
    var subTab by remember { mutableStateOf(AlphabetSubTab.LETTERS) }
    val isDark = CalculatorTheme.colors.isDark
    val tabAccent = MathSolverTab.ALPHABET.accentColor
    val fnBg = if (isDark) MathSolverTheme.alphabetFunctionContainer else tabAccent.copy(alpha = 0.12f)
    val fnColor = if (isDark) MathSolverTheme.alphabetFunctionContent else Color(0xFFC2410C)

    val opBg = if (isDark) Color(0xFF33384B) else Color(0xFFE2E8F0)
    val opColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    val delBg = if (isDark) Color(0xFF452427) else Color(0xFFFFE4E6)
    val delColor = if (isDark) Color(0xFFF87171) else Color(0xFFE11D48)
    val doubleHeight = unitHeight * 2 + spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        // Sub-selector chip row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlphabetSubTab.values().forEach { tab ->
                val isSelected = tab == subTab
                val chipBg = if (isSelected) tabAccent else if (isDark) Color(0xFF282D3D) else Color(0xFFE2E8F0)
                val chipText = if (isSelected) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(unitHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(chipBg)
                        .clickable { subTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(tab.labelRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = chipText,
                        maxLines = 1
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
                    // Col 1: a, b, c, d, e
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("a", "b", "c", "d", "e").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 2: f, g, h, i, j
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("f", "g", "h", "i", "j").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 3: k, l, m, n, o
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("k", "l", "m", "n", "o").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 4: p, q, r, s, t
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("p", "q", "r", "s", "t").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 5: u, v, w, x, y
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("u", "v", "w", "x", "y").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 6: z, (x), ( ), +, DEL
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("z", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey("z") }
                        SquircleKey("(x)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 13.sp) { onKey("(x)") }
                        SquircleKey("( )", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 13.sp) { onKey("()") }
                        SquircleKey("+", opBg, opColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("+") }
                        SquareSystemKey(
                            height = unitHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onDeleteChar
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_digit),
                                tint = delColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            AlphabetSubTab.GREEK -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Col 1: α, β, γ, δ, ε
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("α", "β", "γ", "δ", "ε").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 2: ζ, η, θ, ι, κ
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("ζ", "η", "θ", "ι", "κ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 3: λ, μ, ν, ξ, π
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("λ", "μ", "ν", "ξ", "π").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 4: ρ, σ, τ, υ, φ
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("ρ", "σ", "τ", "υ", "φ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 5: χ, ψ, ω, Δ, Σ
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("χ", "ψ", "ω", "Δ", "Σ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 6: Ω, Π, Φ, Ψ, DEL
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("Ω", "Π", "Φ", "Ψ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 15.sp) { onKey(key) }
                        }
                        SquareSystemKey(
                            height = unitHeight,
                            modifier = Modifier.fillMaxWidth(),
                            bgColor = delBg,
                            contentColor = delColor,
                            onClick = onDeleteChar
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = stringResource(R.string.delete_digit),
                                tint = delColor,
                                modifier = Modifier.size(20.dp)
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
                    // Col 1: c, G, g, h, ħ
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        listOf("c", "G", "g", "h", "ħ").forEach { key ->
                            SquircleKey(key, fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 15.sp) { onKey(key) }
                        }
                    }
                    // Col 2: k_B, N_A, R, q_e, m_e
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("k_B", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("k_B") }
                        SquircleKey("N_A", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("N_A") }
                        SquircleKey("R", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("R") }
                        SquircleKey("q_e", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("q_e") }
                        SquircleKey("m_e", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("m_e") }
                    }
                    // Col 3: m_p, u, ε₀, μ₀, σ
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("m_p", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("m_p") }
                        SquircleKey("u", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("u") }
                        SquircleKey("ε₀", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("ε₀") }
                        SquircleKey("μ₀", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("μ₀") }
                        SquircleKey("σ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("σ") }
                    }
                    // Col 4: φ, e, π, √2, √3
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("φ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 15.sp) { onKey("φ") }
                        SquircleKey("e", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("e") }
                        SquircleKey("π", fnBg, tabAccent, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 16.sp) { onKey("π") }
                        SquircleKey("√2", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("sqrt(2)") }
                        SquircleKey("√3", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 12.sp) { onKey("sqrt(3)") }
                    }
                    // Col 5: α_fs, a₀, R_inf, μ_B, F
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("α_fs", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("α_fs") }
                        SquircleKey("a₀", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 12.sp) { onKey("a₀") }
                        SquircleKey("R_inf", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("R_inf") }
                        SquircleKey("μ_B", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 11.sp) { onKey("μ_B") }
                        SquircleKey("F", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), isBold = true, fontSize = 14.sp) { onKey("F") }
                    }
                    // Col 6: atm, c_s, M_☉, [ ⌫ (double height) ]
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("atm", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("atm") }
                        SquircleKey("c_s", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("c_sound") }
                        SquircleKey("M_☉", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("M_sun") }
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
                    }
                }
            }

            AlphabetSubTab.SI_PREFIXES -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    // Col 1: Y, Z, E, P, T
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("Y (10²⁴)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e24)") }
                        SquircleKey("Z (10²¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e21)") }
                        SquircleKey("E (10¹⁸)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e18)") }
                        SquircleKey("P (10¹⁵)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e15)") }
                        SquircleKey("T (10¹²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e12)") }
                    }
                    // Col 2: G, M, k, h, da
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("G (10⁹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e9)") }
                        SquircleKey("M (10⁶)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e6)") }
                        SquircleKey("k (10³)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e3)") }
                        SquircleKey("h (10²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e2)") }
                        SquircleKey("da (10¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e1)") }
                    }
                    // Col 3: d, c, m, μ, n
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("d (10⁻¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-1)") }
                        SquircleKey("c (10⁻²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-2)") }
                        SquircleKey("m (10⁻³)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-3)") }
                        SquircleKey("μ (10⁻⁶)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-6)") }
                        SquircleKey("n (10⁻⁹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-9)") }
                    }
                    // Col 4: p, f, a, z, y
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("p (10⁻¹²)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-12)") }
                        SquircleKey("f (10⁻¹⁵)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-15)") }
                        SquircleKey("a (10⁻¹⁸)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-18)") }
                        SquircleKey("z (10⁻²¹)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-21)") }
                        SquircleKey("y (10⁻²⁴)", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 8.sp) { onKey("*(1e-24)") }
                    }
                    // Col 5: 10ⁿ, ×, ÷, +, -
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        SquircleKey("10ⁿ", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 11.sp) { onKey("*10^") }
                        SquircleKey("×", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("*") }
                        SquircleKey("÷", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("/") }
                        SquircleKey("+", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("+") }
                        SquircleKey("-", fnBg, fnColor, unitHeight, Modifier.fillMaxWidth(), fontSize = 16.sp) { onKey("-") }
                    }
                    // Col 6: =, 0, ., [ ⌫ (double height) ]
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
        contentColor = contentColor,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
    text: String,
    bgColor: Color,
    textColor: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp,
    onClick: () -> Unit
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
        shape = RoundedCornerShape(percent = 50),
        color = bgColor,
        contentColor = textColor,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
