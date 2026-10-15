package com.google.pixel.calculator.core.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Node in the Abstract Syntax Tree (AST) representing mathematical expressions.
 */
sealed class MathNode {
    abstract fun evaluate(context: EvaluationContext): BigDecimal

    data class Number(val value: BigDecimal) : MathNode() {
        constructor(valueStr: String) : this(BigDecimal(valueStr))
        override fun evaluate(context: EvaluationContext): BigDecimal = value
    }

    data class Variable(val name: String) : MathNode() {
        override fun evaluate(context: EvaluationContext): BigDecimal {
            return context.variables[name]
                ?: context.variables[name.lowercase()]
                ?: context.variables[name.uppercase()]
                ?: throw IllegalArgumentException("Variabile non definita: $name")
        }
    }

    data class Fraction(val numerator: MathNode, val denominator: MathNode) : MathNode() {
        override fun evaluate(context: EvaluationContext): BigDecimal {
            val den = denominator.evaluate(context)
            if (den.compareTo(BigDecimal.ZERO) == 0) {
                throw ArithmeticException("Divisione per zero")
            }
            val num = numerator.evaluate(context)
            return num.divide(den, context.mathContext)
        }
    }

    data class BinaryOp(val left: MathNode, val operator: String, val right: MathNode) : MathNode() {
        override fun evaluate(context: EvaluationContext): BigDecimal {
            val l = left.evaluate(context)
            val r = right.evaluate(context)
            return when (operator) {
                "+", "add" -> {
                    if (right is UnaryOp && (right.operator == "%" || right.operator == "percent")) {
                        val pct = right.operand.evaluate(context)
                        val delta = l.multiply(pct, context.mathContext).divide(BigDecimal(100), context.mathContext)
                        l.add(delta, context.mathContext)
                    } else {
                        l.add(r, context.mathContext)
                    }
                }
                "-", "sub" -> {
                    if (right is UnaryOp && (right.operator == "%" || right.operator == "percent")) {
                        val pct = right.operand.evaluate(context)
                        val delta = l.multiply(pct, context.mathContext).divide(BigDecimal(100), context.mathContext)
                        l.subtract(delta, context.mathContext)
                    } else {
                        l.subtract(r, context.mathContext)
                    }
                }
                "*", "×", "mul" -> l.multiply(r, context.mathContext)
                "/", "÷", "div" -> {
                    if (r.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Divisione per zero")
                    l.divide(r, context.mathContext)
                }
                "%", "mod" -> {
                    // If used as classic binary operator (modulo)
                    l.remainder(r, context.mathContext)
                }
                "^", "pow" -> {
                    // If the exponent is an integer, use BigDecimal.pow for maximum precision, otherwise Double
                    try {
                        val intExp = r.intValueExact()
                        if (kotlin.math.abs(intExp) > 10000) throw ArithmeticException("Esponente troppo elevato (massimo 10000)")
                        if (intExp >= 0) l.pow(intExp, context.mathContext)
                        else BigDecimal.ONE.divide(l.pow(-intExp, context.mathContext), context.mathContext)
                    } catch (e: ArithmeticException) {
                        if (e.message?.contains("Esponente") == true) throw e
                        val res = l.toDouble().pow(r.toDouble())
                        if (res.isNaN() || res.isInfinite()) throw ArithmeticException("Risultato non valido o indefinito")
                        BigDecimal(res, context.mathContext)
                    }
                }
                "ʸ√", "nroot", "root", "ˣ√", "ⁿ√" -> {
                    // l = index (y), r = radicand (x) -> y-th root of x
                    computeNthRoot(radicand = r, index = l, context = context)
                }
                else -> throw UnsupportedOperationException("Operatore non supportato: $operator")
            }
        }
    }

    data class UnaryOp(val operator: String, val operand: MathNode) : MathNode() {
        override fun evaluate(context: EvaluationContext): BigDecimal {
            val v = operand.evaluate(context)
            return when (operator) {
                "-", "neg" -> v.negate(context.mathContext)
                "+", "pos" -> v
                "%", "percent" -> {
                    // Percentage: e.g. 50% = 0.5
                    v.divide(BigDecimal(100), context.mathContext)
                }
                "ʸ√", "nroot", "root", "√" -> {
                    // If used as unary operator without index, defaults to square root
                    if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                    v.sqrt(context.mathContext)
                }
                "!" -> {
                    val n = v.toInt()
                    if (n < 0 || v.compareTo(BigDecimal(n)) != 0) {
                        throw ArithmeticException("Il fattoriale richiede un numero intero non negativo")
                    }
                    if (n > 1000) {
                        throw ArithmeticException("Argomento fattoriale troppo elevato (massimo 1000)")
                    }
                    var fact = BigDecimal.ONE
                    for (i in 2..n) {
                        fact = fact.multiply(BigDecimal(i), context.mathContext)
                    }
                    fact
                }
                else -> throw UnsupportedOperationException("Operatore unario non supportato: $operator")
            }
        }
    }

    data class FunctionCall(val name: String, val arguments: List<MathNode>) : MathNode() {
        override fun evaluate(context: EvaluationContext): BigDecimal {
            val evaluatedArgs = arguments.map { it.evaluate(context) }
            val mc = context.mathContext

            val customFn = context.customFunctions[name.lowercase()]
            if (customFn != null) {
                return customFn(evaluatedArgs, context)
            }

            return when (name.lowercase()) {
                "sin" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    if (!context.isRadians) {
                        val norm = ((angle % 360.0) + 360.0) % 360.0
                        if (norm == 0.0 || norm == 180.0) return BigDecimal.ZERO
                        if (norm == 90.0) return BigDecimal.ONE
                        if (norm == 270.0) return BigDecimal("-1")
                        if (norm == 30.0 || norm == 150.0) return BigDecimal("0.5")
                        if (norm == 210.0 || norm == 330.0) return BigDecimal("-0.5")
                    }
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val s = sin(rad)
                    BigDecimal(s).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "cos" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    if (!context.isRadians) {
                        val norm = ((angle % 360.0) + 360.0) % 360.0
                        if (norm == 90.0 || norm == 270.0) return BigDecimal.ZERO
                        if (norm == 0.0) return BigDecimal.ONE
                        if (norm == 180.0) return BigDecimal("-1")
                        if (norm == 60.0 || norm == 300.0) return BigDecimal("0.5")
                        if (norm == 120.0 || norm == 240.0) return BigDecimal("-0.5")
                    }
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val c = cos(rad)
                    BigDecimal(c).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "tan" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    if (!context.isRadians) {
                        val norm = ((angle % 360.0) + 360.0) % 360.0
                        if (norm == 90.0 || norm == 270.0) throw ArithmeticException("Tangente indefinita")
                        if (norm == 0.0 || norm == 180.0) return BigDecimal.ZERO
                        if (norm == 45.0 || norm == 225.0) return BigDecimal.ONE
                        if (norm == 135.0 || norm == 315.0) return BigDecimal("-1")
                    }
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val cosVal = cos(rad)
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Tangente indefinita")
                    val t = tan(rad)
                    BigDecimal(t).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "asin" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v < -1.0 || v > 1.0) throw ArithmeticException("Dominio asin non valido [-1, 1]")
                    val res = asin(v)
                    val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                    BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "acos" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v < -1.0 || v > 1.0) throw ArithmeticException("Dominio acos non valido [-1, 1]")
                    val res = acos(v)
                    val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                    BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "atan" -> {
                    val v = evaluatedArgs[0].toDouble()
                    val res = atan(v)
                    val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                    BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "sqrt", "√" -> {
                    val v = evaluatedArgs[0]
                    if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                    v.sqrt(mc)
                }
                "nroot" -> {
                    if (evaluatedArgs.size == 1) {
                        val v = evaluatedArgs[0]
                        if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                        v.sqrt(mc)
                    } else {
                        // nroot(index, radicand)
                        computeNthRoot(radicand = evaluatedArgs[1], index = evaluatedArgs[0], context = context)
                    }
                }
                "root" -> {
                    if (evaluatedArgs.size == 1) {
                        val v = evaluatedArgs[0]
                        if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                        v.sqrt(mc)
                    } else {
                        // root(radicand, index)
                        computeNthRoot(radicand = evaluatedArgs[0], index = evaluatedArgs[1], context = context)
                    }
                }
                "cbrt" -> {
                    computeNthRoot(radicand = evaluatedArgs[0], index = BigDecimal("3"), context = context)
                }
                "ln" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v <= 0.0) throw ArithmeticException("Argomento logaritmo <= 0")
                    BigDecimal(ln(v), mc)
                }
                "log", "log10" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v <= 0.0) throw ArithmeticException("Argomento logaritmo <= 0")
                    BigDecimal(log10(v), mc)
                }
                "log2" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v <= 0.0) throw ArithmeticException("Argomento logaritmo <= 0")
                    BigDecimal(ln(v) / ln(2.0), mc)
                }
                "exp" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(exp(v), mc)
                }
                "cot" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val s = sin(rad)
                    if (abs(s) < 1e-15) throw ArithmeticException("Cotangente indefinita")
                    val c = cos(rad)
                    BigDecimal(c / s).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "sec" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val c = cos(rad)
                    if (abs(c) < 1e-15) throw ArithmeticException("Secante indefinita")
                    BigDecimal(1.0 / c).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "csc" -> {
                    val angle = evaluatedArgs[0].toDouble()
                    val rad = if (context.isRadians) angle else Math.toRadians(angle)
                    val s = sin(rad)
                    if (abs(s) < 1e-15) throw ArithmeticException("Cosecante indefinita")
                    BigDecimal(1.0 / s).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "acot" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (abs(v) < 1e-15) {
                        val res = Math.PI / 2.0
                        val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                        BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                    } else {
                        val res = atan(1.0 / v)
                        val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                        BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                    }
                }
                "asec" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (abs(v) < 1.0) throw ArithmeticException("Dominio asec non valido (|x| >= 1)")
                    val res = acos(1.0 / v)
                    val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                    BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "acsc" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (abs(v) < 1.0) throw ArithmeticException("Dominio acsc non valido (|x| >= 1)")
                    val res = asin(1.0 / v)
                    val degOrRad = if (context.isRadians) res else Math.toDegrees(res)
                    BigDecimal(degOrRad).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "sinh" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(sinh(v), mc)
                }
                "cosh" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(cosh(v), mc)
                }
                "tanh" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(tanh(v), mc)
                }
                "coth" -> {
                    val v = evaluatedArgs[0].toDouble()
                    val s = sinh(v)
                    if (abs(s) < 1e-15) throw ArithmeticException("Cotangente iperbolica indefinita a 0")
                    BigDecimal(cosh(v) / s, mc)
                }
                "sech" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(1.0 / cosh(v), mc)
                }
                "csch" -> {
                    val v = evaluatedArgs[0].toDouble()
                    val s = sinh(v)
                    if (abs(s) < 1e-15) throw ArithmeticException("Cosecante iperbolica indefinita a 0")
                    BigDecimal(1.0 / s, mc)
                }
                "asinh" -> {
                    val x = evaluatedArgs[0].toDouble()
                    BigDecimal(ln(x + sqrt(x * x + 1.0)), mc)
                }
                "acosh" -> {
                    val x = evaluatedArgs[0].toDouble()
                    if (x < 1.0) throw ArithmeticException("Dominio acosh non valido (x >= 1)")
                    BigDecimal(ln(x + sqrt(x * x - 1.0)), mc)
                }
                "atanh" -> {
                    val x = evaluatedArgs[0].toDouble()
                    if (abs(x) >= 1.0) throw ArithmeticException("Dominio atanh non valido (|x| < 1)")
                    BigDecimal(0.5 * ln((1.0 + x) / (1.0 - x)), mc)
                }
                "acoth" -> {
                    val x = evaluatedArgs[0].toDouble()
                    if (abs(x) <= 1.0) throw ArithmeticException("Dominio acoth non valido (|x| > 1)")
                    BigDecimal(0.5 * ln((x + 1.0) / (x - 1.0)), mc)
                }
                "asech" -> {
                    val x = evaluatedArgs[0].toDouble()
                    if (x <= 0.0 || x > 1.0) throw ArithmeticException("Dominio asech non valido (0 < x <= 1)")
                    BigDecimal(ln((1.0 + sqrt(1.0 - x * x)) / x), mc)
                }
                "acsch" -> {
                    val x = evaluatedArgs[0].toDouble()
                    if (x == 0.0) throw ArithmeticException("Dominio acsch non valido (x != 0)")
                    BigDecimal(ln((1.0 / x) + sqrt((1.0 / (x * x)) + 1.0)), mc)
                }
                "sign" -> {
                    val v = evaluatedArgs[0]
                    BigDecimal(v.signum())
                }
                "lg" -> {
                    val v = evaluatedArgs[0].toDouble()
                    if (v <= 0.0) throw ArithmeticException("Argomento logaritmo <= 0")
                    BigDecimal(log10(v), mc)
                }
                "logn", "log_n" -> {
                    if (evaluatedArgs.size >= 2) {
                        val base = evaluatedArgs[0].toDouble()
                        val x = evaluatedArgs[1].toDouble()
                        if (base <= 0.0 || base == 1.0 || x <= 0.0) throw ArithmeticException("Dominio logaritmo non valido")
                        BigDecimal(ln(x) / ln(base), mc)
                    } else {
                        val v = evaluatedArgs[0].toDouble()
                        if (v <= 0.0) throw ArithmeticException("Argomento logaritmo <= 0")
                        BigDecimal(log10(v), mc)
                    }
                }
                "abs" -> evaluatedArgs[0].abs(mc)

                // ==========================================
                // COMBINATORICS & PROBABILITY
                // ==========================================
                "binom", "ncr", "comb" -> {
                    if (evaluatedArgs.size < 2) throw ArithmeticException("binom(n, k) richiede 2 argomenti")
                    val n = evaluatedArgs[0].toInt()
                    val k = evaluatedArgs[1].toInt()
                    if (k < 0 || k > n || n < 0) BigDecimal.ZERO
                    else {
                        var res = BigDecimal.ONE
                        val actualK = minOf(k, n - k)
                        for (i in 1..actualK) {
                            res = res.multiply(BigDecimal(n - i + 1)).divide(BigDecimal(i), mc)
                        }
                        res
                    }
                }
                "perm", "npr" -> {
                    if (evaluatedArgs.size < 2) throw ArithmeticException("perm(n, k) richiede 2 argomenti")
                    val n = evaluatedArgs[0].toInt()
                    val k = evaluatedArgs[1].toInt()
                    if (k < 0 || k > n || n < 0) BigDecimal.ZERO
                    else {
                        var res = BigDecimal.ONE
                        for (i in (n - k + 1)..n) {
                            res = res.multiply(BigDecimal(i))
                        }
                        res
                    }
                }
                "binompdf" -> {
                    // binompdf(n, p, k)
                    if (evaluatedArgs.size < 3) throw ArithmeticException("binompdf(n, p, k) richiede 3 argomenti")
                    val n = evaluatedArgs[0].toInt()
                    val p = evaluatedArgs[1].toDouble()
                    val k = evaluatedArgs[2].toInt()
                    if (k < 0 || k > n || p < 0.0 || p > 1.0) BigDecimal.ZERO
                    else {
                        var coeff = 1.0
                        val actualK = minOf(k, n - k)
                        for (i in 1..actualK) {
                            coeff = coeff * (n - i + 1) / i
                        }
                        val prob = coeff * p.pow(k.toDouble()) * (1.0 - p).pow((n - k).toDouble())
                        BigDecimal(prob).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                    }
                }
                "binomcdf" -> {
                    // binomcdf(n, p, k) -> P(X <= k)
                    if (evaluatedArgs.size < 3) throw ArithmeticException("binomcdf(n, p, k) richiede 3 argomenti")
                    val n = evaluatedArgs[0].toInt()
                    val p = evaluatedArgs[1].toDouble()
                    val maxK = evaluatedArgs[2].toInt().coerceAtMost(n)
                    if (maxK < 0 || p < 0.0 || p > 1.0) BigDecimal.ZERO
                    else {
                        var cumProb = 0.0
                        for (k in 0..maxK) {
                            var coeff = 1.0
                            val actualK = minOf(k, n - k)
                            for (i in 1..actualK) {
                                coeff = coeff * (n - i + 1) / i
                            }
                            cumProb += coeff * p.pow(k.toDouble()) * (1.0 - p).pow((n - k).toDouble())
                        }
                        BigDecimal(cumProb).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                    }
                }
                "normpdf" -> {
                    // normpdf(x) o normpdf(x, mu, sigma)
                    val x = evaluatedArgs[0].toDouble()
                    val mu = if (evaluatedArgs.size >= 2) evaluatedArgs[1].toDouble() else 0.0
                    val sigma = if (evaluatedArgs.size >= 3) evaluatedArgs[2].toDouble() else 1.0
                    if (sigma <= 0.0) throw ArithmeticException("sigma deve essere positivo")
                    val z = (x - mu) / sigma
                    val pdf = (1.0 / (sigma * sqrt(2.0 * Math.PI))) * exp(-0.5 * z * z)
                    BigDecimal(pdf).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "normcdf" -> {
                    // normcdf(x) o normcdf(x, mu, sigma)
                    val x = evaluatedArgs[0].toDouble()
                    val mu = if (evaluatedArgs.size >= 2) evaluatedArgs[1].toDouble() else 0.0
                    val sigma = if (evaluatedArgs.size >= 3) evaluatedArgs[2].toDouble() else 1.0
                    if (sigma <= 0.0) throw ArithmeticException("sigma deve essere positivo")
                    val z = (x - mu) / (sigma * sqrt(2.0))
                    val cdf = 0.5 * (1.0 + approxErf(z))
                    BigDecimal(cdf).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "mean" -> {
                    if (evaluatedArgs.isEmpty()) throw ArithmeticException("mean() richiede almeno un valore")
                    val sum = evaluatedArgs.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }
                    sum.divide(BigDecimal(evaluatedArgs.size), mc)
                }
                "var" -> {
                    if (evaluatedArgs.isEmpty()) throw ArithmeticException("var() richiede almeno un valore")
                    val n = evaluatedArgs.size
                    val mean = evaluatedArgs.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }.divide(BigDecimal(n), mc).toDouble()
                    val sumSq = evaluatedArgs.sumOf { (it.toDouble() - mean).pow(2) }
                    val divisor = if (n > 1) (n - 1).toDouble() else 1.0
                    BigDecimal(sumSq / divisor).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "std" -> {
                    if (evaluatedArgs.isEmpty()) throw ArithmeticException("std() richiede almeno un valore")
                    val n = evaluatedArgs.size
                    val mean = evaluatedArgs.fold(BigDecimal.ZERO) { acc, v -> acc.add(v) }.divide(BigDecimal(n), mc).toDouble()
                    val sumSq = evaluatedArgs.sumOf { (it.toDouble() - mean).pow(2) }
                    val divisor = if (n > 1) (n - 1).toDouble() else 1.0
                    BigDecimal(sqrt(sumSq / divisor)).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "median" -> {
                    if (evaluatedArgs.isEmpty()) throw ArithmeticException("median() richiede almeno un valore")
                    val sorted = evaluatedArgs.map { it.toDouble() }.sorted()
                    val n = sorted.size
                    val med = if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
                    BigDecimal(med).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
                }
                "rand" -> {
                    BigDecimal(Math.random()).setScale(8, RoundingMode.HALF_UP)
                }
                "randint" -> {
                    val min = if (evaluatedArgs.isNotEmpty()) evaluatedArgs[0].toInt() else 0
                    val max = if (evaluatedArgs.size >= 2) evaluatedArgs[1].toInt() else 100
                    val r = (min..max).random()
                    BigDecimal(r)
                }
                else -> throw UnsupportedOperationException("Funzione non supportata: $name")
            }
        }

        private fun approxErf(x: Double): Double {
            val sign = if (x < 0) -1.0 else 1.0
            val absX = abs(x)
            val t = 1.0 / (1.0 + 0.3275911 * absX)
            val a1 = 0.254829592
            val a2 = -0.284496736
            val a3 = 1.421413741
            val a4 = -1.453152027
            val a5 = 1.061405429
            val poly = ((((a5 * t + a4) * t + a3) * t + a2) * t + a1) * t
            val res = 1.0 - poly * exp(-absX * absX)
            return sign * res
        }
    }
}

/**
 * Evaluation context with mathematical precision and variables.
 */
data class EvaluationContext(
    val variables: Map<String, BigDecimal> = DEFAULT_VARIABLES,
    val isRadians: Boolean = true,
    val mathContext: MathContext = MathContext(34, RoundingMode.HALF_UP), // 34 decimal digits precision (128-bit IEEE 754r standard)
    val customFunctions: Map<String, (List<BigDecimal>, EvaluationContext) -> BigDecimal> = emptyMap()
) {
    companion object {
        val DEFAULT_VARIABLES: Map<String, BigDecimal> = mapOf(
            "pi" to BigDecimal("3.14159265358979323846264338327950288"),
            "π" to BigDecimal("3.14159265358979323846264338327950288"),
            "e" to BigDecimal("2.71828182845904523536028747135266249"),
            "phi" to BigDecimal("1.61803398874989484820458683436563811"),
            "φ" to BigDecimal("1.61803398874989484820458683436563811"),
            "c" to BigDecimal("299792458"),
            "g" to BigDecimal("9.80665"),
            "g_grav" to BigDecimal("6.67430e-11"),
            "h" to BigDecimal("6.62607015e-34"),
            "hbar" to BigDecimal("1.054571817e-34"),
            "ħ" to BigDecimal("1.054571817e-34"),
            "kb" to BigDecimal("1.380649e-23"),
            "na" to BigDecimal("6.02214076e23"),
            "r_gas" to BigDecimal("8.314462618"),
            "qe" to BigDecimal("1.602176634e-19"),
            "me" to BigDecimal("9.1093837015e-31"),
            "mp" to BigDecimal("1.67262192369e-27"),
            "eps0" to BigDecimal("8.8541878128e-12"),
            "eps_0" to BigDecimal("8.8541878128e-12"),
            "ε₀" to BigDecimal("8.8541878128e-12"),
            "mu0" to BigDecimal("1.25663706212e-6"),
            "mu_0" to BigDecimal("1.25663706212e-6"),
            "μ₀" to BigDecimal("1.25663706212e-6"),
            "G" to BigDecimal("6.67430e-11"),
            "R" to BigDecimal("8.314462618"),
            "u" to BigDecimal("1.66053906660e-27"),
            "sigma" to BigDecimal("5.670374419e-8"),
            "σ" to BigDecimal("5.670374419e-8")
        )
        val DEFAULT = EvaluationContext(variables = DEFAULT_VARIABLES)
    }
}
