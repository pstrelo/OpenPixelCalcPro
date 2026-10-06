package com.google.pixel.calculator.ui.display

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
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

enum class ActiveConverterField {
    TOP,
    BOTTOM
}

/**
 * Display a due caselle interattive per il convertitore di unità:
 * - Entrambe le caselle sono cliccabili.
 * - La casella attiva (in cui si scrive) ha testo in colore normale ad alto contrasto e bordo attivo.
 * - La casella passiva mostra il valore convertito con testo più sbiadito (faded/trasparente).
 * - Cliccando una casella, se ne modifica il valore e l'altra mostra la conversione automatica.
 * - Tasto Swap centrale (⇅) per scambiare le unità.
 */
@Composable
fun ConvertDisplay(
    topValueStr: String,
    bottomValueStr: String,
    topUnit: UnitConverter.UnitDefinition,
    bottomUnit: UnitConverter.UnitDefinition,
    activeField: ActiveConverterField,
    isHapticEnabled: Boolean = true,
    onFocusField: (ActiveConverterField) -> Unit,
    onSwap: () -> Unit,
    onSelectTopUnit: () -> Unit,
    onSelectBottomUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val triggerHaptic: () -> Unit = {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val topScrollState = rememberScrollState()
    val bottomScrollState = rememberScrollState()

    LaunchedEffect(topValueStr) {
        topScrollState.animateScrollTo(topScrollState.maxValue)
    }
    LaunchedEffect(bottomValueStr) {
        bottomScrollState.animateScrollTo(bottomScrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // --- CASELLA SUPERIORE (TOP BOX) ---
        ConverterBox(
            valueStr = topValueStr,
            unit = topUnit,
            isActive = activeField == ActiveConverterField.TOP,
            scrollState = topScrollState,
            onBoxClick = {
                triggerHaptic()
                onFocusField(ActiveConverterField.TOP)
            },
            onUnitChipClick = {
                triggerHaptic()
                onSelectTopUnit()
            }
        )

        // --- BARRA CENTRALE: DIVISORE E PULSANTE SWAP (⇅) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    triggerHaptic()
                    onSwap()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = CalculatorTheme.colors.equalsContainer.copy(alpha = 0.20f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Inverti unità",
                    tint = CalculatorTheme.colors.equalsContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            HorizontalDivider(
                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.weight(1f)
            )
        }

        // --- CASELLA INFERIORE (BOTTOM BOX) ---
        ConverterBox(
            valueStr = bottomValueStr,
            unit = bottomUnit,
            isActive = activeField == ActiveConverterField.BOTTOM,
            scrollState = bottomScrollState,
            onBoxClick = {
                triggerHaptic()
                onFocusField(ActiveConverterField.BOTTOM)
            },
            onUnitChipClick = {
                triggerHaptic()
                onSelectBottomUnit()
            }
        )
    }
}

@Composable
private fun ConverterBox(
    valueStr: String,
    unit: UnitConverter.UnitDefinition,
    isActive: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    onBoxClick: () -> Unit,
    onUnitChipClick: () -> Unit
) {
    val textColor = if (isActive) {
        CalculatorTheme.colors.onSurface
    } else {
        CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.40f) // Sbiadita quando non selezionata
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onBoxClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numero con scorrimento orizzontale
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                val fontSize = when {
                    valueStr.length > 14 -> 28.sp
                    valueStr.length > 10 -> 36.sp
                    valueStr.length > 7 -> 44.sp
                    else -> 52.sp
                }

                AnimatedContent(
                    targetState = valueStr,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "ConverterBoxValueTransition"
                ) { displayVal ->
                    Text(
                        text = if (displayVal.isEmpty()) "0" else displayVal,
                        fontSize = fontSize,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = QuicksandFontFamily,
                        color = textColor,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Chip Unità (cliccabile per cambiare solo l'unità di questa casella)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CalculatorTheme.colors.operatorContainer.copy(alpha = if (isActive) 0.85f else 0.50f),
                contentColor = if (isActive) CalculatorTheme.colors.onSurface else CalculatorTheme.colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onUnitChipClick() }
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
    }
}
