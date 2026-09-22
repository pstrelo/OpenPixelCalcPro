package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AdvancedGraphCanvas(
    state: AdvancedCalculatorState,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onReset: () -> Unit,
    onPointSelected: (Pair<Float, Float>?) -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF222533), RoundedCornerShape(16.dp))
            .fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onTransform(zoom, pan)
                    }
                }
                .pointerInput(state.formulas, state.graphScale, state.graphOffsetX, state.graphOffsetY) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + state.graphOffsetX
                        val centerY = size.height / 2f + state.graphOffsetY
                        val mathX = (tapOffset.x - centerX) / state.graphScale
                        val mathY = -(tapOffset.y - centerY) / state.graphScale

                        // Controlla se abbiamo toccato vicino a una radice o a una curva
                        var closestPoint: Pair<Float, Float>? = null
                        var minDistance = 40f // raggio di aggancio in pixel

                        for (f in state.formulas) {
                            for (p in f.sampledPoints) {
                                if (p != null) {
                                    val screenX = centerX + p.x * state.graphScale
                                    val screenY = centerY - p.y * state.graphScale
                                    val dist = (tapOffset - Offset(screenX, screenY)).getDistance()
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        closestPoint = Pair(p.x, p.y)
                                    }
                                }
                            }
                        }

                        if (closestPoint != null) {
                            onPointSelected(closestPoint)
                        } else {
                            // Seleziona il punto geometrico toccato
                            val roundedX = ((mathX * 100).roundToInt() / 100f)
                            val roundedY = ((mathY * 100).roundToInt() / 100f)
                            onPointSelected(Pair(roundedX, roundedY))
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + state.graphOffsetX
            val centerY = height / 2f + state.graphOffsetY
            val scale = state.graphScale

            // 1. Disegna la griglia di fondo
            val gridStepMath = when {
                scale > 120 -> 0.5f
                scale > 50 -> 1f
                scale > 20 -> 2f
                else -> 5f
            }
            val gridStepPx = gridStepMath * scale

            val startX = (centerX % gridStepPx) - gridStepPx
            var curX = startX
            while (curX < width + gridStepPx) {
                drawLine(
                    color = Color(0xFF2E3347),
                    start = Offset(curX, 0f),
                    end = Offset(curX, height),
                    strokeWidth = 1f
                )
                curX += gridStepPx
            }

            val startY = (centerY % gridStepPx) - gridStepPx
            var curY = startY
            while (curY < height + gridStepPx) {
                drawLine(
                    color = Color(0xFF2E3347),
                    start = Offset(0f, curY),
                    end = Offset(width, curY),
                    strokeWidth = 1f
                )
                curY += gridStepPx
            }

            // 2. Disegna gli assi cartesiani principali (X e Y)
            // Asse X
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx()
            )
            // Asse Y
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = 2.dp.toPx()
            )

            // 3. Etichette numeriche e tacche
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 11.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            // Numeri asse X
            val xMin = -centerX / scale
            val xMax = (width - centerX) / scale
            val firstTickX = (kotlin.math.floor(xMin / gridStepMath) * gridStepMath).toFloat()
            var tX = firstTickX
            while (tX <= xMax) {
                if (abs(tX) > 1e-4) {
                    val px = centerX + tX * scale
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(px, centerY - 4.dp.toPx()),
                        end = Offset(px, centerY + 4.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    val label = if (tX == tX.toLong().toFloat()) tX.toLong().toString() else "%.1f".format(tX)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        px,
                        centerY + 16.dp.toPx(),
                        paint
                    )
                }
                tX += gridStepMath
            }

            // Numeri asse Y
            val yMin = -(height - centerY) / scale
            val yMax = centerY / scale
            val firstTickY = (kotlin.math.floor(yMin / gridStepMath) * gridStepMath).toFloat()
            var tY = firstTickY
            while (tY <= yMax) {
                if (abs(tY) > 1e-4) {
                    val py = centerY - tY * scale
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(centerX - 4.dp.toPx(), py),
                        end = Offset(centerX + 4.dp.toPx(), py),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    val label = if (tY == tY.toLong().toFloat()) tY.toLong().toString() else "%.1f".format(tY)
                    val alignLeftPaint = android.graphics.Paint(paint).apply {
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        centerX - 8.dp.toPx(),
                        py + 4.dp.toPx(),
                        alignLeftPaint
                    )
                }
                tY += gridStepMath
            }

            // Etichette "X" e "Y"
            drawContext.canvas.nativeCanvas.drawText("X", width - 16.dp.toPx(), centerY - 8.dp.toPx(), paint)
            drawContext.canvas.nativeCanvas.drawText("Y", centerX + 12.dp.toPx(), 20.dp.toPx(), paint)
            drawContext.canvas.nativeCanvas.drawText("0", centerX - 10.dp.toPx(), centerY + 14.dp.toPx(), paint)

            // 4. Aree ombreggiate per disequazioni
            for (interval in state.shadedIntervals) {
                val leftPx = centerX + interval.from * scale
                val rightPx = centerX + interval.to * scale
                drawRect(
                    color = Color(0x3310B981), // Verde traslucido
                    topLeft = Offset(leftPx, 0f),
                    size = androidx.compose.ui.geometry.Size(rightPx - leftPx, height)
                )
            }

            // 5. Curve delle funzioni
            for (formula in state.formulas) {
                val points = formula.sampledPoints
                if (points.isNotEmpty()) {
                    val path = Path()
                    var isDrawing = false

                    for (p in points) {
                        if (p == null) {
                            isDrawing = false
                        } else {
                            val px = centerX + p.x * scale
                            val py = centerY - p.y * scale
                            if (!isDrawing) {
                                path.moveTo(px, py)
                                isDrawing = true
                            } else {
                                path.lineTo(px, py)
                            }
                        }
                    }

                    drawPath(
                        path = path,
                        color = formula.color,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // 6. Disegna il punto selezionato o lo zero con tooltip (matching Foto 1)
            state.selectedPoint?.let { (selX, selY) ->
                val px = centerX + selX * scale
                val py = centerY - selY * scale

                // Anello esterno illuminato
                drawCircle(
                    color = Color(0x804E75F6),
                    radius = 9.dp.toPx(),
                    center = Offset(px, py)
                )
                // Pallino pieno centrale
                drawCircle(
                    color = Color(0xFF93C5FD),
                    radius = 5.dp.toPx(),
                    center = Offset(px, py)
                )

                // Fumetto con le coordinate: es. (3.187; 0)
                val coordText = "(%.3f; %.3f)".format(selX, selY)
                val bgPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#1E212D")
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }
                val borderPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#3B4252")
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 1.dp.toPx()
                    isAntiAlias = true
                }
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(coordText, 0, coordText.length, textBounds)
                val bubbleW = textBounds.width() + 24.dp.toPx()
                val bubbleH = textBounds.height() + 16.dp.toPx()
                val bubbleX = px + 12.dp.toPx()
                val bubbleY = py - 20.dp.toPx()

                val rectF = android.graphics.RectF(bubbleX, bubbleY - bubbleH, bubbleX + bubbleW, bubbleY)
                drawContext.canvas.nativeCanvas.drawRoundRect(rectF, 12.dp.toPx(), 12.dp.toPx(), bgPaint)
                drawContext.canvas.nativeCanvas.drawRoundRect(rectF, 12.dp.toPx(), 12.dp.toPx(), borderPaint)
                drawContext.canvas.nativeCanvas.drawText(
                    coordText,
                    bubbleX + bubbleW / 2f,
                    bubbleY - bubbleH / 2f + textBounds.height() / 2f,
                    textPaint
                )
            }
        }

        // Overlay con pulsanti di controllo grafici
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom In
            IconButton(
                onClick = { onTransform(1.25f, Offset.Zero) },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0x801E212D), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
            }
            // Zoom Out
            IconButton(
                onClick = { onTransform(0.8f, Offset.Zero) },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0x801E212D), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
            }
            // Reset Vista
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0x801E212D), CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Vista", tint = Color.White)
            }
            // Ingrandisci / Riduci schermo intero
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0x801E212D), CircleShape)
            ) {
                Icon(
                    imageVector = if (state.screenViewMode == ScreenViewMode.FULLSCREEN_GRAPH)
                        Icons.Default.CloseFullscreen else Icons.Default.Fullscreen,
                    contentDescription = "Schermo Intero",
                    tint = Color.White
                )
            }
        }
    }
}
