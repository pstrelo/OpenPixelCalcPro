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
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily

/**
 * High-readability formula display in Google Pixel Calculator style:
 * - Clean and soft digit rendering
 * - Automatic horizontal scrolling towards the latest typed digit
 * - Instant result preview in soft secondary tone
 */
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration

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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Automatically scroll to the latest added digit
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = if (isLandscape) 4.dp else 12.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Previous calculations from active session (last 2 in portrait, 1 in landscape for compactness)
        if (sessionHistory.isNotEmpty()) {
            val historyCount = if (isLandscape) 1 else 2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isLandscape) 2.dp else 6.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                sessionHistory.takeLast(historyCount).forEach { (prevExpr, prevRes) ->
                    Text(
                        text = prevRes,
                        fontSize = if (isLandscape) 16.sp else 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = QuicksandFontFamily,
                        color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        // 2D fractions if present
        if (expression.contains("frac(")) {
            FractionRenderView(expression = expression)
        } else {
            // Active formula or digit with smooth horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fontSize = if (isLandscape) {
                    when {
                        expression.length > 20 -> 22.sp
                        expression.length > 15 -> 26.sp
                        expression.length > 10 -> 30.sp
                        else -> 36.sp
                    }
                } else {
                    when {
                        expression.length > 15 -> 38.sp
                        expression.length > 10 -> 48.sp
                        expression.length > 7 -> 58.sp
                        else -> 68.sp
                    }
                }

                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = QuicksandFontFamily,
                    color = CalculatorTheme.colors.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 4.dp))

        // Error message
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                fontSize = if (isLandscape) 15.sp else 20.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SystemFontFamily,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End
            )
        }

        // Real-time result preview
        AnimatedVisibility(visible = resultPreview.isNotEmpty() && errorMessage == null) {
            Text(
                text = "= $resultPreview",
                fontSize = if (isLandscape) 20.sp else 32.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = QuicksandFontFamily,
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
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = QuicksandFontFamily,
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
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = QuicksandFontFamily,
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
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = QuicksandFontFamily,
                    color = CalculatorTheme.colors.equalsContainer
                )
            }
        }
    }
}
