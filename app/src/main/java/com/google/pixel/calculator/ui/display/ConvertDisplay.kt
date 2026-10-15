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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import com.google.pixel.calculator.ui.theme.SystemFontFamily

enum class ActiveConverterField {
    TOP,
    BOTTOM
}

/**
 * Interactive two-box display for the unit converter:
 * - Both boxes are clickable.
 * - Active box (for current editing) renders high-contrast text and active focus.
 * - Passive box renders calculated converted value with faded text.
 * - Clicking a box focuses it for input while the other updates in real time.
 * - Central swap button (⇅) swaps the units.
 */
@Composable
fun ConvertDisplay(
    topValueStr: String,
    bottomValueStr: String,
    topUnit: UnitConverter.UnitDefinition,
    bottomUnit: UnitConverter.UnitDefinition,
    activeField: ActiveConverterField,
    isProMode: Boolean = false,
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

    val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isCompact = isLandscape || isProMode

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
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = if (isCompact) 2.dp else 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // --- TOP BOX ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ConverterBox(
                valueStr = topValueStr,
                unit = topUnit,
                isActive = activeField == ActiveConverterField.TOP,
                isCompact = isCompact,
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
        }

        // --- CENTER BAR: DIVIDER AND SWAP BUTTON (⇅) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isCompact) 1.dp else 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    triggerHaptic()
                    onSwap()
                },
                modifier = Modifier
                    .size(if (isCompact) 32.dp else 38.dp)
                    .background(
                        color = CalculatorTheme.colors.equalsContainer.copy(alpha = 0.20f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = stringResource(R.string.swap_units),
                    tint = CalculatorTheme.colors.equalsContainer,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            HorizontalDivider(
                color = CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.weight(1f)
            )
        }

        // --- BOTTOM BOX ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ConverterBox(
                valueStr = bottomValueStr,
                unit = bottomUnit,
                isActive = activeField == ActiveConverterField.BOTTOM,
                isCompact = isCompact,
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
}

@Composable
private fun ConverterBox(
    valueStr: String,
    unit: UnitConverter.UnitDefinition,
    isActive: Boolean,
    isCompact: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    onBoxClick: () -> Unit,
    onUnitChipClick: () -> Unit
) {
    val textColor = if (isActive) {
        CalculatorTheme.colors.onSurface
    } else {
        CalculatorTheme.colors.onSurfaceVariant.copy(alpha = 0.40f) // Faded when not selected
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onBoxClick() }
            .padding(horizontal = 4.dp, vertical = if (isCompact) 1.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = if (isCompact) 2.dp else 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Horizontal scrollable number display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
                val isSmallScreen = screenWidthDp < 360
                val fontSize = if (isCompact) {
                    when {
                        valueStr.length > 14 -> 15.sp
                        valueStr.length > 10 -> 18.sp
                        valueStr.length > 7 -> 22.sp
                        else -> 26.sp
                    }
                } else if (isSmallScreen) {
                    when {
                        valueStr.length > 14 -> 18.sp
                        valueStr.length > 10 -> 22.sp
                        valueStr.length > 7 -> 26.sp
                        else -> 30.sp
                    }
                } else {
                    when {
                        valueStr.length > 14 -> 20.sp
                        valueStr.length > 10 -> 25.sp
                        valueStr.length > 7 -> 30.sp
                        else -> 36.sp
                    }
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
                        maxLines = 1,
                        softWrap = false,
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 10.dp))

            // Unit chip (clickable to change only this box's unit)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CalculatorTheme.colors.operatorContainer.copy(alpha = if (isActive) 0.85f else 0.50f),
                contentColor = if (isActive) CalculatorTheme.colors.onSurface else CalculatorTheme.colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onUnitChipClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = if (isCompact) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = unit.code,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SystemFontFamily
                    )
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = stringResource(R.string.change_unit),
                        tint = CalculatorTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                    )
                }
            }
        }
    }
}
