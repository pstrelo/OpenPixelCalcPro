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
    HORIZONTAL_SURFACE, // Telefono in piano su una superficie (es. tavolo)
    VERTICAL_WALL       // Telefono verticale (in piedi su un bordo o appoggiato su una parete)
}

/**
 * Modello dati per la Livella Digitale in stile Samsung One UI:
 * - Supporta la modalità orizzontale di superficie (inclinazione testa-fondo e destra-sinistra)
 * - Supporta la modalità verticale (inclinazione laterale destra-sinistra e frontale testa-fondo)
 * - Transizione fluida automatica basata sulla posizione nello spazio
 */
data class LevelData(
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val isLevel: Boolean = true,
    val isSensorAvailable: Boolean = true,
    val mode: LevelOrientationMode = LevelOrientationMode.HORIZONTAL_SURFACE,
    val pitchHeadTail: Float = 0f,        // Inclinazione asse Y testa-fondo (-90° a +90°)
    val rollLeftRight: Float = 0f,        // Inclinazione asse X destra-sinistra (-90° a +90°)
    val surfaceTotalAngle: Float = 0f,    // Gradi totali di scostamento dal piano orizzontale
    val verticalLateralAngle: Float = 0f, // Inclinazione destra-sinistra rispetto all'orizzonte (-90° a +90°)
    val verticalFrontalAngle: Float = 0f, // Riferimento altro asse: inclinazione testa-fondo parete (-90° a +90°)
    val primaryDisplayAngle: Int = 0,     // Grado primario intero visualizzato a schermo (es. -12°, 0°, -26°)
    val inclinationFromHorizontal: Float = 0f // Inclinazione rispetto al tavolo (0° = piatto, 90° = dritto in piedi)
)

/**
 * Gestore dell'accelerometro per la Livella Digitale.
 * Applica filtraggio passa-basso stabile e calcola con precisione trigonometrica
 * gli assi e il passaggio automatico con isteresi tra piano e parete.
 */
class LevelSensorManager(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun getLevelFlow(): Flow<LevelData> = callbackFlow {
        if (sensorManager == null || accelerometer == null) {
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
            // Filtro passa-basso per smorzare il rumore del sensore garantendo reattività fluida
            private var smoothedX = 0f
            private var smoothedY = 0f
            private var smoothedZ = 9.8f
            private val alpha = 0.22f

            private var currentMode = LevelOrientationMode.HORIZONTAL_SURFACE

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                smoothedX += alpha * (rawX - smoothedX)
                smoothedY += alpha * (rawY - smoothedY)
                smoothedZ += alpha * (rawZ - smoothedZ)

                val gTotal = sqrt(smoothedX * smoothedX + smoothedY * smoothedY + smoothedZ * smoothedZ)
                if (gTotal < 0.1f) return

                val nx = (smoothedX / gTotal).coerceIn(-1f, 1f)
                val ny = (smoothedY / gTotal).coerceIn(-1f, 1f)
                val nz = (smoothedZ / gTotal).coerceIn(-1f, 1f)

                // Angolo di inclinazione del display rispetto al piano orizzontale (0° = piatto su tavolo, 90° = dritto in piedi)
                val inclinationFromHorizontal = Math.toDegrees(acos(abs(nz).coerceIn(0f, 1f).toDouble())).toFloat()

                // Transizione automatica con isteresi (38° / 46°) per evitare oscillazioni o sfarfallii
                currentMode = computeOrientationMode(currentMode, inclinationFromHorizontal)

                // 1. Modalità Orizzontale (Superficie / Tavolo)
                val (pitchHeadTail, rollLeftRight) = computeSurfaceAngles(nx, ny)
                val isSurfaceLevel = abs(pitchHeadTail) < 0.6f && abs(rollLeftRight) < 0.6f

                // 2. Modalità Verticale (Parete / In piedi sul bordo)
                val (verticalLateralAngle, verticalFrontalAngle) = computeVerticalAngles(nx, ny, nz)
                val isVerticalLevel = abs(verticalLateralAngle) < 0.6f

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

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    companion object {
        /**
         * Calcola la modalità di orientamento tra superficie e parete con isteresi
         * per evitare sfarfallii intorno all'angolo di transizione.
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
         * Calcola gli angoli di superficie (testa-fondo e destra-sinistra) in gradi.
         */
        fun computeSurfaceAngles(nx: Float, ny: Float): Pair<Float, Float> {
            val pitchHeadTail = Math.toDegrees(asin(ny.coerceIn(-1f, 1f).toDouble())).toFloat()
            val rollLeftRight = Math.toDegrees(asin(nx.coerceIn(-1f, 1f).toDouble())).toFloat()
            return Pair(pitchHeadTail, rollLeftRight)
        }

        /**
         * Calcola gli angoli di parete (inclinazione laterale destra-sinistra e planarità frontale).
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
         * Calcola il grado primario intero visualizzato al centro (es. -12°, 0°, -26°).
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
