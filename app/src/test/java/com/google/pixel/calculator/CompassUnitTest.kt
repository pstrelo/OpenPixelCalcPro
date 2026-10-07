package com.google.pixel.calculator

import com.google.pixel.calculator.core.sensors.CompassLocationManager
import com.google.pixel.calculator.core.sensors.CompassSensorManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassUnitTest {

    @Test
    fun testCoordinateDmsFormatting() {
        // Test Latitudine (Florida / 40.093056)
        val (latDms, latCard) = CompassLocationManager.formatCoordinate(40.093056, isLatitude = true)
        assertEquals("40°5'35\"", latDms)
        assertEquals("NL", latCard)

        // Test Longitudine (120.033611)
        val (lonDms, lonCard) = CompassLocationManager.formatCoordinate(120.033611, isLatitude = false)
        assertEquals("120°2'1\"", lonDms)
        assertEquals("EL", lonCard)

        // Test Sud Latitudine
        val (sLatDms, sLatCard) = CompassLocationManager.formatCoordinate(-33.8688, isLatitude = true)
        assertEquals("33°52'8\"", sLatDms)
        assertEquals("SL", sLatCard)

        // Test Ovest Longitudine
        val (wLonDms, wLonCard) = CompassLocationManager.formatCoordinate(-74.0060, isLatitude = false)
        assertEquals("74°0'22\"", wLonDms)
        assertEquals("WL", wLonCard)
    }

    @Test
    fun testCardinalDirections() {
        assertEquals("N", CompassSensorManager.getCardinal(0f))
        assertEquals("N", CompassSensorManager.getCardinal(359f))
        assertEquals("N", CompassSensorManager.getCardinal(10f))
        assertEquals("NE", CompassSensorManager.getCardinal(45f))
        assertEquals("E", CompassSensorManager.getCardinal(90f))
        assertEquals("SE", CompassSensorManager.getCardinal(135f))
        assertEquals("S", CompassSensorManager.getCardinal(180f))
        assertEquals("SW", CompassSensorManager.getCardinal(225f))
        assertEquals("W", CompassSensorManager.getCardinal(270f))
        assertEquals("NW", CompassSensorManager.getCardinal(315f))
    }

    @Test
    fun testAngularSmoothingWrapAround() {
        // Attraversamento tra 355° e 5° (attraverso 0°): deve passare in avanti, non all'indietro
        val smoothed = CompassSensorManager.smoothAngle(355f, 5f, alpha = 0.5f)
        // La differenza angolare minima è +10°, quindi 355 + 5 = 360° (0°)
        assertEquals(0f, smoothed, 0.1f)

        // Attraversamento inverso tra 5° e 355°
        val smoothedInv = CompassSensorManager.smoothAngle(5f, 355f, alpha = 0.5f)
        assertEquals(0f, smoothedInv, 0.1f)

        // Movimento normale
        val normalSmooth = CompassSensorManager.smoothAngle(10f, 20f, alpha = 0.5f)
        assertEquals(15f, normalSmooth, 0.1f)
    }
}
