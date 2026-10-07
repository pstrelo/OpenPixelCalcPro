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
 * Digital Bubble Level screen:
 * - Horizontal Mode (Surface / Table) with 2 symmetric interactive circles
 *   tracking pitch and roll relative to axes.
 * - Vertical Mode (Wall / Edge) with artificial horizon and reticle indicators.
 * - Automatic smooth transition between the two modes based on physical orientation.
 * - Total consistency with active theme (Light, Dark, Candy Pop, Retro Cyber, System Monet).
 * - Haptic feedback when reaching true 0° level.
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

    // Haptic feedback on locking true zero
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

    // Active theme accent color as primary level indicator color
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
            // Viewport card covering screen up to bottom toolbar
            LevelViewportCard(
                levelData = levelData,
                isDark = isDark,
                accentColor = themeAccent,
                onAccentColor = onThemeAccent,
                isLandscape = isLandscape,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (isLandscape) 8.dp else 16.dp,
                        bottom = if (isLandscape) 0.dp else 16.dp
                    )
            )

            // Back button top left
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = if (isLandscape) 24.dp else 20.dp, top = if (isLandscape) 14.dp else 20.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_to_calculator),
                    tint = if (levelData.isLevel) onThemeAccent else MaterialTheme.colorScheme.onSurface
                )
            }

            // Material 3 floating toolbar
            PixelFloatingToolBar(
                currentScreen = ActiveScreen.TOOLBOX_LEVEL,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * Main level container card:
 * - Horizontal surface mode: renders two symmetric circles with central degree value.
 * - Vertical wall mode: renders artificial horizon with reticle and central degree value.
 * - Smooth color animation transitioning to theme accent at 0°.
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
    val defaultContainerBg = MaterialTheme.colorScheme.surfaceContainer
        ?: if (isDark) Color(0xFF1E2025) else Color(0xFFF1F3F8)

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
 * HORIZONTAL MODE (Surface / Table):
 * - Two symmetric circles driven by pitch and roll.
 * - Central degree readout (e.g. -26° or 0°).
 * - Synchronized theme colors.
 * - At 0° circles merge into center.
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

    val isLightAccent = (0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue) > 0.5f

    val levelMergedCircleColor = if (isLightAccent) Color.White.copy(alpha = 0.96f) else onAccentColor

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

            val circleRadius = (minDim * 0.17f).coerceIn(42.dp.toPx(), 72.dp.toPx())
            val maxTravel = (minDim / 2f) - circleRadius - 8.dp.toPx()

            val normX = (animRoll / 35f).coerceIn(-1f, 1f)
            val normY = (animPitch / 35f).coerceIn(-1f, 1f)

            val offsetX = normX * maxTravel
            val offsetY = normY * maxTravel

            val c1Center = Offset(center.x + offsetX, center.y + offsetY)
            val c2Center = Offset(center.x - offsetX, center.y - offsetY)

            if (levelData.isLevel) {
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
 * VERTICAL MODE (Wall / Edge):
 * - Artificial horizon rotating with lateral roll.
 * - Reticle indicator with central degree readout: `— -12° —`.
 * - Frontal planar reference indicator.
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

            val frontalOffsetPx = (animFrontal / 45f).coerceIn(-1f, 1f) * (height * 0.18f)

            rotate(-animLateral, pivot = center) {
                val horizonY = center.y + frontalOffsetPx
                drawLine(
                    color = horizonLineColor,
                    start = Offset(-width * 0.5f, horizonY),
                    end = Offset(width * 1.5f, horizonY),
                    strokeWidth = if (levelData.isLevel) 3.5.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val dashLength = 36.dp.toPx()
            val textGap = 54.dp.toPx()

            // Left dash `—`
            drawLine(
                color = contentColor,
                start = Offset(center.x - textGap - dashLength, center.y),
                end = Offset(center.x - textGap, center.y),
                strokeWidth = if (levelData.isLevel) 4.5.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Right dash `—`
            drawLine(
                color = contentColor,
                start = Offset(center.x + textGap, center.y),
                end = Offset(center.x + textGap + dashLength, center.y),
                strokeWidth = if (levelData.isLevel) 4.5.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Frontal planarity bubble
            val bubbleRadius = 4.dp.toPx()
            val bubbleY = (center.y + (animFrontal / 30f).coerceIn(-1f, 1f) * 36.dp.toPx()).coerceIn(center.y - 36.dp.toPx(), center.y + 36.dp.toPx())

            drawCircle(
                color = contentColor.copy(alpha = if (abs(animFrontal) < 1f) 0.95f else 0.45f),
                radius = bubbleRadius,
                center = Offset(center.x, bubbleY)
            )
        }

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
