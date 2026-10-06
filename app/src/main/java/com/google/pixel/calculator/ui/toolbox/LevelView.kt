package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.toolbox.PixelFloatingToolBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelView(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val sensorManager = remember { LevelSensorManager(context) }
    val levelData by sensorManager.getLevelFlow().collectAsState(
        initial = LevelSensorManager.LevelData(0f, 0f, true)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Livella a Bolla Digitale",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val levelCardColor = if (levelData.isLevel) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
        val levelTextColor = if (levelData.isLevel) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                // Colonna sinistra: Scheda di stato e suggerimento
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = levelCardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (levelData.isLevel) "Perfettamente in piano! ✨" else "Inclinazione attiva",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = levelTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Roll (X): ${"%.1f".format(levelData.roll)}°\nPitch (Y): ${"%.1f".format(levelData.pitch)}°",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = levelTextColor.copy(alpha = 0.85f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Appoggia il Pixel su una superficie.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Colonna destra: Canvas Livella a bolla (220dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer ?: Color(0xFF242424),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val circleGuideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        val activeBubbleColor = if (levelData.isLevel) MaterialTheme.colorScheme.primary else Color(0xFFFFB300)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f - 16f

                            // Anelli concentrici di riferimento
                            drawCircle(circleGuideColor, radius = radius, center = center, style = Stroke(width = 3f))
                            drawCircle(circleGuideColor, radius = radius * 0.6f, center = center, style = Stroke(width = 2f))
                            drawCircle(circleGuideColor, radius = radius * 0.2f, center = center, style = Stroke(width = 2.5f))

                            // Linee a croce del mirino
                            drawLine(circleGuideColor, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 2f)
                            drawLine(circleGuideColor, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = 2f)

                            // Posizione bolla (limitata al raggio dell'indicatore)
                            val maxOffset = radius - 28f
                            val bubbleX = (center.x + (levelData.roll / 45f) * maxOffset).coerceIn(center.x - maxOffset, center.x + maxOffset)
                            val bubbleY = (center.y + (levelData.pitch / 45f) * maxOffset).coerceIn(center.y - maxOffset, center.y + maxOffset)

                            // Disegno bolla fluida con riflesso
                            drawCircle(activeBubbleColor, radius = 24f, center = Offset(bubbleX, bubbleY))
                            drawCircle(Color.White.copy(alpha = 0.5f), radius = 8f, center = Offset(bubbleX - 6f, bubbleY - 6f))
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = levelCardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (levelData.isLevel) "Perfettamente in piano! ✨" else "Inclinazione attiva",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = levelTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Roll (X): ${"%.1f".format(levelData.roll)}° | Pitch (Y): ${"%.1f".format(levelData.pitch)}°",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = levelTextColor.copy(alpha = 0.85f)
                        )
                    }
                }

                // Canvas Livella a bolla d'aria Expressive
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer ?: Color(0xFF242424),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val circleGuideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    val activeBubbleColor = if (levelData.isLevel) MaterialTheme.colorScheme.primary else Color(0xFFFFB300)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 20f

                        // Anelli concentrici di riferimento
                        drawCircle(circleGuideColor, radius = radius, center = center, style = Stroke(width = 3f))
                        drawCircle(circleGuideColor, radius = radius * 0.6f, center = center, style = Stroke(width = 2f))
                        drawCircle(circleGuideColor, radius = radius * 0.2f, center = center, style = Stroke(width = 2.5f))

                        // Linee a croce del mirino
                        drawLine(circleGuideColor, Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 2f)
                        drawLine(circleGuideColor, Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = 2f)

                        // Posizione bolla (limitata al raggio dell'indicatore)
                        val maxOffset = radius - 35f
                        val bubbleX = (center.x + (levelData.roll / 45f) * maxOffset).coerceIn(center.x - maxOffset, center.x + maxOffset)
                        val bubbleY = (center.y + (levelData.pitch / 45f) * maxOffset).coerceIn(center.y - maxOffset, center.y + maxOffset)

                        // Disegno bolla fluida con riflesso
                        drawCircle(activeBubbleColor, radius = 30f, center = Offset(bubbleX, bubbleY))
                        drawCircle(Color.White.copy(alpha = 0.5f), radius = 10f, center = Offset(bubbleX - 8f, bubbleY - 8f))
                    }
                }

                    Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Appoggia il Pixel sulla superficie da verificare. Si illuminerà a 0°.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        PixelFloatingToolBar(
            currentScreen = ActiveScreen.TOOLBOX_LEVEL,
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
}

