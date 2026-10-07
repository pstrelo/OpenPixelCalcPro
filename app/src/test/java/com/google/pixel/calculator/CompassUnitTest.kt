package com.google.pixel.calculator

import com.google.pixel.calculator.core.sensors.CompassLocationManager
import com.google.pixel.calculator.core.sensors.CompassSensorManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassUnitTest {

    @Test
    fun testCoordinateDmsFormatting() {
        // Latitude test (Florida / 40.093056)
        val (latDms, latCard) = CompassLocationManager.formatCoordinate(40.093056, isLatitude = true)
        assertEquals("40°5'35\"", latDms)
        assertEquals("N", latCard)

        // Longitude test (120.033611)
        val (lonDms, lonCard) = CompassLocationManager.formatCoordinate(120.033611, isLatitude = false)
        assertEquals("120°2'1\"", lonDms)
        assertEquals("E", lonCard)

        // South Latitude test
        val (sLatDms, sLatCard) = CompassLocationManager.formatCoordinate(-33.8688, isLatitude = true)
        assertEquals("33°52'8\"", sLatDms)
        assertEquals("S", sLatCard)

        // West Longitude test
        val (wLonDms, wLonCard) = CompassLocationManager.formatCoordinate(-74.0060, isLatitude = false)
        assertEquals("74°0'22\"", wLonDms)
        assertEquals("W", wLonCard)
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
        // Crossing between 355° and 5° (across 0°): must move forward, not backward
        val smoothed = CompassSensorManager.smoothAngle(355f, 5f, alpha = 0.5f)
        // Minimal angular difference is +10°, so 355 + 5 = 360° (0°)
        assertEquals(0f, smoothed, 0.1f)

        // Reverse crossing between 5° and 355°
        val smoothedInv = CompassSensorManager.smoothAngle(5f, 355f, alpha = 0.5f)
        assertEquals(0f, smoothedInv, 0.1f)

        // Normal rotation
        val normalSmooth = CompassSensorManager.smoothAngle(10f, 20f, alpha = 0.5f)
        assertEquals(15f, normalSmooth, 0.1f)
    }

    @Test
    fun testCalculateAzimuthFlatAndVertical() {
        // Flat facing North
        val flatNorth = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        assertEquals(0f, CompassSensorManager.calculateAzimuth(flatNorth), 0.1f)

        // Vertical facing North
        val verticalNorth = floatArrayOf(
            1f, 0f, 0f,
            0f, 0f, -1f,
            0f, 1f, 0f
        )
        assertEquals(0f, CompassSensorManager.calculateAzimuth(verticalNorth), 0.1f)

        // Flat facing East
        val flatEast = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f
        )
        assertEquals(90f, CompassSensorManager.calculateAzimuth(flatEast), 0.1f)

        // Vertical facing East
        val verticalEast = floatArrayOf(
            0f, 0f, -1f,
            -1f, 0f, 0f,
            0f, 1f, 0f
        )
        assertEquals(90f, CompassSensorManager.calculateAzimuth(verticalEast), 0.1f)

        // Flat facing South
        val flatSouth = floatArrayOf(
            -1f, 0f, 0f,
            0f, -1f, 0f,
            0f, 0f, 1f
        )
        assertEquals(180f, CompassSensorManager.calculateAzimuth(flatSouth), 0.1f)

        // Vertical facing South
        val verticalSouth = floatArrayOf(
            -1f, 0f, 0f,
            0f, 0f, 1f,
            0f, 1f, 0f
        )
        assertEquals(180f, CompassSensorManager.calculateAzimuth(verticalSouth), 0.1f)

        // Flat facing West
        val flatWest = floatArrayOf(
            0f, -1f, 0f,
            1f, 0f, 0f,
            0f, 0f, 1f
        )
        assertEquals(270f, CompassSensorManager.calculateAzimuth(flatWest), 0.1f)

        // Vertical facing West
        val verticalWest = floatArrayOf(
            0f, 0f, 1f,
            1f, 0f, 0f,
            0f, 1f, 0f
        )
        assertEquals(270f, CompassSensorManager.calculateAzimuth(verticalWest), 0.1f)
    }

    @Test
    fun testDeadbandSmoothingEliminatesTremor() {
        // Tiny difference below 0.25°: deadband filter must keep previous angle stationary
        val unchanged = CompassSensorManager.smoothAngle(45.0f, 45.15f)
        assertEquals(45.0f, unchanged, 0.001f)

        // Real movement: must apply dynamic smoothing
        val moved = CompassSensorManager.smoothAngle(45.0f, 50.0f)
        assertTrue(moved > 45.0f && moved < 50.0f)
    }
}
