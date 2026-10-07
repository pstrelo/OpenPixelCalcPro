package com.google.pixel.calculator.ui.toolbox

import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.sensors.CompassSensorManager
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * 3-axis motion tracker to calculate figure-8 calibration progress.
 */
private class CalibrationTracker(initialYaw: Float) {
    var minRoll: Float = 0f
    var maxRoll: Float = 0f
    var minPitch: Float = 0f
    var maxPitch: Float = 0f
    var totalYawTurn: Float = 0f
    var lastYaw: Float = initialYaw
    var isInitialized: Boolean = false

    fun update(roll: Float, pitch: Float, yaw: Float): Float {
        if (!isInitialized) {
            minRoll = roll
            maxRoll = roll
            minPitch = pitch
            maxPitch = pitch
            lastYaw = yaw
            isInitialized = true
        } else {
            if (roll < minRoll) minRoll = roll
            if (roll > maxRoll) maxRoll = roll
            if (pitch < minPitch) minPitch = pitch
            if (pitch > maxPitch) maxPitch = pitch
        }

        var yawDiff = abs(yaw - lastYaw)
        if (yawDiff > 180f) yawDiff = 360f - yawDiff
        if (yawDiff > 2f) {
            totalYawTurn += yawDiff
            lastYaw = yaw
        }

        val rSpan = ((maxRoll - minRoll).coerceIn(0f, 45f)) / 45f
        val pSpan = ((maxPitch - minPitch).coerceIn(0f, 45f)) / 45f
        val ySpan = (totalYawTurn / 360f).coerceIn(0f, 1f)

        return (rSpan * 0.35f + pSpan * 0.35f + ySpan * 0.30f).coerceIn(0f, 1f)
    }
}

/**
 * Compass Recalibration Dialog in Material 3 / Pixel style.
 * Guides the user through the figure-8 motion and tracks angular coverage in real-time.
 */
@Composable
fun CompassCalibrationDialog(
    compassData: CompassSensorManager.CompassData,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val tracker = remember { CalibrationTracker(compassData.rawAzimuth) }
    var motionScore by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(compassData.roll, compassData.pitch, compassData.rawAzimuth) {
        motionScore = tracker.update(compassData.roll, compassData.pitch, compassData.rawAzimuth)
    }

    val accuracyBonus = when (compassData.accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> 0.4f
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> 0.2f
        else -> 0f
    }

    val rawProgress = (motionScore * 0.7f + accuracyBonus).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CalibrationProgressAnimation"
    )

    val isCalibrated = animatedProgress >= 0.95f

    var hasTriggeredSuccessHaptic by remember { mutableStateOf(false) }
    LaunchedEffect(isCalibrated) {
        if (isCalibrated && !hasTriggeredSuccessHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredSuccessHaptic = true
        }
    }

    // Indicator animating along the figure-8 path
    val infiniteTransition = rememberInfiniteTransition(label = "Figure8Loop")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Figure8Time"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isCalibrated) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = if (isCalibrated) Icons.Default.CheckCircle else Icons.Default.RotateRight,
                        contentDescription = null,
                        tint = if (isCalibrated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isCalibrated) stringResource(R.string.calibration_completed) else stringResource(R.string.calibration_recalibrate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCalibrated) {
                        stringResource(R.string.calibration_success_desc)
                    } else {
                        stringResource(R.string.calibration_instruction)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Canvas rendering figure-8 lemniscata animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val pathColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val activeColor = MaterialTheme.colorScheme.primary

                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f
                        val a = (w * 0.38f)
                        val b = (h * 0.42f)

                        val numSteps = 120
                        val figure8Path = Path()
                        for (i in 0..numSteps) {
                            val angle = (i.toFloat() / numSteps) * 2f * PI.toFloat()
                            val px = cx + a * sin(angle)
                            val py = cy + b * sin(2f * angle) / 2f
                            if (i == 0) figure8Path.moveTo(px, py) else figure8Path.lineTo(px, py)
                        }
                        figure8Path.close()

                        drawPath(
                            path = figure8Path,
                            color = pathColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        val indicatorX = cx + a * sin(t)
                        val indicatorY = cy + b * sin(2f * t) / 2f

                        drawCircle(
                            color = activeColor.copy(alpha = 0.25f),
                            radius = 16.dp.toPx(),
                            center = Offset(indicatorX, indicatorY)
                        )
                        drawCircle(
                            color = activeColor,
                            radius = 7.dp.toPx(),
                            center = Offset(indicatorX, indicatorY)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress bar and percentage
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.calibration_progress),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isCalibrated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sensor accuracy badge
                val accuracyLabel = when (compassData.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> stringResource(R.string.sensor_accuracy_high)
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> stringResource(R.string.sensor_accuracy_medium)
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> stringResource(R.string.sensor_accuracy_low)
                    else -> stringResource(R.string.sensor_uncalibrated)
                }

                val accuracyColor = when (compassData.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> MaterialTheme.colorScheme.primary
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Color(0xFFEAB308)
                    else -> MaterialTheme.colorScheme.error
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = accuracyColor.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accuracyColor, CircleShape)
                        )
                        Text(
                            text = accuracyLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = accuracyColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCalibrated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (isCalibrated) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = if (isCalibrated) stringResource(R.string.done) else stringResource(R.string.close),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
