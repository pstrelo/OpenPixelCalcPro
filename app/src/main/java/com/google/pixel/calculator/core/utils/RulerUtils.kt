package com.google.pixel.calculator.core.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Physical screen calibration utility for Pixel Ruler.
 * Converts real pixels into accurate millimeters, centimeters, and inches leveraging OLED panel xdpi/ydpi.
 * Includes persistent calibration to guarantee on-screen dimensions are 100% exact.
 */
object RulerUtils {

    const val MM_PER_INCH = 25.4f
    const val CM_PER_INCH = 2.54f

    private const val PREFS_NAME = "pixel_ruler_prefs"
    private const val KEY_CALIBRATION_FACTOR = "calibration_factor"

    // Standard physical references for real calibration
    const val CREDIT_CARD_WIDTH_MM = 85.60f // Standard ISO/IEC 7810 ID-1
    const val CREDIT_CARD_HEIGHT_MM = 53.98f
    const val EURO_2_COIN_MM = 25.75f       // 2 Euro coin
    const val EURO_1_COIN_MM = 23.25f       // 1 Euro coin

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
     * Detects raw physical panel DPI without applying calibration factor.
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
     * Detects calibrated display DPI (raw DPI multiplied by calibration factor).
     */
    fun getScreenDpi(context: Context): Pair<Float, Float> {
        val (rawXdpi, rawYdpi) = getRawScreenDpi(context)
        val factor = getCalibrationFactor(context)
        return Pair(rawXdpi * factor, rawYdpi * factor)
    }
}
