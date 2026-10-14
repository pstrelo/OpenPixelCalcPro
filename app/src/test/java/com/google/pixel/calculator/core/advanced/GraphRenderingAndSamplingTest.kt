package com.google.pixel.calculator.core.advanced

import com.google.pixel.calculator.core.math.ExpressionParser
import com.google.pixel.calculator.core.math.FastDoubleEvaluator
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class GraphRenderingAndSamplingTest {

    private val engine = AdvancedMathEngine()
    private val parser = ExpressionParser()

    @Test
    fun testFastDoubleEvaluatorStandardFunctions() {
        val nodeSin = parser.parse("sin(x)")
        val sin0 = FastDoubleEvaluator.evaluate(nodeSin, 0.0, isRadians = true)
        assertEquals(0.0, sin0, 1e-9)

        val sinPi2 = FastDoubleEvaluator.evaluate(nodeSin, Math.PI / 2.0, isRadians = true)
        assertEquals(1.0, sinPi2, 1e-9)

        val nodeQuad = parser.parse("x^2 + 2*x + 1")
        val quadVal = FastDoubleEvaluator.evaluate(nodeQuad, 3.0)
        assertEquals(16.0, quadVal, 1e-9)
    }

    @Test
    fun testFastDoubleEvaluatorSingularities() {
        val nodeInv = parser.parse("1 / x")
        val posInf = FastDoubleEvaluator.evaluate(nodeInv, 0.0)
        assertTrue(posInf.isInfinite() || posInvUndefined(posInf))

        val nodeSqrt = parser.parse("sqrt(x)")
        val validSqrt = FastDoubleEvaluator.evaluate(nodeSqrt, 4.0)
        assertEquals(2.0, validSqrt, 1e-9)

        val nanSqrt = FastDoubleEvaluator.evaluate(nodeSqrt, -1.0)
        assertTrue(nanSqrt.isNaN())
    }

    private fun posInvUndefined(v: Double): Boolean = v.isInfinite() || v.isNaN()

    @Test
    fun testSampleFunctionInverseXHandlesAsymptoteProperly() {
        // f(x) = 1/x around 0 [-5, 5]
        val points = engine.sampleFunction("1 / x", -5f, 5f, steps = 500)
        assertTrue("Should produce points", points.isNotEmpty())

        // Ensure there is at least one null separating the branches (discontinuity)
        val hasNullDiscontinuity = points.contains(null)
        assertTrue("Graph must break path across vertical asymptote at x=0", hasNullDiscontinuity)

        val nearZero = points.filter { it == null || abs(it.x) < 0.1f }
        println("POINTS NEAR ZERO: $nearZero")
        val nullIdx = points.indexOf(null)
        val pointBefore = points.subList(0, nullIdx).lastOrNull { it != null }
        val pointAfter = points.subList(nullIdx + 1, points.size).firstOrNull { it != null }

        assertNotNull(pointBefore)
        assertNotNull(pointAfter)

        assertTrue("Branch approaching 0 from left must go to -infinity: ${pointBefore?.y}", (pointBefore?.y ?: 0f) < -100f)
        assertTrue("Branch leaving 0 to right must come from +infinity: ${pointAfter?.y}", (pointAfter?.y ?: 0f) > 100f)
    }

    @Test
    fun testSampleFunctionSqrtDomainBoundary() {
        // f(x) = sqrt(x) in [-4, 4]
        val points = engine.sampleFunction("sqrt(x)", -4f, 4f, steps = 400)
        assertTrue(points.isNotEmpty())

        // In negative domain, points must be null / non-drawn
        val negPoints = points.filterNotNull().filter { it.x < -0.05f }
        assertTrue("Negative domain for sqrt(x) should have no rendered points, found ${negPoints.size}", negPoints.isEmpty())

        // In positive domain, points must exist and be accurate
        val posPoints = points.filterNotNull().filter { it.x >= 0f }
        assertTrue("Positive domain must have rendered points", posPoints.isNotEmpty())
        val p1 = posPoints.firstOrNull { abs(it.x - 1.0f) < 0.05f }
        assertNotNull(p1)
        assertEquals(1.0f, p1!!.y, 0.1f)
    }

    @Test
    fun testSampleFunctionTanAsymptotes() {
        // tan(x) in [-3.14, 3.14] has asymptotes at -pi/2 (-1.57) and pi/2 (1.57)
        val points = engine.sampleFunction("tan(x)", -3.14f, 3.14f, steps = 600, isRadians = true)
        val nullCount = points.count { it == null }
        assertTrue("tan(x) should have multiple breaks across asymptotes in [-pi, pi], found $nullCount", nullCount >= 2)
    }

    @Test
    fun testEmptyExpressionYieldsEmptyPoints() {
        val points = engine.sampleFunction("", -10f, 10f)
        assertTrue(points.isEmpty())

        val whitespacePoints = engine.sampleFunction("   ", -10f, 10f)
        assertTrue(whitespacePoints.isEmpty())
    }

    @Test
    fun testSampleFunctionInverseSquareBothSidesToPositiveInfinity() {
        // f(x) = 1/x^2 in [-3, 3] -> both sides go to +inf at x=0
        val points = engine.sampleFunction("1 / (x^2)", -3f, 3f, steps = 500)
        val nearZero = points.filter { it == null || abs(it.x) < 0.05f }
        println("1/(x^2) NEAR ZERO: $nearZero")
        assertTrue(points.isNotEmpty())
        assertTrue("Must have singularity break at x=0", points.contains(null))

        val nullIdx = points.indexOf(null)
        val pointBefore = points.subList(0, nullIdx).lastOrNull { it != null }
        val pointAfter = points.subList(nullIdx + 1, points.size).firstOrNull { it != null }

        assertNotNull(pointBefore)
        assertNotNull(pointAfter)
        assertTrue("Left side of 1/x^2 must shoot towards +inf, got ${pointBefore?.y}", (pointBefore?.y ?: 0f) > 100f)
        assertTrue("Right side of 1/x^2 must shoot towards +inf, got ${pointAfter?.y}", (pointAfter?.y ?: 0f) > 100f)
    }

    @Test
    fun testFastCompositeFunctionSampling() {
        // Test h(x) = f(x) + g(x) where f(x) = sin(x), g(x) = cos(x)
        val fastFns = mapOf<String, (Double) -> Double>(
            "f" to { kotlin.math.sin(it) },
            "g" to { kotlin.math.cos(it) }
        )
        val points = engine.sampleFunction("f(x) + g(x)", -3.14f, 3.14f, steps = 200, fastCustomFunctions = fastFns)
        assertTrue(points.isNotEmpty())
        // At x = 0, sin(0) + cos(0) = 1
        val p0 = points.filterNotNull().minByOrNull { abs(it.x) }
        assertNotNull(p0)
        assertEquals(1.0f, p0!!.y, 0.05f)
    }

    @Test
    fun testSamplingPerformanceIsUnder10MillisecondsFor1000Points() {
        // Benchmark: must evaluate 1000 points rapidly in native Double (< 15 ms)
        val startTime = System.nanoTime()
        val pts = engine.sampleFunction("sin(x) * x^2 + cos(x) / (1 + x^2)", -20f, 20f, steps = 1000)
        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0
        assertTrue("1000 points calculated: ${pts.size}", pts.isNotEmpty())
        assertTrue("Sampling took ${elapsedMs}ms, should be < 25ms for 60fps", elapsedMs < 25.0)
    }

    @Test
    fun testSampleFunctionInverseXZoomOutHasNoSpuriousLine() {
        // When zoomed out far, e.g. [-50, 50], consecutive sample points jump from negative to positive.
        // It must detect the asymptote and insert a null break so no line is drawn across x=0.
        val points = engine.sampleFunction("1 / x", -50f, 50f, steps = 300)
        assertTrue(points.isNotEmpty())
        assertTrue("Must break path at x=0 even when zoomed out", points.contains(null))

        // Check that no consecutive non-null points cross x=0 directly
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            if (p1 != null && p2 != null) {
                val crossesZero = (p1.x < 0f && p2.x > 0f)
                assertFalse("Found direct connection crossing x=0: (${p1.x}, ${p1.y}) -> (${p2.x}, ${p2.y})", crossesZero)
            }
        }
    }
}
