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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Elenco equazioni stile Desmos/GeoGebra con badge colorati interattivi
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
                    // Badge colorato interattivo della curva (es. y₁, y₂, ... cliccabile per cambiare colore)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(eq.color.copy(alpha = 0.25f))
                            .border(1.5.dp, eq.color, CircleShape)
                            .clickable { onCycleColor(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "y${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = eq.color
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Espressione con cursore in-place e formattazione naturale
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        FormulaMath2DRenderer(
                            expression = eq.expression,
                            isActive = isActive,
                            cursorPosition = eq.cursorPosition
                        )

                        // Risultato singolo (scalare, disequazione o errore) quando non è una matrice multi-radice
                        if (eq.formattedResult.isNotBlank() && eq.roots.size <= 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                            val resultColor = when {
                                eq.isError -> Color(0xFFF87171)
                                eq.formattedResult.contains("∈") || eq.formattedResult.contains("<") || eq.formattedResult.contains(">") -> Color(0xFF38BDF8)
                                else -> Color(0xFF6EE7B7)
                            }
                            Text(
                                text = eq.formattedResult,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = resultColor
                            )
                        }
                    }

                    // Lato Destro: Matrice soluzioni a staffe quadrate verticali per radici multiple [ x = -3 ; x = 3 ]
                    if (eq.roots.size > 1) {
                        SolutionMatrixBracket(
                            roots = eq.roots,
                            format = eq.formatMode
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Pulsante rimozione equazione secondaria
                    if (state.equations.size > 1) {
                        IconButton(
                            onClick = { onRemoveFormula(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rimuovi equazione",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Riga inferiore: Tasto Mostra/Nascondi Grafico + Tasto "+ Aggiungi equazione"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tasto Grafico incorporato nella card (sostituisce il vecchio badge formato)
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
                        text = if (isGraphOpen) "Nascondi Grafico ✕" else "Mostra Grafico 📈",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = graphBtnText
                    )
                }

                // Pulsante "+ Aggiungi equazione" (fino a 4 simultanee)
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
                            contentDescription = "Aggiungi",
                            tint = addColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Aggiungi equazione",
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
 * Visualizzazione a staffa / parentesi quadra verticale per equazioni a più radici:
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
        // Staffa sinistra
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

        // Staffa destra
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
 * Renderizza formule con esponenti formattati in apice (x², xⁿ), frazioni 2D e cursore dinamico in-place.
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

    if (expression.isBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "Inserisci espressione o equazione...",
                color = placeholderColor,
                fontSize = 15.sp
            )
        }
        return
    }

    // Frazione 2D se contiene "/" isolato
    if (expression.contains("/") && !expression.contains("d/dx") && !expression.contains("=")) {
        val slashIndex = expression.indexOf('/')
        val numPart = expression.substring(0, slashIndex).trim().removePrefix("(").removeSuffix(")")
        val remainder = expression.substring(slashIndex + 1).trim()
        val spaceAfterDen = remainder.indexOfAny(charArrayOf('+', '-'))

        val (denPart, restPart) = if (spaceAfterDen != -1 && !remainder.startsWith("(")) {
            Pair(
                remainder.substring(0, spaceAfterDen).trim().removePrefix("(").removeSuffix(")"),
                remainder.substring(spaceAfterDen).trim()
            )
        } else {
            Pair(remainder.removePrefix("(").removeSuffix(")"), "")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            if (isActive && cursorPosition <= slashIndex) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = numPart,
                    color = textColor,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .width((maxOf(numPart.length, denPart.length) * 10).dp.coerceAtLeast(24.dp))
                        .height(1.5.dp)
                        .background(divBarColor)
                )
                Text(
                    text = denPart,
                    color = textColor,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isActive && cursorPosition > slashIndex) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
            }

            if (restPart.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatExponents(restPart),
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    } else {
        // Lineare con posizionamento esatto del cursore in-place
        val safeCursor = cursorPosition.coerceIn(0, expression.length)
        val beforeCursor = expression.substring(0, safeCursor)
        val afterCursor = expression.substring(safeCursor)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            if (beforeCursor.isNotEmpty()) {
                Text(
                    text = formatExponents(beforeCursor),
                    color = textColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                )
            }
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
            }
            if (afterCursor.isNotEmpty()) {
                Text(
                    text = formatExponents(afterCursor),
                    color = textColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

private fun formatExponents(text: String): String {
    return text
        .replace("^2", "²")
        .replace("^3", "³")
        .replace("^4", "⁴")
        .replace("^5", "⁵")
        .replace("sqrt(", "√(")
}
