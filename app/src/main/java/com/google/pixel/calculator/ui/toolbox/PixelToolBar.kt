package com.google.pixel.calculator.ui.toolbox

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.icons.BubbleLevel
import com.google.pixel.calculator.ui.icons.SquareFoot
import com.google.pixel.calculator.ui.keypad.PixelButton
import com.google.pixel.calculator.ui.keypad.PixelButtonType
import com.google.pixel.calculator.ui.theme.CalculatorTheme

data class ToolDestination(
    val screen: ActiveScreen,
    val icon: ImageVector,
    val title: String,
    val contentDescription: String,
    val titleRes: Int = 0,
    val descRes: Int = 0
)

val AllToolDestinations: List<ToolDestination> = listOf(
    ToolDestination(
        screen = ActiveScreen.MAIN_CALCULATOR,
        icon = Icons.Default.Calculate,
        title = "Calcolatrice",
        contentDescription = "Calcolatrice standard",
        titleRes = R.string.tool_main_calc,
        descRes = R.string.tool_main_calc_desc
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_CONVERTER,
        icon = Icons.Default.SwapHoriz,
        title = "Convertitore",
        contentDescription = "Convertitore di unità",
        titleRes = R.string.tool_converter,
        descRes = R.string.tool_converter_desc
    ),
    ToolDestination(
        screen = ActiveScreen.ADVANCED_CALCULATOR,
        icon = Icons.AutoMirrored.Filled.ShowChart,
        title = "Avanzata",
        contentDescription = "Calcolatrice avanzata e grafici",
        titleRes = R.string.tool_advanced,
        descRes = R.string.tool_advanced_desc
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_LEVEL,
        icon = BubbleLevel,
        title = "Livella",
        contentDescription = "Livella a bolla digitale",
        titleRes = R.string.tool_level,
        descRes = R.string.tool_level_desc
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_COMPASS,
        icon = Icons.Default.Explore,
        title = "Bussola",
        contentDescription = "Bussola digitale",
        titleRes = R.string.tool_compass,
        descRes = R.string.tool_compass_desc
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_RULER,
        icon = SquareFoot,
        title = "Righello",
        contentDescription = "Righello su schermo",
        titleRes = R.string.tool_ruler,
        descRes = R.string.tool_ruler_desc
    )
)

/**
 * Unified dimensions for the ToolBar across all screens
 * (Standard Calculator, Advanced, Converter, Level, Compass, Ruler).
 */
data class ToolBarDimensions(
    val height: Dp,
    val btnWidth: Dp,
    val pillWidth: Dp,
    val horizontalPadding: Dp,
    val bottomPadding: Dp,
    val spacing: Dp
)

@Composable
fun rememberToolBarDimensions(): ToolBarDimensions {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp

    val hPadding = if (isLandscape) 10.dp else 16.dp
    val bPadding = if (isLandscape) 2.dp else 6.dp
    val spacing = if (isLandscape) 6.dp else 12.dp
    val height = if (isLandscape) 48.dp else 82.dp

    // Width of floating tool button
    val btnWidth = if (isLandscape) {
        48.dp
    } else {
        ((screenWidth - (hPadding * 2) - (spacing * 3)) / 4).coerceAtLeast(82.dp)
    }

    // Width of pill bar
    val pillWidth = if (isLandscape) {
        260.dp
    } else {
        (screenWidth - (hPadding * 2) - btnWidth - spacing).coerceAtLeast(240.dp)
    }

    return remember(isLandscape, screenWidth) {
        ToolBarDimensions(
            height = height,
            btnWidth = btnWidth,
            pillWidth = pillWidth,
            horizontalPadding = hPadding,
            bottomPadding = bPadding,
            spacing = spacing
        )
    }
}

/**
 * Material 3 tool trigger button:
 * - Squircle (RoundedCornerShape 18dp) matching calculator keys in all themes.
 */
@Composable
fun PixelToolToggleButton(
    currentScreen: ActiveScreen,
    isExpanded: Boolean,
    height: Dp = rememberToolBarDimensions().height,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentDestination = remember(currentScreen) {
        AllToolDestinations.firstOrNull { it.screen == currentScreen } ?: AllToolDestinations[0]
    }

    val cornerRadius = 18.dp

    val isDark = CalculatorTheme.colors.isDark
    val activeContainer = MaterialTheme.colorScheme.primaryContainer.takeIf { it != Color.Unspecified }
        ?: if (isDark) CalculatorTheme.colors.equalsContainer else CalculatorTheme.colors.operatorContainer
    val activeContent = MaterialTheme.colorScheme.onPrimaryContainer.takeIf { it != Color.Unspecified }
        ?: if (isDark) CalculatorTheme.colors.equalsContent else CalculatorTheme.colors.operatorContent

    val containerColor by animateColorAsState(
        targetValue = if (isExpanded) activeContainer else CalculatorTheme.colors.controlContainer,
        animationSpec = tween(durationMillis = 200),
        label = "ToolButtonContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isExpanded) activeContent else CalculatorTheme.colors.controlContent,
        animationSpec = tween(durationMillis = 200),
        label = "ToolButtonContentColor"
    )

    val view = LocalView.current

    Surface(
        onClick = {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.height(height),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (isExpanded) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val titleStr = if (currentDestination.titleRes != 0) stringResource(currentDestination.titleRes) else currentDestination.title
            Icon(
                imageVector = currentDestination.icon,
                contentDescription = if (isExpanded) stringResource(R.string.close_toolbar) else stringResource(R.string.open_toolbar, titleStr),
                tint = contentColor,
                modifier = Modifier.size(if (height <= 48.dp) 20.dp else 26.dp)
            )
        }
    }
}

/**
 * Material 3 horizontal pill container with the other 5 selectable tools.
 * Floating with elevation, synchronized in geometry with keypad.
 */
@Composable
fun PixelToolPillBar(
    currentScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = rememberToolBarDimensions().height
) {
    val haptic = LocalHapticFeedback.current
    val selectableDestinations = remember(currentScreen) {
        AllToolDestinations.filter { it.screen != currentScreen }
    }

    val isDark = CalculatorTheme.colors.isDark
    val pillContainer = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh.takeIf { it != Color.Unspecified }
            ?: Color(0xFF282A30)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.takeIf { it != Color.Unspecified }
            ?: Color(0xFFEEF0F6)
    }

    val pillContent = MaterialTheme.colorScheme.onSurfaceVariant.takeIf { it != Color.Unspecified }
        ?: CalculatorTheme.colors.onSurfaceVariant

    Surface(
        modifier = modifier.height(height),
        shape = CircleShape,
        color = pillContainer,
        contentColor = pillContent,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            selectableDestinations.forEach { destination ->
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigate(destination.screen)
                    },
                    modifier = Modifier.size(if (height <= 48.dp) 36.dp else 48.dp)
                ) {
                    val descStr = if (destination.descRes != 0) stringResource(destination.descRes) else destination.contentDescription
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = descStr,
                        tint = pillContent,
                        modifier = Modifier.size(if (height <= 48.dp) 20.dp else 26.dp)
                    )
                }
            }
        }
    }
}

/**
 * Floating bottom-anchored toolbar for secondary utility screens
 * (Level, Compass, Ruler, Fullscreen Graph).
 */
@Composable
fun PixelFloatingToolBar(
    currentScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier,
    alignRight: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dims = rememberToolBarDimensions()

    if (isExpanded) {
        BackHandler { isExpanded = false }

        // Transparent full-screen overlay to catch taps outside toolbar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isExpanded = false
                }
        )
    }

    val paddingModifier = if (alignRight) {
        modifier.padding(end = dims.horizontalPadding, bottom = dims.bottomPadding)
    } else {
        modifier.padding(start = dims.horizontalPadding, bottom = dims.bottomPadding)
    }

    val origin = if (alignRight) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)

    Box(modifier = paddingModifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dims.spacing)
        ) {
            if (alignRight) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(180)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        transformOrigin = origin
                    ),
                    exit = fadeOut(tween(140)) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        transformOrigin = origin
                    )
                ) {
                    PixelToolPillBar(
                        currentScreen = currentScreen,
                        height = dims.height,
                        onNavigate = { screen ->
                            isExpanded = false
                            onNavigate(screen)
                        },
                        modifier = Modifier.size(width = dims.pillWidth, height = dims.height)
                    )
                }

                PixelToolToggleButton(
                    currentScreen = currentScreen,
                    isExpanded = isExpanded,
                    height = dims.height,
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(width = dims.btnWidth, height = dims.height)
                )
            } else {
                PixelToolToggleButton(
                    currentScreen = currentScreen,
                    isExpanded = isExpanded,
                    height = dims.height,
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(width = dims.btnWidth, height = dims.height)
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(180)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        transformOrigin = origin
                    ),
                    exit = fadeOut(tween(140)) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        transformOrigin = origin
                    )
                ) {
                    PixelToolPillBar(
                        currentScreen = currentScreen,
                        height = dims.height,
                        onNavigate = { screen ->
                            isExpanded = false
                            onNavigate(screen)
                        },
                        modifier = Modifier.size(width = dims.pillWidth, height = dims.height)
                    )
                }
            }
        }
    }
}

/**
 * Keypad row containing the tool trigger button (Row 5 portrait or Row 4 numbers in landscape).
 * - Tool trigger button smoothly transitions between circle and rounded squircle.
 * - Number keys remain intact.
 * - Pill appears in floating overlay above remaining columns.
 */
@Composable
fun PixelKeypadToolRow(
    currentScreen: ActiveScreen,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit,
    btnHeight: Dp,
    spacing: Dp,
    isLandscape: Boolean,
    onDigit: (String) -> Unit,
    onEquals: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val pillWeight = if (!isLandscape && onEquals != null) 3f else 2f

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Key 1 (bottom left): tool toggle button
            PixelToolToggleButton(
                currentScreen = currentScreen,
                isExpanded = isExpanded,
                height = btnHeight,
                onClick = onToggleExpand,
                modifier = Modifier.weight(1f)
            )

            // Keys under the pill container with exit/enter animation
            Box(
                modifier = Modifier
                    .weight(pillWeight)
                    .height(btnHeight),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn(tween(180)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    ),
                    exit = fadeOut(tween(140)) + scaleOut(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelButton(
                            text = "0",
                            type = PixelButtonType.NUMERIC,
                            height = btnHeight,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDigit("0")
                            }
                        )
                        PixelButton(
                            text = ".",
                            type = PixelButtonType.NUMERIC,
                            height = btnHeight,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDigit(".")
                            }
                        )
                        if (!isLandscape && onEquals != null) {
                            PixelButton(
                                text = "=",
                                type = PixelButtonType.EQUALS,
                                height = btnHeight,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onEquals()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating pill bar overlay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = 0.62f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                ),
                exit = fadeOut(tween(140)) + scaleOut(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                ),
                modifier = Modifier.weight(pillWeight)
            ) {
                PixelToolPillBar(
                    currentScreen = currentScreen,
                    height = btnHeight,
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(btnHeight)
                )
            }
        }
    }
}
