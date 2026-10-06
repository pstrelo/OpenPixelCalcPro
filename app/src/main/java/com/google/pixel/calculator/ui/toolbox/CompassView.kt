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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bussola Digitale",
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

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Colonna sinistra: Scheda azimut e orientamento
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${compassData.azimuthDegrees.toInt()}° ${compassData.cardinalDirection}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Orientamento verso il Nord Magnetico",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = "Tieni il telefono in piano.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Colonna destra: Quadrante Bussola (220dp)
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
                                color = MaterialTheme.colorScheme.surfaceContainer ?: Color(0xFF1E1E1E),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val dialOutlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        val primaryNeedleColor = MaterialTheme.colorScheme.error
                        val secondaryNeedleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f - 16f

                            // Anello esterno
                            drawCircle(dialOutlineColor, radius = radius, center = center, style = Stroke(width = 3f))

                            // Ruota la freccia e i riferimenti in base all'azimut inverso
                            rotate(-compassData.azimuthDegrees, pivot = center) {
                                // Ago Nord
                                val northPath = Path().apply {
                                    moveTo(center.x, center.y - radius + 12f)
                                    lineTo(center.x - 14f, center.y)
                                    lineTo(center.x + 14f, center.y)
                                    close()
                                }
                                drawPath(northPath, color = primaryNeedleColor)

                                // Ago Sud
                                val southPath = Path().apply {
                                    moveTo(center.x, center.y + radius - 12f)
                                    lineTo(center.x - 14f, center.y)
                                    lineTo(center.x + 14f, center.y)
                                    close()
                                }
                                drawPath(southPath, color = secondaryNeedleColor)

                                // Tacche ogni 30 gradi
                                for (angle in 0 until 360 step 30) {
                                    rotate(angle.toFloat(), pivot = center) {
                                        val tickLen = if (angle % 90 == 0) 20f else 10f
                                        drawLine(
                                            color = if (angle == 0) primaryNeedleColor else dialOutlineColor,
                                            start = Offset(center.x, center.y - radius),
                                            end = Offset(center.x, center.y - radius + tickLen),
                                            strokeWidth = if (angle % 90 == 0) 3.5f else 2f
                                        )
                                    }
                                }
                            }

                            // Perno centrale
                            drawCircle(Color.White, radius = 8f, center = center)
                            drawCircle(primaryNeedleColor, radius = 4f, center = center)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${compassData.azimuthDegrees.toInt()}° ${compassData.cardinalDirection}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Orientamento verso il Nord Magnetico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quadrante Bussola Canvas
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer ?: Color(0xFF1E1E1E),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val dialOutlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    val primaryNeedleColor = MaterialTheme.colorScheme.error // Rosso Google / M3 error
                    val secondaryNeedleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 20f

                        // Anello esterno
                        drawCircle(dialOutlineColor, radius = radius, center = center, style = Stroke(width = 3f))

                        // Ruota la freccia e i riferimenti in base all'azimut inverso
                        rotate(-compassData.azimuthDegrees, pivot = center) {
                            // Ago Nord (Rosso Material)
                            val northPath = Path().apply {
                                moveTo(center.x, center.y - radius + 15f)
                                lineTo(center.x - 18f, center.y)
                                lineTo(center.x + 18f, center.y)
                                close()
                            }
                            drawPath(northPath, color = primaryNeedleColor)

                            // Ago Sud (Colore neutro di contrasto)
                            val southPath = Path().apply {
                                moveTo(center.x, center.y + radius - 15f)
                                lineTo(center.x - 18f, center.y)
                                lineTo(center.x + 18f, center.y)
                                close()
                            }
                            drawPath(southPath, color = secondaryNeedleColor)

                            // Tacche ogni 30 gradi
                            for (angle in 0 until 360 step 30) {
                                rotate(angle.toFloat(), pivot = center) {
                                    val tickLen = if (angle % 90 == 0) 25f else 12f
                                    drawLine(
                                        color = if (angle == 0) primaryNeedleColor else dialOutlineColor,
                                        start = Offset(center.x, center.y - radius),
                                        end = Offset(center.x, center.y - radius + tickLen),
                                        strokeWidth = if (angle % 90 == 0) 4f else 2f
                                    )
                                }
                            }
                        }

                        // Perno centrale
                        drawCircle(Color.White, radius = 10f, center = center)
                        drawCircle(primaryNeedleColor, radius = 5f, center = center)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Tieni il telefono in orizzontale per la massima precisione dei sensori.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
