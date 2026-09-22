package com.google.pixel.calculator.core.utils

import android.content.Context
import android.util.DisplayMetrics

/**
 * Utilità di calibrazione fisica dello schermo per il Righello Pixel.
 * Converte pixel reali in millimetri e pollici precisi sfruttando xdpi/ydpi del pannello OLED.
 * Include protezione contro IEEE 754 NaN e Infinite.
 */
object RulerUtils {

    const val MM_PER_INCH = 25.4f

    fun pixelsToMm(pixels: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || pixels.isNaN()) return 0f
        val inches = pixels / dpi
        return inches * MM_PER_INCH
    }

    fun mmToPixels(mm: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || mm.isNaN()) return 0f
        val inches = mm / MM_PER_INCH
        return inches * dpi
    }

    fun pixelsToInches(pixels: Float, dpi: Float): Float {
        if (dpi <= 0f || dpi.isNaN() || dpi.isInfinite() || pixels.isNaN()) return 0f
        return pixels / dpi
    }

    fun getScreenDpi(context: Context): Pair<Float, Float> {
        val dm: DisplayMetrics = context.resources.displayMetrics
        val xdpi = if (dm.xdpi > 0 && !dm.xdpi.isNaN()) dm.xdpi else dm.densityDpi.toFloat()
        val ydpi = if (dm.ydpi > 0 && !dm.ydpi.isNaN()) dm.ydpi else dm.densityDpi.toFloat()
        return Pair(xdpi, ydpi)
    }
}
