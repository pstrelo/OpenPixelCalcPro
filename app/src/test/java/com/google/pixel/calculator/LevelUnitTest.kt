package com.google.pixel.calculator

import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sin

class LevelUnitTest {

    @Test
    fun testHysteresisOrientationModeTransition() {
        // From horizontal surface to vertical wall
        var mode = LevelOrientationMode.HORIZONTAL_SURFACE

        // Moderate tilt below rising threshold (46°)
        mode = LevelSensorManager.computeOrientationMode(mode, 25f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        mode = LevelSensorManager.computeOrientationMode(mode, 42f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        // Exceeding 46° threshold: transition to VERTICAL_WALL
        mode = LevelSensorManager.computeOrientationMode(mode, 48f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // Now in VERTICAL_WALL, must stay VERTICAL_WALL inside hysteresis band (between 38° and 46°)
        mode = LevelSensorManager.computeOrientationMode(mode, 42f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        mode = LevelSensorManager.computeOrientationMode(mode, 39f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // Below 38°: transition back to HORIZONTAL_SURFACE
        mode = LevelSensorManager.computeOrientationMode(mode, 35f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)
    }

    @Test
    fun testSurfaceAnglesComputation() {
        // Perfectly flat (nx = 0, ny = 0)
        val (flatPitch, flatRoll) = LevelSensorManager.computeSurfaceAngles(0f, 0f)
        assertEquals(0f, flatPitch, 0.01f)
        assertEquals(0f, flatRoll, 0.01f)

        // 30° head-to-tail tilt (sin(30°) = 0.5)
        val (pitch30, _) = LevelSensorManager.computeSurfaceAngles(0f, 0.5f)
        assertEquals(30f, pitch30, 0.1f)

        // -20° left-to-right tilt
        val rad20 = Math.toRadians(-20.0).toFloat()
        val (_, rollNeg20) = LevelSensorManager.computeSurfaceAngles(sin(rad20), 0f)
        assertEquals(-20f, rollNeg20, 0.1f)
    }

    @Test
    fun testVerticalAnglesPortraitAndLandscape() {
        // Vertical upright (Portrait), plumb alignment
        val (latPortrait, frontPortrait) = LevelSensorManager.computeVerticalAngles(0f, 1f, 0f)
        assertEquals(0f, latPortrait, 0.01f)
        assertEquals(0f, frontPortrait, 0.01f)

        // Vertical tilted -12° laterally
        val rad12 = Math.toRadians(-12.0)
        val nx12 = sin(rad12).toFloat()
        val ny12 = kotlin.math.cos(rad12).toFloat()
        val (latTilted, _) = LevelSensorManager.computeVerticalAngles(nx12, ny12, 0f)
        assertEquals(-12f, latTilted, 0.2f)

        // Vertical resting on long edge (Landscape)
        val (latLandscape, _) = LevelSensorManager.computeVerticalAngles(1f, 0f, 0f)
        assertEquals(0f, latLandscape, 0.01f)

        // Frontal tilt relative to wall (forward by 8°)
        val rad8 = Math.toRadians(8.0).toFloat()
        val (_, frontTilted) = LevelSensorManager.computeVerticalAngles(0f, 1f, sin(rad8))
        assertEquals(8f, frontTilted, 0.2f)
    }

    @Test
    fun testPrimaryDisplayAngle() {
        // Surface mode: -26° head-to-tail
        val degSurface = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.HORIZONTAL_SURFACE,
            isLevel = false,
            pitchHeadTail = -26.3f,
            rollLeftRight = 4.1f,
            verticalLateralAngle = 0f
        )
        assertEquals(-26, degSurface)

        // Vertical mode: -12° lateral tilt
        val degVertical = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.VERTICAL_WALL,
            isLevel = false,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            verticalLateralAngle = -12.1f
        )
        assertEquals(-12, degVertical)

        // Level mode at 0°
        val degLevel = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.VERTICAL_WALL,
            isLevel = true,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            verticalLateralAngle = 0.2f
        )
        assertEquals(0, degLevel)
    }
}
