package com.google.pixel.calculator

import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sin

class LevelUnitTest {

    @Test
    fun testHysteresisOrientationModeTransition() {
        // Da superficie orizzontale a parete
        var mode = LevelOrientationMode.HORIZONTAL_SURFACE

        // Inclinazione moderata sotto la soglia di salita (46°)
        mode = LevelSensorManager.computeOrientationMode(mode, 25f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        mode = LevelSensorManager.computeOrientationMode(mode, 42f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)

        // Superamento soglia di 46°: transizione a VERTICAL_WALL
        mode = LevelSensorManager.computeOrientationMode(mode, 48f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // Ora in VERTICAL_WALL, deve rimanere VERTICAL_WALL anche nella zona isteresi (tra 38° e 46°)
        mode = LevelSensorManager.computeOrientationMode(mode, 42f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        mode = LevelSensorManager.computeOrientationMode(mode, 39f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, mode)

        // Sotto 38°: transizione a HORIZONTAL_SURFACE
        mode = LevelSensorManager.computeOrientationMode(mode, 35f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, mode)
    }

    @Test
    fun testSurfaceAnglesComputation() {
        // Perfettamente in piano (nx = 0, ny = 0)
        val (flatPitch, flatRoll) = LevelSensorManager.computeSurfaceAngles(0f, 0f)
        assertEquals(0f, flatPitch, 0.01f)
        assertEquals(0f, flatRoll, 0.01f)

        // Inclinazione testa-fondo di 30° (sin(30°) = 0.5)
        val (pitch30, _) = LevelSensorManager.computeSurfaceAngles(0f, 0.5f)
        assertEquals(30f, pitch30, 0.1f)

        // Inclinazione destra-sinistra di -20°
        val rad20 = Math.toRadians(-20.0).toFloat()
        val (_, rollNeg20) = LevelSensorManager.computeSurfaceAngles(sin(rad20), 0f)
        assertEquals(-20f, rollNeg20, 0.1f)
    }

    @Test
    fun testVerticalAnglesPortraitAndLandscape() {
        // Verticale in piedi (Portrait), perfettamente a piombo
        val (latPortrait, frontPortrait) = LevelSensorManager.computeVerticalAngles(0f, 1f, 0f)
        assertEquals(0f, latPortrait, 0.01f)
        assertEquals(0f, frontPortrait, 0.01f)

        // Verticale inclinato di -12° lateralmente (come Foto 1 utente)
        val rad12 = Math.toRadians(-12.0)
        val nx12 = sin(rad12).toFloat()
        val ny12 = kotlin.math.cos(rad12).toFloat()
        val (latTilted, _) = LevelSensorManager.computeVerticalAngles(nx12, ny12, 0f)
        assertEquals(-12f, latTilted, 0.2f)

        // Verticale appoggiato sul lato lungo (Landscape)
        val (latLandscape, _) = LevelSensorManager.computeVerticalAngles(1f, 0f, 0f)
        assertEquals(0f, latLandscape, 0.01f)

        // Inclinazione frontale rispetto alla parete (avanti di 8°)
        val rad8 = Math.toRadians(8.0).toFloat()
        val (_, frontTilted) = LevelSensorManager.computeVerticalAngles(0f, 1f, sin(rad8))
        assertEquals(8f, frontTilted, 0.2f)
    }

    @Test
    fun testPrimaryDisplayAngle() {
        // Modalità superficie: -26° testa-fondo (come Foto 2 utente)
        val degSurface = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.HORIZONTAL_SURFACE,
            isLevel = false,
            pitchHeadTail = -26.3f,
            rollLeftRight = 4.1f,
            verticalLateralAngle = 0f
        )
        assertEquals(-26, degSurface)

        // Modalità verticale: -12° inclinazione laterale (come Foto 1 utente)
        val degVertical = LevelSensorManager.computePrimaryDisplayAngle(
            mode = LevelOrientationMode.VERTICAL_WALL,
            isLevel = false,
            pitchHeadTail = 0f,
            rollLeftRight = 0f,
            verticalLateralAngle = -12.1f
        )
        assertEquals(-12, degVertical)

        // Modalità allineata a 0°
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
