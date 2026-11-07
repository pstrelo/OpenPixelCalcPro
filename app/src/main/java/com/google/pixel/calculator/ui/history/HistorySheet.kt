package com.google.pixel.calculator.ui.history

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.data.HistoryItem
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * History panel identical to Google Pixel Calculator experience:
 * - Slides down to occupy the upper display area above the keypad
 * - Expands to the bottom of the screen (full height) when scrolled or dragged down
 * - Minimalist empty state with clock icon and "History is empty"
 * - Right-aligned items grouped by date ("Today", "Yesterday", etc.)
 * - Drag handle and "Current expression" row at the bottom
 */
@Composable
fun PixelHistoryPanel(
    historyList: List<HistoryItem>,
    currentExpression: String,
    currentPreview: String,
    onSelectItem: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearHistory: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    onExpand: () -> Unit = {},
    onCollapse: () -> Unit = {},
    topContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val colors = CalculatorTheme.colors

    LaunchedEffect(historyList.size) {
        if (historyList.isNotEmpty() && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)) {
            listState.scrollToItem(0)
        }
    }

    // Automatically expand to full height when the list starts scrolling
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (!isExpanded && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 2)) {
            onExpand()
        }
    }

    // NestedScrollConnection so even a short list (or initial scroll touch) expands the shade immediately
    val nestedScrollConnection = remember(isExpanded, onExpand) {
        object : NestedScrollConnection {
            private var accumulatedScroll = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!isExpanded && abs(available.y) > 0.5f) {
                    accumulatedScroll += abs(available.y)
                    if (accumulatedScroll > 3f) {
                        accumulatedScroll = 0f
                        onExpand()
                    }
                }
                return Offset.Zero
            }
        }
    }

    val bottomCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 28.dp,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "HistoryBottomCornerRadius"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    bottomStart = bottomCornerRadius,
                    bottomEnd = bottomCornerRadius
                )
            )
            .background(colors.historyBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Consume clicks so taps don't leak through to the keypad when expanded
            }
            .padding(
                top = topContentPadding,
                bottom = if (isExpanded) bottomContentPadding else 0.dp
            )
    ) {
        // CLEAR HISTORY BUTTON AT TOP
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
                        contentDescription = stringResource(R.string.clear_history),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.clear_history),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // HISTORY BODY
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .nestedScroll(nestedScrollConnection)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        if (!isExpanded && abs(delta) > 10f) {
                            onExpand()
                        } else if (isExpanded && historyList.isEmpty() && delta < -20f) {
                            onDismiss()
                        }
                    }
                )
        ) {
            if (historyList.isEmpty()) {
                // EMPTY STATE
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.history_empty),
                        tint = colors.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.history_empty),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SystemFontFamily,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                // GROUPED CALCULATION LIST
                val todayLabel = stringResource(R.string.history_today)
                val yesterdayLabel = stringResource(R.string.history_yesterday)
                val grouped = remember(historyList, todayLabel, yesterdayLabel) {
                    groupHistoryByDate(historyList, todayLabel, yesterdayLabel)
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

        // BOTTOM DRAG HANDLE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        if (delta > 10f && !isExpanded) {
                            onExpand()
                        } else if (delta < -15f) {
                            onDismiss()
                        }
                    }
                )
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
    }
}

// Helper function to group calculations by date ("Today", "Yesterday", etc.)
private fun groupHistoryByDate(
    items: List<HistoryItem>,
    todayLabel: String,
    yesterdayLabel: String
): Map<String, List<HistoryItem>> {
    val result = mutableMapOf<String, MutableList<HistoryItem>>()
    val now = Calendar.getInstance()
    val itemCal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("d MMMM", Locale.getDefault())

    for (item in items) {
        itemCal.timeInMillis = item.timestamp
        val isToday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - itemCal.get(Calendar.DAY_OF_YEAR) == 1

        val label = when {
            isToday -> todayLabel
            isYesterday -> yesterdayLabel
            else -> dateFormat.format(Date(item.timestamp))
        }

        result.getOrPut(label) { mutableListOf() }.add(item)
    }
    return result
}
