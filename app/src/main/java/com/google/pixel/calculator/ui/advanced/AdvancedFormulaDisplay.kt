package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine

@Composable
fun AdvancedFormulaDisplay(
    state: AdvancedCalculatorState,
    onSelectFormula: (Int) -> Unit,
    onAddFormula: () -> Unit,
    onRemoveFormula: (Int) -> Unit,
    onCycleFormat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF242736)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Elenco equazioni stile Desmos/GeoGebra con badge colorati
            state.equations.forEachIndexed { index, eq ->
                val isActive = index == state.activeEquationIndex
                val borderModifier = if (isActive) {
                    Modifier.border(1.dp, Color(0xFF474E68), RoundedCornerShape(12.dp))
                } else Modifier

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isActive) Color(0xFF2C3042) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .then(borderModifier)
                        .clickable { onSelectFormula(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge colorato della curva (abbinato al grafico 2D)
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(if (eq.expression.contains("/") || eq.roots.size > 1) 44.dp else 28.dp)
                            .background(eq.color, RoundedCornerShape(3.dp))
                    )

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

            // Riga inferiore: Badge FORMAT + Tasto "+ Aggiungi equazione"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Formato Attivo (DEC / FRAC / ENG / DMS / POL)
                val curFormat = state.equations.getOrNull(state.activeEquationIndex)?.formatMode ?: AdvancedMathEngine.ResultFormat.DECIMAL
                Row(
                    modifier = Modifier
                        .background(Color(0xFF33384C), RoundedCornerShape(10.dp))
                        .clickable { onCycleFormat() }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FORMAT:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = curFormat.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF60A5FA)
                    )
                }

                // Pulsante "+ Aggiungi equazione" (fino a 4 simultanee)
                if (state.equations.size < 4) {
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
                            tint = Color(0xFF93C5FD),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Aggiungi equazione",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF93C5FD)
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xFF1A1D27), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Staffa sinistra
        Text(
            text = "[",
            fontSize = if (roots.size > 2) 24.sp else 20.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF60A5FA),
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
                    color = Color(0xFF6EE7B7),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Staffa destra
        Text(
            text = "]",
            fontSize = if (roots.size > 2) 24.sp else 20.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF60A5FA),
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
                color = Color(0xFF64748B),
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
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .width((maxOf(numPart.length, denPart.length) * 10).dp.coerceAtLeast(24.dp))
                        .height(1.5.dp)
                        .background(Color(0xFFE2E8F0))
                )
                Text(
                    text = denPart,
                    color = Color.White,
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
                    color = Color.White,
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
                    color = Color.White,
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
                    color = Color.White,
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
