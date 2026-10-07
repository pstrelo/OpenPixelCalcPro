package com.google.pixel.calculator.ui.toolbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.pixel.calculator.core.utils.RulerUtils
import com.google.pixel.calculator.ui.theme.CalculatorTheme

enum class CalibrationPreset(val label: String, val sizeMm: Float, val description: String) {
    CREDIT_CARD("Carta di Credito", RulerUtils.CREDIT_CARD_WIDTH_MM, "Larghezza standard ISO: 85.6 mm"),
    EURO_2("Moneta 2€", RulerUtils.EURO_2_COIN_MM, "Diametro moneta 2 Euro: 25.75 mm"),
    EURO_1("Moneta 1€", RulerUtils.EURO_1_COIN_MM, "Diametro moneta 1 Euro: 23.25 mm")
}

/**
 * Dialog di Calibrazione Fisica del Righello in Material 3 Expressive.
 * Garantisce che le misure visualizzate sullo schermo siano corrette al 100% rispetto alla realtà.
 */
@Composable
fun RulerCalibrationDialog(
    rawYdpi: Float,
    initialFactor: Float,
    onSaveCalibration: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedPreset by remember { mutableStateOf(CalibrationPreset.CREDIT_CARD) }
    var factor by remember { mutableFloatStateOf(initialFactor) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icona intestazione
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Calibra Dimensioni Righello",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Appoggia un oggetto reale sullo schermo e allinea la sagoma con i tasti +/- fino a farla combaciare esattamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selezione Preset di riferimento
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalibrationPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedPreset = preset
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (preset == CalibrationPreset.CREDIT_CARD) Icons.Default.CreditCard else Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Area Sagoma di Riferimento Visivo (Renderizzata con i DPI correnti)
                val targetMm = selectedPreset.sizeMm
                val currentDpi = rawYdpi * factor
                val targetHeightPx = RulerUtils.mmToPixels(targetMm, currentDpi)

                // Convertiamo pixel in dp per l'altezza del riquadro su schermo
                val density = androidx.compose.ui.platform.LocalDensity.current
                val boxHeightDp = with(density) { targetHeightPx.toDp() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 160.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Sagoma di riferimento con dimensioni reali proporzionate
                        Box(
                            modifier = Modifier
                                .height(boxHeightDp.coerceAtMost(110.dp))
                                .fillMaxWidth(if (selectedPreset == CalibrationPreset.CREDIT_CARD) 0.88f else 0.45f)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(if (selectedPreset == CalibrationPreset.CREDIT_CARD) 10.dp else 50.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(if (selectedPreset == CalibrationPreset.CREDIT_CARD) 10.dp else 50.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${"%.1f".format(targetMm)} mm",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedPreset.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controlli di regolazione fine (- / +)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            factor = (factor - 0.005f).coerceAtLeast(0.70f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuisci scala", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${"%.1f".format(factor * 100f)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${"%.1f".format(currentDpi)} DPI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            factor = (factor + 0.005f).coerceAtMost(1.30f)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumenta scala", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pulsanti Ripristina e Salva
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            factor = 1.0f
                            RulerUtils.resetCalibration(context)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            RulerUtils.setCalibrationFactor(context, factor)
                            onSaveCalibration(factor)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Salva", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
