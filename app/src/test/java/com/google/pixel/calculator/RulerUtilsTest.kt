package com.google.pixel.calculator

import com.google.pixel.calculator.core.utils.RulerUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulerUtilsTest {

    private val testDpi = 411.0f // Standard Pixel 6/7/8/9 ydpi

    @Test
    fun testMmToPixelsAndBack() {
        val mm = 50.0f
        val px = RulerUtils.mmToPixels(mm, testDpi)
        val backMm = RulerUtils.pixelsToMm(px, testDpi)
        assertEquals(mm, backMm, 0.001f)
    }

    @Test
    fun testCmToPixelsAndBack() {
        val cm = 5.00f
        val px = RulerUtils.cmToPixels(cm, testDpi)
        val backCm = RulerUtils.pixelsToCm(px, testDpi)
        assertEquals(cm, backCm, 0.001f)
    }

    @Test
    fun testCmAndMmConsistency() {
        val cm = 5.00f
        val mm = 50.0f
        val pxFromCm = RulerUtils.cmToPixels(cm, testDpi)
        val pxFromMm = RulerUtils.mmToPixels(mm, testDpi)
        assertEquals(pxFromCm, pxFromMm, 0.001f)
    }

    @Test
    fun testInchesToPixelsAndBack() {
        val inches = 2.0f
        val px = RulerUtils.inchesToPixels(inches, testDpi)
        val backInches = RulerUtils.pixelsToInches(px, testDpi)
        assertEquals(inches, backInches, 0.001f)
    }

    @Test
    fun testKnownCreditCardDimensions() {
        // Carta di credito standard: 85.60 mm
        val px = RulerUtils.mmToPixels(RulerUtils.CREDIT_CARD_WIDTH_MM, testDpi)
        val calculatedCm = RulerUtils.pixelsToCm(px, testDpi)
        assertEquals(8.56f, calculatedCm, 0.01f)
    }

    @Test
    fun testInvalidInputsProtected() {
        assertEquals(0f, RulerUtils.pixelsToMm(100f, 0f), 0.0001f)
        assertEquals(0f, RulerUtils.pixelsToMm(100f, Float.NaN), 0.0001f)
        assertEquals(0f, RulerUtils.pixelsToMm(100f, Float.POSITIVE_INFINITY), 0.0001f)
        assertEquals(0f, RulerUtils.cmToPixels(5f, 0f), 0.0001f)
        assertEquals(0f, RulerUtils.cmToPixels(5f, Float.NaN), 0.0001f)
    }
}
