package com.google.pixel.calculator

import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class AdvancedMathEngineTest {

    private val engine = AdvancedMathEngine()

    @Test
    fun testQuadraticEquation() {
        val res = engine.solve("x^2 - 4 = 0")
        assertTrue("Should find roots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(2, roots.size)
        assertTrue(roots.any { abs(it - 2.0) < 1e-3 })
        assertTrue(roots.any { abs(it - (-2.0)) < 1e-3 })
    }

    @Test
    fun testEquationFromImageSample() {
        // x^2 + 2x - 3 = 2x + 6  ->  x^2 - 9 = 0  ->  roots x = -3, x = 3
        val res = engine.solve("x^2 + 2x - 3 = 2x + 6")
        assertTrue("Should find roots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(2, roots.size)
        assertTrue("Contains 3", roots.any { abs(it - 3.0) < 1e-3 })
        assertTrue("Contains -3", roots.any { abs(it - (-3.0)) < 1e-3 })
    }

    @Test
    fun testLinearEquation() {
        val res = engine.solve("2x - 5 = 0")
        assertTrue("Should solve linear", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(1, roots.size)
        assertEquals(2.5, roots[0], 1e-3)
    }

    @Test
    fun testInequality() {
        val res = engine.solve("x^2 - 4 < 0")
        assertTrue("Should solve inequality", res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("-2") && ineq.text.contains("2"))
    }

    @Test
    fun testDerivative() {
        // d/dx(x^2, 3) -> f'(3) = 6
        val res = engine.solve("d/dx(x^2, 3)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val valResult = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(6.0, valResult, 1e-2)
    }

    @Test
    fun testDefiniteIntegral() {
        // ∫(x^2, 0, 3) -> x^3/3 |_0^3 = 9
        val res = engine.solve("∫(x^2, 0, 3)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val valResult = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(9.0, valResult, 1e-2)
    }

    @Test
    fun testLimit() {
        // lim(x^2, 2) = 4
        val res = engine.solve("lim(x^2, 2)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val valResult = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(4.0, valResult, 1e-2)
    }

    @Test
    fun testFractionFormat() {
        assertEquals("3/8", engine.toFraction(0.375))
        assertEquals("1/3", engine.toFraction(1.0 / 3.0))
        assertEquals("5/2", engine.toFraction(2.5))
        assertEquals("-7/8", engine.toFraction(-0.875))
        assertEquals("4", engine.toFraction(4.0))
    }

    @Test
    fun testEngineeringFormat() {
        val eng1 = engine.toEngineering(1250000.0)
        assertTrue("Engineering 1.25M", eng1.contains("1.25") && eng1.contains("10^6"))

        val eng2 = engine.toEngineering(0.00047)
        assertTrue("Engineering 470u", eng2.contains("470") && eng2.contains("10^-6"))
    }

    @Test
    fun testDmsFormat() {
        val dms = engine.toDms(32.25)
        // 32 degrees, 15 minutes, 0 seconds
        assertTrue("DMS degrees", dms.contains("32°"))
        assertTrue("DMS minutes", dms.contains("15'"))
    }

    @Test
    fun testPolarFormat() {
        val polPos = engine.toPolar(5.0, false)
        assertTrue("Polar positive", polPos.contains("5") && polPos.contains("0°"))

        val polNeg = engine.toPolar(-5.0, false)
        assertTrue("Polar negative", polNeg.contains("5") && polNeg.contains("180°"))
    }

    @Test
    fun testGraphSampling() {
        val pts = engine.sampleFunction("sin(x)", -10f, 10f, 100)
        assertTrue(pts.isNotEmpty())
        assertTrue(pts.any { it != null })
    }

    @Test
    fun testCosX_doesNotExplodeWith32Roots() {
        val res = engine.solve("cos(x)")
        assertTrue("cos(x) should yield roots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        // Must NOT return 32 roots across space, only at most 2 principal roots near zero (±π/2)
        assertTrue("Roots count must be <= 2 instead of 32, got ${roots.size}", roots.size <= 2)
        assertTrue(roots.any { abs(it - 1.570796) < 1e-2 })
        assertTrue(roots.any { abs(it - (-1.570796)) < 1e-2 })
    }

    @Test
    fun testTrigInversesAndDegree() {
        val asinRes = engine.evaluateExpression("arcsin(1)", isRadians = true)
        val acosRes = engine.evaluateExpression("arccos(1)", isRadians = true)
        val atanRes = engine.evaluateExpression("arctan(1)", isRadians = true)
        assertTrue("arcsin(1) is ~1.5708", asinRes.startsWith("1.57"))
        assertEquals("0", acosRes)
        assertTrue("arctan(1) is ~0.785", atanRes.startsWith("0.785"))
    }

    @Test
    fun testTanDiscontinuityHandling() {
        // tan(x) has vertical asymptote at π/2 (~1.5708)
        val pts = engine.sampleFunction("tan(x)", 1.0f, 2.0f, 60)
        assertTrue("Sampling tan(x) across π/2 must insert null at asymptote", pts.any { it == null })
    }
}
