package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AdvancedGraphCanvas(
    state: AdvancedCalculatorState,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onReset: () -> Unit,
    onPointSelected: (Pair<Float, Float>?) -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleEquationVisibility: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewport = state.graphViewport

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF222533), RoundedCornerShape(16.dp))
            .fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onTransform(zoom, pan)
                    }
                }
                .pointerInput(state.equations, viewport) {
                    detectTapGestures { tapOffset ->
                        val canvasSize = size
                        val (mathX, mathY) = viewport.screenToMath(tapOffset, Size(canvasSize.width.toFloat(), canvasSize.height.toFloat()))

                        // Magnetic snap: search analytical roots or sampled points nearby (< 40px)
                        var closestPoint: Pair<Float, Float>? = null
                        var minDistance = 40f

                        for (eq in state.equations) {
                            if (!eq.isVisible) continue
                            // 1. Priority: analytical roots of the equation
                            for (root in eq.roots) {
                                val rootScreen = viewport.mathToScreen(root, 0.0, Size(canvasSize.width.toFloat(), canvasSize.height.toFloat()))
                                val dist = (tapOffset - rootScreen).getDistance()
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closestPoint = Pair(root.toFloat(), 0f)
                                }
                            }

                            // 2. Sampled points on the curve
                            for (p in eq.sampledPoints) {
                                if (p != null) {
                                    val screenPoint = viewport.mathToScreen(p.x.toDouble(), p.y.toDouble(), Size(canvasSize.width.toFloat(), canvasSize.height.toFloat()))
                                    val dist = (tapOffset - screenPoint).getDistance()
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
                            val roundedX = ((mathX * 100).roundToInt() / 100f)
                            val roundedY = ((mathY * 100).roundToInt() / 100f)
                            onPointSelected(Pair(roundedX, roundedY))
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val canvasSize = Size(width, height)
            val origin = viewport.mathToScreen(0.0, 0.0, canvasSize)
            val centerX = origin.x
            val centerY = origin.y
            val scale = viewport.scaleX

            // 1. Cartesian grid
            val gridStepMath = when {
                scale > 120 -> 0.5f
                scale > 50 -> 1f
                scale > 20 -> 2f
                else -> 5f
            }
            val gridStepPx = gridStepMath * scale

            if (gridStepPx > 0.1f) {
                val leftRem = centerX % gridStepPx
                var curX = if (leftRem < 0) leftRem + gridStepPx else leftRem
                while (curX <= width) {
                    drawLine(
                        color = Color(0x1A94A3B8),
                        start = Offset(curX, 0f),
                        end = Offset(curX, height),
                        strokeWidth = 1f
                    )
                    curX += gridStepPx
                }

                val topRem = centerY % gridStepPx
                var curY = if (topRem < 0) topRem + gridStepPx else topRem
                while (curY <= height) {
                    drawLine(
                        color = Color(0x1A94A3B8),
                        start = Offset(0f, curY),
                        end = Offset(width, curY),
                        strokeWidth = 1f
                    )
                    curY += gridStepPx
                }
            }

            // 2. Shaded inequality regions (Per-Equation Shaded Intervals)
            val anyEqHasIntervals = state.equations.any { it.isVisible && it.shadedIntervals.isNotEmpty() }
            if (anyEqHasIntervals) {
                for (eq in state.equations) {
                    if (!eq.isVisible) continue
                    val isStrict = listOf("<", ">").any { eq.expression.contains(it) } &&
                        listOf("<=", ">=", "≤", "≥").none { eq.expression.contains(it) }
                    val pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f) else null

                    for (interval in eq.shadedIntervals) {
                        val xLeft = viewport.mathToScreen(interval.from.toDouble(), 0.0, canvasSize).x
                        val xRight = viewport.mathToScreen(interval.to.toDouble(), 0.0, canvasSize).x
                        val leftClamped = xLeft.coerceIn(0f, width)
                        val rightClamped = xRight.coerceIn(0f, width)

                        if (rightClamped > leftClamped) {
                            // Translucent region background matching equation color
                            drawRect(
                                color = eq.color.copy(alpha = 0.22f),
                                topLeft = Offset(leftClamped, 0f),
                                size = Size(rightClamped - leftClamped, height)
                            )
                        }

                        // Vertical boundary lines matching equation color (omit infinite bounds at +/-50)
                        if (interval.from > -45f && xLeft in 0f..width) {
                            drawLine(
                                color = eq.color,
                                start = Offset(xLeft, 0f),
                                end = Offset(xLeft, height),
                                strokeWidth = 2.5.dp.toPx(),
                                pathEffect = pathEffect
                            )
                        }
                        if (interval.to < 45f && xRight in 0f..width) {
                            drawLine(
                                color = eq.color,
                                start = Offset(xRight, 0f),
                                end = Offset(xRight, height),
                                strokeWidth = 2.5.dp.toPx(),
                                pathEffect = pathEffect
                            )
                        }
                    }
                }
            } else if (state.shadedIntervals.isNotEmpty() && state.equations.any { listOf("<=", ">=", "≤", "≥", "<", ">").any { op -> it.expression.contains(op) } }) {
                // Fallback for legacy state when an inequality equation is present
                for (interval in state.shadedIntervals) {
                    val xLeft = viewport.mathToScreen(interval.from.toDouble(), 0.0, canvasSize).x
                    val xRight = viewport.mathToScreen(interval.to.toDouble(), 0.0, canvasSize).x
                    val leftClamped = xLeft.coerceIn(0f, width)
                    val rightClamped = xRight.coerceIn(0f, width)

                    drawRect(
                        color = Color(0x2810B981),
                        topLeft = Offset(leftClamped, 0f),
                        size = Size(rightClamped - leftClamped, height)
                    )

                    if (xLeft in 0f..width) {
                        drawLine(
                            color = Color(0xFF10B981),
                            start = Offset(xLeft, 0f),
                            end = Offset(xLeft, height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    if (xRight in 0f..width) {
                        drawLine(
                            color = Color(0xFF10B981),
                            start = Offset(xRight, 0f),
                            end = Offset(xRight, height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }

            // 2b. 2D Inequality Shaded Semi-planes (e.g. y >= x, y + x > 2)
            for (eq in state.equations) {
                if (!eq.isVisible) continue
                val ineq = eq.inequality2D
                if (ineq != null && eq.sampledPoints.isNotEmpty()) {
                    val segments = mutableListOf<MutableList<AdvancedMathEngine.Point>>()
                    var currentSegment = mutableListOf<AdvancedMathEngine.Point>()
                    for (p in eq.sampledPoints) {
                        if (p != null) {
                            currentSegment.add(p)
                        } else {
                            if (currentSegment.isNotEmpty()) {
                                segments.add(currentSegment)
                                currentSegment = mutableListOf()
                            }
                        }
                    }
                    if (currentSegment.isNotEmpty()) {
                        segments.add(currentSegment)
                    }

                    val edgeY = if (ineq.isAbove) -20f else height + 20f
                    for (segment in segments) {
                        if (segment.isNotEmpty()) {
                            val shadePath = Path()
                            val firstScreen = viewport.mathToScreen(segment.first().x.toDouble(), segment.first().y.toDouble(), canvasSize)
                            val lastScreen = viewport.mathToScreen(segment.last().x.toDouble(), segment.last().y.toDouble(), canvasSize)

                            shadePath.moveTo(firstScreen.x, edgeY)
                            for (p in segment) {
                                val screenPt = viewport.mathToScreen(p.x.toDouble(), p.y.toDouble(), canvasSize)
                                val yClamped = screenPt.y.coerceIn(-height * 2f, height * 3f)
                                shadePath.lineTo(screenPt.x, yClamped)
                            }
                            shadePath.lineTo(lastScreen.x, edgeY)
                            shadePath.close()

                            drawPath(
                                path = shadePath,
                                color = eq.color.copy(alpha = 0.22f)
                            )
                        }
                    }
                }
            }

            // 3. Main Cartesian axes (X and Y)
            if (centerY in 0f..height) {
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2.dp.toPx()
                )
            }
            if (centerX in 0f..width) {
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 4. Numerical labels and ticks
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 11.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            if (gridStepMath > 0.001f) {
                val domain = viewport.getVisibleDomainX(width)
                val firstTickX = (kotlin.math.floor(domain.first / gridStepMath) * gridStepMath).toFloat()
                var tX = firstTickX
                while (tX <= domain.second) {
                    if (abs(tX) > 1e-4) {
                        val px = viewport.mathToScreen(tX.toDouble(), 0.0, canvasSize).x
                        val pyClamped = centerY.coerceIn(15f, height - 25f)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(px, pyClamped - 4f),
                            end = Offset(px, pyClamped + 4f),
                            strokeWidth = 1.5f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            if (tX == tX.toInt().toFloat()) tX.toInt().toString() else "%.1f".format(tX),
                            px,
                            pyClamped + 18f,
                            paint
                        )
                    }
                    tX += gridStepMath
                }

                val yPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                val halfSpanY = (height / (2f * scale)).toDouble()
                val yMin = viewport.centerMathY - halfSpanY
                val yMax = viewport.centerMathY + halfSpanY
                val firstTickY = (kotlin.math.floor(yMin / gridStepMath) * gridStepMath).toFloat()
                var tY = firstTickY
                while (tY <= yMax) {
                    if (abs(tY) > 1e-4) {
                        val py = viewport.mathToScreen(0.0, tY.toDouble(), canvasSize).y
                        val pxClamped = centerX.coerceIn(25f, width - 15f)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(pxClamped - 4f, py),
                            end = Offset(pxClamped + 4f, py),
                            strokeWidth = 1.5f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            if (tY == tY.toInt().toFloat()) tY.toInt().toString() else "%.1f".format(tY),
                            pxClamped - 8f,
                            py + 4f,
                            yPaint
                        )
                    }
                    tY += gridStepMath
                }
            }

            // 5. Multi-equation mathematical curve rendering
            for (eq in state.equations) {
                if (!eq.isVisible) continue
                if (eq.sampledPoints.isNotEmpty()) {
                    val path = Path()
                    var isDrawing = false

                    for (p in eq.sampledPoints) {
                        if (p != null) {
                            val screenPoint = viewport.mathToScreen(p.x.toDouble(), p.y.toDouble(), canvasSize)
                            val yClamped = screenPoint.y.coerceIn(-height * 2f, height * 3f)
                            if (!isDrawing) {
                                path.moveTo(screenPoint.x, yClamped)
                                isDrawing = true
                            } else {
                                path.lineTo(screenPoint.x, yClamped)
                            }
                        } else {
                            isDrawing = false
                        }
                    }

                    val ineq = eq.inequality2D
                    val pathEffect = if (ineq?.isStrict == true) {
                        PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    } else {
                        null
                    }

                    drawPath(
                        path = path,
                        color = eq.color,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = pathEffect
                        )
                    )
                }
            }

            // 6. Analytical roots tracking (Zero Marker)
            for (eq in state.equations) {
                if (!eq.isVisible) continue
                for (root in eq.roots) {
                    val rootScreen = viewport.mathToScreen(root, 0.0, canvasSize)
                    if (rootScreen.x in 0f..width && rootScreen.y in 0f..height) {
                        drawCircle(
                            color = eq.color.copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = rootScreen
                        )
                        drawCircle(
                            color = eq.color,
                            radius = 4.dp.toPx(),
                            center = rootScreen
                        )
                    }
                }
            }

            // 7. Selected point with coordinate callout (x; y)
            val selPoint = state.selectedPoint
            if (selPoint != null) {
                val screenPt = viewport.mathToScreen(selPoint.first.toDouble(), selPoint.second.toDouble(), canvasSize)
                // Highlight aura
                drawCircle(
                    color = Color(0x6660A5FA),
                    radius = 12.dp.toPx(),
                    center = screenPt
                )
                drawCircle(
                    color = Color(0xFF60A5FA),
                    radius = 5.dp.toPx(),
                    center = screenPt
                )

                // Coordinate callout (x; y)
                val coordText = "(%.2f; %.2f)".format(selPoint.first, selPoint.second)
                val calloutPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                val calloutBgPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#DD1E212D")
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }

                val textW = calloutPaint.measureText(coordText)
                val bubbleLeft = (screenPt.x - textW / 2f - 12f).coerceIn(10f, width - textW - 24f)
                val bubbleTop = (screenPt.y - 42.dp.toPx()).coerceAtLeast(10f)

                drawContext.canvas.nativeCanvas.drawRoundRect(
                    bubbleLeft,
                    bubbleTop,
                    bubbleLeft + textW + 24f,
                    bubbleTop + 24.dp.toPx(),
                    12f,
                    12f,
                    calloutBgPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    coordText,
                    bubbleLeft + (textW + 24f) / 2f,
                    bubbleTop + 16.dp.toPx(),
                    calloutPaint
                )
            }
        }

        // Top bar overlay: Interactive Curve Legend (Left) & Controls (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Equations Legend Chips
            val activeEquations = state.equations.mapIndexed { idx, eq -> Pair(idx, eq) }
                .filter { it.second.expression.isNotBlank() }

            if (activeEquations.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(Color(0xCC1A1D27), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeEquations.forEach { (idx, eq) ->
                        val isVis = eq.isVisible
                        val chipBg = if (isVis) eq.color.copy(alpha = 0.20f) else Color(0x22374151)
                        val chipBorder = if (isVis) eq.color.copy(alpha = 0.75f) else Color(0x446B7280)
                        val contentColor = if (isVis) eq.color else Color(0xFF94A3B8)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                                .clickable { onToggleEquationVisibility(idx) }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isVis) eq.color else Color.Transparent)
                                    .then(if (!isVis) Modifier.border(1.dp, Color(0xFF94A3B8), CircleShape) else Modifier)
                            )
                            Text(
                                text = AdvancedCalculatorViewModel.getFunctionName(idx),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                textDecoration = if (!isVis) TextDecoration.LineThrough else null
                            )
                            if (!isVis) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(R.string.hide_graph),
                                    tint = contentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Quick Controls (Zoom +, Zoom -, Reset, Fullscreen)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CanvasOverlayButton(onClick = { onTransform(1.25f, Offset.Zero) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.zoom_in), tint = Color.White, modifier = Modifier.size(16.dp))
                }
                CanvasOverlayButton(onClick = { onTransform(0.8f, Offset.Zero) }) {
                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.zoom_out), tint = Color.White, modifier = Modifier.size(16.dp))
                }
                CanvasOverlayButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_view), tint = Color.White, modifier = Modifier.size(16.dp))
                }
                CanvasOverlayButton(onClick = onToggleFullscreen) {
                    Icon(
                        imageVector = if (state.screenViewMode == ScreenViewMode.FULLSCREEN_GRAPH) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen,
                        contentDescription = stringResource(R.string.fullscreen),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CanvasOverlayButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color(0x99242736), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
