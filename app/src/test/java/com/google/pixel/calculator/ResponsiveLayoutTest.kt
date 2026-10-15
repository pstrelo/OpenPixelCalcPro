package com.google.pixel.calculator

import org.junit.Assert.assertTrue
import org.junit.Test

class ResponsiveLayoutTest {

    data class DeviceScreen(
        val name: String,
        val widthDp: Float,
        val heightDp: Float
    )

    private val testDevices = listOf(
        DeviceScreen("Compact 360x640", 360f, 640f),
        DeviceScreen("Standard Reference 400x840", 400f, 840f),
        DeviceScreen("Tall Samsung 392x915", 392f, 915f),
        DeviceScreen("Extra Tall 21:9 384x960", 384f, 960f),
        DeviceScreen("Large Foldable 600x900", 600f, 900f),
        DeviceScreen("Small Split-Screen 390x520", 390f, 520f)
    )

    @Test
    fun testBaseKeypadProportions() {
        for (device in testDevices) {
            val screenHeight = device.heightDp
            val screenWidth = device.widthDp

            // Basic Mode Calculations
            val targetKeypadHeightBasic = (screenHeight * 0.63f).coerceIn(300f, 600f)
            val targetSpacingBasic = (screenWidth * 0.028f).coerceIn(6f, 12f)
            val targetBtnHeightBasic = ((targetKeypadHeightBasic - 40f - 12f - (targetSpacingBasic * 4)) / 5).coerceIn(46f, 96f)

            val hPadding = 16f
            val btnWidthBasic = (screenWidth - (hPadding * 2) - (targetSpacingBasic * 3)) / 4

            // Pro Mode Calculations
            val targetKeypadHeightPro = (screenHeight * 0.71f).coerceIn(360f, 690f)
            val targetSpacingPro = (screenWidth * 0.015f).coerceIn(4f, 7f)
            val targetBtnHeightPro = ((targetKeypadHeightPro - 40f - 12f - (targetSpacingPro * 7)) / 8).coerceIn(36f, 72f)
            val btnWidthPro = (screenWidth - (hPadding * 2) - (targetSpacingPro * 3)) / 4

            // 1. Check Keypad percentage ratio is balanced
            val basicKeypadRatio = targetKeypadHeightBasic / screenHeight
            assertTrue(
                "Device ${device.name}: basic keypad ratio $basicKeypadRatio should be between 55% and 75%",
                basicKeypadRatio in 0.55f..0.75f
            )

            // 2. Check Display has sufficient space
            val remainingDisplayBasic = screenHeight - targetKeypadHeightBasic - 64f // minus top bar
            assertTrue(
                "Device ${device.name}: display space $remainingDisplayBasic should be at least 110dp",
                remainingDisplayBasic >= 110f
            )

            // 3. Check Basic button aspect ratio (nearly square on full phones, wider on split screen)
            val aspectRatioBasic = targetBtnHeightBasic / btnWidthBasic
            assertTrue(
                "Device ${device.name}: basic button aspect ratio $aspectRatioBasic should be between 0.50 and 1.30",
                aspectRatioBasic in 0.50f..1.30f
            )

            // 4. Check Pro mode button aspect ratio
            val aspectRatioPro = targetBtnHeightPro / btnWidthPro
            assertTrue(
                "Device ${device.name}: pro button aspect ratio $aspectRatioPro should be comfortable (between 0.40 and 1.0)",
                aspectRatioPro in 0.40f..1.0f
            )
        }
    }

    @Test
    fun testAdvancedKeypadProportions() {
        for (device in testDevices) {
            val screenHeight = device.heightDp
            val screenWidth = device.widthDp

            val targetKeypadHeight = (screenHeight * 0.60f).coerceIn(320f, 620f)
            val spacing = 6f
            val availableForUnits = targetKeypadHeight - 44f - (spacing * 7) - 12f
            val unitHeight = (availableForUnits / 7.38f).coerceIn(34f, 72f)
            val bottomRowHeight = (unitHeight * 1.38f).coerceIn(48f, 84f)

            val hPadding = 12f
            val btnWidth6Col = (screenWidth - (hPadding * 2) - (spacing * 5)) / 6

            // Aspect ratio of 6-column square keys
            val squircleRatio = unitHeight / btnWidth6Col
            assertTrue(
                "Device ${device.name}: advanced squircle ratio $squircleRatio should be between 0.50 and 1.30",
                squircleRatio in 0.50f..1.30f
            )

            // Verify FORMAT key has enough space to not wrap (at least 45dp)
            assertTrue(
                "Device ${device.name}: 6-column key width $btnWidth6Col should be >= 45dp",
                btnWidth6Col >= 45f
            )
        }
    }

    @Test
    fun testTabWidthOnSmallScreens() {
        val screenWidth = 360f // Compact device width
        val hPadding = 12f // Advanced screen horizontal padding
        val availableWidth = screenWidth - (hPadding * 2)
        val tabCount = 4
        val singleTabWidth = availableWidth / tabCount // 84dp

        // Longest tab label: "Trig" (4 chars), "Algebra" (7 chars), "Calculus" (8 chars), "Alphabet" (8 chars)
        // With 12sp font, 8 chars is ~55-60dp wide.
        assertTrue(
            "Tab width $singleTabWidth dp on 360dp screen should comfortably exceed 65dp to fit 'Alphabet' and 'Calculus'",
            singleTabWidth >= 65f
        )
    }
}
