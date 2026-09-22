package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.utils.RulerUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulerView(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val (xdpi, ydpi) = remember { RulerUtils.getScreenDpi(context) }

    var sliderY by remember { mutableStateOf(200f) }
    var useInches by remember { mutableStateOf(false) }

    val measuredValue = remember(sliderY, ydpi, useInches) {
        if (useInches) {
            val inches = RulerUtils.pixelsToInches(sliderY, ydpi)
            "%.2f in".format(inches)
        } else {
            val mm = RulerUtils.pixelsToMm(sliderY, ydpi)
            "%.1f mm (%.2f cm)".format(mm, mm / 10f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Righello Schermo Pixel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    FilterChip(
                        selected = useInches,
                        onClick = { useInches = !useInches },
                        label = { Text(if (useInches) "Pollici (in)" else "Millimetri (mm)") }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scheda valore misurato in cima
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Misura attuale:", fontSize = 13.sp)
                        Text(
                            text = measuredValue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Trascina la linea blu",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Area righello interattiva
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B1B1B))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            sliderY = (sliderY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val dpi = ydpi

                    // Disegna tacche mm/cm
                    val totalMm = RulerUtils.pixelsToMm(height, dpi)
                    val mmStepPx = RulerUtils.mmToPixels(1f, dpi)

                    var curMm = 0
                    while (curMm <= totalMm.toInt()) {
                        val y = curMm * mmStepPx
                        val isCm = curMm % 10 == 0
                        val isHalfCm = curMm % 5 == 0

                        val tickWidth = when {
                            isCm -> 70f
                            isHalfCm -> 45f
                            else -> 25f
                        }

                        drawLine(
                            color = if (isCm) Color.White else Color(0xFF888888),
                            start = Offset(0f, y),
                            end = Offset(tickWidth, y),
                            strokeWidth = if (isCm) 3f else 1.5f
                        )

                        curMm++
                    }

                    // Linea cursore di misurazione (Blu Pixel)
                    drawLine(
                        color = Color(0xFF4285F4),
                        start = Offset(0f, sliderY),
                        end = Offset(width, sliderY),
                        strokeWidth = 4f
                    )
                    drawCircle(
                        color = Color(0xFF4285F4),
                        radius = 18f,
                        center = Offset(width - 40f, sliderY)
                    )
                }
            }
        }
    }
}
