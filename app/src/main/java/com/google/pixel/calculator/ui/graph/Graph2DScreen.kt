package com.google.pixel.calculator.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.math.GraphEvaluator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Graph2DScreen(
    onBack: () -> Unit
) {
    var formula by remember { mutableStateOf("sin(x)") }
    var scale by remember { mutableStateOf(40f) } // Pixel per unità matematica
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val graphEvaluator = remember { GraphEvaluator() }
    val parser = remember { com.google.pixel.calculator.core.math.ExpressionParser() }
    val parsedAst = remember(formula) {
        try { parser.parse(formula) } catch (e: Exception) { null }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grafici 2D f(x)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        scale = 40f
                        offsetX = 0f
                        offsetY = 0f
                    }) {
                        Text("Reset Vista")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra input formula
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text("f(x) =") },
                    placeholder = { Text("es: sin(x), x^2 - 4, 1/x") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = {
                    // Trigger ricalcolo
                }) {
                    Text("Disegna", fontWeight = FontWeight.Bold)
                }
            }

            // Pulsanti veloci di formule predefinite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(onClick = { formula = "sin(x)" }, label = { Text("sin(x)") })
                SuggestionChip(onClick = { formula = "cos(x)" }, label = { Text("cos(x)") })
                SuggestionChip(onClick = { formula = "x^2 - 4" }, label = { Text("x² - 4") })
                SuggestionChip(onClick = { formula = "1/x" }, label = { Text("1/x") })
            }

            // Area Canvas del Grafico con Pan e Zoom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(10f, 300f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f + offsetX
                    val centerY = height / 2f + offsetY

                    // 1. Griglia di sfondo
                    val gridColor = Color(0xFF333333)
                    val step = scale

                    // Linee verticali
                    var x = centerX % step
                    while (x < width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                        x += step
                    }

                    // Linee orizzontali
                    var y = centerY % step
                    while (y < height) {
                        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                        y += step
                    }

                    // 2. Assi cartesiani X e Y
                    val axisColor = Color(0xFF888888)
                    drawLine(axisColor, Offset(0f, centerY), Offset(width, centerY), strokeWidth = 2.5f) // Asse X
                    drawLine(axisColor, Offset(centerX, 0f), Offset(centerX, height), strokeWidth = 2.5f) // Asse Y

                    // 3. Campionamento e tracciamento della curva f(x)
                    val xMinMath = (0f - centerX) / scale
                    val xMaxMath = (width - centerX) / scale

                    val points = graphEvaluator.sampleAst(parsedAst, xMinMath, xMaxMath, steps = 250)

                    val curveColor = Color(0xFF4CAF50) // Verde Google Pixel
                    val path = Path()
                    var isDrawing = false

                    for (p in points) {
                        if (p != null) {
                            val canvasX = centerX + p.x * scale
                            val canvasY = centerY - p.y * scale

                            // Limita per evitare overflow di canvas
                            if (canvasY in -1000f..(height + 1000f)) {
                                if (!isDrawing) {
                                    path.moveTo(canvasX, canvasY)
                                    isDrawing = true
                                } else {
                                    path.lineTo(canvasX, canvasY)
                                }
                            } else {
                                isDrawing = false
                            }
                        } else {
                            isDrawing = false
                        }
                    }

                    drawPath(path, color = curveColor, style = Stroke(width = 3.5f))
                }

                // Indicatore zoom in basso a destra
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Scala: ${(scale / 40f * 100).toInt()}% | Usa tocco per zoom/spostamento",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
