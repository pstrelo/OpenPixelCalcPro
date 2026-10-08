package com.google.pixel.calculator.ui.toolbox

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.sensors.LevelData
import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import kotlin.math.abs

/**
 * Schermata della Livella Digitale a TUTTO SCHERMO:
 * - Senza riquadri o cornici intermedie: lo sfondo riempie l'intero display.
 * - Modalità Orizzontale (Superficie / Tavolo) con 2 cerchi interattivi simmetrici.
 * - Modalità Verticale (Parete / Bordo) con orizzonte artificiale e mirini.
 * - Colori vivaci e accesi legati al tema attivo (Google Blue Pixel in Light, Luminous Blue in Dark,
 *   Neon in Retro Cyber, Coral in Candy Pop, Verde Smeraldo brillante a 0°).
 * - Transizione fluida e feedback aptico al raggiungimento dello 0.0° perfetto.
 */
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

    // Colore accento vivo, acceso e coerente con il tema attivo
    val themeAccent = CalculatorTheme.colors.equalsContainer.takeIf { it != Color.Unspecified }
        ?: MaterialTheme.colorScheme.primary

    val vibrantAccent = when {
        // Pixel Light: anziché grigio ardesia (#354558), usiamo il vivo Google Blue Pixel (#1A73E8)
        !isDark && (themeAccent == Color(0xFF354558) || themeAccent == Color(0xFFE2E3EC)) -> Color(0xFF1A73E8)
        // Pixel Dark: azzurro vivo luminoso (#589BFF) anziché pastello slavato
        isDark && (themeAccent == Color(0xFFD3E3FD) || themeAccent == Color(0xFF373942)) -> Color(0xFF589BFF)
        themeAccent != Color.Unspecified -> themeAccent
        else -> MaterialTheme.colorScheme.primary
    }

    val onVibrantAccent = when {
        vibrantAccent == Color(0xFF1A73E8) || vibrantAccent == Color(0xFF589BFF) -> Color.White
        else -> CalculatorTheme.colors.equalsContent.takeIf { it != Color.Unspecified } ?: Color.White
    }

    // Verde smeraldo One UI brillante al raggiungimento dello 0.0°
    val oneUiSuccessGreen = Color(0xFF00D06C)

    // Sfondo a tutto schermo: quando è a livello (0°) si illumina fluidamente di verde smeraldo
    val targetBgColor = if (levelData.isLevel) {
        oneUiSuccessGreen
    } else {
        CalculatorTheme.colors.background
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "FullScreenLevelBackground"
    )

    BackHandler { onBack() }

    Scaffold(
        containerColor = animatedBgColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Viewport a tutto schermo senza riquadri o margini intermedi
            AnimatedContent(
                targetState = levelData.mode,
                transitionSpec = {
                    (fadeIn(tween(350)) + scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                        .togetherWith(fadeOut(tween(250)) + scaleOut(targetScale = 0.94f))
                },
                label = "LevelModeContentTransition",
                modifier = Modifier.fillMaxSize()
            ) { mode ->
                when (mode) {
                    LevelOrientationMode.HORIZONTAL_SURFACE -> {
                        SurfaceLevelCanvas(
                            levelData = levelData,
                            isDark = isDark,
                            accentColor = vibrantAccent,
                            onAccentColor = onVibrantAccent
                        )
                    }
                    LevelOrientationMode.VERTICAL_WALL -> {
                        VerticalWallLevelCanvas(
                            levelData = levelData,
                            isDark = isDark,
                            accentColor = vibrantAccent,
                            onAccentColor = onVibrantAccent
                        )
                    }
                }
            }

            // Pulsante indietro in alto a sinistra (discreto e fluttuante)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = if (isLandscape) 20.dp else 16.dp,
                        top = if (isLandscape) 12.dp else 16.dp
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_calculator),
                    tint = if (levelData.isLevel) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Barra degli strumenti fluttuante Material 3 (ancorata in basso a sinistra)
            PixelFloatingToolBar(
                currentScreen = ActiveScreen.TOOLBOX_LEVEL,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * MODALITÀ ORIZZONTALE (Superficie / Tavolo - 2 cerchi simmetrici):
 * - Due cerchi con contorno vivace e punto centrale che si muovono con Roll e Pitch.
 * - Cerchio bersaglio al centro con mirini a croce.
 * - Testo dei gradi ad altissimo contrasto (es. -26° o 0°).
 * - A 0° i cerchi si fondono fluidamente al centro in un cerchio bianco splendente.
 */
@Composable
private fun SurfaceLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    accentColor: Color,
    onAccentColor: Color
) {
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

    // Riempimento dei cerchi in base al tema
    val tiltedCircleFill = if (isDark) {
        Color(0xFF22252C)
    } else {
        Color.White
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = size.minDimension

            // Raggio cerchi proporzionato al display
            val circleRadius = (minDim * 0.17f).coerceIn(44.dp.toPx(), 74.dp.toPx())
            val maxTravel = (minDim / 2f) - circleRadius - 12.dp.toPx()

            val normX = (animRoll / 35f).coerceIn(-1f, 1f)
            val normY = (animPitch / 35f).coerceIn(-1f, 1f)

            val offsetX = normX * maxTravel
            val offsetY = normY * maxTravel

            val c1Center = Offset(center.x + offsetX, center.y + offsetY)
            val c2Center = Offset(center.x - offsetX, center.y - offsetY)

            if (levelData.isLevel) {
                // Fusione perfetta al centro a 0°
                drawCircle(
                    color = Color.White.copy(alpha = 0.30f),
                    radius = circleRadius + 16.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = circleRadius,
                    center = center
                )
            } else {
                // Mirino bersaglio al centro con anelli concentrici accesi
                drawCircle(
                    color = accentColor.copy(alpha = 0.35f),
                    radius = circleRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.18f),
                    radius = circleRadius * 0.5f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Trattini di mira a croce
                val reticleTick = 12.dp.toPx()
                val reticleGap = 4.dp.toPx()
                drawLine(
                    color = accentColor.copy(alpha = 0.55f),
                    start = Offset(center.x - circleRadius - reticleTick, center.y),
                    end = Offset(center.x - circleRadius + reticleGap, center.y),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.55f),
                    start = Offset(center.x + circleRadius - reticleGap, center.y),
                    end = Offset(center.x + circleRadius + reticleTick, center.y),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.55f),
                    start = Offset(center.x, center.y - circleRadius - reticleTick),
                    end = Offset(center.x, center.y - circleRadius + reticleGap),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.55f),
                    start = Offset(center.x, center.y + circleRadius - reticleGap),
                    end = Offset(center.x, center.y + circleRadius + reticleTick),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Cerchio 1 (polo testa/destra)
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.40f else 0.12f),
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
                    style = Stroke(width = 3.5.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 6.dp.toPx(),
                    center = c1Center
                )

                // Cerchio 2 (polo fondo/sinistra)
                drawCircle(
                    color = Color.Black.copy(alpha = if (isDark) 0.40f else 0.12f),
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
                    style = Stroke(width = 3.5.dp.toPx())
                )
                drawCircle(
                    color = accentColor,
                    radius = 6.dp.toPx(),
                    center = c2Center
                )
            }
        }

        // Testo gradi al centro dello schermo
        val displayDeg = if (levelData.isLevel) "0°" else "${levelData.primaryDisplayAngle}°"
        val degreeTextColor = if (levelData.isLevel) {
            Color(0xFF0A5528) // Verde profondo ad alto contrasto sul cerchio bianco centrale
        } else {
            if (isDark) Color.White else Color(0xFF191C20)
        }

        Text(
            text = displayDeg,
            fontSize = if (levelData.isLevel) 52.sp else 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = degreeTextColor,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * MODALITÀ VERTICALE (Parete / Bordo):
 * - Orizzonte artificiale che ruota con il rollio laterale.
 * - Mirino a trattini con gradi centrali: `—  -12°  —`.
 * - Indicatore di planarità frontale.
 * - Linea e indicatore a contrasto elevato con colori vivaci.
 */
@Composable
private fun VerticalWallLevelCanvas(
    levelData: LevelData,
    isDark: Boolean,
    accentColor: Color,
    onAccentColor: Color
) {
    val animLateral by animateFloatAsState(
        targetValue = levelData.verticalLateralAngle,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "WallAnimLateral"
    )

    val animFrontal by animateFloatAsState(
        targetValue = levelData.verticalFrontalAngle,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "WallAnimFrontal"
    )

    val contentColor = if (levelData.isLevel) {
        Color.White
    } else {
        if (isDark) Color.White else Color(0xFF191C20)
    }

    val horizonLineColor = if (levelData.isLevel) {
        Color.White.copy(alpha = 0.85f)
    } else {
        accentColor
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val width = size.width
            val height = size.height

            // 1. Orizzonte artificiale rotante
            val frontalOffsetPx = (animFrontal / 45f).coerceIn(-1f, 1f) * (height * 0.18f)

            rotate(-animLateral, pivot = center) {
                val horizonY = center.y + frontalOffsetPx
                drawLine(
                    color = horizonLineColor,
                    start = Offset(-width * 0.5f, horizonY),
                    end = Offset(width * 1.5f, horizonY),
                    strokeWidth = if (levelData.isLevel) 4.dp.toPx() else 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Trattini di mira orizzontali: `—   ...   —`
            val dashLength = 40.dp.toPx()
            val textGap = 56.dp.toPx()

            // Trattino sinistro
            drawLine(
                color = contentColor,
                start = Offset(center.x - textGap - dashLength, center.y),
                end = Offset(center.x - textGap, center.y),
                strokeWidth = if (levelData.isLevel) 5.dp.toPx() else 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Trattino destro
            drawLine(
                color = contentColor,
                start = Offset(center.x + textGap, center.y),
                end = Offset(center.x + textGap + dashLength, center.y),
                strokeWidth = if (levelData.isLevel) 5.dp.toPx() else 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 3. Indicatore di planarità frontale sulla mezzeria
            val bubbleRadius = 5.dp.toPx()
            val bubbleY = (center.y + (animFrontal / 30f).coerceIn(-1f, 1f) * 38.dp.toPx())
                .coerceIn(center.y - 38.dp.toPx(), center.y + 38.dp.toPx())

            drawCircle(
                color = contentColor.copy(alpha = if (abs(animFrontal) < 1.2f) 0.95f else 0.45f),
                radius = bubbleRadius,
                center = Offset(center.x, bubbleY)
            )
        }

        // Testo gradi al centro
        val displayAngleText = if (levelData.isLevel) "0°" else "${levelData.primaryDisplayAngle}°"
        Text(
            text = displayAngleText,
            fontSize = if (levelData.isLevel) 52.sp else 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )
    }
}
