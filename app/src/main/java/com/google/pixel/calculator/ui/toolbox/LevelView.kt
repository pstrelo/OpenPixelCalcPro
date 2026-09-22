package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { LevelSensorManager(context) }
    val levelData by sensorManager.getLevelFlow().collectAsState(
        initial = LevelSensorManager.LevelData(0f, 0f, true)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Livella a Bolla Digitale") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (levelData.isLevel) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (levelData.isLevel) "Perfettamente in piano!" else "Inclinazione attiva",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (levelData.isLevel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Roll (X): ${"%.1f".format(levelData.roll)}° | Pitch (Y): ${"%.1f".format(levelData.pitch)}°",
                        fontSize = 15.sp,
                        color = if (levelData.isLevel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Canvas Livella a bolla d'aria
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color(0xFF242424), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 20f

                    // Anelli concentrici di riferimento
                    drawCircle(Color(0xFF555555), radius = radius, center = center, style = Stroke(width = 3f))
                    drawCircle(Color(0xFF555555), radius = radius * 0.6f, center = center, style = Stroke(width = 2f))
                    drawCircle(Color(0xFF555555), radius = radius * 0.2f, center = center, style = Stroke(width = 2f))

                    // Linee a croce del mirino
                    drawLine(Color(0xFF555555), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 2f)
                    drawLine(Color(0xFF555555), Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = 2f)

                    // Posizione bolla (limitata al raggio dell'indicatore)
                    val maxOffset = radius - 35f
                    val bubbleX = (center.x + (levelData.roll / 45f) * maxOffset).coerceIn(center.x - maxOffset, center.x + maxOffset)
                    val bubbleY = (center.y + (levelData.pitch / 45f) * maxOffset).coerceIn(center.y - maxOffset, center.y + maxOffset)

                    val bubbleColor = if (levelData.isLevel) Color(0xFF4CAF50) else Color(0xFFFFB300)

                    // Disegno bolla fluida con riflesso
                    drawCircle(bubbleColor, radius = 30f, center = Offset(bubbleX, bubbleY))
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = 10f, center = Offset(bubbleX - 8f, bubbleY - 8f))
                }
            }

            Text(
                text = "Appoggia il Pixel sulla superficie da verificare. La bolla si colorerà di verde a 0°.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
