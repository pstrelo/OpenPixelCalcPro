package com.google.pixel.calculator.core.sensors

import android.os.Build

/**
 * Device detection and profiling utility:
 * Distinguishes genuine Google Pixel devices (which use Google Tensor / specialized hardware fusion)
 * from other Android smartphone manufacturers (Samsung Galaxy, Xiaomi, Motorola, etc.)
 * to apply tailored sensor pipelines and hardware optimizations while preserving 100% of the
 * Pixel-specific behavior intact.
 */
object DeviceProfile {

    // Allows unit tests to explicitly test either Google Pixel or Non-Pixel profiles
    var testIsGooglePixelOverride: Boolean? = null

    /**
     * Returns true if the device is a genuine Google Pixel device.
     */
    fun isGooglePixel(): Boolean {
        testIsGooglePixelOverride?.let { return it }
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()

        return manufacturer.equals("Google", ignoreCase = true) ||
                brand.equals("google", ignoreCase = true) ||
                model.startsWith("Pixel", ignoreCase = true)
    }

    /**
     * Returns true if the device is a Samsung Galaxy device.
     */
    fun isSamsung(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        return manufacturer.equals("samsung", ignoreCase = true) ||
                brand.equals("samsung", ignoreCase = true)
    }
}
