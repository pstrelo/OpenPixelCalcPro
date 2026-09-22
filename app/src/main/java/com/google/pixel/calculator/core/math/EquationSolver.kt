package com.google.pixel.calculator.core.math

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Risolutore di equazioni algebriche per la Modalità Pro:
 * - Equazioni di 1° grado: ax + b = 0
 * - Equazioni di 2° grado: ax² + bx + c = 0 (con soluzioni reali o complesse e calcolo di Δ)
 * - Risolutore numerico generico per f(x) = 0 tramite metodo di Newton-Raphson / Bisezione
 */
class EquationSolver(
    private val parser: ExpressionParser = ExpressionParser()
) {

    sealed class Solution {
        data class Linear(val x: Double, val steps: List<String>) : Solution()
        data class QuadraticReal(val delta: Double, val x1: Double, val x2: Double, val steps: List<String>) : Solution()
        data class QuadraticComplex(val delta: Double, val realPart: Double, val imagPart: Double, val steps: List<String>) : Solution()
        data class NumericRoot(val root: Double, val iterations: Int) : Solution()
        data class InfiniteSolutions(val message: String) : Solution()
        data class NoSolution(val message: String) : Solution()
        data class Error(val message: String) : Solution()
    }

    /**
     * Risolve un'equazione di 2° grado: ax² + bx + c = 0
     */
    fun solveQuadratic(a: Double, b: Double, c: Double): Solution {
        if (abs(a) < 1e-12) {
            return solveLinear(b, c)
        }

        val delta = b * b - 4 * a * c
        val steps = mutableListOf<String>()
        steps.add("Equazione: ${formatCoeff(a)}x² + ${formatCoeff(b)}x + ${formatCoeff(c)} = 0")
        steps.add("Calcolo discriminante: Δ = b² - 4ac = ($b)² - 4($a)($c) = $delta")

        return when {
            abs(delta) < 1e-12 -> {
                val x = -b / (2 * a)
                steps.add("Δ = 0: Due soluzioni reali coincidenti")
                steps.add("x₁ = x₂ = -b / 2a = $x")
                Solution.QuadraticReal(delta, x, x, steps)
            }
            delta > 0 -> {
                val sqrtDelta = sqrt(delta)
                val x1 = (-b + sqrtDelta) / (2 * a)
                val x2 = (-b - sqrtDelta) / (2 * a)
                steps.add("Δ > 0: Due soluzioni reali distinte")
                steps.add("x₁ = (-b + √Δ) / 2a = $x1")
                steps.add("x₂ = (-b - √Δ) / 2a = $x2")
                Solution.QuadraticReal(delta, x1, x2, steps)
            }
            else -> {
                val rawReal = -b / (2 * a)
                val realPart = if (abs(rawReal) < 1e-12) 0.0 else rawReal
                val imagPart = sqrt(-delta) / (2 * a)
                steps.add("Δ < 0: Nessuna soluzione reale. Due soluzioni complesse coniugate:")
                steps.add("x = $realPart ± ${abs(imagPart)}i")
                Solution.QuadraticComplex(delta, realPart, abs(imagPart), steps)
            }
        }
    }

    /**
     * Risolve un'equazione di 1° grado: ax + b = 0
     */
    fun solveLinear(a: Double, b: Double): Solution {
        val steps = mutableListOf<String>()
        steps.add("Equazione lineare: ${formatCoeff(a)}x + ${formatCoeff(b)} = 0")
        if (abs(a) < 1e-12) {
            return if (abs(b) < 1e-12) {
                Solution.InfiniteSolutions("Indeterminata: 0 = 0 (infinite soluzioni)")
            } else {
                Solution.NoSolution("Impossibile: $b = 0 (nessuna soluzione)")
            }
        }
        val x = -b / a
        steps.add("x = -b / a = -$b / $a = $x")
        return Solution.Linear(x, steps)
    }

    /**
     * Risolutore numerico generico per f(x) = 0
     */
    fun solveNumeric(expressionFofX: String, initialGuess: Double = 0.0, maxIter: Int = 100): Solution {
        return try {
            val ast = parser.parse(expressionFofX)
            var x = initialGuess
            val h = 1e-6

            for (iter in 0 until maxIter) {
                val fx = evalAt(ast, x)
                if (abs(fx) < 1e-10) {
                    return Solution.NumericRoot(x, iter)
                }

                // Derivata numerica f'(x)
                val fxh = evalAt(ast, x + h)
                val dfx = (fxh - fx) / h

                if (abs(dfx) < 1e-12) {
                    // Minimo o punto di flesso: perturbazione
                    x += 0.1
                    continue
                }

                val nextX = x - (fx / dfx)
                if (abs(nextX - x) < 1e-10) {
                    val checkFx = evalAt(ast, nextX)
                    if (abs(checkFx) < 1e-6) {
                        return Solution.NumericRoot(nextX, iter)
                    }
                }
                x = nextX
            }

            val finalFx = evalAt(ast, x)
            if (abs(finalFx) < 1e-6) {
                Solution.NumericRoot(x, maxIter)
            } else {
                Solution.NoSolution("Nessuna convergenza trovata dopo $maxIter iterazioni (f(x) ≈ $finalFx)")
            }
        } catch (e: Exception) {
            Solution.Error("Impossibile trovare una radice: ${e.message}")
        }
    }

    private fun evalAt(ast: MathNode, x: Double): Double {
        val ctx = EvaluationContext(
            variables = mapOf("x" to BigDecimal(x.toString(), MathContext.DECIMAL64))
        )
        return ast.evaluate(ctx).toDouble()
    }

    private fun formatCoeff(v: Double): String {
        return if (abs(v) <= Long.MAX_VALUE.toDouble() && v % 1.0 == 0.0) {
            v.toLong().toString()
        } else {
            v.toString()
        }
    }
}
