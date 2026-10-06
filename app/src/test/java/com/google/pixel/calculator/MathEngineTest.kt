package com.google.pixel.calculator

import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.MathEvaluator
import com.google.pixel.calculator.core.utils.UnitConverter
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class MathEngineTest {

    private val evaluator = MathEvaluator()
    private val converter = UnitConverter()

    @Test
    fun testPrecisionDecimalAddition() {
        // Verifica che 0.1 + 0.2 dia esattamente 0.3 senza l'errore floating point IEEE 754
        val result = evaluator.evaluate("0.1 + 0.2")
        assertTrue(result.isSuccess)
        assertEquals("0,3", result.formattedResult)
        assertEquals(0, result.rawValue?.compareTo(BigDecimal("0.3")))
    }

    @Test
    fun testOperatorPrecedenceAndParentheses() {
        val res1 = evaluator.evaluate("2 + 3 * 4")
        assertTrue(res1.isSuccess)
        assertEquals("14", res1.formattedResult)

        val res2 = evaluator.evaluate("(2 + 3) * 4")
        assertTrue(res2.isSuccess)
        assertEquals("20", res2.formattedResult)
    }

    @Test
    fun testImplicitMultiplication() {
        // 2(5) -> 10
        val res = evaluator.evaluate("2(5)")
        assertTrue(res.isSuccess)
        assertEquals("10", res.formattedResult)
    }

    @Test
    fun testFractions2D() {
        // frac(1,2) + frac(1,4) = 0.75
        val res = evaluator.evaluate("frac(1,2) + frac(1,4)")
        assertTrue(res.isSuccess)
        assertEquals("0,75", res.formattedResult)
    }

    @Test
    fun testUnitConverter() {
        // 1 pollice (in) = 2.54 centimetri (cm)
        val cm = converter.convert(1.0, "in", "cm", UnitConverter.Category.LENGTH)
        assertEquals(2.54, cm, 1e-6)

        // 100 °C = 212 °F
        val fahrenheit = converter.convert(100.0, "C", "F", UnitConverter.Category.TEMPERATURE)
        assertEquals(212.0, fahrenheit, 1e-6)
    }

    @Test
    fun testNthRootFunction() {
        val resCube = evaluator.evaluate("root(8, 3)")
        assertTrue(resCube.isSuccess)
        assertEquals("2", resCube.formattedResult)

        val resFourth = evaluator.evaluate("root(81, 4)")
        assertTrue(resFourth.isSuccess)
        assertEquals("3", resFourth.formattedResult)
    }

    @Test
    fun testShiftScientificFunctions() {
        // sin⁻¹(1) in gradi = 90
        val resAsin = evaluator.evaluate("sin⁻¹(1)", EvaluationContext(isRadians = false))
        assertTrue(resAsin.isSuccess)
        assertEquals("90", resAsin.formattedResult)

        // tan⁻¹(1) in gradi = 45
        val resAtan = evaluator.evaluate("tan⁻¹(1)", EvaluationContext(isRadians = false))
        assertTrue(resAtan.isSuccess)
        assertEquals("45", resAtan.formattedResult)

        // 10^3 = 1000
        val resTenExp = evaluator.evaluate("10^3")
        assertTrue(resTenExp.isSuccess)
        assertEquals("1.000", resTenExp.formattedResult)

        // ln(e) = 1
        val resLn = evaluator.evaluate("ln(e)")
        assertTrue(resLn.isSuccess)
        assertEquals("1", resLn.formattedResult)
    }

    @Test
    fun testAutoClosingParentheses() {
        // Parentesi non chiuse dall'utente: sin(30 deve essere valutato come sin(30)
        val resSin = evaluator.evaluate("sin(30", EvaluationContext(isRadians = false))
        assertTrue(resSin.isSuccess)
        assertEquals("0,5", resSin.formattedResult)

        // √(16 senza parentesi chiusa -> 4
        val resSqrt = evaluator.evaluate("√(16")
        assertTrue(resSqrt.isSuccess)
        assertEquals("4", resSqrt.formattedResult)

        // Espressioni annidate senza parentesi chiuse: (10 + 2 * (3 + 2 -> (10 + 2 * (3 + 2)) = 20
        val resNested = evaluator.evaluate("(10 + 2 * (3 + 2")
        assertTrue(resNested.isSuccess)
        assertEquals("20", resNested.formattedResult)

        // log(100 -> 2
        val resLog = evaluator.evaluate("log(100")
        assertTrue(resLog.isSuccess)
        assertEquals("2", resLog.formattedResult)
    }

    @Test
    fun testSystemCalculatorPercentage() {
        // Calcolatrice di sistema Google / Pixel:
        // 100 + 20% = 120 (percentuale applicata come incremento)
        val resAdd = evaluator.evaluate("100 + 20%")
        assertTrue(resAdd.isSuccess)
        assertEquals("120", resAdd.formattedResult)

        // 100 - 20% = 80 (percentuale applicata come sconto)
        val resSub = evaluator.evaluate("100 - 20%")
        assertTrue(resSub.isSuccess)
        assertEquals("80", resSub.formattedResult)

        // 200 + 10% = 220
        val resAdd2 = evaluator.evaluate("200 + 10%")
        assertTrue(resAdd2.isSuccess)
        assertEquals("220", resAdd2.formattedResult)

        // 50 * 20% = 10
        val resMul = evaluator.evaluate("50 * 20%")
        assertTrue(resMul.isSuccess)
        assertEquals("10", resMul.formattedResult)

        // 200 / 50% = 400 (200 / 0.5)
        val resDiv = evaluator.evaluate("200 / 50%")
        assertTrue(resDiv.isSuccess)
        assertEquals("400", resDiv.formattedResult)

        // 50% standalone = 0.5
        val resSolo = evaluator.evaluate("50%")
        assertTrue(resSolo.isSuccess)
        assertEquals("0,5", resSolo.formattedResult)

        // 50% + 5 = 5.5
        val resSoloAdd = evaluator.evaluate("50% + 5")
        assertTrue(resSoloAdd.isSuccess)
        assertEquals("5,5", resSoloAdd.formattedResult)
    }

    @Test
    fun testSymbolRootsAndRadDeg() {
        // Radice quadrata con simbolo √(9) e auto-close √(9
        val resSqrtClosed = evaluator.evaluate("√(9)")
        assertTrue(resSqrtClosed.isSuccess)
        assertEquals("3", resSqrtClosed.formattedResult)

        val resSqrtOpen = evaluator.evaluate("√(9")
        assertTrue(resSqrtOpen.isSuccess)
        assertEquals("3", resSqrtOpen.formattedResult)

        // Radice n-esima con simbolo operatore: 3ʸ√8 = 2
        val resNthRoot = evaluator.evaluate("3ʸ√8")
        assertTrue(resNthRoot.isSuccess)
        assertEquals("2", resNthRoot.formattedResult)

        // 4ʸ√81 = 3
        val resNthRoot4 = evaluator.evaluate("4ʸ√81")
        assertTrue(resNthRoot4.isSuccess)
        assertEquals("3", resNthRoot4.formattedResult)

        // Radice senza indice prefisso: ʸ√16 = 4 (default indice 2)
        val resUnaryRoot = evaluator.evaluate("ʸ√16")
        assertTrue(resUnaryRoot.isSuccess)
        assertEquals("4", resUnaryRoot.formattedResult)

        // Funzioni trigonometriche inverse e DEG/RAD
        val resAsin = evaluator.evaluate("sin⁻¹(0.5)", EvaluationContext(isRadians = false))
        assertTrue(resAsin.isSuccess)
        assertEquals("30", resAsin.formattedResult)

        val resAcos = evaluator.evaluate("cos⁻¹(0.5)", EvaluationContext(isRadians = false))
        assertTrue(resAcos.isSuccess)
        assertEquals("60", resAcos.formattedResult)
    }

    @Test
    fun testSuperscriptIndexedRoots() {
        // Test radice cubica con apice: ³√(8) = 2
        val resCubeClosed = evaluator.evaluate("³√(8)")
        assertTrue(resCubeClosed.isSuccess)
        assertEquals("2", resCubeClosed.formattedResult)

        // Test radice cubica con parentesi auto-chiusa: ³√(8 -> 2
        val resCubeAuto = evaluator.evaluate("³√(8")
        assertTrue(resCubeAuto.isSuccess)
        assertEquals("2", resCubeAuto.formattedResult)

        // Test radice quarta: ⁴√(81) = 3
        val resFourth = evaluator.evaluate("⁴√(81)")
        assertTrue(resFourth.isSuccess)
        assertEquals("3", resFourth.formattedResult)

        // Test combinazione con operatori: ³√(27) + 5 = 8
        val resComb = evaluator.evaluate("³√(27) + 5")
        assertTrue(resComb.isSuccess)
        assertEquals("8", resComb.formattedResult)

        // Moltiplicazione implicita o esplicita: 2 * ³√(8) = 4
        val resMul = evaluator.evaluate("2 * ³√(8)")
        assertTrue(resMul.isSuccess)
        assertEquals("4", resMul.formattedResult)

        // Simbolo radice cubica Unicode: ∛(8) = 2
        val resCbrt = evaluator.evaluate("∛(8)")
        assertTrue(resCbrt.isSuccess)
        assertEquals("2", resCbrt.formattedResult)
    }
}
