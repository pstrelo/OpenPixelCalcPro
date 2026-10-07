package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.toolbox.PixelFloatingToolBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCalculatorScreen(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {},
    viewModel: AdvancedCalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = CalculatorTheme.colors.isDark
    var isToolBarExpanded by rememberSaveable { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (isToolBarExpanded) {
            isToolBarExpanded = false
        } else {
            when (state.screenViewMode) {
                ScreenViewMode.FULLSCREEN_GRAPH -> viewModel.setScreenViewMode(ScreenViewMode.SPLIT_SCREEN)
                ScreenViewMode.SPLIT_SCREEN -> viewModel.setScreenViewMode(ScreenViewMode.CALCULATOR_ONLY)
                ScreenViewMode.CALCULATOR_ONLY -> onBack()
            }
        }
    }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = CalculatorTheme.colors.onSurface
                        )
                    }
                },
                title = {
                    Text(
                        text = "Calcolatrice avanzata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CalculatorTheme.colors.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CalculatorTheme.colors.background
                )
            )
        },
        containerColor = CalculatorTheme.colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.screenViewMode) {
                // STATO C: GRAFICO A TUTTO SCHERMO
                ScreenViewMode.FULLSCREEN_GRAPH -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AdvancedGraphCanvas(
                            state = state,
                            onTransform = { zoom, pan -> viewModel.onGraphTransform(zoom, pan) },
                            onReset = { viewModel.onResetGraph() },
                            onPointSelected = { viewModel.selectGraphPoint(it) },
                            onToggleFullscreen = { viewModel.setScreenViewMode(ScreenViewMode.SPLIT_SCREEN) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        )

                        // Floating Action Pill per tornare alla tastiera
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp)
                                .background(Color(0xFF2563EB), RoundedCornerShape(24.dp))
                                .clickable { viewModel.setScreenViewMode(ScreenViewMode.SPLIT_SCREEN) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Torna al Calcolatore",
                                tint = Color.White
                            )
                            Text(
                                text = "Apri Tastiera Calcolo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // STATO A & B: CALCOLATORE STANDARD & GRAFICO DIVISO
                // Struttura a 2 sezioni per garantire che tutti i tasti siano SEMPRE visibili al 100%
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.screenViewMode == ScreenViewMode.SPLIT_SCREEN) {
                            // Quando il grafico è mostrato, occupa TUTTO lo spazio disponibile sopra la tastiera
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                AdvancedFormulaDisplay(
                                    state = state,
                                    onSelectFormula = { viewModel.selectFormula(it) },
                                    onAddFormula = { viewModel.addFormula() },
                                    onRemoveFormula = { viewModel.removeFormula(it) },
                                    onCycleFormat = { viewModel.onCycleFormat() },
                                    onCycleColor = { viewModel.cycleEquationColor(it) },
                                    onToggleGraph = { viewModel.toggleGraphView() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                MinimizedGraphCard(
                                    state = state,
                                    onTransform = { zoom, pan -> viewModel.onGraphTransform(zoom, pan) },
                                    onReset = { viewModel.onResetGraph() },
                                    onPointSelected = { viewModel.selectGraphPoint(it) },
                                    onExpandFullscreen = { viewModel.setScreenViewMode(ScreenViewMode.FULLSCREEN_GRAPH) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        } else {
                            // Modalità solo calcolatrice (grafico nascosto): scrollabile per formule multiple
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AdvancedFormulaDisplay(
                                    state = state,
                                    onSelectFormula = { viewModel.selectFormula(it) },
                                    onAddFormula = { viewModel.addFormula() },
                                    onRemoveFormula = { viewModel.removeFormula(it) },
                                    onCycleFormat = { viewModel.onCycleFormat() },
                                    onCycleColor = { viewModel.cycleEquationColor(it) },
                                    onToggleGraph = { viewModel.toggleGraphView() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // 2. ZONA INFERIORE: TASTIERA ANCORATA ALLA BASE - SEMPRE VISIBILE AL 100%
                        AdvancedKeypad(
                            state = state,
                            onTabSelected = { viewModel.selectTab(it) },
                            onInsertChar = { viewModel.insertCharacter(it) },
                            onMoveCursorLeft = { viewModel.moveCursorLeft() },
                            onMoveCursorRight = { viewModel.moveCursorRight() },
                            onDeleteChar = { viewModel.deleteAtCursor() },
                            onClearAll = { viewModel.onClearAll() },
                            onCycleFormat = { viewModel.onCycleFormat() },
                            onExecute = { viewModel.onExecute() },
                            onToggleRadians = { viewModel.toggleRadians() },
                            isToolBarExpanded = isToolBarExpanded,
                            onToggleToolBar = { isToolBarExpanded = !isToolBarExpanded },
                            onNavigate = { screen ->
                                isToolBarExpanded = false
                                onNavigate(screen)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Overlay di dismiss al tocco fuori dalla toolbar quando aperta in modalità tastiera
            if (isToolBarExpanded && state.screenViewMode != ScreenViewMode.FULLSCREEN_GRAPH) {
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

/**
 * Pulsante grafico posizionato appena sotto la finestra di testo.
 * Apre la versione minimizzata del grafico inline, oppure la richiude.
 */
@Composable
fun InlineGraphToggleButton(
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val bgColor = if (isOpen) {
        if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
    } else {
        if (isDark) Color(0xFF262C3E) else CalculatorTheme.colors.surface
    }
    val contentColor = if (isOpen) {
        if (isDark) Color.White else Color(0xFF1D4ED8)
    } else {
        if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)
    }
    val borderColor = if (isOpen) {
        Color(0xFF60A5FA)
    } else {
        if (isDark) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFF93C5FD)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(percent = 50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (isOpen) "Nascondi Grafico ✕" else "Mostra Grafico 📈",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Card Grafico Minimizzato:
 * Mostra il piano cartesiano 2D con altezza compatta (~170dp),
 * con legenda colori delle equazioni e tasto per espandere a schermo intero.
 */
@Composable
fun MinimizedGraphCard(
    state: AdvancedCalculatorState,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onReset: () -> Unit,
    onPointSelected: (Pair<Float, Float>?) -> Unit,
    onExpandFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val cardBg = if (isDark) Color(0xFF181B26) else CalculatorTheme.colors.surface
    val cardBorder = if (isDark) Color(0xFF353C52) else CalculatorTheme.colors.operatorContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Canvas Cartesiano interattivo 2D
            AdvancedGraphCanvas(
                state = state,
                onTransform = onTransform,
                onReset = onReset,
                onPointSelected = onPointSelected,
                onToggleFullscreen = onExpandFullscreen,
                modifier = Modifier.fillMaxSize()
            )

            // Header bar overlay: Legenda rapida dei colori delle curve
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(Color(0xCC1A1D27), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.equations.forEachIndexed { idx, eq ->
                    if (eq.expression.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(eq.color)
                            )
                            Text(
                                text = "y${idx + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = eq.color
                            )
                        }
                    }
                }
            }

            // Pulsante rapido "Espandi ⤢" nell'angolo in basso a destra
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0xDD1E3A8A), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(12.dp))
                    .clickable { onExpandFullscreen() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Espandi a Schermo Intero",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Espandi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Pulsante grafico scorporato (retrocompatibile per test legacy se invocato).
 */
@Composable
fun DetachedGraphButton(
    screenViewMode: ScreenViewMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor, label) = when (screenViewMode) {
        ScreenViewMode.CALCULATOR_ONLY -> Triple(Color(0xFF2C3042), Color(0xFF93C5FD), "Grafico 📈")
        ScreenViewMode.SPLIT_SCREEN -> Triple(Color(0xFF2563EB), Color.White, "Split 2D ✕")
        ScreenViewMode.FULLSCREEN_GRAPH -> Triple(Color(0xFF1D4ED8), Color.White, "Full 2D")
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(percent = 50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
