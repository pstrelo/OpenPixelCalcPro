package com.google.pixel.calculator

import com.google.pixel.calculator.core.math.*
import com.google.pixel.calculator.core.utils.UnitConverter
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

/**
 * Exhaustive QA and Mathematics Verification Test Suite for Pixel Calculator.
 * Executes 30+ edge-case tests across all modules of the math engine.
 */
object ExhaustiveMathTestSuite {

    data class TestCaseResult(
        val id: Int,
        val category: String,
        val description: String,
        val passed: Boolean,
        val actual: String,
        val expected: String,
        val bugNote: String? = null
    )

    private val results = mutableListOf<TestCaseResult>()
    private var testCounter = 1

    private val evaluator = MathEvaluator()
    private val converter = UnitConverter()

    // Safe context explicitly including standard mathematical constants
    private val radContext = EvaluationContext.DEFAULT.copy(isRadians = true)
    private val degContext = EvaluationContext.DEFAULT.copy(isRadians = false)

    fun runAll(): List<TestCaseResult> {
        results.clear()
        testCounter = 1

        println("================================================================================")
        println("         PIXEL CALCULATOR - QA & MATHEMATICS VERIFICATION TEST SUITE            ")
        println("================================================================================")

        // ---------------------------------------------------------
        // GROUP 1: Floating Point vs BigDecimal Precision
        // ---------------------------------------------------------
        runTest("Precisione", "0.1 + 0.2 exactness (evita errore IEEE 754 0.30000000000000004)") {
            val res = evaluator.evaluate("0.1 + 0.2", radContext)
            check(res.isSuccess && res.formattedResult == "0,3" && res.rawValue?.compareTo(BigDecimal("0.3")) == 0,
                expected = "0,3 (BigDecimal 0.3)",
                actual = "${res.formattedResult} (raw=${res.rawValue})")
        }

        runTest("Precisione", "1 / 3 precisione a 34 cifre decimali (MathContext IEEE 754r)") {
            val res = evaluator.evaluate("1 / 3", radContext)
            val expectedStart = "0.3333333333333333333333333333333333"
            val rawStr = res.rawValue?.toPlainString() ?: ""
            check(res.isSuccess && rawStr.startsWith(expectedStart),
                expected = "Valore a 34 cifre iniziante per $expectedStart",
                actual = "raw=$rawStr, formatted=${res.formattedResult}")
        }

        runTest("Precisione", "Cancellazione catastrofica 1 - 0.9 - 0.1 = 0") {
            val res = evaluator.evaluate("1 - 0.9 - 0.1", radContext)
            check(res.isSuccess && res.rawValue?.compareTo(BigDecimal.ZERO) == 0,
                expected = "0",
                actual = "${res.formattedResult} (raw=${res.rawValue})")
        }

        runTest("Precisione", "Grandi numeri: 10 ^ 50") {
            val res = evaluator.evaluate("10 ^ 50", radContext)
            check(res.isSuccess && (res.formattedResult == "1E50" || res.formattedResult == "1,0E50" || res.formattedResult == "1E+50"),
                expected = "1E50 (notazione scientifica)",
                actual = res.formattedResult)
        }

        runTest("Precisione", "Numeri infinitesimi: 10 ^ (-50)") {
            val res = evaluator.evaluate("10 ^ (-50)", radContext)
            check(res.isSuccess && (res.formattedResult == "1E-50" || res.formattedResult == "1,0E-50"),
                expected = "1E-50 (notazione scientifica)",
                actual = res.formattedResult)
        }

        // ---------------------------------------------------------
        // GROUP 2: Division by zero (basic and 2D fractions)
        // ---------------------------------------------------------
        runTest("Divisione per zero", "Divisione standard per zero: 1 / 0") {
            val res = evaluator.evaluate("1 / 0", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, formatted=${res.formattedResult}, err=${res.errorMessage}")
        }

        runTest("Divisione per zero", "Forma indeterminata 0 / 0") {
            val res = evaluator.evaluate("0 / 0", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, formatted=${res.formattedResult}, err=${res.errorMessage}")
        }

        runTest("Divisione per zero", "Frazione 2D con denominatore zero: frac(1, 0)") {
            val res = evaluator.evaluate("frac(1, 0)", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Divisione per zero", "Frazione 2D indeterminata: frac(0, 0)") {
            val res = evaluator.evaluate("frac(0, 0)", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Divisione per zero", "Divisione per espressione a denominatore nullo: 5 / (2 - 2)") {
            val res = evaluator.evaluate("5 / (2 - 2)", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        // ---------------------------------------------------------
        // GROUP 3: Complex and nested 2D fractions
        // ---------------------------------------------------------
        runTest("Frazioni 2D", "Frazione complessa a due piani: frac(frac(1,2), frac(3,4)) = 2/3") {
            val res = evaluator.evaluate("frac(frac(1,2), frac(3,4))", radContext)
            val isTwoThirds = res.rawValue?.toPlainString()?.startsWith("0.6666666") == true
            check(res.isSuccess && isTwoThirds,
                expected = "0,6666666666 (2/3)",
                actual = "${res.formattedResult} (raw=${res.rawValue})")
        }

        runTest("Frazioni 2D", "Frazione nidificata al denominatore: frac(1, frac(2, 3)) = 1.5") {
            val res = evaluator.evaluate("frac(1, frac(2, 3))", radContext)
            check(res.isSuccess && res.formattedResult == "1,5",
                expected = "1,5",
                actual = res.formattedResult)
        }

        runTest("Frazioni 2D", "Frazione nidificata con espressioni algebriche: frac(1 + frac(1,2), frac(3,4) - 1)") {
            // Numerator = 1.5, Denominator = -0.25 -> 1.5 / -0.25 = -6
            val res = evaluator.evaluate("frac(1 + frac(1,2), frac(3,4) - 1)", radContext)
            check(res.isSuccess && res.rawValue?.compareTo(BigDecimal("-6")) == 0,
                expected = "-6",
                actual = "${res.formattedResult} (raw=${res.rawValue})")
        }

        runTest("Frazioni 2D", "Frazione a denominatore con divisione per zero nidificata: frac(1, frac(2, 0))") {
            val res = evaluator.evaluate("frac(1, frac(2, 0))", radContext)
            check(!res.isSuccess && res.errorMessage == "Divisione per zero",
                expected = "Errore: Divisione per zero",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        // ---------------------------------------------------------
        // GROUP 4: Implicit Multiplication
        // ---------------------------------------------------------
        runTest("Moltiplicazione Implicita", "Prodotto implicito a catena: 2(3)(4) = 24") {
            val res = evaluator.evaluate("2(3)(4)", radContext)
            check(res.isSuccess && res.formattedResult == "24",
                expected = "24",
                actual = res.formattedResult)
        }

        runTest("Moltiplicazione Implicita", "Numero e costante: 2pi (con contesto di default)") {
            val res = evaluator.evaluate("2pi", EvaluationContext.DEFAULT)
            check(res.isSuccess && res.formattedResult.startsWith("6,28"),
                expected = "6,283185...",
                actual = "${res.formattedResult}, err=${res.errorMessage}")
        }

        runTest("Moltiplicazione Implicita", "Numero e radice: 3sqrt(4) = 6") {
            val res = evaluator.evaluate("3sqrt(4)", radContext)
            check(res.isSuccess && res.formattedResult == "6",
                expected = "6",
                actual = res.formattedResult)
        }

        runTest("Moltiplicazione Implicita", "Numero e frazione tra parentesi: 2(1/2) = 1") {
            val res = evaluator.evaluate("2(1/2)", radContext)
            check(res.isSuccess && res.formattedResult == "1",
                expected = "1",
                actual = res.formattedResult)
        }

        runTest("Moltiplicazione Implicita", "Parentesi seguita direttamente da numero: (2)3 = 6") {
            val res = evaluator.evaluate("(2)3", radContext)
            val ok = res.isSuccess && res.formattedResult == "6"
            check(ok,
                expected = "6",
                actual = "${res.formattedResult} (raw=${res.rawValue})",
                bugNote = if (!ok) "BUG: ExpressionParser non inserisce '*' tra CloseParen e un Token.Number. L'espressione (2)3 scarta il 2 e restituisce 3!" else null)
        }

        runTest("Moltiplicazione Implicita", "Due parentesi consecutive: (2)(3) = 6") {
            val res = evaluator.evaluate("(2)(3)", radContext)
            check(res.isSuccess && res.formattedResult == "6",
                expected = "6",
                actual = res.formattedResult)
        }

        // ---------------------------------------------------------
        // GROUP 5: Trigonometry and Domains
        // ---------------------------------------------------------
        runTest("Trigonometria", "tan(90°) in modalità DEG: asintoto verticale / indefinita") {
            val res = evaluator.evaluate("tan(90)", degContext)
            check(!res.isSuccess && res.errorMessage == "Tangente indefinita",
                expected = "Errore: Tangente indefinita",
                actual = "success=${res.isSuccess}, formatted=${res.formattedResult}, err=${res.errorMessage}")
        }

        runTest("Trigonometria", "tan(270°) in modalità DEG: indefinita") {
            val res = evaluator.evaluate("tan(270)", degContext)
            check(!res.isSuccess && res.errorMessage == "Tangente indefinita",
                expected = "Errore: Tangente indefinita",
                actual = "success=${res.isSuccess}, formatted=${res.formattedResult}, err=${res.errorMessage}")
        }

        runTest("Trigonometria", "asin(2): argomento fuori dominio [-1, 1]") {
            val res = evaluator.evaluate("asin(2)", radContext)
            check(!res.isSuccess && res.errorMessage?.contains("Dominio asin") == true,
                expected = "Errore dominio asin [-1, 1]",
                actual = "err=${res.errorMessage}")
        }

        runTest("Trigonometria", "asin(-2): argomento negativo fuori dominio [-1, 1]") {
            val res = evaluator.evaluate("asin(-2)", radContext)
            check(!res.isSuccess && res.errorMessage?.contains("Dominio asin") == true,
                expected = "Errore dominio asin [-1, 1]",
                actual = "err=${res.errorMessage}")
        }

        runTest("Trigonometria", "Precisione cos(90°) in DEG: deve essere 0 senza residui floating point") {
            val res = evaluator.evaluate("cos(90)", degContext)
            val isZero = res.isSuccess && (res.formattedResult == "0" || res.rawValue?.compareTo(BigDecimal.ZERO) == 0)
            check(isZero,
                expected = "0",
                actual = "${res.formattedResult} (raw=${res.rawValue})",
                bugNote = if (!isZero) "PRECISIONE: cos(90°) produce 6.123234E-17 a causa dell'arrotondamento di Math.toRadians(90.0)" else null)
        }

        runTest("Trigonometria", "Precisione sin(180°) in DEG: deve essere 0 senza residui floating point") {
            val res = evaluator.evaluate("sin(180)", degContext)
            val isZero = res.isSuccess && (res.formattedResult == "0" || res.rawValue?.compareTo(BigDecimal.ZERO) == 0)
            check(isZero,
                expected = "0",
                actual = "${res.formattedResult} (raw=${res.rawValue})",
                bugNote = if (!isZero) "PRECISIONE: sin(180°) produce 1.224647E-16 a causa dell'arrotondamento di Math.toRadians(180.0)" else null)
        }

        runTest("Trigonometria", "Precisione tan(45°) in DEG: deve essere esattamente 1") {
            val res = evaluator.evaluate("tan(45)", degContext)
            val isOne = res.isSuccess && (res.formattedResult == "1" || res.rawValue?.compareTo(BigDecimal.ONE) == 0)
            check(isOne,
                expected = "1",
                actual = "${res.formattedResult} (raw=${res.rawValue})",
                bugNote = if (!isOne) "PRECISIONE: tan(45°) produce 0,9999999999 invece di 1" else null)
        }

        // ---------------------------------------------------------
        // GROUP 6: Roots, Powers, and Factorials
        // ---------------------------------------------------------
        runTest("Radici e Potenze", "Radice quadrata di numero negativo: sqrt(-4)") {
            val res = evaluator.evaluate("sqrt(-4)", radContext)
            check(!res.isSuccess && res.errorMessage == "Radice quadrata di numero negativo",
                expected = "Errore: Radice quadrata di numero negativo",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Radici e Potenze", "Radice cubica di numero negativo: cbrt(-8) = -2") {
            val res = evaluator.evaluate("cbrt(-8)", radContext)
            check(res.isSuccess && res.formattedResult == "-2",
                expected = "-2",
                actual = res.formattedResult)
        }

        runTest("Radici e Potenze", "Potenza 0^0: convenzione standard = 1") {
            val res = evaluator.evaluate("0^0", radContext)
            check(res.isSuccess && res.formattedResult == "1",
                expected = "1",
                actual = res.formattedResult)
        }

        runTest("Radici e Potenze", "Potenza 0^(-1): divisione per zero") {
            val res = evaluator.evaluate("0^(-1)", radContext)
            check(!res.isSuccess,
                expected = "Errore aritmetico (divisione per zero)",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Radici e Potenze", "Base negativa con esponente non intero: (-2)^0.5") {
            val res = evaluator.evaluate("(-2)^0.5", radContext)
            check(!res.isSuccess,
                expected = "Errore: Risultato non valido o indefinito",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Fattoriale", "Fattoriale di zero: 0! = 1") {
            val res = evaluator.evaluate("0!", radContext)
            check(res.isSuccess && res.formattedResult == "1",
                expected = "1",
                actual = res.formattedResult)
        }

        runTest("Fattoriale", "Fattoriale standard: 5! = 120") {
            val res = evaluator.evaluate("5!", radContext)
            check(res.isSuccess && res.formattedResult == "120",
                expected = "120",
                actual = res.formattedResult)
        }

        runTest("Fattoriale", "Fattoriale con numero decimale non intero: 3.5!") {
            val res = evaluator.evaluate("3.5!", radContext)
            check(!res.isSuccess && res.errorMessage?.contains("intero non negativo") == true,
                expected = "Errore: Il fattoriale richiede un numero intero non negativo",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}")
        }

        runTest("Fattoriale", "Precedenza operatore unario negativo vs fattoriale: -(3!) = -6") {
            val res = evaluator.evaluate("-(3!)", radContext)
            check(res.isSuccess && res.formattedResult == "-6",
                expected = "-6",
                actual = res.formattedResult)
        }

        // ---------------------------------------------------------
        // GROUP 7: Unit Converter
        // ---------------------------------------------------------
        runTest("Unit Converter", "0 °C convertito a °F e Kelvin") {
            val f = converter.convert(0.0, "C", "F", UnitConverter.Category.TEMPERATURE)
            val k = converter.convert(0.0, "C", "K", UnitConverter.Category.TEMPERATURE)
            check(f == 32.0 && k == 273.15,
                expected = "32.0 °F, 273.15 K",
                actual = "$f °F, $k K")
        }

        runTest("Unit Converter", "Punto di incrocio scale termiche: -40 °C == -40 °F") {
            val f = converter.convert(-40.0, "C", "F", UnitConverter.Category.TEMPERATURE)
            val c = converter.convert(-40.0, "F", "C", UnitConverter.Category.TEMPERATURE)
            check(f == -40.0 && c == -40.0,
                expected = "-40 °F e -40 °C",
                actual = "F=$f, C=$c")
        }

        runTest("Unit Converter", "Zero assoluto: -273.15 °C == 0 K") {
            val k = converter.convert(-273.15, "C", "K", UnitConverter.Category.TEMPERATURE)
            val c = converter.convert(0.0, "K", "C", UnitConverter.Category.TEMPERATURE)
            check(abs(k - 0.0) < 1e-9 && abs(c - (-273.15)) < 1e-9,
                expected = "0 K e -273.15 °C",
                actual = "K=$k, C=$c")
        }

        runTest("Unit Converter", "Roundtrip di precisione per unità di lunghezza: 1 pollice -> cm -> pollice") {
            val cm = converter.convert(1.0, "in", "cm", UnitConverter.Category.LENGTH)
            val inchBack = converter.convert(cm, "cm", "in", UnitConverter.Category.LENGTH)
            check(cm == 2.54 && abs(inchBack - 1.0) < 1e-12,
                expected = "2.54 cm e 1.0 in",
                actual = "$cm cm -> $inchBack in")
        }

        runTest("Unit Converter", "Conversione dati digitali: 1 GB in MB e Byte") {
            val mb = converter.convert(1.0, "GB", "MB", UnitConverter.Category.DATA)
            val b = converter.convert(1.0, "GB", "B", UnitConverter.Category.DATA)
            check(mb == 1024.0 && b == 1073741824.0,
                expected = "1024 MB e 1073741824 Byte",
                actual = "$mb MB, $b Byte")
        }

        // ---------------------------------------------------------
        // GROUP 9: Architectural Regressions and Global Constants
        // ---------------------------------------------------------
        runTest("Costanti e Contesto", "Costante 'pi' accessibile con EvaluationContext(isRadians = false)") {
            // Verify that EvaluationContext constructor preserves pi when isRadians is specified
            val ctx = EvaluationContext(isRadians = false)
            val res = evaluator.evaluate("pi", ctx)
            val ok = res.isSuccess && res.formattedResult.startsWith("3,14")
            check(ok,
                expected = "3,141592...",
                actual = "success=${res.isSuccess}, err=${res.errorMessage}",
                bugNote = if (!ok) "BUG: EvaluationContext defaults 'variables = emptyMap()'! When CalculatorViewModel instantiates EvaluationContext(isRadians = ...), all constants (pi, e, phi) are lost!" else null)
        }

        runTest("Costanti e Contesto", "Costante 'e' di Eulero con contesto standard") {
            val res = evaluator.evaluate("e", EvaluationContext.DEFAULT)
            check(res.isSuccess && res.formattedResult.startsWith("2,718"),
                expected = "2,718281...",
                actual = res.formattedResult)
        }

        runTest("Costanti e Contesto", "Operatore pendente incompleto: 2 * (deve essere errore sintassi)") {
            val res = evaluator.evaluate("2 *", radContext)
            val ok = !res.isSuccess
            check(ok,
                expected = "Errore di sintassi",
                actual = "success=${res.isSuccess}, formatted=${res.formattedResult}",
                bugNote = if (!ok) "BUG: Expression '2 *' evaluates to 0 because popOperatorToOutput replaces missing operand with ZERO and inverts operand order (0 * 2 = 0)!" else null)
        }

        runTest("Unit Converter", "Controllo typo enum Category: LENGHT vs LENGTH") {
            val enumName = UnitConverter.Category.values().find { it.displayName == "Lunghezza" }?.name
            val ok = enumName == "LENGTH"
            check(ok,
                expected = "LENGTH",
                actual = enumName ?: "NULL",
                bugNote = if (!ok) "TYPO: Category enum is 'LENGHT' instead of 'LENGTH'" else null)
        }

        printSummary()
        return results
    }

    private var currentCategory: String = ""
    private var currentDescription: String = ""

    private fun runTest(
        category: String,
        description: String,
        testLogic: () -> Unit
    ) {
        val id = testCounter++
        currentCategory = category
        currentDescription = description
        try {
            testLogic()
        } catch (t: Throwable) {
            val result = TestCaseResult(
                id = id,
                category = category,
                description = description,
                passed = false,
                actual = t.message ?: t::class.simpleName ?: "Unknown error",
                expected = "Test execution without assertion exception",
                bugNote = "Exception thrown: ${t::class.simpleName}"
            )
            results.add(result)
            println(formatRow(result))
        }
    }

    private fun check(
        condition: Boolean,
        expected: String,
        actual: String,
        bugNote: String? = null
    ) {
        val id = testCounter - 1
        val res = TestCaseResult(
            id = id,
            category = currentCategory,
            description = currentDescription,
            passed = condition,
            actual = actual,
            expected = expected,
            bugNote = bugNote
        )
        // Store
        results.add(res)
        println(formatRow(res))
    }

    private fun formatRow(r: TestCaseResult): String {
        val status = if (r.passed) "[ PASS ]" else "[ FAIL ]"
        val sb = StringBuilder()
        sb.append(String.format("%-8s #%-2d %-25s : %s\n", status, r.id, r.category, r.description))
        if (!r.passed) {
            sb.append(String.format("         Expected : %s\n", r.expected))
            sb.append(String.format("         Actual   : %s\n", r.actual))
            if (r.bugNote != null) {
                sb.append(String.format("         ANOMALY  : %s\n", r.bugNote))
            }
        }
        return sb.toString().trimEnd()
    }

    private fun printSummary() {
        val total = results.size
        val passed = results.count { it.passed }
        val failed = results.count { !it.passed }

        println("\n================================================================================")
        println("                                TEST SUMMARY                                    ")
        println("================================================================================")
        println(String.format("Total tests executed : %d", total))
        println(String.format("Passed (PASS)        : %d (%.1f%%)", passed, (passed.toDouble() / total) * 100))
        println(String.format("Failed/Bug (FAIL)    : %d (%.1f%%)", failed, (failed.toDouble() / total) * 100))
        println("================================================================================")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runAll()
    }
}
