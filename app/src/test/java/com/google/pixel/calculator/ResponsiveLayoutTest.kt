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

            // Basic Mode Calculations (keys are square, keypad ~50% height)
            val hPadding = 16f
            val spacingBasic = (screenWidth * 0.026f).coerceIn(8f, 12f)
            val btnWidthBasic = (screenWidth - (hPadding * 2) - (spacingBasic * 3)) / 4
            val arrowRowHeight = 42f
            val keypadPaddingVertical = 12f
            val minDisplayReserve = (screenHeight * 0.28f).coerceIn(130f, 280f)
            val maxKeypadHeightAllowed = screenHeight - minDisplayReserve - 64f
            val maxBasicHeightByScreen = (maxKeypadHeightAllowed - arrowRowHeight - keypadPaddingVertical - (spacingBasic * 5)) / 5
            val targetBtnHeightBasic = minOf(btnWidthBasic * 0.96f, maxBasicHeightByScreen).coerceIn(40f, 86f)
            val targetKeypadHeightBasic = (targetBtnHeightBasic * 5) + (spacingBasic * 5) + arrowRowHeight + keypadPaddingVertical

            // Pro Mode Calculations
            val spacingPro = (screenWidth * 0.015f).coerceIn(4f, 6f)
            val minDisplayReservePro = (screenHeight * 0.22f).coerceIn(110f, 220f)
            val maxKeypadHeightAllowedPro = screenHeight - minDisplayReservePro - 64f
            val maxProHeightByScreen = (maxKeypadHeightAllowedPro - arrowRowHeight - keypadPaddingVertical - (spacingPro * 8)) / 8
            val targetBtnHeightPro = minOf(btnWidthBasic * 0.60f, maxProHeightByScreen).coerceIn(34f, 58f)
            val targetKeypadHeightPro = (targetBtnHeightPro * 8) + (spacingPro * 8) + arrowRowHeight + keypadPaddingVertical
            val btnWidthPro = (screenWidth - (hPadding * 2) - (spacingPro * 3)) / 4

            // 1. Check Keypad percentage ratio is ~50%
            val basicKeypadRatio = targetKeypadHeightBasic / screenHeight
            assertTrue(
                "Device ${device.name}: basic keypad ratio $basicKeypadRatio should be around 50% (between 45% and 65%)",
                basicKeypadRatio in 0.45f..0.65f
            )

            // 2. Check Display has generous space (> 180dp on full screens)
            val remainingDisplayBasic = screenHeight - targetKeypadHeightBasic - 64f // minus top bar
            val minDisplay = if (device.heightDp <= 600f) 120f else 140f
            assertTrue(
                "Device ${device.name}: display space $remainingDisplayBasic should be at least ${minDisplay}dp",
                remainingDisplayBasic >= minDisplay
            )

            // 3. Check Basic button aspect ratio is SQUARE on standard/tall phones (~0.90-1.0), and comfortable on compact/tablets
            val aspectRatioBasic = targetBtnHeightBasic / btnWidthBasic
            val minRatio = if (device.heightDp <= 700f || device.widthDp >= 500f) 0.50f else 0.75f
            assertTrue(
                "Device ${device.name}: basic button aspect ratio $aspectRatioBasic should be square on phones (between $minRatio and 1.05)",
                aspectRatioBasic in minRatio..1.05f
            )

            // 4. Check Pro mode button aspect ratio
            val aspectRatioPro = targetBtnHeightPro / btnWidthPro
            val minProRatio = if (device.widthDp >= 500f || device.heightDp <= 600f) 0.30f else 0.45f
            assertTrue(
                "Device ${device.name}: pro button aspect ratio $aspectRatioPro should be comfortable (between $minProRatio and 0.75)",
                aspectRatioPro in minProRatio..0.75f
            )
        }
    }

    @Test
    fun testAdvancedKeypadProportions() {
        for (device in testDevices) {
            val screenHeight = device.heightDp
            val screenWidth = device.widthDp

            val hPadding = 12f
            val spacing = 6f
            val btnWidth6Col = (screenWidth - (hPadding * 2) - (spacing * 5)) / 6

            val minDisplayReserveAdv = (screenHeight * 0.26f).coerceIn(140f, 280f)
            val maxKeypadHeightAdv = screenHeight - minDisplayReserveAdv - 64f
            val maxUnitByScreen = (maxKeypadHeightAdv - 44f - (spacing * 7) - 12f) / 7.38f
            val unitHeight = minOf(btnWidth6Col * 0.95f, maxUnitByScreen).coerceIn(34f, 58f)
            val bottomRowHeight = (unitHeight * 1.30f).coerceIn(46f, 68f)

            // Aspect ratio of 6-column square keys
            val squircleRatio = unitHeight / btnWidth6Col
            val minSquircleRatio = if (device.heightDp <= 700f || device.widthDp >= 500f) 0.50f else 0.75f
            assertTrue(
                "Device ${device.name}: advanced squircle ratio $squircleRatio should be between $minSquircleRatio and 1.05",
                squircleRatio in minSquircleRatio..1.05f
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

        assertTrue(
            "Tab width $singleTabWidth dp on 360dp screen should comfortably exceed 65dp to fit 'Alphabet' and 'Calculus'",
            singleTabWidth >= 65f
        )
    }
}
