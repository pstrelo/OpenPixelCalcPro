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
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    val contentDescription: String
)

val AllToolDestinations: List<ToolDestination> = listOf(
    ToolDestination(
        screen = ActiveScreen.MAIN_CALCULATOR,
        icon = Icons.Default.Calculate,
        title = "Calcolatrice",
        contentDescription = "Calcolatrice standard"
    ),
    ToolDestination(
        screen = ActiveScreen.ADVANCED_CALCULATOR,
        icon = Icons.AutoMirrored.Filled.ShowChart,
        title = "Avanzata",
        contentDescription = "Calcolatrice avanzata e grafici"
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_CONVERTER,
        icon = Icons.Default.SwapHoriz,
        title = "Convertitore",
        contentDescription = "Convertitore di unità"
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_LEVEL,
        icon = BubbleLevel,
        title = "Livella",
        contentDescription = "Livella a bolla digitale"
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_COMPASS,
        icon = Icons.Default.Explore,
        title = "Bussola",
        contentDescription = "Bussola digitale"
    ),
    ToolDestination(
        screen = ActiveScreen.TOOLBOX_RULER,
        icon = SquareFoot,
        title = "Righello",
        contentDescription = "Righello su schermo"
    )
)

/**
 * Pulsante di attivazione strumenti Material 3:
 * - Da chiuso: tondo (CircleShape), mostra l'icona dello strumento attualmente attivo.
 * - Da aperto: quadrato arrotondato (Squircle / RoundedCornerShape 18dp) con colore d'accento tertiary/secondary.
 */
@Composable
fun PixelToolToggleButton(
    currentScreen: ActiveScreen,
    isExpanded: Boolean,
    height: Dp = 56.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentDestination = remember(currentScreen) {
        AllToolDestinations.firstOrNull { it.screen == currentScreen } ?: AllToolDestinations[0]
    }

    val cornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 18.dp else (height / 2),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ToolButtonCornerAnimation"
    )

    // Colori adattati al tema dell'applicazione M3
    val containerColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            CalculatorTheme.colors.controlContainer
        },
        animationSpec = tween(durationMillis = 200),
        label = "ToolButtonContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            CalculatorTheme.colors.controlContent
        },
        animationSpec = tween(durationMillis = 200),
        label = "ToolButtonContentColor"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier.height(height),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (isExpanded) 3.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = currentDestination.icon,
                contentDescription = if (isExpanded) "Chiudi barra strumenti" else "Apri barra strumenti (${currentDestination.title})",
                tint = contentColor,
                modifier = Modifier.size(if (height <= 44.dp) 20.dp else 24.dp)
            )
        }
    }
}

/**
 * Pillola orizzontale Material 3 contenente le icone degli altri 5 strumenti selezionabili.
 */
@Composable
fun PixelToolPillBar(
    currentScreen: ActiveScreen,
    height: Dp = 56.dp,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectableDestinations = remember(currentScreen) {
        AllToolDestinations.filter { it.screen != currentScreen }
    }

    Surface(
        modifier = modifier
            .height(height)
            .shadow(3.dp, shape = CircleShape),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = if (height <= 44.dp) 6.dp else 10.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            selectableDestinations.forEach { destination ->
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigate(destination.screen)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .size(if (height <= 44.dp) 32.dp else height - 12.dp)
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.contentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (height <= 44.dp) 18.dp else 22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Barra fluttuante ancorata in basso a sinistra per le schermate secondarie
 * (Livella, Bussola, Righello, Calcolatrice Avanzata).
 */
@Composable
fun PixelFloatingToolBar(
    currentScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    if (isExpanded) {
        BackHandler { isExpanded = false }

        // Area trasparente a schermo intero per catturare tocchi fuori dalla barra
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

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PixelToolToggleButton(
                currentScreen = currentScreen,
                isExpanded = isExpanded,
                height = 56.dp,
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(56.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(tween(140)) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )
            ) {
                PixelToolPillBar(
                    currentScreen = currentScreen,
                    height = 56.dp,
                    onNavigate = { screen ->
                        isExpanded = false
                        onNavigate(screen)
                    },
                    modifier = Modifier.width(240.dp)
                )
            }
        }
    }
}

/**
 * Riga del tastierino contenente il tasto strumenti (Row 5 portrait o Row 4 numeri in landscape).
 * Quando isExpanded è true, la pillola copre fluidamente i tasti adiacenti della riga.
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tasto 1 (in basso a sinistra): Strumento attivo (da tondo a quadrato)
        PixelToolToggleButton(
            currentScreen = currentScreen,
            isExpanded = isExpanded,
            height = btnHeight,
            onClick = onToggleExpand,
            modifier = Modifier.weight(1f)
        )

        val remainingWeight = if (isLandscape) 2f else 3f

        // Se espanso: mostra la pillola che occupa le rimanenti colonne.
        // Se chiuso: mostra i normali tasti numerici [0] [.] [=]
        if (isExpanded) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut(tween(140)) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                modifier = Modifier.weight(remainingWeight)
            ) {
                PixelToolPillBar(
                    currentScreen = currentScreen,
                    height = btnHeight,
                    onNavigate = onNavigate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
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
