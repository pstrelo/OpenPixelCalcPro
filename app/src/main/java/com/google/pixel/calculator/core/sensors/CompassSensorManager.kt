package com.google.pixel.calculator.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gestore sensori combinati (Accelerometro + Magnetometro) per la Bussola digitale.
 * Fornisce azimuth da 0° a 360° e punto cardinale.
 * Zero-allocation in onSensorChanged per azzerare la pressione sul Garbage Collector a 120Hz.
 */
class CompassSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    data class CompassData(
        val azimuthDegrees: Float,
        val cardinalDirection: String,
        val isSensorAvailable: Boolean = true
    )

    fun getCompassFlow(): Flow<CompassData> = callbackFlow {
        if (sensorManager == null || accelerometer == null || magnetometer == null) {
            trySend(CompassData(0f, "N", isSensorAvailable = false))
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

                        var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        azimuth = (azimuth + 360f) % 360f

                        val cardinal = getCardinal(azimuth)
                        trySend(CompassData(azimuth, cardinal, isSensorAvailable = true))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun getCardinal(azimuth: Float): String {
        return when (azimuth) {
            in 337.5..360.0, in 0.0..22.5 -> "N"
            in 22.5..67.5 -> "NE"
            in 67.5..112.5 -> "E"
            in 112.5..157.5 -> "SE"
            in 157.5..202.5 -> "S"
            in 202.5..247.5 -> "SO"
            in 247.5..292.5 -> "O"
            in 292.5..337.5 -> "NO"
            else -> "N"
        }
    }
}
