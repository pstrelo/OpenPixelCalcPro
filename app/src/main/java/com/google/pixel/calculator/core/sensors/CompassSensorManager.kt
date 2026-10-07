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
 * Gestore sensori unificato per la Bussola digitale in stile Google Pixel.
 * Supporta contemporaneamente:
 * 1. Sensor.TYPE_ROTATION_VECTOR (Moderno vettore di rotazione con fusione hardware, supportato nativamente dall'emulatore Android per "Device Pose" / "Compass" e dai dispositivi fisici a 120Hz).
 * 2. Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR (Vettore geomagnetico a basso consumo).
 * 3. Sensor.TYPE_ORIENTATION (Compatibilità legacy con simulatori e vecchi dispositivi).
 * 4. Sensor.TYPE_ACCELEROMETER + Sensor.TYPE_MAGNETIC_FIELD (Fusione matriciale grezza di fallback).
 */
class CompassSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // Sensori di orientamento e fusione
    private val rotationVector = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val geomagneticVector = sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    private val orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
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

    fun getCompassFlow(): Flow<CompassData> = callbackFlow {
        val sm = sensorManager
        if (sm == null || (rotationVector == null && geomagneticVector == null && orientationSensor == null && (accelerometer == null || magnetometer == null))) {
            trySend(CompassData(0f, 0f, "N", isSensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        val rMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        var smoothedAzimuth = 0f
        var isFirstReading = true
        var lastFusedTimestamp = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                var rawDeg: Float? = null
                var pitchDeg = 0f
                var rollDeg = 0f

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        // Standard Google: calcolo matrice di rotazione da quaternione 3D
                        try {
                            SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
                            SensorManager.getOrientation(rMatrix, orientation)
                            pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                            rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                            rawDeg = calculateAzimuth(rMatrix, pitchDeg)
                            lastFusedTimestamp = System.currentTimeMillis()
                        } catch (_: Exception) {}
                    }

                    @Suppress("DEPRECATION")
                    Sensor.TYPE_ORIENTATION -> {
                        // Se non abbiamo ancora ricevuto dati da ROTATION_VECTOR negli ultimi 500ms
                        if (System.currentTimeMillis() - lastFusedTimestamp > 500L) {
                            rawDeg = (event.values[0] + 360f) % 360f
                            pitchDeg = event.values[1]
                            rollDeg = event.values[2]
                        }
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                        if (hasGravity && hasGeomagnetic && (System.currentTimeMillis() - lastFusedTimestamp > 500L)) {
                            if (SensorManager.getRotationMatrix(rMatrix, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rMatrix, orientation)
                                pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                                rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                                rawDeg = calculateAzimuth(rMatrix, pitchDeg)
                            }
                        }
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasGeomagnetic = true
                        if (hasGravity && hasGeomagnetic && (System.currentTimeMillis() - lastFusedTimestamp > 500L)) {
                            if (SensorManager.getRotationMatrix(rMatrix, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rMatrix, orientation)
                                pitchDeg = Math.toDegrees(orientation[1].toDouble()).toFloat()
                                rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()
                                rawDeg = calculateAzimuth(rMatrix, pitchDeg)
                            }
                        }
                    }
                }

                if (rawDeg != null) {
                    val isLevel = abs(pitchDeg) < 25f && abs(rollDeg) < 25f

                    if (isFirstReading) {
                        smoothedAzimuth = rawDeg
                        isFirstReading = false
                    } else {
                        smoothedAzimuth = smoothAngle(smoothedAzimuth, rawDeg, alpha = 0.35f)
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

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR ||
                    sensor?.type == Sensor.TYPE_MAGNETIC_FIELD ||
                    @Suppress("DEPRECATION")
                    sensor?.type == Sensor.TYPE_ORIENTATION
                ) {
                    currentAccuracy = accuracy
                }
            }
        }

        // Registrazione su tutti i sensori disponibili
        rotationVector?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        geomagneticVector?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        orientationSensor?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        accelerometer?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        awaitClose {
            sm.unregisterListener(listener)
        }
    }

    companion object {
        fun calculateAzimuth(rMatrix: FloatArray, pitchDeg: Float): Float {
            val pitchRad = Math.toRadians(pitchDeg.toDouble())
            val cosPitch = kotlin.math.cos(pitchRad).toFloat()
            val sinPitch = kotlin.math.sin(pitchRad).toFloat()

            // Vettore forward proiettato sul piano orizzontale terrestre: continuo sia in piano che in verticale
            val east = rMatrix[1] * cosPitch + rMatrix[2] * sinPitch
            val north = rMatrix[4] * cosPitch + rMatrix[5] * sinPitch

            return (Math.toDegrees(kotlin.math.atan2(east.toDouble(), north.toDouble())).toFloat() + 360f) % 360f
        }

        fun smoothAngle(current: Float, target: Float, alpha: Float = 0.35f): Float {
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
