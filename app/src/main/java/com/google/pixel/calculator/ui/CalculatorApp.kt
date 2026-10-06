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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel? = null,
    historyStorage: HistoryStorage? = null
) {
    val context = LocalContext.current
    val storage = historyStorage ?: remember { HistoryStorage(context) }
    val themePrefs = remember { ThemePreferences(context) }
    val vm = viewModel ?: remember { CalculatorViewModel(storage, themePrefs) }

    val state by vm.uiState.collectAsState()
    val historyList by storage.historyFlow.collectAsState()

    var showHistorySheet by rememberSaveable { mutableStateOf(false) }
    var isToolBarExpanded by rememberSaveable { mutableStateOf(false) }

    // Routing schermate secondarie
    when (state.activeScreen) {
        ActiveScreen.TOOLBOX_MENU -> {
            ToolboxScreen(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onSelectTool = { vm.navigateTo(it) }
            )
            return
        }
        ActiveScreen.ADVANCED_CALCULATOR -> {
            AdvancedCalculatorScreen(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onNavigate = { vm.navigateTo(it) }
            )
            return
        }
        ActiveScreen.TOOLBOX_CONVERTER -> {
            ConverterCalculatorView(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onNavigate = { vm.navigateTo(it) }
            )
            return
        }
        ActiveScreen.TOOLBOX_LEVEL -> {
            LevelView(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onNavigate = { vm.navigateTo(it) }
            )
            return
        }
        ActiveScreen.TOOLBOX_COMPASS -> {
            CompassView(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onNavigate = { vm.navigateTo(it) }
            )
            return
        }
        ActiveScreen.TOOLBOX_RULER -> {
            RulerView(
                onBack = { vm.navigateTo(ActiveScreen.MAIN_CALCULATOR) },
                onNavigate = { vm.navigateTo(it) }
            )
            return
        }
        else -> {
            // Schermata Principale della Calcolatrice
        }
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
                    // Tasto Cronologia in alto a sinistra (con sfondo attivo quando la cronologia è aperta)
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
                            contentDescription = "Cronologia calcoli",
                            tint = CalculatorTheme.colors.onSurface
                        )
                    }
                },
                title = {
                    // Spazio vuoto minimalista senza titolo testuale (stile identico a Google Pixel)
                },
                actions = {
                    // Menu a tre puntini in alto a destra ancorato vicino all'icona
                    Box {
                        IconButton(onClick = { vm.toggleSettingsDropdown(true) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opzioni e impostazioni",
                                tint = CalculatorTheme.colors.onSurface
                            )
                        }

                        // DropdownMenu ancorato direttamente al pulsante a 3 puntini
                        DropdownMenu(
                            expanded = state.showSettingsDropdown,
                            onDismissRequest = { vm.toggleSettingsDropdown(false) }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cancella cronologia") },
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
                                text = { Text("Scegli tema") },
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
                                    Text(if (state.isHapticEnabled) "Disattiva vibrazione" else "Attiva vibrazione")
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
                                    Text(if (state.isRadians) "Usa Gradi (DEG)" else "Usa Radianti (RAD)")
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

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Strumenti avanzati") },
                                onClick = {
                                    vm.toggleSettingsDropdown(false)
                                    vm.navigateTo(ActiveScreen.TOOLBOX_MENU)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null
                                    )
                                }
                            )
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
                // AREA DISPLAY / CRONOLOGIA (Transizione a discesa dall'alto verso il basso)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = showHistorySheet,
                        transitionSpec = {
                            if (targetState) {
                                (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                            } else {
                                (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                            }
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

                // TASTIERINO CON LEVA ESPANSIONE STILE PIXEL E TOOLBAR STRUMENTI M3
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
                    onToggleAngleUnit = { vm.toggleAngleUnit() },
                    onOpenToolbox = { vm.navigateTo(ActiveScreen.TOOLBOX_MENU) }
                )
            }

            // Overlay di dismiss al tocco fuori dalla toolbar quando aperta
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

    // DIALOG SELEZIONE TEMA (5 temi disponibili)
    if (state.showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = state.themeMode,
            onSelectTheme = { mode ->
                vm.setThemeMode(mode)
            },
            onDismiss = { vm.toggleThemeDialog(false) }
        )
    }

    // DIALOG CONFERMA CANCELLAZIONE CRONOLOGIA
    if (state.showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                vm.clearAllHistory()
            },
            onDismiss = { vm.toggleClearHistoryDialog(false) }
        )
    }
}
