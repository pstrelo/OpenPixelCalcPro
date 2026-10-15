package com.google.pixel.calculator

import com.google.pixel.calculator.core.sensors.CompassSensorManager
import com.google.pixel.calculator.core.sensors.DeviceProfile
import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class SensorsCompatibilityTest {

    @Before
    fun setUp() {
        DeviceProfile.testIsGooglePixelOverride = null
    }

    @After
    fun tearDown() {
        DeviceProfile.testIsGooglePixelOverride = null
    }

    // =========================================================================
    // 1. COMPASS TESTS: Google Pixel Profile (Preserved Exact Original)
    // =========================================================================

    @Test
    fun testPixelCompassFlatNorth() {
        DeviceProfile.testIsGooglePixelOverride = true

        // Flat on table facing North:
        // X = East, Y = North, Z = Up
        // R = Identity matrix
        val rMatrix = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        val azimuth = CompassSensorManager.calculateAzimuth(rMatrix)
        assertEquals("Pixel compass should point to North (0°)", 0f, azimuth, 0.01f)
        assertEquals("N", CompassSensorManager.getCardinal(azimuth))
    }

    @Test
    fun testPixelCompassFlatEast() {
        DeviceProfile.testIsGooglePixelOverride = true

        // Facing East (rotated 90° clockwise):
        // Device Y points East (R[1] = 1, R[4] = 0)
        val rMatrix = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f
        )
        val azimuth = CompassSensorManager.calculateAzimuth(rMatrix)
        assertEquals("Pixel compass should point to East (90°)", 90f, azimuth, 0.01f)
        assertEquals("E", CompassSensorManager.getCardinal(azimuth))
    }

    // =========================================================================
    // 2. COMPASS TESTS: Samsung / Non-Pixel Profile (Robust Tilt Compensation)
    // =========================================================================

    @Test
    fun testNonPixelCompassCardinals() {
        DeviceProfile.testIsGooglePixelOverride = false

        // 1. North: R[1] = 0, R[4] = 1
        val rNorth = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        val azNorth = CompassSensorManager.calculateAzimuthNonPixel(rNorth, pitchDeg = 0f, rollDeg = 0f)
        assertEquals("Non-Pixel North should be 0°", 0f, azNorth, 0.01f)
        assertEquals("N", CompassSensorManager.getCardinal(azNorth))

        // 2. East: R[1] = 1, R[4] = 0
        val rEast = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f
        )
        val azEast = CompassSensorManager.calculateAzimuthNonPixel(rEast, pitchDeg = 0f, rollDeg = 0f)
        assertEquals("Non-Pixel East should be 90°", 90f, azEast, 0.01f)
        assertEquals("E", CompassSensorManager.getCardinal(azEast))

        // 3. South: R[1] = 0, R[4] = -1
        val rSouth = floatArrayOf(
            -1f, 0f, 0f,
            0f, -1f, 0f,
            0f, 0f, 1f
        )
        val azSouth = CompassSensorManager.calculateAzimuthNonPixel(rSouth, pitchDeg = 0f, rollDeg = 0f)
        assertEquals("Non-Pixel South should be 180°", 180f, azSouth, 0.01f)
        assertEquals("S", CompassSensorManager.getCardinal(azSouth))

        // 4. West: R[1] = -1, R[4] = 0
        val rWest = floatArrayOf(
            0f, -1f, 0f,
            1f, 0f, 0f,
            0f, 0f, 1f
        )
        val azWest = CompassSensorManager.calculateAzimuthNonPixel(rWest, pitchDeg = 0f, rollDeg = 0f)
        assertEquals("Non-Pixel West should be 270°", 270f, azWest, 0.01f)
        assertEquals("W", CompassSensorManager.getCardinal(azWest))
    }

    @Test
    fun testNonPixelCompassRollImmunity() {
        DeviceProfile.testIsGooglePixelOverride = false

        // Facing North, but holding phone with natural handheld tilt (pitch 35°, roll 15°):
        // In the horizontal world plane, device top points North: R[1] = 0, R[4] = 1.
        val rTilted = floatArrayOf(
            0.965f, 0f, 0.258f,
            0f, 1f, 0f,
            -0.258f, 0f, 0.965f
        )
        val azTilted = CompassSensorManager.calculateAzimuthNonPixel(rTilted, pitchDeg = 35f, rollDeg = 15f)
        assertEquals("Non-Pixel compass heading must remain 0° North despite roll", 0f, azTilted, 0.01f)
    }

    @Test
    fun testNonPixelCompassUprightCameraPose() {
        DeviceProfile.testIsGooglePixelOverride = false

        // Device held upright vertically (pitch = 70° > 55°), rear camera facing North:
        // Rear camera is along -Z. In North direction: R[2] = 0, R[5] = -1.
        val rUprightNorth = floatArrayOf(
            1f, 0f, 0f,
            0f, 0f, -1f,
            0f, 1f, 0f
        )
        val azUpright = CompassSensorManager.calculateAzimuthNonPixel(rUprightNorth, pitchDeg = 70f, rollDeg = 0f)
        assertEquals("Upright camera pointing North should read 0°", 0f, azUpright, 0.01f)

        // Rear camera facing East: R[2] = -1, R[5] = 0
        val rUprightEast = floatArrayOf(
            0f, 0f, -1f,
            -1f, 0f, 0f,
            0f, 1f, 0f
        )
        val azEast = CompassSensorManager.calculateAzimuthNonPixel(rUprightEast, pitchDeg = 70f, rollDeg = 0f)
        assertEquals("Upright camera pointing East should read 90°", 90f, azEast, 0.01f)
    }

    @Test
    fun testSmoothAngleWrapping() {
        // Smoothing across 359° -> 1° should interpolate smoothly over 0°, not spin 358° around!
        val smoothedWrapForward = CompassSensorManager.smoothAngle(current = 359f, target = 1f, alpha = 0.5f)
        assertEquals("Should smoothly step from 359° to 0°", 0f, smoothedWrapForward, 0.01f)

        // Smoothing across 1° -> 359°
        val smoothedWrapBackward = CompassSensorManager.smoothAngle(current = 1f, target = 359f, alpha = 0.5f)
        assertEquals("Should smoothly step from 1° to 0°", 0f, smoothedWrapBackward, 0.01f)
    }

    // =========================================================================
    // 3. LEVEL TESTS: Google Pixel Profile (Preserved Exact Original)
    // =========================================================================

    @Test
    fun testPixelLevelModeHysteresis() {
        DeviceProfile.testIsGooglePixelOverride = true

        // 1. Flat on table (inclination 0°) -> HORIZONTAL_SURFACE
        var mode = LevelSensorManager.computeOrientationMode(LevelOrientationMode.HORIZONTAL_SURFACE, 0f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        // 2. Inclination rises to 40° (below 46° threshold) -> remains HORIZONTAL_SURFACE
        mode = LevelSensorManager.computeOrientationMode(mode, 40f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        // 3. Inclination exceeds 46° -> switches to VERTICAL_WALL
        mode = LevelSensorManager.computeOrientationMode(mode, 47f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // 4. Inclination drops to 42° (above 38° lower threshold) -> remains VERTICAL_WALL
        mode = LevelSensorManager.computeOrientationMode(mode, 42f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // 5. Inclination drops below 38° -> switches back to HORIZONTAL_SURFACE
        mode = LevelSensorManager.computeOrientationMode(mode, 35f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)
    }

    // =========================================================================
    // 4. LEVEL TESTS: Samsung / Non-Pixel Profile (Reliable Wall Mode)
    // =========================================================================

    @Test
    fun testNonPixelLevelModeHysteresis() {
        DeviceProfile.testIsGooglePixelOverride = false

        // 1. Flat on table -> HORIZONTAL_SURFACE
        var mode = LevelSensorManager.computeOrientationModeNonPixel(LevelOrientationMode.HORIZONTAL_SURFACE, 0f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        // 2. Held against a wall (inclination 85°) -> switches reliably to VERTICAL_WALL (> 40°)
        mode = LevelSensorManager.computeOrientationModeNonPixel(mode, 85f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // 3. Leaning wall or propped on shelf at 50° -> remains VERTICAL_WALL
        mode = LevelSensorManager.computeOrientationModeNonPixel(mode, 50f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // 4. Laid down onto table at 20° (< 34°) -> switches back to HORIZONTAL_SURFACE
        mode = LevelSensorManager.computeOrientationModeNonPixel(mode, 20f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)
    }

    @Test
    fun testNonPixelVerticalAnglesPortrait() {
        DeviceProfile.testIsGooglePixelOverride = false

        // Standing perfectly plumb upright on bottom edge (gravity along -Y, normal force +Y):
        // nx = 0, ny = 1, nz = 0
        val (latZero, frontZero) = LevelSensorManager.computeVerticalAnglesNonPixel(0f, 1f, 0f)
        assertEquals("Plumb lateral angle should be 0°", 0f, latZero, 0.01f)
        assertEquals("Plumb frontal angle should be 0°", 0f, frontZero, 0.01f)

        // Tilted slightly to the right (clockwise): nx > 0
        val (latRight, _) = LevelSensorManager.computeVerticalAnglesNonPixel(0.087f, 0.996f, 0f) // ~5°
        assertTrue("Clockwise tilt should produce positive lateral angle", latRight > 0)
        assertEquals(5f, latRight, 0.5f)

        // Tilted slightly to the left (counter-clockwise): nx < 0
        val (latLeft, _) = LevelSensorManager.computeVerticalAnglesNonPixel(-0.087f, 0.996f, 0f) // ~-5°
        assertTrue("Counter-clockwise tilt should produce negative lateral angle", latLeft < 0)
        assertEquals(-5f, latLeft, 0.5f)
    }

    @Test
    fun testNonPixelAllFourEdgesPlumb() {
        DeviceProfile.testIsGooglePixelOverride = false

        // 1. Portrait Upright (bottom edge down): ny = 1, nx = 0
        val (latPortraitUpright, _) = LevelSensorManager.computeVerticalAnglesNonPixel(0f, 1f, 0f)
        assertEquals(0f, latPortraitUpright, 0.01f)

        // 2. Portrait Inverted (top edge down): ny = -1, nx = 0
        val (latPortraitInverted, _) = LevelSensorManager.computeVerticalAnglesNonPixel(0f, -1f, 0f)
        assertEquals(0f, latPortraitInverted, 0.01f)

        // 3. Landscape Right Edge: nx = -1, ny = 0
        val (latLandscapeRight, _) = LevelSensorManager.computeVerticalAnglesNonPixel(-1f, 0f, 0f)
        assertEquals(0f, latLandscapeRight, 0.01f)

        // 4. Landscape Left Edge: nx = 1, ny = 0
        val (latLandscapeLeft, _) = LevelSensorManager.computeVerticalAnglesNonPixel(1f, 0f, 0f)
        assertEquals(0f, latLandscapeLeft, 0.01f)
    }

    @Test
    fun testDisplayAngleComputation() {
        // Surface mode
        val dispSurfaceLevel = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.HORIZONTAL_SURFACE,
            isLevel = true,
            pitchHeadTail = 0.2f,
            rollLeftRight = 0.1f,
            verticalLateralAngle = 0f
        )
        assertEquals(0, dispSurfaceLevel)

        val dispSurfaceTilted = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.HORIZONTAL_SURFACE,
            isLevel = false,
            pitchHeadTail = 12.4f,
            rollLeftRight = 3.1f,
            verticalLateralAngle = 0f
        )
        assertEquals(12, dispSurfaceTilted)

        // Vertical mode
        val dispVerticalLevel = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.VERTICAL_WALL,
            isLevel = true,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            verticalLateralAngle = 0.3f
        )
        assertEquals(0, dispVerticalLevel)

        val dispVerticalTilted = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.VERTICAL_WALL,
            isLevel = false,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            verticalLateralAngle = -15.8f
        )
        assertEquals(-16, dispVerticalTilted)
    }
}
