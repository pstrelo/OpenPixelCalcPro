package com.google.pixel.calculator.core.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Utilità di calibrazione fisica dello schermo per il Righello Pixel.
 * Converte pixel reali in millimetri, centimetri e pollici precisi sfruttando xdpi/ydpi del pannello OLED.
 * Include calibrazione persistente per garantire che le dimensioni effettive su schermo siano esatte al 100%.
 */
object RulerUtils {

    const val MM_PER_INCH = 25.4f
    const val CM_PER_INCH = 2.54f

    private const val PREFS_NAME = "pixel_ruler_prefs"
    private const val KEY_CALIBRATION_FACTOR = "calibration_factor"

    // Riferimenti fisici standard per la calibrazione reale
    const val CREDIT_CARD_WIDTH_MM = 85.60f // Standard ISO/IEC 7810 ID-1
    const val CREDIT_CARD_HEIGHT_MM = 53.98f
    const val EURO_2_COIN_MM = 25.75f       // Moneta 2 Euro
    const val EURO_1_COIN_MM = 23.25f       // Moneta 1 Euro

    fun getCalibrationFactor(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_CALIBRATION_FACTOR, 1.0f).coerceIn(0.7f, 1.3f)
    }

    fun setCalibrationFactor(context: Context, factor: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_CALIBRATION_FACTOR, factor.coerceIn(0.7f, 1.3f)).apply()
    }

    fun resetCalibration(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CALIBRATION_FACTOR).apply()
    }

    fun pixelsToMm(pixels: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || pixels.isNaN()) return 0f
        return (pixels / dpi) * MM_PER_INCH
    }

    fun mmToPixels(mm: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || mm.isNaN()) return 0f
        return (mm / MM_PER_INCH) * dpi
    }

    fun pixelsToCm(pixels: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || pixels.isNaN()) return 0f
        return (pixels / dpi) * CM_PER_INCH
    }

    fun cmToPixels(cm: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || cm.isNaN()) return 0f
        return (cm / CM_PER_INCH) * dpi
    }

    fun pixelsToInches(pixels: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || pixels.isNaN()) return 0f
        return pixels / dpi
    }

    fun inchesToPixels(inches: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || inches.isNaN()) return 0f
        return inches * dpi
    }

    /**
     * Rileva i DPI grezzi fisici del pannello senza applicare il fattore di calibrazione.
     */
    @Suppress("DEPRECATION")
    fun getRawScreenDpi(context: Context): Pair<Float, Float> {
        val realMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display?.getRealMetrics(realMetrics)
            } catch (_: Exception) {
                windowManager?.defaultDisplay?.getRealMetrics(realMetrics)
            }
        } else {
            windowManager?.defaultDisplay?.getRealMetrics(realMetrics)
        }

        val resMetrics = context.resources.displayMetrics

        val rawXdpi = when {
            realMetrics.xdpi in 120f..800f -> realMetrics.xdpi
            resMetrics.xdpi in 120f..800f -> resMetrics.xdpi
            else -> realMetrics.densityDpi.toFloat()
        }

        val rawYdpi = when {
            realMetrics.ydpi in 120f..800f -> realMetrics.ydpi
            resMetrics.ydpi in 120f..800f -> resMetrics.ydpi
            else -> realMetrics.densityDpi.toFloat()
        }

        return Pair(rawXdpi, rawYdpi)
    }

    /**
     * Rileva i DPI calibrati del display (DPI grezzi moltiplicati per il fattore di calibrazione).
     */
    fun getScreenDpi(context: Context): Pair<Float, Float> {
        val (rawXdpi, rawYdpi) = getRawScreenDpi(context)
        val factor = getCalibrationFactor(context)
        return Pair(rawXdpi * factor, rawYdpi * factor)
    }
}
