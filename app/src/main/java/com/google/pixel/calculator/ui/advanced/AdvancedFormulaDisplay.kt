package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import com.google.pixel.calculator.ui.theme.CalculatorTheme

@Composable
fun AdvancedFormulaDisplay(
    state: AdvancedCalculatorState,
    onSelectFormula: (Int) -> Unit,
    onAddFormula: () -> Unit,
    onRemoveFormula: (Int) -> Unit,
    onCycleFormat: () -> Unit = {},
    onCycleColor: (Int) -> Unit = {},
    onToggleGraph: () -> Unit = {},
    onClearAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val cardBg = if (isDark) Color(0xFF222636) else CalculatorTheme.colors.surface
    val cardBorder = if (isDark) Color(0xFF353C52) else CalculatorTheme.colors.operatorContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Equations list with interactive color badges
            state.equations.forEachIndexed { index, eq ->
                val isActive = index == state.activeEquationIndex
                val rowBorder = if (isActive) {
                    Modifier.border(1.5.dp, eq.color.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                } else {
                    val borderCol = if (isDark) Color(0xFF2E3346) else CalculatorTheme.colors.operatorContainer
                    Modifier.border(1.dp, borderCol, RoundedCornerShape(14.dp))
                }
                val rowBg = if (isActive) {
                    eq.color.copy(alpha = if (isDark) 0.14f else 0.08f)
                } else {
                    if (isDark) Color(0xFF1E2230) else CalculatorTheme.colors.background
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(rowBg, RoundedCornerShape(14.dp))
                        .then(rowBorder)
                        .clickable { onSelectFormula(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive curve/function color badge (e.g. f(x), g(x), h(x) or a, b clickable to cycle color)
                    val isAssignment = AdvancedMathEngine().isVariableAssignment(eq.expression)
                    val badgeText = if (isAssignment) {
                        eq.expression.substringBefore("=").trim()
                    } else {
                        AdvancedCalculatorViewModel.getFunctionName(index)
                    }
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(eq.color.copy(alpha = 0.20f))
                            .border(1.5.dp, eq.color, RoundedCornerShape(14.dp))
                            .clickable { onCycleColor(index) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = eq.color
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Expression with in-place cursor and natural formatting
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        FormulaMath2DRenderer(
                            expression = eq.expression,
                            isActive = isActive,
                            cursorPosition = eq.cursorPosition
                        )

                        // Single result (scalar, inequality, or error) when not a multi-root matrix
                        if (eq.formattedResult.isNotBlank() && eq.roots.size <= 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                            val isInformative = eq.formattedResult.contains("Nessun") || 
                                                eq.formattedResult.contains("Nessuna") ||
                                                eq.formattedResult.contains("nessun", ignoreCase = true)
                            val resultColor = when {
                                eq.isError && !isInformative -> Color(0xFFF87171)
                                isInformative -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                eq.formattedResult.contains("∈") || eq.formattedResult.contains("<") || eq.formattedResult.contains(">") -> Color(0xFF38BDF8)
                                else -> Color(0xFF6EE7B7)
                            }
                            val displayResult = if (eq.formattedResult == "Nessuna radice reale") {
                                stringResource(R.string.no_real_roots)
                            } else {
                                eq.formattedResult
                            }
                            Text(
                                text = displayResult,
                                fontSize = 14.sp,
                                fontWeight = if (isInformative) FontWeight.Medium else FontWeight.Bold,
                                color = resultColor
                            )
                        }
                    }

                    // Right side: Vertical bracket solution matrix for multiple roots [ x = -3 ; x = 3 ]
                    if (eq.roots.size > 1) {
                        SolutionMatrixBracket(
                            roots = eq.roots,
                            format = eq.formatMode
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Secondary equation removal button
                    if (state.equations.size > 1) {
                        IconButton(
                            onClick = { onRemoveFormula(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove_equation),
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom row: Show/Hide Graph button + "+ Add equation" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isGraphOpen = state.screenViewMode != ScreenViewMode.CALCULATOR_ONLY
                val graphBtnBg = if (isGraphOpen) {
                    if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
                } else {
                    if (isDark) Color(0xFF262C3E) else CalculatorTheme.colors.operatorContainer
                }
                val graphBtnText = if (isGraphOpen) {
                    if (isDark) Color.White else Color(0xFF1D4ED8)
                } else {
                    if (isDark) Color(0xFF93C5FD) else CalculatorTheme.colors.operatorContent
                }
                val graphBorderColor = if (isGraphOpen) {
                    Color(0xFF60A5FA)
                } else {
                    if (isDark) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFF93C5FD)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(graphBtnBg)
                        .border(1.dp, graphBorderColor, RoundedCornerShape(percent = 50))
                        .clickable { onToggleGraph() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = graphBtnText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isGraphOpen) stringResource(R.string.hide_graph) else stringResource(R.string.show_graph),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = graphBtnText
                    )
                }

                // "+ Add equation" button (up to 4 simultaneously)
                if (state.equations.size < 4) {
                    val addColor = if (isDark) Color(0xFF93C5FD) else CalculatorTheme.colors.onSurfaceVariant
                    Row(
                        modifier = Modifier
                            .clickable { onAddFormula() }
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_equation),
                            tint = addColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_equation),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = addColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vertical square bracket matrix display for multi-root equations:
 * [ x = -3 ]
 * [ x =  3 ]
 */
@Composable
fun SolutionMatrixBracket(
    roots: List<Double>,
    format: AdvancedMathEngine.ResultFormat,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val bracketBg = if (isDark) Color(0xFF1A1D27) else CalculatorTheme.colors.operatorContainer
    val bracketColor = if (isDark) Color(0xFF60A5FA) else CalculatorTheme.colors.operatorContent
    val rootColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF059669)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bracketBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Left bracket
        Text(
            text = "[",
            fontSize = if (roots.size > 2) 24.sp else 20.sp,
            fontWeight = FontWeight.Light,
            color = bracketColor,
            fontFamily = FontFamily.Monospace
        )

        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            roots.forEachIndexed { idx, r ->
                val rStr = AdvancedMathEngine().formatNumber(r, format, false)
                val label = if (roots.size <= 2) "x = " else "x${idx + 1} = "
                Text(
                    text = "$label$rStr",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = rootColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Right bracket
        Text(
            text = "]",
            fontSize = if (roots.size > 2) 24.sp else 20.sp,
            fontWeight = FontWeight.Light,
            color = bracketColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Renders 2D math formulas with:
 * - 2D Fractions (numerator over denominator with horizontal fraction bar)
 * - Elevated exponents with halved cursor height
 * - Radicals with root overbar
 * - Dynamic blinking cursor positioned exactly in the active math block
 */
@Composable
fun FormulaMath2DRenderer(
    expression: String,
    isActive: Boolean,
    cursorPosition: Int = 0,
    modifier: Modifier = Modifier
) {
    val isDark = CalculatorTheme.colors.isDark
    val textColor = if (isDark) Color.White else CalculatorTheme.colors.onSurface
    val placeholderColor = if (isDark) Color(0xFF64748B) else CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
    val divBarColor = if (isDark) Color(0xFFE2E8F0) else CalculatorTheme.colors.onSurfaceVariant
    val cursorColor = Color(0xFF60A5FA)

    if (expression.isBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            if (isActive) {
                BlinkingCursor(height = 20.dp, color = cursorColor)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = stringResource(R.string.formula_placeholder),
                color = placeholderColor,
                fontSize = 15.sp
            )
        }
        return
    }

    val safeCursor = cursorPosition.coerceIn(0, expression.length)
    val segments = Formula2DParser.parse(expression)

    // Check if cursor was rendered inside any segment
    var cursorRendered = false

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        segments.forEach { seg ->
            when (seg) {
                is FormulaSegment.SimpleText -> {
                    val inSeg = isActive && safeCursor >= seg.startOffset && safeCursor < seg.endOffset
                    if (inSeg) {
                        cursorRendered = true
                        val localPos = safeCursor - seg.startOffset
                        val before = seg.text.substring(0, localPos)
                        val after = seg.text.substring(localPos)
                        if (before.isNotEmpty()) {
                            Text(text = before, color = textColor, fontSize = 16.sp)
                        }
                        BlinkingCursor(height = 20.dp, color = cursorColor)
                        if (after.isNotEmpty()) {
                            Text(text = after, color = textColor, fontSize = 16.sp)
                        }
                    } else {
                        Text(text = seg.text, color = textColor, fontSize = 16.sp)
                    }
                }

                is FormulaSegment.PowerSegment -> {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    ) {
                        // Base on baseline
                        val inBase = isActive && safeCursor >= seg.baseStart && safeCursor < seg.baseEnd
                        if (inBase) {
                            cursorRendered = true
                            val localPos = safeCursor - seg.baseStart
                            val before = seg.baseText.substring(0, localPos)
                            val after = seg.baseText.substring(localPos)
                            if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 16.sp)
                            BlinkingCursor(height = 20.dp, color = cursorColor)
                            if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 16.sp)
                        } else {
                            Text(text = seg.baseText, color = textColor, fontSize = 16.sp)
                        }

                        // Elevated exponent (dimezzato in altezza e dimensione)
                        val inExp = isActive && safeCursor >= seg.expStart && safeCursor <= seg.expEnd
                        Box(
                            modifier = Modifier.padding(bottom = 8.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (inExp) {
                                cursorRendered = true
                                val localPos = (safeCursor - seg.expStart).coerceIn(0, seg.expText.length)
                                val before = seg.expText.substring(0, localPos)
                                val after = seg.expText.substring(localPos)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    BlinkingCursor(height = 10.dp, color = cursorColor)
                                    if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            } else {
                                Text(
                                    text = if (seg.expText.isEmpty()) "□" else seg.expText,
                                    color = if (seg.expText.isEmpty()) placeholderColor else textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                is FormulaSegment.FractionSegment -> {
                    val inNum = isActive && safeCursor >= seg.numStart && safeCursor <= seg.numEnd
                    val inDen = isActive && (safeCursor >= seg.denStart && safeCursor <= seg.denEnd || (seg.denText.isEmpty() && safeCursor == seg.slashPos))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .padding(horizontal = 4.dp)
                    ) {
                        // Numerator
                        Box(
                            modifier = Modifier
                                .padding(bottom = 1.dp)
                                .wrapContentWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (inNum) {
                                cursorRendered = true
                                val localPos = (safeCursor - seg.numStart).coerceIn(0, seg.numText.length)
                                val before = seg.numText.substring(0, localPos)
                                val after = seg.numText.substring(localPos)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 14.sp)
                                    BlinkingCursor(height = 12.dp, color = cursorColor)
                                    if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 14.sp)
                                }
                            } else {
                                Text(
                                    text = if (seg.numText.isEmpty()) "□" else seg.numText,
                                    color = if (seg.numText.isEmpty()) placeholderColor else textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Horizontal Fraction Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .background(divBarColor, RoundedCornerShape(1.dp))
                        )

                        // Denominator
                        Box(
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .wrapContentWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (inDen) {
                                cursorRendered = true
                                val localPos = (safeCursor - seg.denStart).coerceIn(0, seg.denText.length)
                                val before = seg.denText.substring(0, localPos)
                                val after = seg.denText.substring(localPos)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 14.sp)
                                    BlinkingCursor(height = 12.dp, color = cursorColor)
                                    if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 14.sp)
                                }
                            } else {
                                Text(
                                    text = if (seg.denText.isEmpty()) "□" else seg.denText,
                                    color = if (seg.denText.isEmpty()) placeholderColor else textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                is FormulaSegment.RadicalSegment -> {
                    val inRad = isActive && safeCursor >= seg.innerStart && safeCursor <= seg.innerEnd
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "√",
                            fontSize = 18.sp,
                            color = textColor,
                            fontWeight = FontWeight.Light
                        )
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(textColor)
                            )
                            Box(modifier = Modifier.padding(top = 1.dp, start = 2.dp, end = 2.dp)) {
                                if (inRad) {
                                    cursorRendered = true
                                    val localPos = (safeCursor - seg.innerStart).coerceIn(0, seg.innerText.length)
                                    val before = seg.innerText.substring(0, localPos)
                                    val after = seg.innerText.substring(localPos)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 14.sp)
                                        BlinkingCursor(height = 12.dp, color = cursorColor)
                                        if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 14.sp)
                                    }
                                } else {
                                    Text(
                                        text = if (seg.innerText.isEmpty()) "□" else seg.innerText,
                                        color = if (seg.innerText.isEmpty()) placeholderColor else textColor,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                is FormulaSegment.AbsSegment -> {
                    val inAbs = isActive && safeCursor >= seg.innerStart && safeCursor <= seg.innerEnd
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(text = "|", fontSize = 18.sp, color = textColor, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                            if (inAbs) {
                                cursorRendered = true
                                val localPos = (safeCursor - seg.innerStart).coerceIn(0, seg.innerText.length)
                                val before = seg.innerText.substring(0, localPos)
                                val after = seg.innerText.substring(localPos)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (before.isNotEmpty()) Text(text = before, color = textColor, fontSize = 16.sp)
                                    BlinkingCursor(height = 20.dp, color = cursorColor)
                                    if (after.isNotEmpty()) Text(text = after, color = textColor, fontSize = 16.sp)
                                }
                            } else {
                                Text(text = seg.innerText, color = textColor, fontSize = 16.sp)
                            }
                        }
                        Text(text = "|", fontSize = 18.sp, color = textColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // If cursor was at the end of the entire expression or after the last block
        if (isActive && (!cursorRendered || safeCursor == expression.length)) {
            BlinkingCursor(height = 20.dp, color = cursorColor)
        }
    }
}

@Composable
fun BlinkingCursor(
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Box(
        modifier = modifier
            .width(2.dp)
            .height(height)
            .background(color.copy(alpha = alpha), RoundedCornerShape(1.dp))
    )
}
