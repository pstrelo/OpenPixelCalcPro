package com.google.pixel.calculator.ui.display

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily

/**
 * Display a due righe in perfetto stile iPhone (iOS 18) per il convertitore di unità:
 * - Riga 1: Valore/Espressione digitata in input + Chip unità cliccabile (con icona frecce su/giù).
 * - Divisore con tasto Swap circolare arancione/accento Pixel (⇅).
 * - Riga 2: Risultato convertito calcolato in tempo reale + Chip unità di destinazione cliccabile.
 */
@Composable
fun ConvertDisplay(
    expression: String,
    convertedValueStr: String,
    fromUnit: UnitConverter.UnitDefinition,
    toUnit: UnitConverter.UnitDefinition,
    isHapticEnabled: Boolean = true,
    onSwap: () -> Unit,
    onSelectFromUnit: () -> Unit,
    onSelectToUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val triggerHaptic: () -> Unit = {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val inputScrollState = rememberScrollState()
    val outputScrollState = rememberScrollState()

    // Scorre alla fine del testo quando la cifra cambia
    LaunchedEffect(expression) {
        inputScrollState.animateScrollTo(inputScrollState.maxValue)
    }
    LaunchedEffect(convertedValueStr) {
        outputScrollState.animateScrollTo(outputScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // --- RIGA 1: VALORE INPUT + UNITA DA CONVERTIRE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numero input con scroll orizzontale
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(inputScrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                val inputFontSize = when {
                    expression.length > 14 -> 28.sp
                    expression.length > 10 -> 36.sp
                    expression.length > 7 -> 44.sp
                    else -> 52.sp
                }

                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    fontSize = inputFontSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = QuicksandFontFamily,
                    color = CalculatorTheme.colors.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Chip Unità Input (es. USD ↕)
            UnitChip(
                unit = fromUnit,
                onClick = {
                    triggerHaptic()
                    onSelectFromUnit()
                }
            )
        }

        // --- BARRA CENTRALE: DIVISORE E PULSANTE SWAP (⇅) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsante Swap in stile iPhone
            IconButton(
                onClick = {
                    triggerHaptic()
                    onSwap()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = CalculatorTheme.colors.equalsContainer.copy(alpha = 0.18f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Inverti unità di misura",
                    tint = CalculatorTheme.colors.equalsContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            HorizontalDivider(
                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.weight(1f)
            )
        }

        // --- RIGA 2: RISULTATO CONVERTITO + UNITA DESTINAZIONE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Risultato convertito con animazione morbida
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(outputScrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                val resultFontSize = when {
                    convertedValueStr.length > 14 -> 28.sp
                    convertedValueStr.length > 10 -> 36.sp
                    convertedValueStr.length > 7 -> 44.sp
                    else -> 52.sp
                }

                AnimatedContent(
                    targetState = convertedValueStr,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "ConvertResultTransition"
                ) { targetVal ->
                    Text(
                        text = if (targetVal.isEmpty()) "0" else targetVal,
                        fontSize = resultFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = QuicksandFontFamily,
                        color = CalculatorTheme.colors.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Chip Unità Risultato (es. EUR ↕)
            UnitChip(
                unit = toUnit,
                onClick = {
                    triggerHaptic()
                    onSelectToUnit()
                }
            )
        }
    }
}

@Composable
private fun UnitChip(
    unit: UnitConverter.UnitDefinition,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CalculatorTheme.colors.operatorContainer.copy(alpha = 0.7f),
        contentColor = CalculatorTheme.colors.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = unit.code,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SystemFontFamily
            )
            Icon(
                imageVector = Icons.Default.UnfoldMore,
                contentDescription = "Cambia unità",
                tint = CalculatorTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
