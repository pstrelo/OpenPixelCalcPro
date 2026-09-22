package com.google.pixel.calculator.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Gestore dell'accelerometro per la Livella a bolla digitale.
 * Calcola inclinazione pitch (asse X) e roll (asse Y) in gradi.
 */
class LevelSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    data class LevelData(
        val roll: Float,  // Inclinazione sinistra/destra (-90° a +90°)
        val pitch: Float, // Inclinazione alto/basso (-90° a +90°)
        val isLevel: Boolean, // true se perfettamente in piano entro tolleranza di 0.5°
        val isSensorAvailable: Boolean = true
    )

    fun getLevelFlow(): Flow<LevelData> = callbackFlow {
        if (sensorManager == null || accelerometer == null) {
            trySend(LevelData(0f, 0f, isLevel = true, isSensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            // Filtro passa-basso per smorzare il rumore del sensore
            private var smoothedX = 0f
            private var smoothedY = 0f
            private var smoothedZ = 9.8f
            private val alpha = 0.15f

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                smoothedX = smoothedX + alpha * (rawX - smoothedX)
                smoothedY = smoothedY + alpha * (rawY - smoothedY)
                smoothedZ = smoothedZ + alpha * (rawZ - smoothedZ)

                // Calcolo angoli in gradi
                val roll = Math.toDegrees(atan2(smoothedX.toDouble(), sqrt((smoothedY * smoothedY + smoothedZ * smoothedZ).toDouble()))).toFloat()
                val pitch = Math.toDegrees(atan2(smoothedY.toDouble(), sqrt((smoothedX * smoothedX + smoothedZ * smoothedZ).toDouble()))).toFloat()

                val isLevel = kotlin.math.abs(roll) < 0.5f && kotlin.math.abs(pitch) < 0.5f

                trySend(LevelData(roll, pitch, isLevel, isSensorAvailable = true))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
