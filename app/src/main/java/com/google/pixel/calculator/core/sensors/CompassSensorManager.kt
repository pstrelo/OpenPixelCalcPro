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

/**
 * Gestore sensori combinati (Accelerometro + Magnetometro) per la Bussola digitale in stile Google Pixel.
 * Fornisce azimuth filtrato a basso rumore da 0° a 360°, punti cardinali, pitch/roll,
 * monitoraggio dell'accuratezza (bassa, media, alta) e sessioni di calibrazione a figura di 8.
 */
class CompassSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    data class CompassData(
        val azimuthDegrees: Float,
        val rawAzimuth: Float,
        val cardinalDirection: String,
        val pitch: Float = 0f,
        val roll: Float = 0f,
        val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
        val isSensorAvailable: Boolean = true,
        val isLevel: Boolean = true
    )

    data class CalibrationState(
        val progress: Float, // 0.0f a 1.0f
        val accuracy: Int,
        val isComplete: Boolean,
        val pitchCoverage: Float,
        val rollCoverage: Float,
        val yawCoverage: Float
    )

    fun getCompassFlow(): Flow<CompassData> = callbackFlow {
        if (sensorManager == null || accelerometer == null || magnetometer == null) {
            trySend(CompassData(0f, 0f, "N", isSensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rMatrix = FloatArray(9)
        val iMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        var smoothedAzimuth = 0f
        var isFirstReading = true

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                    hasGeomagnetic = true
                }

                if (hasGravity && hasGeomagnetic) {
                    if (SensorManager.getRotationMatrix(rMatrix, iMatrix, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rMatrix, orientation)

                        var rawDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        rawDeg = (rawDeg + 360f) % 360f

                        val pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                        val isLevel = abs(pitchDeg) < 25f && abs(rollDeg) < 25f

                        if (isFirstReading) {
                            smoothedAzimuth = rawDeg
                            isFirstReading = false
                        } else {
                            smoothedAzimuth = smoothAngle(smoothedAzimuth, rawDeg, alpha = 0.22f)
                        }

                        val cardinal = getCardinal(smoothedAzimuth)
                        trySend(
                            CompassData(
                                azimuthDegrees = smoothedAzimuth,
                                rawAzimuth = rawDeg,
                                cardinalDirection = cardinal,
                                pitch = pitchDeg,
                                roll = rollDeg,
                                accuracy = currentAccuracy,
                                isSensorAvailable = true,
                                isLevel = isLevel
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    currentAccuracy = accuracy
                }
            }
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    companion object {
        /**
         * Lisciatura angolare continua (evita scatti al passaggio tra 0° e 360°).
         */
        fun smoothAngle(current: Float, target: Float, alpha: Float = 0.2f): Float {
            var diff = target - current
            while (diff < -180f) diff += 360f
            while (diff > 180f) diff -= 360f
            return (current + diff * alpha + 360f) % 360f
        }

        fun getCardinal(azimuth: Float): String {
            return when (azimuth) {
                in 337.5..360.0, in 0.0..22.5 -> "N"
                in 22.5..67.5 -> "NE"
                in 67.5..112.5 -> "E"
                in 112.5..157.5 -> "SE"
                in 157.5..202.5 -> "S"
                in 202.5..247.5 -> "SW"
                in 247.5..292.5 -> "W"
                in 292.5..337.5 -> "NW"
                else -> "N"
            }
        }
    }
}
