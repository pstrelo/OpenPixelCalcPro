package com.google.pixel.calculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.data.HistoryStorage
import com.google.pixel.calculator.data.ThemePreferences
import com.google.pixel.calculator.ui.advanced.AdvancedCalculatorScreen
import com.google.pixel.calculator.ui.display.FormulaDisplay
import com.google.pixel.calculator.ui.history.PixelHistoryPanel
import com.google.pixel.calculator.ui.keypad.BaseKeypad
import com.google.pixel.calculator.ui.settings.ClearHistoryConfirmDialog
import com.google.pixel.calculator.ui.settings.ThemeSelectionDialog
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.toolbox.*
import com.google.pixel.calculator.ui.easteregg.EasterEggScreen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel? = null,
    historyStorage: HistoryStorage? = null
) {
    val context = LocalContext.current
    val storage = historyStorage ?: remember { HistoryStorage.getInstance(context) }
    val themePrefs = remember { ThemePreferences(context) }
    val vm = viewModel ?: remember { CalculatorViewModel(storage, themePrefs) }

    val state by vm.uiState.collectAsState()
    val historyList by vm.historyFlow.collectAsState()

    var showHistorySheet by rememberSaveable { mutableStateOf(false) }
    var isToolBarExpanded by rememberSaveable { mutableStateOf(false) }

    // Animated transitions between utility tools screens and main calculator
    AnimatedContent(
        targetState = state.activeScreen,
        transitionSpec = {
            if (targetState == ActiveScreen.MAIN_CALCULATOR) {
                (slideInHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeIn(animationSpec = tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(250)) { it / 3 } + fadeOut(animationSpec = tween(250)))
            } else {
                (slideInHorizontally(animationSpec = tween(300)) { it / 3 } + fadeIn(animationSpec = tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(250)) { -it / 4 } + fadeOut(animationSpec = tween(250)))
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            ActiveScreen.ADVANCED_CALCULATOR -> {
                AdvancedCalculatorScreen(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                    onNavigate = { vm.navigateTo(it) }
                )
            }
            ActiveScreen.TOOLBOX_CONVERTER -> {
                ConverterCalculatorView(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                    onNavigate = { vm.navigateTo(it) }
                )
            }
            ActiveScreen.TOOLBOX_LEVEL -> {
                LevelView(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                    onNavigate = { vm.navigateTo(it) }
                )
            }
            ActiveScreen.TOOLBOX_COMPASS -> {
                CompassView(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                    onNavigate = { vm.navigateTo(it) }
                )
            }
            ActiveScreen.TOOLBOX_RULER -> {
                RulerView(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                    onNavigate = { vm.navigateTo(it) }
                )
            }
            ActiveScreen.EASTER_EGG -> {
                EasterEggScreen(
                    onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) }
                )
            }
            else -> {
                if (showHistorySheet) {
                    BackHandler { showHistorySheet = false }
                }
                if (isToolBarExpanded) {
                    BackHandler { isToolBarExpanded = false }
                }

                Scaffold(
                    containerColor = CalculatorTheme.colors.background,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            navigationIcon = {
                                // History toggle button top-left
                                IconButton(
                                    onClick = { showHistorySheet = !showHistorySheet },
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .then(
                                            if (showHistorySheet) {
                                                Modifier.background(
                                                    color = CalculatorTheme.colors.operatorContainer.copy(alpha = 0.8f),
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = stringResource(R.string.calc_history_desc),
                                        tint = CalculatorTheme.colors.onSurface
                                    )
                                }
                            },
                            title = {
                                // Minimalist header without text title (Pixel style)
                            },
                            actions = {
                                // 3-dot menu button top-right
                                Box {
                                    IconButton(onClick = { vm.toggleSettingsDropdown(true) }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.options_settings_desc),
                                            tint = CalculatorTheme.colors.onSurface
                                        )
                                    }

                                    MaterialTheme(
                                        shapes = MaterialTheme.shapes.copy(
                                            extraSmall = RoundedCornerShape(20.dp)
                                        )
                                    ) {
                                        DropdownMenu(
                                            expanded = state.showSettingsDropdown,
                                            onDismissRequest = { vm.toggleSettingsDropdown(false) },
                                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.clear_history)) },
                                                onClick = {
                                                    vm.toggleSettingsDropdown(false)
                                                    vm.toggleClearHistoryDialog(true)
                                                },
                                                enabled = historyList.isNotEmpty(),
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteSweep,
                                                        contentDescription = null
                                                    )
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.choose_theme)) },
                                                onClick = {
                                                    vm.toggleSettingsDropdown(false)
                                                    vm.toggleThemeDialog(true)
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Palette,
                                                        contentDescription = null
                                                    )
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (state.isHapticEnabled) {
                                                            stringResource(R.string.disable_vibration)
                                                        } else {
                                                            stringResource(R.string.enable_vibration)
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    vm.toggleHaptic()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Vibration,
                                                        contentDescription = null
                                                    )
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (state.isRadians) {
                                                            stringResource(R.string.use_degrees)
                                                        } else {
                                                            stringResource(R.string.use_radians)
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    vm.toggleAngleUnit()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Tune,
                                                        contentDescription = null
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Display and history slide panel
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                AnimatedContent(
                                    targetState = showHistorySheet,
                                    transitionSpec = {
                                        (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                    },
                                    label = "HistoryDisplayTransition"
                                ) { isHistoryOpen ->
                                    if (isHistoryOpen) {
                                        PixelHistoryPanel(
                                            historyList = historyList,
                                            currentExpression = state.expression,
                                            currentPreview = state.resultPreview,
                                            onSelectItem = { expr ->
                                                vm.selectHistoryItem(expr)
                                                showHistorySheet = false
                                            },
                                            onDismiss = { showHistorySheet = false },
                                            onClearHistory = { vm.toggleClearHistoryDialog(true) },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        FormulaDisplay(
                                            expression = state.expression,
                                            resultPreview = state.resultPreview,
                                            ast = state.ast,
                                            errorMessage = state.errorMessage,
                                            sessionHistory = state.sessionHistory,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // Calculator keypad with expand lever and M3 toolbar
                            BaseKeypad(
                                isProMode = state.isProMode,
                                isShiftActive = state.isShiftActive,
                                isRadians = state.isRadians,
                                isHapticEnabled = state.isHapticEnabled,
                                currentScreen = ActiveScreen.MAIN_CALCULATOR,
                                isToolBarExpanded = isToolBarExpanded,
                                onToggleToolBar = { isToolBarExpanded = !isToolBarExpanded },
                                onSelectTool = {
                                    isToolBarExpanded = false
                                    vm.navigateTo(it)
                                },
                                onDigit = { vm.onDigit(it) },
                                onOperator = { vm.onOperator(it) },
                                onFunction = { vm.onFunction(it) },
                                onConstant = { vm.onConstant(it) },
                                onSmartParenthesis = { vm.onSmartParenthesis() },
                                onClear = { vm.onClearAll() },
                                onBackspace = { vm.onBackspace() },
                                onEquals = { vm.onEquals() },
                                onToggleProMode = { vm.toggleProMode() },
                                onToggleShift = { vm.toggleShift() },
                                onToggleAngleUnit = { vm.toggleAngleUnit() }
                            )
                        }

                        // Dismiss overlay when toolbar is open
                        if (isToolBarExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 76.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isToolBarExpanded = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    // Theme and icon selection dialog
    if (state.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = state.themeMode,
            currentAppIcon = state.appIconMode,
            syncIconWithTheme = state.syncIconWithTheme,
            onSelectTheme = { mode ->
                vm.setThemeMode(mode)
            },
            onSetAppIcon = { mode ->
                vm.setAppIconMode(mode)
            },
            onToggleSyncIcon = { enabled ->
                vm.setSyncIconWithTheme(enabled)
            },
            onDismiss = { vm.toggleThemeDialog(false) }
        )
    }

    // Clear history confirmation dialog
    if (state.showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                vm.clearAllHistory()
            },
            onDismiss = { vm.toggleClearHistoryDialog(false) }
        )
    }
}
