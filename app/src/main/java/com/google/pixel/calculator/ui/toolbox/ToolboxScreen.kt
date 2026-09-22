package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.ui.ActiveScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(
    onBack: () -> Unit,
    onSelectTool: (ActiveScreen) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna alla calcolatrice"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Strumenti e Funzioni",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // SEZIONE 1: CALCOLO AVANZATO
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Calcolo Avanzato",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                ToolCard(
                    icon = "🧮",
                    title = "Calcolatrice Avanzata & Grafica",
                    description = "Calcolo scientifico, equazioni, disequazioni, derivate, integrali e grafici 2D interattivi",
                    onClick = { onSelectTool(ActiveScreen.ADVANCED_CALCULATOR) }
                )

                ToolCard(
                    icon = "📐",
                    title = "Risolutore di Equazioni",
                    description = "Equazioni di 1°, 2° grado e sistemi lineari a due incognite con passaggi",
                    onClick = { onSelectTool(ActiveScreen.EQUATION_SOLVER) }
                )

                ToolCard(
                    icon = "📈",
                    title = "Grafici 2D di Funzioni",
                    description = "Grafico cartesiano interattivo con zoom, pan, tracciamento radici e massimi",
                    onClick = { onSelectTool(ActiveScreen.GRAPH_2D) }
                )
            }

            // SEZIONE 2: SENSORI E STRUMENTI FISICI PIXEL
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Utilità e Sensori Pixel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                ToolCard(
                    icon = "⚖️",
                    title = "Convertitore di Unità",
                    description = "Lunghezza, massa, temperatura, volume, velocità e archiviazione dati offline",
                    onClick = { onSelectTool(ActiveScreen.TOOLBOX_CONVERTER) }
                )

                ToolCard(
                    icon = "🟢",
                    title = "Livella a Bolla Digitale",
                    description = "Misurazione dell'inclinazione su due assi con feedback haptic e accelerometro",
                    onClick = { onSelectTool(ActiveScreen.TOOLBOX_LEVEL) }
                )

                ToolCard(
                    icon = "🧭",
                    title = "Bussola Digitale",
                    description = "Orientamento magnetico 360°, calcolo azimut e indicazione punti cardinali",
                    onClick = { onSelectTool(ActiveScreen.TOOLBOX_COMPASS) }
                )

                ToolCard(
                    icon = "📏",
                    title = "Righello su Schermo",
                    description = "Scala millimetrica e pollici calibrata fisicamente sui display Pixel",
                    onClick = { onSelectTool(ActiveScreen.TOOLBOX_RULER) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToolCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
