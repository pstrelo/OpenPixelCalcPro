package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCalculatorScreen(
    onBack: () -> Unit,
    viewModel: AdvancedCalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna agli strumenti",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Calcolatrice Scientifica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                actions = {
                    // Pillola DEG / RAD
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF383C4E), RoundedCornerShape(14.dp))
                            .clickable { viewModel.toggleRadians() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (state.isRadians) "RAD" else "DEG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E212D)
                )
            )
        },
        containerColor = Color(0xFF1E212D)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.screenViewMode) {
                // STATO A: SOLO CALCOLATORE SCIENTIFICO
                ScreenViewMode.CALCULATOR_ONLY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        AdvancedFormulaDisplay(
                            state = state,
                            onSelectFormula = { viewModel.selectFormula(it) },
                            onAddFormula = { viewModel.addFormula() },
                            onRemoveFormula = { viewModel.removeFormula(it) },
                            onCycleFormat = { viewModel.onCycleFormat() }
                        )

                        AdvancedKeypad(
                            state = state,
                            onKeyClick = { viewModel.onKeyClick(it) },
                            onExecute = { viewModel.onExecute() },
                            onCycleFormat = { viewModel.onCycleFormat() },
                            onBackspace = { viewModel.onBackspace() },
                            onClearAll = { viewModel.onClearAll() },
                            onOpenCatalog = { viewModel.openCatalog() },
                            onToggleGraph = { viewModel.toggleGraphView() }
                        )
                    }
                }

                // STATO B: SPLIT-SCREEN (Metà superiore Grafico, metà inferiore Display + Tastiera)
                ScreenViewMode.SPLIT_SCREEN -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Finestra Grafico 2D
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            AdvancedGraphCanvas(
                                state = state,
                                onTransform = { zoom, pan -> viewModel.onGraphTransform(zoom, pan) },
                                onReset = { viewModel.onResetGraph() },
                                onPointSelected = { viewModel.selectGraphPoint(it) },
                                onToggleFullscreen = { viewModel.setScreenViewMode(ScreenViewMode.FULLSCREEN_GRAPH) }
                            )
                        }

                        // 2. Display + Tastiera
                        Column(
                            modifier = Modifier
                                .weight(1.4f)
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            AdvancedFormulaDisplay(
                                state = state,
                                onSelectFormula = { viewModel.selectFormula(it) },
                                onAddFormula = { viewModel.addFormula() },
                                onRemoveFormula = { viewModel.removeFormula(it) },
                                onCycleFormat = { viewModel.onCycleFormat() }
                            )

                            AdvancedKeypad(
                                state = state,
                                onKeyClick = { viewModel.onKeyClick(it) },
                                onExecute = { viewModel.onExecute() },
                                onCycleFormat = { viewModel.onCycleFormat() },
                                onBackspace = { viewModel.onBackspace() },
                                onClearAll = { viewModel.onClearAll() },
                                onOpenCatalog = { viewModel.openCatalog() },
                                onToggleGraph = { viewModel.toggleGraphView() }
                            )
                        }
                    }
                }

                // STATO C: GRAFICO A TUTTO SCHERMO
                ScreenViewMode.FULLSCREEN_GRAPH -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AdvancedGraphCanvas(
                            state = state,
                            onTransform = { zoom, pan -> viewModel.onGraphTransform(zoom, pan) },
                            onReset = { viewModel.onResetGraph() },
                            onPointSelected = { viewModel.selectGraphPoint(it) },
                            onToggleFullscreen = { viewModel.setScreenViewMode(ScreenViewMode.SPLIT_SCREEN) },
                            modifier = Modifier.padding(8.dp)
                        )

                        // Floating Action Pill per ripristinare il calcolo
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
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
            }

            // Bottom sheet modale per il Catalogo Funzioni / f(x)
            AdvancedCatalogSheet(
                isOpen = state.isCatalogOpen,
                onDismiss = { viewModel.closeCatalog() },
                onSelectItem = { viewModel.onSelectCatalogItem(it) }
            )
        }
    }
}
