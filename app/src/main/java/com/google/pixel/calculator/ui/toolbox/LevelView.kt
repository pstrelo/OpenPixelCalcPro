package com.google.pixel.calculator.ui.toolbox

import androidx.activity.compose.BackHandler
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

    // Colore accento del tema attivo come colore principale della livella
    val themeAccent = CalculatorTheme.colors.equalsContainer.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary
    val onThemeAccent = CalculatorTheme.colors.equalsContent.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.onPrimary

    BackHandler { onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Viewport a schermo intero fino alla linea del pulsante strumenti con solo il riquadro grafico
            LevelViewportCard(
                levelData = levelData,
                isDark = isDark,
                accentColor = themeAccent,
                onAccentColor = onThemeAccent,
                isLandscape = isLandscape,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = if (isLandscape) 16.dp else 16.dp,
                        end = if (isLandscape) 16.dp else 16.dp,
                        top = if (isLandscape) 8.dp else 16.dp,
                        bottom = if (isLandscape) 0.dp else 16.dp
                    )
            )

            // Freccia indietro in alto a sinistra
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = if (isLandscape) 24.dp else 20.dp, top = if (isLandscape) 14.dp else 20.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna alla calcolatrice",
                    tint = if (levelData.isLevel) onThemeAccent else MaterialTheme.colorScheme.onSurface
                )
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
 * Card contenitrice principale della Livella:
 * - In modalità Orizzontale (Surface): renderizza i due cerchi simmetrici con il grado centrale.
 * - In modalità Verticale (Wall): renderizza l'orizzonte artificiale con i trattini e il grado centrale.
 * - Colori sincronizzati con il tema attivo: lo sfondo si colora fluidamente del colore d'accento a 0°.
 */
@Composable
private fun LevelViewportCard(
    levelData: LevelData,
    isDark: Boolean,
    accentColor: Color,
    onAccentColor: Color,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Sfondo del container dal tema attivo quando non allineato
    val defaultContainerBg = MaterialTheme.colorScheme.surfaceContainer
        ?: if (isDark) Color(0xFF1E2025) else Color(0xFFF1F3F8)

    // A 0° (perfetto allineamento), lo sfondo si illumina in modo fluido del colore d'accento del tema
    val targetBgColor = if (levelData.isLevel) {
        accentColor
    } else {
        defaultContainerBg
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "LevelViewportBackground"
    )

    val cardShape = if (isLandscape) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(28.dp)
    }

    Card(
        modifier = modifier
            .shadow(if (levelData.isLevel) 12.dp else 4.dp, shape = cardShape)
            .clip(cardShape),
        shape = cardShape,
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
                            accentColor = accentColor,
                            onAccentColor = onAccentColor
                        )
                    }
                    LevelOrientationMode.VERTICAL_WALL -> {
                        VerticalWallLevelCanvas(
                            levelData = levelData,
                            isDark = isDark,
                            accentColor = accentColor,
                            onAccentColor = onAccentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * MODALITÀ ORIZZONTALE (Superficie / Tavolo):
 * - Due cerchi simmetrici guidati dagli assi testa-fondo (Pitch) e destra-sinistra (Roll).
 * - Valore dei gradi reintrodotto al centro dello schermo (es. -26° o 0°).
 * - Colori sincronizzati con il tema attivo, con l'accento come colore principale.
 * - A 0° i cerchi si fondono esattamente al centro e lo sfondo si accende con l'accento del tema.
 */
@Composable
private fun SurfaceLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    accentColor: Color,
    onAccentColor: Color
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

    // Luminanza dell'accento per garantire sempre contrasto perfetto
    val isLightAccent = (0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue) > 0.5f

    // A 0° il cerchio fuso centrale a contrasto
    val levelMergedCircleColor = if (isLightAccent) Color.White.copy(alpha = 0.96f) else onAccentColor

    // Colore del testo dei gradi al centro:
    val centerTextColorAtZero = if (isLightAccent) onAccentColor else Color.White
    val centerTextColorTilted = if (!isDark && isLightAccent) MaterialTheme.colorScheme.onSurface else accentColor
    val centerDegreeTextColor = if (levelData.isLevel) centerTextColorAtZero else centerTextColorTilted

    val tiltedCircleFill = if (isDark) {
        (MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.92f)
    } else {
        Color.White.copy(alpha = 0.95f)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = size.minDimension

            // Raggio dei cerchi proporzionato al display (~44-72dp)
            val circleRadius = (minDim * 0.17f).coerceIn(42.dp.toPx(), 72.dp.toPx())

            // Massima escursione consentita per mantenere i cerchi visibili
            val maxTravel = (minDim / 2f) - circleRadius - 8.dp.toPx()

            // Normalizzazione angolo (fondo scala a circa 35°)
            val normX = (animRoll / 35f).coerceIn(-1f, 1f)
            val normY = (animPitch / 35f).coerceIn(-1f, 1f)

            val offsetX = normX * maxTravel
            val offsetY = normY * maxTravel

            // Cerchio 1 (polo positivo: testa e destra)
            val c1Center = Offset(center.x + offsetX, center.y + offsetY)
            // Cerchio 2 (polo simmetrico opposto: fondo e sinistra)
            val c2Center = Offset(center.x - offsetX, center.y - offsetY)

            if (levelData.isLevel) {
                // Quando è in piano (0°): i due cerchi si fondono esattamente al centro
                drawCircle(
                    color = (if (isLightAccent) Color.White else onAccentColor).copy(alpha = 0.25f),
                    radius = circleRadius + 14.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = levelMergedCircleColor,
                    radius = circleRadius,
                    center = center
                )
            } else {
                // Cerchio di mira guida centrale di riferimento
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = circleRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                val reticleTick = 10.dp.toPx()
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(center.x - circleRadius - reticleTick, center.y),
                    end = Offset(center.x - circleRadius + 4.dp.toPx(), center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(center.x + circleRadius - 4.dp.toPx(), center.y),
                    end = Offset(center.x + circleRadius + reticleTick, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y - circleRadius - reticleTick),
                    end = Offset(center.x, center.y - circleRadius + 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(center.x, center.y + circleRadius - 4.dp.toPx()),
                    end = Offset(center.x, center.y + circleRadius + reticleTick),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Cerchio 1 con bordo accento e riempimento tema
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
                    radius = circleRadius + 4.dp.toPx(),
                    center = c1Center
                )
                drawCircle(
                    color = tiltedCircleFill,
                    radius = circleRadius,
                    center = c1Center
                )
                drawCircle(
                    color = accentColor,
                    radius = circleRadius,
                    center = c1Center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.dp.toPx(),
                    center = c1Center
                )

                // Cerchio 2 simmetrico
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
                    radius = circleRadius + 4.dp.toPx(),
                    center = c2Center
                )
                drawCircle(
                    color = tiltedCircleFill,
                    radius = circleRadius,
                    center = c2Center
                )
                drawCircle(
                    color = accentColor,
                    radius = circleRadius,
                    center = c2Center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 5.dp.toPx(),
                    center = c2Center
                )
            }
        }

        // Valore dei gradi reintrodotto al centro dello schermo
        val displayDeg = if (levelData.isLevel) "0°" else "${levelData.primaryDisplayAngle}°"
        Text(
            text = displayDeg,
            fontSize = if (levelData.isLevel) 52.sp else 46.sp,
            fontWeight = FontWeight.Bold,
            color = centerDegreeTextColor,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * MODALITÀ VERTICALE (Parete / Bordo):
 * - Orizzonte artificiale che ruota con l'inclinazione destra-sinistra (Roll).
 * - Indicatore con trattini di mira e valore dei gradi reintrodotto al centro: `—   -12°   —`.
 * - Riferimento all'altro asse: inclinazione frontale testa-fondo (avanti/indietro rispetto alla parete).
 * - Colori sincronizzati con il tema attivo, con l'accento come colore principale.
 * - A 0° lo sfondo si illumina con l'accento del tema e gli indicatori passano a onAccent: `—  0°  —`.
 */
@Composable
private fun VerticalWallLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    accentColor: Color,
    onAccentColor: Color
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

    val isLightAccent = (0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue) > 0.5f

    val contentColor = if (levelData.isLevel) {
        if (isLightAccent) onAccentColor else Color.White
    } else {
        if (!isDark && isLightAccent) MaterialTheme.colorScheme.onSurface else accentColor
    }

    val horizonLineColor = if (levelData.isLevel) {
        (if (isLightAccent) onAccentColor else Color.White).copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f)
    }

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

            // 2. Trattini di mira orizzontali con spazio centrale per il testo dei gradi: `—   [Gradi]   —`
            val dashLength = 36.dp.toPx()
            val textGap = 54.dp.toPx()

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
            val bubbleRadius = 4.dp.toPx()
            val bubbleY = (center.y + (animFrontal / 30f).coerceIn(-1f, 1f) * 36.dp.toPx()).coerceIn(center.y - 36.dp.toPx(), center.y + 36.dp.toPx())

            // Indicatore di planarità frontale sulla mezzeria
            drawCircle(
                color = contentColor.copy(alpha = if (abs(animFrontal) < 1f) 0.95f else 0.45f),
                radius = bubbleRadius,
                center = Offset(center.x, bubbleY)
            )
        }

        // 4. Valore dei gradi reintrodotto al centro dello schermo (es. -12° o 0°)
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
