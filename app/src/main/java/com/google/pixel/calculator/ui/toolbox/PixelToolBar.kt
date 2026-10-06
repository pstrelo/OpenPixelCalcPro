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
import androidx.compose.ui.graphics.Color
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
 * - Da aperto: quadrato arrotondato (Squircle / RoundedCornerShape 18dp) con colore d'accento di sistema (primaryContainer).
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

    // Colori rigorosamente legati al tema di sistema / tema calcolatrice (nessun rosa arbitrario)
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

    Surface(
        onClick = {
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
 * SEMPRE fluttuante con ombra/elevazione e della STESSA identica dimensione fissa (altezza 56dp, larghezza 252dp) in tutte le schermate.
 */
@Composable
fun PixelToolPillBar(
    currentScreen: ActiveScreen,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .height(56.dp)
            .width(252.dp)
            .shadow(6.dp, shape = CircleShape),
        shape = CircleShape,
        color = pillContainer,
        contentColor = pillContent,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            selectableDestinations.forEach { destination ->
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigate(destination.screen)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.contentDescription,
                        tint = pillContent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Barra fluttuante ancorata in basso a sinistra per le schermate secondarie
 * (Livella, Bussola, Righello, Schermo intero grafico).
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
                    onNavigate = { screen ->
                        isExpanded = false
                        onNavigate(screen)
                    }
                )
            }
        }
    }
}

/**
 * Riga del tastierino contenente il tasto strumenti (Row 5 portrait o Row 4 numeri in landscape).
 * - SOLO il pulsante strumenti è integrato nella tastiera e si modifica da cerchio a quadrato quando cliccato.
 * - I tasti numerici rimangono sempre presenti nella tastiera.
 * - La pillola compare SEMPRE FLUTTUANTE sopra la tastiera con dimensione identica fissa (56dp x 252dp).
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
        // Tasto 1 (in basso a sinistra): SOLO questo pulsante è integrato nella tastiera
        PixelToolToggleButton(
            currentScreen = currentScreen,
            isExpanded = isExpanded,
            height = btnHeight,
            onClick = onToggleExpand,
            modifier = Modifier.weight(1f)
        )

        // Colonne rimanenti: tasti numerici della tastiera + pillola fluttuante sovrapposta ad essi
        Box(
            modifier = Modifier.weight(if (isLandscape) 2f else 3f),
            contentAlignment = Alignment.CenterStart
        ) {
            // Tasti numerici normali della tastiera (sempre renderizzati e stabili)
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

            // Pillola fluttuante: fluttua sopra i tasti alla STESSA dimensione fissa
            androidx.compose.animation.AnimatedVisibility(
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
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                PixelToolPillBar(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
        }
    }
}
