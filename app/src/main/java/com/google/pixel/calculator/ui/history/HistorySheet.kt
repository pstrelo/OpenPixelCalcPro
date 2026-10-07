package com.google.pixel.calculator.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.data.HistoryItem
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pannello cronologia identico all'esperienza Google Pixel Calculator:
 * - Scende e occupa l'area display superiore sopra il tastierino
 * - Stato vuoto minimalista con icona orologio e "Cronologia vuota"
 * - Elementi allineati a destra raggruppati per data ("Oggi", "Ieri", ecc.)
 * - Maniglia di trascinamento e riga "Espressione corrente" sul fondo
 */
@Composable
fun PixelHistoryPanel(
    historyList: List<HistoryItem>,
    currentExpression: String,
    currentPreview: String,
    onSelectItem: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val colors = CalculatorTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.historyBackground)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    // Se l'utente trascina verso l'alto (delta < -15), chiudi la cronologia
                    if (delta < -15) {
                        onDismiss()
                    }
                }
            )
    ) {
        // TASTO CANCELLA CRONOLOGIA IN ALTO
        if (historyList.isNotEmpty() && onClearHistory != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Cancella cronologia",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancella cronologia",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // CORPO DELLA CRONOLOGIA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            if (historyList.isEmpty()) {
                // STATO VUOTO (Esattamente come Screenshot 3: media_1790088839748.png)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Cronologia vuota",
                        tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cronologia vuota",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SystemFontFamily,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                // LISTA CALCOLI RAGGRUPPATI (Esattamente come Screenshot 4: media_1790088914317.png)
                val grouped = remember(historyList) {
                    groupHistoryByDate(historyList)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    grouped.forEach { (dateHeader, items) ->
                        item {
                            Text(
                                text = dateHeader,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = SystemFontFamily,
                                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(items, key = { it.id }) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectItem(item.expression)
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = item.expression,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = QuicksandFontFamily,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.result,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = QuicksandFontFamily,
                                    color = colors.onSurface,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        // SEPARATORE CON MANIGLIA DI CHIUSURA (Screenshot 3 e 4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = colors.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
            ) {}
        }

        // RIGA INFERIORE: "Espressione corrente" A SINISTRA E RISULTATO/ESPRESSIONE A DESTRA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Espressione corrente",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SystemFontFamily,
                color = colors.onSurfaceVariant.copy(alpha = 0.75f)
            )

            val displayVal = when {
                currentPreview.isNotEmpty() -> currentPreview
                currentExpression.isNotEmpty() -> currentExpression
                else -> "0"
            }

            Text(
                text = displayVal,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = QuicksandFontFamily,
                color = colors.onSurface,
                textAlign = TextAlign.End
            )
        }
    }
}

// Funzione di utilità per raggruppare i calcoli per data ("Oggi", "Ieri", ecc.)
private fun groupHistoryByDate(items: List<HistoryItem>): Map<String, List<HistoryItem>> {
    val result = mutableMapOf<String, MutableList<HistoryItem>>()
    val now = Calendar.getInstance()
    val itemCal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("d MMMM", Locale.ITALIAN)

    for (item in items) {
        itemCal.timeInMillis = item.timestamp
        val isToday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - itemCal.get(Calendar.DAY_OF_YEAR) == 1

        val label = when {
            isToday -> "Oggi"
            isYesterday -> "Ieri"
            else -> dateFormat.format(Date(item.timestamp))
        }

        result.getOrPut(label) { mutableListOf() }.add(item)
    }
    return result
}
