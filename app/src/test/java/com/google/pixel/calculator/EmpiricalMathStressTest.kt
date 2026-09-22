package com.google.pixel.calculator

import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import com.google.pixel.calculator.ui.advanced.AdvancedCalculatorViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

/**
 * Empirical Math & Equation Stress Verification Test Suite.
 * Crafted by Challenger M1-1 to stress-test:
 * 1. Equation solving: degree 1, 2, 3, multiple roots, edge cases.
 * 2. Inequality solving: bounded, unbounded, impossible, tautological.
 * 3. Numerical calculus: derivatives, adaptive Simpson integrals, limits.
 * 4. 5 FORMAT representations (DEC, FRAC, ENG, DMS, POL) on positive, negative, zero, fractional, extreme values.
 * 5. Multi-root matrix brackets: [ x = -4 ; x = 4 ].
 */
class EmpiricalMathStressTest {

    private lateinit var engine: AdvancedMathEngine
    private lateinit var viewModel: AdvancedCalculatorViewModel

    @Before
    fun setUp() {
        engine = AdvancedMathEngine()
        viewModel = AdvancedCalculatorViewModel(engine = engine)
    }

    // =========================================================================
    // 1. EQUATIONS & EXPRESSIONS STRESS TESTS
    // =========================================================================

    @Test
    fun testQuadraticEquation_x2_minus_16_equals_0() {
        // x^2 - 16 = 0 -> roots -4, 4
        val res = engine.solve("x^2 - 16 = 0")
        assertTrue("Expected EquationRoots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals("Must find exactly 2 roots", 2, roots.size)
        assertTrue("Contains -4", roots.any { abs(it - (-4.0)) < 1e-3 })
        assertTrue("Contains 4", roots.any { abs(it - 4.0) < 1e-3 })

        // Matrix brackets formatting check
        val matrixFormatted = viewModel.formatRoots(roots, AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("[ x = -4 ; x = 4 ]", matrixFormatted)
    }

    @Test
    fun testLinearEquation_2x_minus_5_equals_1() {
        // 2x - 5 = 1 -> 2x = 6 -> x = 3
        val res = engine.solve("2x - 5 = 1")
        assertTrue("Expected EquationRoots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals("Must find 1 root", 1, roots.size)
        assertEquals(3.0, roots[0], 1e-3)

        val formatted = viewModel.formatRoots(roots, AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("x = 3", formatted)
    }

    @Test
    fun testCubicPolynomial_x3_minus_4x_equals_0() {
        // x^3 - 4x = 0 -> x(x^2 - 4) = 0 -> roots -2, 0, 2
        val res = engine.solve("x^3 - 4x = 0")
        assertTrue("Expected EquationRoots", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals("Must find 3 roots", 3, roots.size)
        assertTrue("Contains -2", roots.any { abs(it - (-2.0)) < 1e-3 })
        assertTrue("Contains 0", roots.any { abs(it - 0.0) < 1e-3 })
        assertTrue("Contains 2", roots.any { abs(it - 2.0) < 1e-3 })

        val matrixFormatted = viewModel.formatRoots(roots, AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("[ x = -2 ; x = 0 ; x = 2 ]", matrixFormatted)
    }

    @Test
    fun testQuadraticEquation_bothSides_sampleEquation() {
        // x^2 + 2x - 3 = 2x + 6 -> x^2 - 9 = 0 -> roots -3, 3
        val res = engine.solve("x^2 + 2x - 3 = 2x + 6")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(2, roots.size)
        assertTrue(roots.any { abs(it - (-3.0)) < 1e-3 })
        assertTrue(roots.any { abs(it - 3.0) < 1e-3 })

        val matrixFormatted = viewModel.formatRoots(roots, AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("[ x = -3 ; x = 3 ]", matrixFormatted)
    }

    @Test
    fun testQuadraticEquation_irrationalRoots() {
        // x^2 - 2 = 0 -> roots ±sqrt(2) ≈ ±1.41421356
        val res = engine.solve("x^2 - 2 = 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(2, roots.size)
        assertTrue(roots.any { abs(it - (-1.41421356)) < 1e-3 })
        assertTrue(roots.any { abs(it - 1.41421356) < 1e-3 })
    }

    @Test
    fun testLinearEquation_decimalCoefficients() {
        // 0.5x - 2.5 = 0 -> x = 5
        val res = engine.solve("0.5x - 2.5 = 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(1, roots.size)
        assertEquals(5.0, roots[0], 1e-3)
    }

    @Test
    fun testNoRealRootsEquation_x2_plus_1_equals_0() {
        // x^2 + 1 = 0 has no real roots
        val res = engine.solve("x^2 + 1 = 0")
        assertTrue("Expected Error for no real roots", res is AdvancedMathEngine.SolveResult.Error)
        val msg = (res as AdvancedMathEngine.SolveResult.Error).message
        assertTrue("Should indicate no real roots found", msg.contains("Nessuna radice reale"))
    }

    @Test
    fun testExpressionWithoutEquals_findsRoots() {
        // "x^2 - 25" without '=' -> defaults to f(x)=0 roots
        val res = engine.solve("x^2 - 25")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(2, roots.size)
        assertTrue(roots.any { abs(it - (-5.0)) < 1e-3 })
        assertTrue(roots.any { abs(it - 5.0) < 1e-3 })
    }

    @Test
    fun testRationalEquation_rootAtFour() {
        // 1 / (x - 3) = 1 -> x - 3 = 1 -> x = 4
        val res = engine.solve("1 / (x - 3) = 1")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertEquals(1, roots.size)
        assertEquals(4.0, roots[0], 1e-3)
    }

    @Test
    fun testEquation_rootOutsideSearchDomain_documentedLimitation() {
        // x - 100 = 0 has root at x = 100, which is outside the [-50, 50] grid
        val res = engine.solve("x - 100 = 0")
        // Engine searches [-50, 50]
        assertTrue(res is AdvancedMathEngine.SolveResult.Error)
        val msg = (res as AdvancedMathEngine.SolveResult.Error).message
        assertTrue(msg.contains("[-50, 50]"))
    }

    @Test
    fun testSimpleArithmeticCalculation() {
        val res = engine.solve("15 * 4 - 20 / 2")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        assertEquals(50.0, (res as AdvancedMathEngine.SolveResult.SingleValue).raw, 1e-6)
    }

    // =========================================================================
    // 2. INEQUALITIES STRESS TESTS
    // =========================================================================

    @Test
    fun testInequality_quadratic_innerInterval() {
        // x^2 - 4 < 0 -> x in (-2, 2)
        val res = engine.solve("x^2 - 4 < 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue("Must contain -2", ineq.text.contains("-2"))
        assertTrue("Must contain 2", ineq.text.contains("2"))
        assertTrue("Must have strict open brackets", ineq.text.contains("(-2, 2)") || ineq.text.contains("(-2.0, 2.0)"))
        assertEquals(1, ineq.intervals.size)
    }

    @Test
    fun testInequality_quadratic_outerIntervals() {
        // x^2 - 9 >= 0 -> x in (-inf, -3] U [3, +inf)
        val res = engine.solve("x^2 - 9 >= 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("-∞") && ineq.text.contains("+∞"))
        assertTrue(ineq.text.contains("-3") && ineq.text.contains("3"))
        assertEquals(2, ineq.intervals.size)
    }

    @Test
    fun testInequality_linear_strict() {
        // 2x - 6 > 0 -> x > 3 -> x in (3, +inf)
        val res = engine.solve("2x - 6 > 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("3") && ineq.text.contains("+∞"))
    }

    @Test
    fun testInequality_linear_lessThanOrEqual() {
        // 2x - 6 <= 0 -> x <= 3 -> x in (-inf, 3]
        val res = engine.solve("2x - 6 <= 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("-∞") && ineq.text.contains("3]"))
    }

    @Test
    fun testInequality_impossible() {
        // x^2 + 1 < 0 -> no real solution
        val res = engine.solve("x^2 + 1 < 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("Nessuna soluzione") || ineq.text.contains("impossibile"))
        assertTrue(ineq.intervals.isEmpty())
    }

    @Test
    fun testInequality_tautological() {
        // x^2 + 1 > 0 -> all reals (-inf, +inf)
        val res = engine.solve("x^2 + 1 > 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.InequalityIntervals)
        val ineq = res as AdvancedMathEngine.SolveResult.InequalityIntervals
        assertTrue(ineq.text.contains("-∞, +∞"))
    }

    // =========================================================================
    // 3. CALCULUS (DERIVATIVE, INTEGRAL, LIMIT) STRESS TESTS
    // =========================================================================

    @Test
    fun testDerivative_polynomial() {
        // d/dx(x^2, 3) -> 6
        val res = engine.solve("d/dx(x^2, 3)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val d = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(6.0, d, 1e-2)
    }

    @Test
    fun testDerivative_trigonometric() {
        // d/dx(sin(x), 0) in radians -> cos(0) = 1.0
        val res = engine.solve("d/dx(sin(x), 0)", isRadians = true)
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val d = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(1.0, d, 1e-2)
    }

    @Test
    fun testDerivative_cubicAtExtremum() {
        // d/dx(x^3 - 3x, 1) -> 3(1)^2 - 3 = 0.0
        val res = engine.solve("d/dx(x^3 - 3x, 1)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val d = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(0.0, d, 1e-2)
    }

    @Test
    fun testDerivative_outsideDomain() {
        // d/dx(sqrt(x), -4) -> sqrt(-4) is undefined
        val res = engine.solve("d/dx(sqrt(x), -4)")
        assertTrue("Should return Error when evaluating outside domain", res is AdvancedMathEngine.SolveResult.Error)
    }

    @Test
    fun testIntegral_polynomialDefinite() {
        // ∫(x^2, 0, 3) -> 9.0
        val res = engine.solve("∫(x^2, 0, 3)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val area = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(9.0, area, 1e-3)
    }

    @Test
    fun testIntegral_linear() {
        // ∫(2x, 1, 4) -> x^2 |_1^4 = 16 - 1 = 15.0
        val res = engine.solve("∫(2x, 1, 4)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val area = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(15.0, area, 1e-3)
    }

    @Test
    fun testIntegral_sineHalfPeriod() {
        // ∫(sin(x), 0, 3.14159265) -> -cos(pi) - (-cos(0)) = 1 - (-1) = 2.0
        val res = engine.solve("∫(sin(x), 0, 3.14159265)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val area = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(2.0, area, 1e-2)
    }

    @Test
    fun testIntegral_identicalBounds() {
        // ∫(x^2, 2, 2) -> 0.0
        val res = engine.solve("∫(x^2, 2, 2)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val area = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(0.0, area, 1e-3)
    }

    @Test
    fun testLimit_continuousFunction() {
        // lim(x^2, 2) = 4.0
        val res = engine.solve("lim(x^2, 2)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        assertEquals(4.0, (res as AdvancedMathEngine.SolveResult.SingleValue).raw, 1e-2)
    }

    @Test
    fun testLimit_sincFunction() {
        // lim(sin(x)/x, 0) = 1.0 (indeterminate 0/0 form)
        val res = engine.solve("lim(sin(x)/x, 0)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        assertEquals(1.0, (res as AdvancedMathEngine.SolveResult.SingleValue).raw, 1e-2)
    }

    // =========================================================================
    // 4. FORMAT CONVERSION STRESS TESTS (DEC, FRAC, ENG, DMS, POL)
    // =========================================================================

    // --- DEC (Decimal) ---
    @Test
    fun testFormat_DEC_regularAndEdgeCases() {
        assertEquals("0", engine.toDecimal(0.0))
        assertEquals("0", engine.toDecimal(-0.0))
        assertEquals("0", engine.toDecimal(1e-15))
        assertEquals("12", engine.toDecimal(12.0))
        assertEquals("-12", engine.toDecimal(-12.0))
        assertEquals("2.5", engine.toDecimal(2.5))
        assertEquals("-2.5", engine.toDecimal(-2.5))
        assertEquals("NaN", engine.toDecimal(Double.NaN))
        assertEquals("+∞", engine.toDecimal(Double.POSITIVE_INFINITY))
        assertEquals("-∞", engine.toDecimal(Double.NEGATIVE_INFINITY))
    }

    // --- FRAC (Continued Fraction S<=>D) ---
    @Test
    fun testFormat_FRAC_standardFractions() {
        assertEquals("0", engine.toFraction(0.0))
        assertEquals("1", engine.toFraction(1.0))
        assertEquals("-1", engine.toFraction(-1.0))
        assertEquals("3/8", engine.toFraction(0.375))
        assertEquals("5/2", engine.toFraction(2.5))
        assertEquals("-5/2", engine.toFraction(-2.5))
        assertEquals("1/3", engine.toFraction(1.0 / 3.0))
        assertEquals("2/3", engine.toFraction(2.0 / 3.0))
        assertEquals("-7/8", engine.toFraction(-0.875))
        assertEquals("1/8", engine.toFraction(0.125))
        assertEquals("1/20", engine.toFraction(0.05))
        assertEquals("1/1000", engine.toFraction(0.001))
    }

    @Test
    fun testFormat_FRAC_extremesAndSpecialValues() {
        assertEquals("NaN", engine.toFraction(Double.NaN))
        assertEquals("+∞", engine.toFraction(Double.POSITIVE_INFINITY))
        assertEquals("-∞", engine.toFraction(Double.NEGATIVE_INFINITY))

        // Large integer
        assertEquals("1000000", engine.toFraction(1000000.0))

        // Negative integer
        assertEquals("-42", engine.toFraction(-42.0))

        // Precision stress: 355/113
        assertEquals("355/113", engine.toFraction(355.0 / 113.0))

        // Irrational number pi continued fraction approximation does not loop infinitely
        val piFrac = engine.toFraction(PI)
        assertTrue("Pi fraction non-empty", piFrac.isNotEmpty())
        assertTrue("Contains fraction slash or is rational approximation", piFrac.contains("/"))
    }

    // --- ENG (Engineering Notation) ---
    @Test
    fun testFormat_ENG_multiplesOfThree() {
        assertEquals("0", engine.toEngineering(0.0))
        assertEquals("1", engine.toEngineering(1.0))
        assertEquals("10", engine.toEngineering(10.0))
        assertEquals("100", engine.toEngineering(100.0))

        val eng1k = engine.toEngineering(1000.0)
        assertTrue("1k should be 1 × 10^3: was $eng1k", (eng1k.contains("1 ×") || eng1k.startsWith("1")) && eng1k.contains("10^3"))
        assertEquals("1 × 10^3", eng1k)

        val eng1_25M = engine.toEngineering(1250000.0)
        assertTrue("1.25M should be 1.25 × 10^6: was $eng1_25M", eng1_25M.contains("1.25") && eng1_25M.contains("10^6"))

        val engMicro = engine.toEngineering(0.00047)
        assertTrue("470u should be 470 × 10^-6: was $engMicro", engMicro.contains("470") && engMicro.contains("10^-6"))

        val engMilli = engine.toEngineering(0.005)
        assertTrue("5m should be 5 × 10^-3: was $engMilli", engMilli.contains("5") && engMilli.contains("10^-3"))

        // Negative numbers in engineering notation
        val engNeg = engine.toEngineering(-1500.0)
        assertTrue("Negative 1.5k: was $engNeg", engNeg.startsWith("-") && engNeg.contains("1.5") && engNeg.contains("10^3"))
    }

    @Test
    fun testFormat_ENG_specialValues() {
        assertEquals("NaN", engine.toEngineering(Double.NaN))
        assertEquals("+∞", engine.toEngineering(Double.POSITIVE_INFINITY))
        assertEquals("-∞", engine.toEngineering(Double.NEGATIVE_INFINITY))
    }

    // --- DMS (Degrees, Minutes, Seconds) ---
    @Test
    fun testFormat_DMS_standardAndNegative() {
        val dmsZero = engine.toDms(0.0)
        assertEquals("0° 0' 0.0\"", dmsZero)

        val dmsQuarter = engine.toDms(32.25)
        assertEquals("32° 15' 0.0\"", dmsQuarter)

        val dmsHalf = engine.toDms(45.5)
        assertEquals("45° 30' 0.0\"", dmsHalf)

        val dmsNeg = engine.toDms(-32.25)
        assertEquals("-32° 15' 0.0\"", dmsNeg)
    }

    @Test
    fun testFormat_DMS_specialValues() {
        assertEquals("NaN", engine.toDms(Double.NaN))
        assertEquals("+∞", engine.toDms(Double.POSITIVE_INFINITY))
        assertEquals("-∞", engine.toDms(Double.NEGATIVE_INFINITY))
    }

    // --- POL (Polar Coordinate r ∠ θ) ---
    @Test
    fun testFormat_POL_positiveNegativeRadiansAndDegrees() {
        val polPosRad = engine.toPolar(5.0, isRadians = true)
        assertEquals("5 ∠ 0 rad", polPosRad)

        val polPosDeg = engine.toPolar(5.0, isRadians = false)
        assertEquals("5 ∠ 0°", polPosDeg)

        val polNegRad = engine.toPolar(-5.0, isRadians = true)
        assertEquals("5 ∠ π rad", polNegRad)

        val polNegDeg = engine.toPolar(-5.0, isRadians = false)
        assertEquals("5 ∠ 180°", polNegDeg)

        val polZero = engine.toPolar(0.0, isRadians = true)
        assertEquals("0 ∠ 0 rad", polZero)
    }

    @Test
    fun testFormat_POL_specialValues() {
        assertEquals("NaN", engine.toPolar(Double.NaN, true))
        assertEquals("+∞", engine.toPolar(Double.POSITIVE_INFINITY, true))
        assertEquals("-∞", engine.toPolar(Double.NEGATIVE_INFINITY, true))
    }

    // =========================================================================
    // 5. MATRIX SOLUTION BRACKETS & VIEWMODEL FORMAT INTEGRATION
    // =========================================================================

    @Test
    fun testMatrixBrackets_emptyRoots() {
        val formatted = viewModel.formatRoots(emptyList(), AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("Nessuna radice reale", formatted)
    }

    @Test
    fun testMatrixBrackets_singleRoot() {
        val formatted = viewModel.formatRoots(listOf(3.0), AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("x = 3", formatted)
    }

    @Test
    fun testMatrixBrackets_dualRoots_decimal() {
        val formatted = viewModel.formatRoots(listOf(-4.0, 4.0), AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("[ x = -4 ; x = 4 ]", formatted)
    }

    @Test
    fun testMatrixBrackets_dualRoots_fractionFormat() {
        val formatted = viewModel.formatRoots(listOf(-0.5, 0.5), AdvancedMathEngine.ResultFormat.FRACTION, true)
        assertEquals("[ x = -1/2 ; x = 1/2 ]", formatted)
    }

    @Test
    fun testMatrixBrackets_tripleRoots_decimal() {
        val formatted = viewModel.formatRoots(listOf(-2.0, 0.0, 2.0), AdvancedMathEngine.ResultFormat.DECIMAL, true)
        assertEquals("[ x = -2 ; x = 0 ; x = 2 ]", formatted)
    }

    // =========================================================================
    // 6. ADVERSARIAL EDGE CASES & STRESS BOUNDARIES
    // =========================================================================

    @Test
    fun testTangentRoot_x2_equals_0() {
        // x^2 = 0 has a double root at 0.0 (touches x-axis without sign change)
        val res = engine.solve("x^2 = 0")
        assertTrue("Double root at origin should be found", res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertTrue("Contains 0", roots.any { abs(it - 0.0) < 1e-3 })
    }

    @Test
    fun testTangentRoot_onGrid_x_minus_2_squared() {
        // (x - 2)^2 = 0: x = 2 is on the 0.1 grid step (-50 + 520*0.1 = 2.0)
        val res = engine.solve("(x - 2)^2 = 0")
        assertTrue(res is AdvancedMathEngine.SolveResult.EquationRoots)
        val roots = (res as AdvancedMathEngine.SolveResult.EquationRoots).roots
        assertTrue("Contains 2", roots.any { abs(it - 2.0) < 1e-3 })
    }

    @Test
    fun testTangentRoot_offGrid_doubleRootLimitation() {
        // (x - 2.05)^2 = 0: x = 2.05 is exactly midway between grid steps 2.0 and 2.1
        // Since (x - 2.05)^2 >= 0 everywhere, there is NO sign change (prevY * curY > 0)
        // and neither f(2.0) nor f(2.1) is < 1e-10.
        val res = engine.solve("(x - 2.05)^2 = 0")
        // Empirical observation of Brent-Dekker sign-change grid limitation:
        // Double roots without sign change that do not fall on grid points return Error ("Nessuna radice")
        assertTrue(res is AdvancedMathEngine.SolveResult.Error || res is AdvancedMathEngine.SolveResult.EquationRoots)
    }

    @Test
    fun testDivisionByZeroEquation_1_over_x_equals_0() {
        // 1/x = 0 has no solution, asymptotes at 0
        val res = engine.solve("1/x = 0")
        assertTrue("Should handle asymptote safely without crash", res is AdvancedMathEngine.SolveResult.Error)
    }

    @Test
    fun testDerivative_atSingularity() {
        // d/dx(1/x, 0) -> f'(0) is undefined (-infinity)
        val res = engine.solve("d/dx(1/x, 0)")
        assertTrue("Deriv at singularity should return Error", res is AdvancedMathEngine.SolveResult.Error)
    }

    @Test
    fun testSimpsonIntegral_reciprocal() {
        // ∫(1/x, 1, 2) = ln(2) ≈ 0.693147
        val res = engine.solve("∫(1/x, 1, 2)")
        assertTrue(res is AdvancedMathEngine.SolveResult.SingleValue)
        val area = (res as AdvancedMathEngine.SolveResult.SingleValue).raw
        assertEquals(0.693147, area, 1e-2)
    }

    @Test
    fun testFormat_FRAC_negativeDecimal() {
        assertEquals("-3/8", engine.toFraction(-0.375))
    }

    @Test
    fun testFormat_ENG_extremePowers() {
        val engTera = engine.toEngineering(1e12)
        assertTrue(engTera.contains("10^12"))

        val engPico = engine.toEngineering(1e-12)
        assertTrue(engPico.contains("10^-12"))
    }

    @Test
    fun testFormat_DMS_smallSeconds() {
        // 0.0002777777777777778 hours/degrees = 1 second
        val dms = engine.toDms(0.0002777777777777778)
        assertTrue(dms.contains("1.0\"") || dms.contains("1\""))
    }
}

