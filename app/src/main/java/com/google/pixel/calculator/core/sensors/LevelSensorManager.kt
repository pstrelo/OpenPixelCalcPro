package com.google.pixel.calculator.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class LevelOrientationMode {
    HORIZONTAL_SURFACE, // Phone resting flat on a surface (e.g. table)
    VERTICAL_WALL       // Phone vertical (standing on an edge or resting against a wall)
}

/**
 * Data model for the Samsung One UI-style Digital Level:
 * - Supports horizontal surface mode (head-to-tail and left-to-right tilt)
 * - Supports vertical wall mode (lateral left-to-right tilt and frontal head-to-tail pitch)
 * - Smooth automatic transition based on spatial pose
 */
data class LevelData(
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val isLevel: Boolean = true,
    val isSensorAvailable: Boolean = true,
    val mode: LevelOrientationMode = LevelOrientationMode.HORIZONTAL_SURFACE,
    val pitchHeadTail: Float = 0f,        // Y-axis head-to-tail tilt (-90° to +90°)
    val rollLeftRight: Float = 0f,        // X-axis left-to-right tilt (-90° to +90°)
    val surfaceTotalAngle: Float = 0f,    // Total deviation angle from horizontal plane
    val verticalLateralAngle: Float = 0f, // Left-to-right tilt relative to horizon (-90° to +90°)
    val verticalFrontalAngle: Float = 0f, // Secondary axis reference: wall face pitch (-90° to +90°)
    val primaryDisplayAngle: Int = 0,     // Primary integer angle displayed on screen (e.g. -12°, 0°, -26°)
    val inclinationFromHorizontal: Float = 0f // Tilt relative to table (0° = flat, 90° = standing upright)
)

/**
 * Accelerometer and Gravity sensor manager for the Digital Level.
 * Applies stable low-pass filtering and computes axes with trigonometric accuracy,
 * handling automatic hysteresis transitions between surface and wall modes.
 *
 * Dedicated handling:
 * - Google Pixel: Retains the exact original pipeline and algorithms.
 * - Non-Pixel devices (Samsung Galaxy A-series, etc.): Employs hardware-isolated gravity vectors
 *   (or dynamically calibrated accelerometer filtering) with instantaneous zero-lag orientation detection
 *   and wall alignment.
 */
class LevelSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

    fun getLevelFlow(): Flow<LevelData> = callbackFlow {
        val sm = sensorManager
        val isPixel = DeviceProfile.isGooglePixel()
        val activeSensor = if (isPixel) {
            accelerometer
        } else {
            gravitySensor ?: accelerometer
        }

        if (sm == null || activeSensor == null) {
            trySend(
                LevelData(
                    roll = 0f,
                    pitch = 0f,
                    isLevel = true,
                    isSensorAvailable = false,
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
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            // Low-pass filter to damp sensor noise while preserving fluid responsiveness
            private var smoothedX = 0f
            private var smoothedY = 0f
            private var smoothedZ = if (isPixel) 9.8f else 0f
            private val alpha = 0.22f
            private var isFirstEvent = true

            private var currentMode = LevelOrientationMode.HORIZONTAL_SURFACE

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                if (isPixel) {
                    smoothedX += alpha * (rawX - smoothedX)
                    smoothedY += alpha * (rawY - smoothedY)
                    smoothedZ += alpha * (rawZ - smoothedZ)
                } else {
                    if (isFirstEvent) {
                        smoothedX = rawX
                        smoothedY = rawY
                        smoothedZ = rawZ
                        isFirstEvent = false
                    } else {
                        smoothedX += alpha * (rawX - smoothedX)
                        smoothedY += alpha * (rawY - smoothedY)
                        smoothedZ += alpha * (rawZ - smoothedZ)
                    }
                }

                val gTotal = sqrt(smoothedX * smoothedX + smoothedY * smoothedY + smoothedZ * smoothedZ)
                if (gTotal < 0.1f) return

                val nx = (smoothedX / gTotal).coerceIn(-1f, 1f)
                val ny = (smoothedY / gTotal).coerceIn(-1f, 1f)
                val nz = (smoothedZ / gTotal).coerceIn(-1f, 1f)

                // Display tilt angle relative to horizontal plane (0° = flat on table, 90° = standing upright)
                val inclinationFromHorizontal = Math.toDegrees(acos(abs(nz).coerceIn(0f, 1f).toDouble())).toFloat()

                // Automatic transition with hysteresis to prevent flapping and jitter
                currentMode = if (isPixel) {
                    computeOrientationMode(currentMode, inclinationFromHorizontal)
                } else {
                    computeOrientationModeNonPixel(currentMode, inclinationFromHorizontal)
                }

                // 1. Horizontal Mode (Surface / Table)
                val (pitchHeadTail, rollLeftRight) = computeSurfaceAngles(nx, ny)
                val isSurfaceLevel = abs(pitchHeadTail) < 0.6f && abs(rollLeftRight) < 0.6f

                // 2. Vertical Mode (Wall / Standing on edge)
                val (verticalLateralAngle, verticalFrontalAngle) = if (isPixel) {
                    computeVerticalAngles(nx, ny, nz)
                } else {
                    computeVerticalAnglesNonPixel(nx, ny, nz)
                }

                val isVerticalLevel = if (isPixel) {
                    abs(verticalLateralAngle) < 0.6f
                } else {
                    abs(verticalLateralAngle) < 0.6f && abs(verticalFrontalAngle) < 8.0f
                }

                val isLevel = if (currentMode == LevelOrientationMode.HORIZONTAL_SURFACE) {
                    isSurfaceLevel
                } else {
                    isVerticalLevel
                }

                val primaryAngle = computePrimaryDisplayAngle(
                    mode = currentMode,
                    isLevel = isLevel,
                    pitchHeadTail = pitchHeadTail,
                    rollLeftRight = rollLeftRight,
                    verticalLateralAngle = verticalLateralAngle
                )

                trySend(
                    LevelData(
                        roll = rollLeftRight,
                        pitch = pitchHeadTail,
                        isLevel = isLevel,
                        isSensorAvailable = true,
                        mode = currentMode,
                        pitchHeadTail = pitchHeadTail,
                        rollLeftRight = rollLeftRight,
                        surfaceTotalAngle = inclinationFromHorizontal,
                        verticalLateralAngle = verticalLateralAngle,
                        verticalFrontalAngle = verticalFrontalAngle,
                        primaryDisplayAngle = primaryAngle,
                        inclinationFromHorizontal = inclinationFromHorizontal
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, activeSensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sm.unregisterListener(listener)
        }
    }

    companion object {
        /**
         * Computes orientation mode between surface and wall with hysteresis
         * for Google Pixel devices (preserved original algorithm).
         */
        fun computeOrientationMode(
            currentMode: LevelOrientationMode,
            inclinationFromHorizontal: Float
        ): LevelOrientationMode {
            return if (currentMode == LevelOrientationMode.HORIZONTAL_SURFACE && inclinationFromHorizontal > 46f) {
                LevelOrientationMode.VERTICAL_WALL
            } else if (currentMode == LevelOrientationMode.VERTICAL_WALL && inclinationFromHorizontal < 38f) {
                LevelOrientationMode.HORIZONTAL_SURFACE
            } else {
                currentMode
            }
        }

        /**
         * Computes orientation mode between surface and wall for non-Pixel devices (Samsung Galaxy A-series, etc.)
         * with broader detection thresholds so holding against a wall reliably activates vertical mode.
         */
        fun computeOrientationModeNonPixel(
            currentMode: LevelOrientationMode,
            inclinationFromHorizontal: Float
        ): LevelOrientationMode {
            return if (currentMode == LevelOrientationMode.HORIZONTAL_SURFACE && inclinationFromHorizontal > 40f) {
                LevelOrientationMode.VERTICAL_WALL
            } else if (currentMode == LevelOrientationMode.VERTICAL_WALL && inclinationFromHorizontal < 34f) {
                LevelOrientationMode.HORIZONTAL_SURFACE
            } else {
                currentMode
            }
        }

        /**
         * Computes surface angles (head-to-tail and left-to-right) in degrees.
         */
        fun computeSurfaceAngles(nx: Float, ny: Float): Pair<Float, Float> {
            val pitchHeadTail = Math.toDegrees(asin(ny.coerceIn(-1f, 1f).toDouble())).toFloat()
            val rollLeftRight = Math.toDegrees(asin(nx.coerceIn(-1f, 1f).toDouble())).toFloat()
            return Pair(pitchHeadTail, rollLeftRight)
        }

        /**
         * Computes wall angles (lateral left-to-right tilt and frontal planarity)
         * for Google Pixel devices (preserved original algorithm).
         */
        fun computeVerticalAngles(nx: Float, ny: Float, nz: Float): Pair<Float, Float> {
            val isLandscapeEdge = abs(nx) > abs(ny)
            val verticalLateralAngle = if (!isLandscapeEdge) {
                if (ny >= 0) {
                    Math.toDegrees(atan2(nx.toDouble(), ny.toDouble())).toFloat()
                } else {
                    Math.toDegrees(atan2(-nx.toDouble(), -ny.toDouble())).toFloat()
                }
            } else {
                if (nx >= 0) {
                    Math.toDegrees(atan2(-ny.toDouble(), nx.toDouble())).toFloat()
                } else {
                    Math.toDegrees(atan2(ny.toDouble(), -nx.toDouble())).toFloat()
                }
            }
            val verticalFrontalAngle = Math.toDegrees(asin(nz.coerceIn(-1f, 1f).toDouble())).toFloat()
            return Pair(verticalLateralAngle, verticalFrontalAngle)
        }

        /**
         * Computes wall angles for non-Pixel devices across all four physical edges
         * (portrait upright, portrait inverted, landscape right edge, landscape left edge).
         */
        fun computeVerticalAnglesNonPixel(nx: Float, ny: Float, nz: Float): Pair<Float, Float> {
            val isLandscapeEdge = abs(nx) > abs(ny)
            val verticalLateralAngle = if (!isLandscapeEdge) {
                if (ny >= 0) {
                    Math.toDegrees(atan2(nx.toDouble(), ny.toDouble())).toFloat()
                } else {
                    Math.toDegrees(atan2(-nx.toDouble(), -ny.toDouble())).toFloat()
                }
            } else {
                if (nx >= 0) {
                    Math.toDegrees(atan2(-ny.toDouble(), nx.toDouble())).toFloat()
                } else {
                    Math.toDegrees(atan2(ny.toDouble(), -nx.toDouble())).toFloat()
                }
            }
            val verticalFrontalAngle = Math.toDegrees(asin(nz.coerceIn(-1f, 1f).toDouble())).toFloat()
            return Pair(verticalLateralAngle, verticalFrontalAngle)
        }

        /**
         * Computes primary integer degree displayed in the center (e.g. -12°, 0°, -26°).
         */
        fun computePrimaryDisplayAngle(
            mode: LevelOrientationMode,
            isLevel: Boolean,
            pitchHeadTail: Float,
            rollLeftRight: Float,
            verticalLateralAngle: Float
        ): Int {
            if (isLevel) return 0
            return if (mode == LevelOrientationMode.HORIZONTAL_SURFACE) {
                if (abs(pitchHeadTail) >= abs(rollLeftRight)) pitchHeadTail.roundToInt() else rollLeftRight.roundToInt()
            } else {
                verticalLateralAngle.roundToInt()
            }
        }
    }
}
