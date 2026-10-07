package com.google.pixel.calculator.ui.toolbox

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.sensors.LevelData
import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import kotlin.math.abs

/**
 * Schermata della Livella Digitale riprogettata in perfetto stile Samsung One UI:
 * - Modalità Orizzontale (Superficie / Tavolo) con 2 cerchi interattivi simmetrici (come foto 2)
 *   che controlla l'inclinazione testa-fondo con riferimento all'asse destra-sinistra.
 * - Modalità Verticale (Parete / Bordo) con orizzonte artificiale e indicatori a trattino (come foto 1)
 *   che controlla l'inclinazione destra-sinistra con riferimento all'asse frontale testa-fondo.
 * - Transizione automatica e ultra-fluida tra le due modalità basata sulla posizione fisica.
 * - Coerenza totale con il tema attivo (Light, Dark, Candy Pop, Retro Cyber, System Monet).
 * - Feedback aptico al raggiungimento dello 0° perfetto e accensione dinamica verde smeraldo One UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelView(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sensorManager = remember { LevelSensorManager(context) }

    val levelData by sensorManager.getLevelFlow().collectAsState(
        initial = LevelData(
            roll = 0f,
            pitch = 0f,
            isLevel = true,
            isSensorAvailable = true,
            mode = LevelOrientationMode.HORIZONTAL_SURFACE,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            surfaceTotalAngle = 0f,
            verticalLateralAngle = 0f,
            verticalFrontalAngle = 0f,
            primaryDisplayAngle = 0,
            inclinationFromHorizontal = 0f
        )
    )

    // Feedback aptico all'aggancio dello zero perfetto
    var wasLevel by remember { mutableStateOf(false) }
    LaunchedEffect(levelData.isLevel) {
        if (levelData.isLevel && !wasLevel) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasLevel = levelData.isLevel
    }

    val isDark = CalculatorTheme.colors.isDark
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Colore One UI Green di successo quando la livella è a 0°
    val oneUiSuccessGreen = Color(0xFF2EC866)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Livella Digitale",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        AnimatedContent(
                            targetState = levelData.mode,
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically { it / 2 })
                                    .togetherWith(fadeOut(tween(180)) + slideOutVertically { -it / 2 })
                            },
                            label = "TopBarSubtitleTransition"
                        ) { mode ->
                            Text(
                                text = if (mode == LevelOrientationMode.HORIZONTAL_SURFACE) {
                                    "Modalità Superficie (In piano)"
                                } else {
                                    "Modalità Parete (Verticale)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLandscape) {
                // Layout Orizzontale (Landscape): Canvas a sinistra, schede e dati ad asse a destra
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Viewport Livella principale a sinistra
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        LevelViewportCard(
                            levelData = levelData,
                            isDark = isDark,
                            successGreen = oneUiSuccessGreen,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Schede assi e stato One UI a destra
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OneUiStatusPill(levelData = levelData, successGreen = oneUiSuccessGreen)

                        Spacer(modifier = Modifier.height(12.dp))

                        OneUiAxesCards(
                            levelData = levelData,
                            successGreen = oneUiSuccessGreen,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Layout Verticale (Portrait): Top Status Pill, Main Viewport centrale, Schede Assi in basso
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Pillola superiore descrittiva One UI
                    OneUiStatusPill(
                        levelData = levelData,
                        successGreen = oneUiSuccessGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Card viewport principale con animazione fluida tra superficie e parete
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LevelViewportCard(
                            levelData = levelData,
                            isDark = isDark,
                            successGreen = oneUiSuccessGreen,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Schede di controllo per entrambi gli assi (Primario e Riferimento)
                    OneUiAxesCards(
                        levelData = levelData,
                        successGreen = oneUiSuccessGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 76.dp)
                    )
                }
            }

            // Barra degli strumenti fluttuante Material 3 coerente con tutte le schermate
            PixelFloatingToolBar(
                currentScreen = ActiveScreen.TOOLBOX_LEVEL,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * Pillola informativa di stato in stile One UI.
 * Mostra con icona e testo chiaro la posizione fisica del dispositivo e lo stato di allineamento.
 */
@Composable
private fun OneUiStatusPill(
    levelData: LevelData,
    successGreen: Color,
    modifier: Modifier = Modifier
) {
    val pillBg = if (levelData.isLevel) {
        successGreen.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant
    }

    val pillTextColor = if (levelData.isLevel) {
        successGreen
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = pillBg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    levelData.isLevel -> Icons.Default.Check
                    levelData.mode == LevelOrientationMode.HORIZONTAL_SURFACE -> Icons.Default.ViewInAr
                    else -> Icons.Default.Wallpaper
                },
                contentDescription = null,
                tint = pillTextColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    levelData.isLevel -> "Perfettamente allineato (0°)"
                    levelData.mode == LevelOrientationMode.HORIZONTAL_SURFACE -> "Appoggiato su superficie piana"
                    else -> "In posizione verticale su parete"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = pillTextColor
            )
        }
    }
}

/**
 * Card contenitrice principale della Livella:
 * - In modalità Orizzontale (Surface): renderizza i due cerchi simmetrici con il grado centrale (Foto 2).
 * - In modalità Verticale (Wall): renderizza l'orizzonte artificiale con i trattini di livello (Foto 1).
 * - Lo sfondo si colora fluidamente di One UI Green quando si raggiunge lo 0°.
 */
@Composable
private fun LevelViewportCard(
    levelData: LevelData,
    isDark: Boolean,
    successGreen: Color,
    modifier: Modifier = Modifier
) {
    // Transizione fluida del colore dello sfondo (diventa verde brillante a 0°)
    val targetBgColor = if (levelData.isLevel) {
        successGreen
    } else {
        if (isDark) Color(0xFF101216) else Color(0xFFF1F3F8)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "LevelViewportBackground"
    )

    Card(
        modifier = modifier
            .shadow(if (levelData.isLevel) 12.dp else 4.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Transizione fluida e automatica tra modalità Orizzontale (piano) e Verticale (parete)
            AnimatedContent(
                targetState = levelData.mode,
                transitionSpec = {
                    (fadeIn(tween(350)) + scaleIn(initialScale = 0.92f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(fadeOut(tween(250)) + scaleOut(targetScale = 0.92f))
                },
                label = "LevelModeContentTransition"
            ) { mode ->
                when (mode) {
                    LevelOrientationMode.HORIZONTAL_SURFACE -> {
                        SurfaceLevelCanvas(
                            levelData = levelData,
                            isDark = isDark,
                            successGreen = successGreen
                        )
                    }
                    LevelOrientationMode.VERTICAL_WALL -> {
                        VerticalWallLevelCanvas(
                            levelData = levelData,
                            isDark = isDark,
                            successGreen = successGreen
                        )
                    }
                }
            }
        }
    }
}

/**
 * MODALITÀ ORIZZONTALE (Superficie / Tavolo - Fedele alla Foto 2 utente):
 * - Due cerchi bianchi/chiari che si muovono nello spazio.
 * - Asse Y: controlla l'inclinazione testa-fondo (Pitch).
 * - Asse X: riferimento all'altro asse destra-sinistra (Roll).
 * - I cerchi si allontanano in modo simmetrico con il grado di inclinazione al centro (es. -26°).
 * - A 0° si uniscono perfettamente al centro in un unico cerchio verde con 0°.
 */
@Composable
private fun SurfaceLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    successGreen: Color
) {
    // Animazione fluida a 120Hz degli assi di inclinazione con spring reattivo
    val animRoll by animateFloatAsState(
        targetValue = levelData.rollLeftRight,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "SurfaceAnimRoll"
    )
    val animPitch by animateFloatAsState(
        targetValue = levelData.pitchHeadTail,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "SurfaceAnimPitch"
    )

    // Colore testo e cerchi
    val circleFillColor = when {
        levelData.isLevel -> Color.White.copy(alpha = 0.96f)
        isDark -> Color.White
        else -> Color.White
    }

    val textColor = when {
        levelData.isLevel -> Color(0xFF0F5A27) // Verde scuro a contrasto perfetto sul cerchio bianco
        isDark -> Color.White
        else -> Color(0xFF191C20)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = size.minDimension

            // Raggio dei due cerchi (proporzionato al display, circa ~50-65dp)
            val circleRadius = (minDim * 0.17f).coerceIn(40.dp.toPx(), 72.dp.toPx())

            // Massima escursione consentita per mantenere i cerchi visibili
            val maxTravel = (minDim / 2f) - circleRadius - 8.dp.toPx()

            // Normalizzazione angolo (fondo scala a circa 35°)
            val normX = (animRoll / 35f).coerceIn(-1f, 1f)
            val normY = (animPitch / 35f).coerceIn(-1f, 1f)

            val offsetX = normX * maxTravel
            val offsetY = normY * maxTravel

            // Cerchio 1 (polo positivo: testa e destra)
            val c1Center = Offset(center.x + offsetX, center.y + offsetY)
            // Cerchio 2 (polo simmetrico opposto: fondo e sinistra, come nella foto 2)
            val c2Center = Offset(center.x - offsetX, center.y - offsetY)

            if (levelData.isLevel) {
                // Quando è in piano (0°): i due cerchi si fondono esattamente al centro
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = circleRadius + 14.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = circleFillColor,
                    radius = circleRadius,
                    center = center
                )
            } else {
                // Quando è inclinato: due cerchi distinti e simmetrici
                // Cerchio 1 con ombra/alone
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
                    radius = circleRadius + 4.dp.toPx(),
                    center = c1Center
                )
                drawCircle(
                    color = circleFillColor,
                    radius = circleRadius,
                    center = c1Center
                )

                // Cerchio 2 con ombra/alone
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
                    radius = circleRadius + 4.dp.toPx(),
                    center = c2Center
                )
                drawCircle(
                    color = circleFillColor,
                    radius = circleRadius,
                    center = c2Center
                )
            }
        }

        // Gradi visualizzati al centro dello schermo (es. -26° o 0°)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val displayDeg = if (levelData.isLevel) "0°" else "${levelData.primaryDisplayAngle}°"
            Text(
                text = displayDeg,
                fontSize = if (levelData.isLevel) 52.sp else 46.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * MODALITÀ VERTICALE (Parete / Bordo - Fedele alla Foto 1 utente):
 * - Orizzonte artificiale che ruota con l'inclinazione destra-sinistra (Roll).
 * - Indicatore con trattini di mira centrali: `—   -12°   —`.
 * - Riferimento all'altro asse: inclinazione frontale testa-fondo (avanti/indietro rispetto alla parete).
 * - A 0° lo sfondo si illumina di One UI Green e la linea si unisce in modo continuo: `—  0°  —`.
 */
@Composable
private fun VerticalWallLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    successGreen: Color
) {
    // Animazione dell'inclinazione laterale (destra/sinistra)
    val animLateral by animateFloatAsState(
        targetValue = levelData.verticalLateralAngle,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "WallAnimLateral"
    )

    // Animazione dell'inclinazione frontale testa-fondo (parete avanti/indietro)
    val animFrontal by animateFloatAsState(
        targetValue = levelData.verticalFrontalAngle,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "WallAnimFrontal"
    )

    val contentColor = if (levelData.isLevel) Color.White else if (isDark) Color.White else Color(0xFF191C20)
    val horizonLineColor = if (levelData.isLevel) Color.White else if (isDark) Color.White.copy(alpha = 0.45f) else Color(0xFF2B303A).copy(alpha = 0.35f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val width = size.width
            val height = size.height

            // 1. Orizzonte artificiale che ruota in tempo reale (-animLateral)
            // L'offset Y dell'orizzonte riflette l'asse frontale testa-fondo (avanti/indietro rispetto alla parete)
            val frontalOffsetPx = (animFrontal / 45f).coerceIn(-1f, 1f) * (height * 0.18f)

            rotate(-animLateral, pivot = center) {
                // Linea dell'orizzonte che attraversa la larghezza
                val horizonY = center.y + frontalOffsetPx
                drawLine(
                    color = horizonLineColor,
                    start = Offset(-width * 0.5f, horizonY),
                    end = Offset(width * 1.5f, horizonY),
                    strokeWidth = if (levelData.isLevel) 3.5.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Trattini di mira orizzontali fissi di riferimento telaio: `—   ...   —`
            val dashLength = 36.dp.toPx()
            val textGap = 52.dp.toPx()

            // Trattino sinistro `—`
            drawLine(
                color = contentColor,
                start = Offset(center.x - textGap - dashLength, center.y),
                end = Offset(center.x - textGap, center.y),
                strokeWidth = if (levelData.isLevel) 4.5.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Trattino destro `—`
            drawLine(
                color = contentColor,
                start = Offset(center.x + textGap, center.y),
                end = Offset(center.x + textGap + dashLength, center.y),
                strokeWidth = if (levelData.isLevel) 4.5.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3. Indicatore di riferimento dell'altro asse (Planarità Parete testa-fondo)
            // Se il telefono è inclinato avanti o indietro rispetto alla parete verticale,
            // un piccolo arco o bolla di planarità mostra lo scostamento frontale
            val bubbleRadius = 4.dp.toPx()
            val bubbleY = (center.y + (animFrontal / 30f).coerceIn(-1f, 1f) * 36.dp.toPx()).coerceIn(center.y - 36.dp.toPx(), center.y + 36.dp.toPx())

            // Indicatore di planarità frontale sulla mezzeria
            drawCircle(
                color = contentColor.copy(alpha = if (abs(animFrontal) < 1f) 0.9f else 0.4f),
                radius = bubbleRadius,
                center = Offset(center.x, bubbleY)
            )
        }

        // 4. Testo centrale con i gradi (es. -12° o 0°)
        val displayAngleText = if (levelData.isLevel) "0°" else "${levelData.primaryDisplayAngle}°"
        Text(
            text = displayAngleText,
            fontSize = if (levelData.isLevel) 52.sp else 46.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Schede informative One UI per ENTRAMBI gli assi di rotazione:
 * - In Orizzontale: Asse Principale Testa-Fondo e Riferimento Destra-Sinistra.
 * - In Verticale: Asse Principale Laterale Destra-Sinistra e Riferimento Parete Testa-Fondo.
 * Ciascuna scheda si colora di verde ed evidenzia l'allineamento quando l'asse raggiunge lo 0.0°.
 */
@Composable
private fun OneUiAxesCards(
    levelData: LevelData,
    successGreen: Color,
    modifier: Modifier = Modifier
) {
    val isHorizontal = levelData.mode == LevelOrientationMode.HORIZONTAL_SURFACE

    // Definizione etichette e valori per asse primario e asse di riferimento
    val primaryTitle = if (isHorizontal) "Asse Testa - Fondo" else "Inclinazione Laterale"
    val primarySubtitle = if (isHorizontal) "Inclinazione longitudinale" else "Destra / Sinistra"
    val primaryValue = if (isHorizontal) levelData.pitchHeadTail else levelData.verticalLateralAngle
    val primaryIsAligned = abs(primaryValue) < 0.6f

    val secondaryTitle = if (isHorizontal) "Asse Destra - Sinistra" else "Planarità Parete"
    val secondarySubtitle = if (isHorizontal) "Riferimento laterale" else "Testa / Fondo (Avanti-Dietro)"
    val secondaryValue = if (isHorizontal) levelData.rollLeftRight else levelData.verticalFrontalAngle
    val secondaryIsAligned = abs(secondaryValue) < 0.6f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scheda 1: Asse Primario
        AxisCard(
            title = primaryTitle,
            subtitle = primarySubtitle,
            value = primaryValue,
            isAligned = primaryIsAligned,
            icon = if (isHorizontal) Icons.Default.Height else Icons.Default.SwapHoriz,
            successGreen = successGreen,
            modifier = Modifier.weight(1f)
        )

        // Scheda 2: Riferimento all'altro asse di rotazione
        AxisCard(
            title = secondaryTitle,
            subtitle = secondarySubtitle,
            value = secondaryValue,
            isAligned = secondaryIsAligned,
            icon = if (isHorizontal) Icons.Default.SwapHoriz else Icons.Default.Height,
            successGreen = successGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Singola scheda di un asse con angoli arrotondati 22dp, icone One UI ed evidenziazione verde.
 */
@Composable
private fun AxisCard(
    title: String,
    subtitle: String,
    value: Float,
    isAligned: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    successGreen: Color,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isAligned) {
        successGreen.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surface
    }

    val valueColor = if (isAligned) {
        successGreen
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isAligned) successGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${"%.1f".format(value)}°",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
