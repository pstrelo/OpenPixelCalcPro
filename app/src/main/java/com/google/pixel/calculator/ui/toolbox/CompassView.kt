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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.sensors.CompassSensorManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val compassManager = remember { CompassSensorManager(context) }
    val compassData by compassManager.getCompassFlow().collectAsState(
        initial = CompassSensorManager.CompassData(0f, "N")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bussola Digitale") },
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${compassData.azimuthDegrees.toInt()}° ${compassData.cardinalDirection}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Orientamento verso il Nord Magnetico",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quadrante Bussola Canvas
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color(0xFF1E1E1E), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 20f

                    // Anello esterno
                    drawCircle(Color(0xFF444444), radius = radius, center = center, style = Stroke(width = 3f))

                    // Ruota la freccia e i riferimenti in base all'azimut inverso
                    rotate(-compassData.azimuthDegrees, pivot = center) {
                        // Ago Nord (Rosso)
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 15f)
                            lineTo(center.x - 18f, center.y)
                            lineTo(center.x + 18f, center.y)
                            close()
                        }
                        drawPath(northPath, color = Color(0xFFE53935))

                        // Ago Sud (Grigio/Bianco)
                        val southPath = Path().apply {
                            moveTo(center.x, center.y + radius - 15f)
                            lineTo(center.x - 18f, center.y)
                            lineTo(center.x + 18f, center.y)
                            close()
                        }
                        drawPath(southPath, color = Color(0xFFCCCCCC))

                        // Tacche ogni 30 gradi
                        for (angle in 0 until 360 step 30) {
                            rotate(angle.toFloat(), pivot = center) {
                                val tickLen = if (angle % 90 == 0) 25f else 12f
                                drawLine(
                                    color = if (angle == 0) Color(0xFFE53935) else Color.White,
                                    start = Offset(center.x, center.y - radius),
                                    end = Offset(center.x, center.y - radius + tickLen),
                                    strokeWidth = if (angle % 90 == 0) 4f else 2f
                                )
                            }
                        }
                    }

                    // Perno centrale
                    drawCircle(Color.White, radius = 10f, center = center)
                    drawCircle(Color(0xFFE53935), radius = 5f, center = center)
                }
            }

            Text(
                text = "Tieni il telefono in orizzontale lontano da forti campi magnetici per la massima precisione.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
