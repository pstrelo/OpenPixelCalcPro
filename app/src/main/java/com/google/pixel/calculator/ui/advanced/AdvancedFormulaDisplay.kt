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
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF242736)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Elenco formule/equazioni inserite
            state.formulas.forEachIndexed { index, formula ->
                val isActive = index == state.activeFormulaIndex
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
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Barra colorata della funzione/curva
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(if (formula.expression.contains("/") || formula.roots.size > 1) 48.dp else 30.dp)
                            .background(formula.color, RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Espressione/Equazione naturale (lato sinistro)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        FormulaMath2DRenderer(
                            expression = formula.expression,
                            isActive = isActive
                        )

                        // Se c'è un risultato singolo non-matrice (es. integrale, derivata, disequazione)
                        if (formula.resultText.isNotBlank() && formula.roots.size <= 1) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formula.resultText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (formula.isError) Color(0xFFF87171) else Color(0xFF6EE7B7)
                            )
                        }
                    }

                    // Lato Destro: Soluzione stile Casio / HiPER Calc con staffa a matrice: [ x = -3 ; x = 3 ] (Matching Foto 1)
                    if (formula.roots.size > 1) {
                        SolutionMatrixBracket(
                            roots = formula.roots,
                            format = formula.activeFormat
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Tasto rimuovi formula (se ce n'è più di 1)
                    if (state.formulas.size > 1) {
                        IconButton(
                            onClick = { onRemoveFormula(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rimuovi funzione",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Barra inferiore interna al display: Aggiungi equazione + Badge Formato attivo (DEC/FRAC/ENG/DMS/POL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Formato (cliccabile per cambiare al volo)
                val curFormat = state.formulas.getOrNull(state.activeFormulaIndex)?.activeFormat ?: AdvancedMathEngine.ResultFormat.DECIMAL
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

                // Aggiungi f(x)
                if (state.formulas.size < 4) {
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
 * Visualizzazione a staffa / parentesi quadra matematica per equazioni con più radici (Foto 1):
 * [ x = -3 ]
 * [ x =  3 ]
 */
@Composable
fun SolutionMatrixBracket(
    roots: List<Double>,
    format: AdvancedMathEngine.ResultFormat
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF1E212D), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Staffa sinistra
        Text(
            text = "[",
            fontSize = if (roots.size > 2) 26.sp else 22.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF60A5FA),
            fontFamily = FontFamily.Monospace
        )

        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            roots.forEachIndexed { idx, r ->
                val rStr = when (format) {
                    AdvancedMathEngine.ResultFormat.DECIMAL -> if (r == r.toLong().toDouble()) r.toLong().toString() else "%.4f".format(r)
                    AdvancedMathEngine.ResultFormat.FRACTION -> AdvancedMathEngine().toFraction(r)
                    AdvancedMathEngine.ResultFormat.ENGINEERING -> AdvancedMathEngine().toEngineering(r)
                    AdvancedMathEngine.ResultFormat.DMS -> AdvancedMathEngine().toDms(r)
                    AdvancedMathEngine.ResultFormat.POLAR -> AdvancedMathEngine().toPolar(r, false)
                }
                Text(
                    text = "x${if (roots.size > 1) " = " else "="}$rStr",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6EE7B7),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Staffa destra
        Text(
            text = "]",
            fontSize = if (roots.size > 2) 26.sp else 22.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF60A5FA),
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Renderizza formule con esponenti formattati in apice (es. x² o xⁿ) e frazioni 2D
 */
@Composable
fun FormulaMath2DRenderer(
    expression: String,
    isActive: Boolean
) {
    if (expression.isBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(18.dp)
                        .background(Color(0xFF60A5FA))
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

    // Frazione 2D se contiene "/"
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
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = numPart,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .width((maxOf(numPart.length, denPart.length) * 10).dp)
                        .height(1.5.dp)
                        .background(Color(0xFFE2E8F0))
                )
                Text(
                    text = denPart,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )
            }

            if (restPart.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatExponents(restPart),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    } else {
        // Lineare con cursore ed esponenti visivi
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(22.dp)
                        .background(Color(0xFF60A5FA), RoundedCornerShape(1.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = formatExponents(expression),
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal
            )
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
