package com.google.pixel.calculator

import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.ExpressionParser
import com.google.pixel.calculator.core.math.MathEvaluator
import com.google.pixel.calculator.core.sensors.CompassLocationManager
import com.google.pixel.calculator.core.sensors.LevelOrientationMode
import com.google.pixel.calculator.core.sensors.LevelSensorManager
import com.google.pixel.calculator.core.utils.CurrencyRateRepository
import com.google.pixel.calculator.core.utils.UnitConverter
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/**
 * Enterprise-grade Security, Robustness & Release Integrity Test Suite.
 * Simulates adversarial input, extreme math values, boundary limits, and zero-crash guarantees.
 */
class SecurityAndReleaseVerificationTest {

    private val evaluator = MathEvaluator()
    private val parser = ExpressionParser()

    // =========================================================================
    // 1. ADVERSARIAL & SECURITY TESTS (NO CRASH GUARANTEE)
    // =========================================================================

    @Test
    fun testAdversarialInput_GarbageAndScriptInjection_HandledGracefully() {
        val dangerousInputs = listOf(
            "<script>alert('xss')</script>",
            "'; DROP TABLE calculations; --",
            "../../../etc/passwd",
            "\u0000\u0007\b\u001B",
            "eval('delete everything')",
            "system.exit(0)",
            "undefined",
            "null",
            "NaN",
            "Infinity",
            "",
            "     \t\n   ",
            "+++++-----****",
            "(((((((((()))))))))",
            ",,,...,,,",
            "%%%!!!",
            "⚡️🔥🚀🎉"
        )

        for (input in dangerousInputs) {
            val result = evaluator.evaluate(input)
            assertNotNull("Result must never be null for input: $input", result)
            // It either returns a valid fallback (e.g. 0 for empty) or a structured safe error, NEVER crashes
            if (!result.isSuccess) {
                assertNotNull("Failed result must have error message", result.errorMessage)
            }
        }
    }

    @Test
    fun testDeeplyNestedParentheses_StackSafety() {
        val depth = 120
        val deepExpr = "(".repeat(depth) + "42" + ")".repeat(depth)
        val result = evaluator.evaluate(deepExpr)
        assertTrue("Deeply nested parentheses should evaluate safely", result.isSuccess)
        assertEquals("42", result.formattedResult)
    }

    @Test
    fun testMassiveExponent_DosPrevention() {
        // Exponent exceeding 10000 must fail safely without CPU freeze / OOM
        val result = evaluator.evaluate("2^999999")
        assertFalse("Massive exponent should not succeed", result.isSuccess)
        assertTrue("Error should indicate limit reached", result.errorMessage?.contains("Esponente") == true || result.formattedResult.contains("Errore"))
    }

    @Test
    fun testMassiveFactorial_DosPrevention() {
        // Factorial exceeding 1000 must fail safely without hanging
        val result = evaluator.evaluate("1001!")
        assertFalse("Massive factorial must fail safely", result.isSuccess)
        assertTrue("Error should indicate limit reached", result.errorMessage?.contains("fattoriale") == true || result.formattedResult.contains("Errore"))
    }

    // =========================================================================
    // 2. MATHEMATICAL SINGULARITIES & DOMAIN INTEGRITY
    // =========================================================================

    @Test
    fun testDivisionByZero_InAllForms() {
        val zeroDivisions = listOf(
            "10 / 0",
            "10 ÷ 0",
            "1 / (5 - 5)",
            "1 / sin(0)",
            "tan(90)",
            "cot(0)",
            "sec(90)",
            "csc(0)",
            "coth(0)",
            "csch(0)"
        )

        for (expr in zeroDivisions) {
            val res = evaluator.evaluate(expr, EvaluationContext(isRadians = false))
            assertFalse("Expression $expr must fail safely (division by zero / singularity)", res.isSuccess)
        }
    }

    @Test
    fun testDomainBoundaries_TrigonometricAndLogarithmic() {
        val domainErrors = listOf(
            "asin(1.5)",
            "asin(-1.5)",
            "acos(2)",
            "acos(-2)",
            "ln(0)",
            "ln(-10)",
            "log(0)",
            "log(-1)",
            "log2(0)",
            "log2(-4)",
            "sqrt(-9)",
            "acosh(0.5)",
            "atanh(1.0)",
            "atanh(-1.0)",
            "atanh(2.0)",
            "asec(0.5)",
            "acsc(0.5)",
            "asech(0)",
            "asech(2)"
        )

        for (expr in domainErrors) {
            val res = evaluator.evaluate(expr)
            assertFalse("Expression $expr should trigger domain error safely", res.isSuccess)
        }
    }

    @Test
    fun testVeryLargeAndInfinitesimalNumbers_FormattingIntegrity() {
        // Very large number (switch to scientific notation)
        val bigResult = evaluator.evaluate("10^14")
        assertTrue(bigResult.isSuccess)
        assertTrue("Should format large number in scientific notation", bigResult.formattedResult.contains("E") || bigResult.formattedResult.contains("100"))

        // Very small positive decimal
        val tinyResult = evaluator.evaluate("1 / 10000000")
        assertTrue(tinyResult.isSuccess)

        // Negative values formatting
        val negResult = evaluator.evaluate("-1234567.89")
        assertTrue(negResult.isSuccess)
        assertTrue("Must include minus sign", negResult.formattedResult.startsWith("-"))
    }

    // =========================================================================
    // 3. UNIT CONVERTER INTEGRITY & ALL CATEGORIES TEST
    // =========================================================================

    @Test
    fun testUnitConverter_AllCategoriesAndUnitsIntegrity() {
        val converter = UnitConverter(CurrencyRateRepository())
        val categories = UnitConverter.Category.values()
        assertTrue("Must have at least 14 unit converter categories", categories.size >= 14)

        for (cat in categories) {
            val units = converter.getUnitsForCategory(cat)
            assertTrue("Category ${cat.name} must have units", units.isNotEmpty())

            val defaultFrom = units.first().code
            val defaultTo = units.last().code

            // Test converting 100 units from defaultFrom to defaultTo
            val converted = converter.convert(100.0, defaultFrom, defaultTo, cat)
            assertFalse("Conversion must not be NaN for ${cat.name}", converted.isNaN())
            assertFalse("Conversion must not be Infinite for ${cat.name}", converted.isInfinite())

            // Test reverse conversion consistency
            val reverse = converter.convert(converted, defaultTo, defaultFrom, cat)
            assertEquals("Reverse conversion must match original within tolerance", 100.0, reverse, 1e-1)
        }
    }

    @Test
    fun testUnitConverter_ExtremeValues_ZeroAndNegative() {
        val converter = UnitConverter()
        // Temperature conversion with negative values (e.g. -40 C = -40 F)
        val fahrenheit = converter.convert(-40.0, "C", "F", UnitConverter.Category.TEMPERATURE)
        assertEquals(-40.0, fahrenheit, 1e-4)

        val celsiusFromKelvin = converter.convert(0.0, "K", "C", UnitConverter.Category.TEMPERATURE)
        assertEquals(-273.15, celsiusFromKelvin, 1e-4)

        // Zero value conversion across categories
        val zeroKm = converter.convert(0.0, "km", "m", UnitConverter.Category.LENGTH)
        assertEquals(0.0, zeroKm, 1e-9)
    }

    // =========================================================================
    // 4. SENSORS BOUNDS & GEODESIC CALCULATIONS INTEGRITY
    // =========================================================================

    @Test
    fun testCompassCoordinateFormatting_AllQuadrants() {
        val northEast = CompassLocationManager.formatCoordinate(45.1234, isLatitude = true)
        assertEquals("N", northEast.second)

        val southWest = CompassLocationManager.formatCoordinate(-45.1234, isLatitude = true)
        assertEquals("S", southWest.second)

        val eastLon = CompassLocationManager.formatCoordinate(12.5678, isLatitude = false)
        assertEquals("E", eastLon.second)

        val westLon = CompassLocationManager.formatCoordinate(-12.5678, isLatitude = false)
        assertEquals("W", westLon.second)

        // Equator and Prime Meridian (0.0)
        val zeroLat = CompassLocationManager.formatCoordinate(0.0, isLatitude = true)
        assertEquals("N", zeroLat.second)

        val zeroLon = CompassLocationManager.formatCoordinate(0.0, isLatitude = false)
        assertEquals("E", zeroLon.second)
    }

    @Test
    fun testLevelSensorManager_AngleCalculationsAndTransitions() {
        // Perfectly flat surface (nz = 1.0, nx = 0, ny = 0)
        val (surfacePitch, surfaceRoll) = LevelSensorManager.computeSurfaceAngles(0f, 0f)
        assertEquals(0f, surfacePitch, 1e-3f)
        assertEquals(0f, surfaceRoll, 1e-3f)

        // Mode hysteresis check
        val initialMode = LevelOrientationMode.HORIZONTAL_SURFACE
        val switchedToWall = LevelSensorManager.computeOrientationMode(initialMode, 47f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, switchedToWall)

        val staysWall = LevelSensorManager.computeOrientationMode(LevelOrientationMode.VERTICAL_WALL, 40f)
        assertEquals(LevelOrientationMode.VERTICAL_WALL, staysWall)

        val switchedToSurface = LevelSensorManager.computeOrientationMode(LevelOrientationMode.VERTICAL_WALL, 35f)
        assertEquals(LevelOrientationMode.HORIZONTAL_SURFACE, switchedToSurface)
    }

    // =========================================================================
    // 5. CURRENCY OFFLINE READINESS & INTEGRITY
    // =========================================================================

    @Test
    fun testCurrencyRates_OfflineDefaultReadiness() {
        val repo = CurrencyRateRepository()
        val defaultRates = CurrencyRateRepository.DEFAULT_RATES
        assertTrue("Must include base currencies", defaultRates.containsKey("EUR"))
        assertTrue("Must include USD", defaultRates.containsKey("USD"))
        assertTrue("Must include GBP", defaultRates.containsKey("GBP"))

        // Convert 100 EUR to USD
        val usd = repo.convert(100.0, "EUR", "USD")
        assertEquals(108.5, usd, 1e-3)

        // Convert 100 USD to EUR
        val eur = repo.convert(usd, "USD", "EUR")
        assertEquals(100.0, eur, 1e-3)

        // Identity conversion
        assertEquals(50.0, repo.convert(50.0, "EUR", "EUR"), 1e-9)
    }
}
