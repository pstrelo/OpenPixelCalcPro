package com.google.pixel.calculator.ui.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.math.MathNode
import com.google.pixel.calculator.ui.theme.CalculatorTheme

/**
 * Display formule ad alta leggibilità in stile Google Pixel Calculator:
 * - Visualizzazione pulita e morbida delle cifre
 * - Scorrimento orizzontale automatico verso l'ultima cifra digitata
 * - Anteprima istantanea del risultato in grigio morbido
 */
@Composable
fun FormulaDisplay(
    expression: String,
    resultPreview: String,
    ast: MathNode?,
    errorMessage: String?,
    sessionHistory: List<Pair<String, String>> = emptyList(),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Scorre automaticamente all'ultima cifra aggiunta
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Calcoli precedenti della sessione attiva (ultime 2 operazioni)
        if (sessionHistory.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                sessionHistory.takeLast(2).forEach { (prevExpr, prevRes) ->
                    Text(
                        text = prevRes,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        // Frazioni 2D se presenti
        if (expression.contains("frac(")) {
            FractionRenderView(expression = expression)
        } else {
            // Formula o cifra attiva con scorrimento orizzontale morbido
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fontSize = when {
                    expression.length > 15 -> 38.sp
                    expression.length > 10 -> 48.sp
                    expression.length > 7 -> 58.sp
                    else -> 68.sp
                }

                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    color = CalculatorTheme.colors.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Messaggio di Errore
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End
            )
        }

        // Anteprima Risultato in tempo reale
        AnimatedVisibility(visible = resultPreview.isNotEmpty() && errorMessage == null) {
            Text(
                text = "= $resultPreview",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun FractionRenderView(expression: String) {
    val startIndex = expression.indexOf("frac(")
    if (startIndex != -1) {
        val content = expression.substring(startIndex + 5).removeSuffix(")")
        val parts = content.split(",")
        val num = parts.getOrNull(0) ?: ""
        val den = parts.getOrNull(1) ?: ""
        val prefix = expression.substring(0, startIndex)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = CalculatorTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (num.isEmpty()) "□" else num,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = CalculatorTheme.colors.equalsContainer
                )
                HorizontalDivider(
                    modifier = Modifier.width(64.dp),
                    thickness = 2.dp,
                    color = CalculatorTheme.colors.onSurface
                )
                Text(
                    text = if (den.isEmpty()) "□" else den,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = CalculatorTheme.colors.equalsContainer
                )
            }
        }
    }
}
