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
                    // Se usato come operatore binario classico (modulo)
                    l.remainder(r, context.mathContext)
                }
                "^", "pow" -> {
                    // Se l'esponente è intero usiamo pow di BigDecimal per massima precisione, altrimenti Double
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
                    // l = indice (y), r = radicando (x) -> radice y-esima di x
                    val index = l.toDouble()
                    val radicand = r.toDouble()
                    if (index == 0.0) throw ArithmeticException("Indice della radice non può essere zero")
                    if (radicand < 0.0 && (index % 2.0 == 0.0)) throw ArithmeticException("Radice di indice pari con radicando negativo")
                    val res = if (radicand < 0.0) -kotlin.math.abs(radicand).pow(1.0 / index) else radicand.pow(1.0 / index)
                    if (res.isNaN() || res.isInfinite()) throw ArithmeticException("Risultato radice non valido")
                    BigDecimal(res, context.mathContext)
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
                    // Percentuale: es. 50% = 0.5
                    v.divide(BigDecimal(100), context.mathContext)
                }
                "ʸ√", "nroot", "root", "√" -> {
                    // Se usato come operatore unario senza indice, default radice quadrata
                    if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                    BigDecimal(sqrt(v.toDouble()), context.mathContext)
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
                    BigDecimal(sqrt(v.toDouble()), mc)
                }
                "root", "nroot" -> {
                    if (evaluatedArgs.size == 1) {
                        val v = evaluatedArgs[0]
                        if (v.signum() < 0) throw ArithmeticException("Radice quadrata di numero negativo")
                        BigDecimal(sqrt(v.toDouble()), mc)
                    } else {
                        val x = evaluatedArgs[0].toDouble()
                        val n = evaluatedArgs[1].toDouble()
                        if (n == 0.0) throw ArithmeticException("Indice della radice non può essere zero")
                        if (x < 0 && (n % 2.0 == 0.0)) throw ArithmeticException("Radice di indice pari con radicando negativo")
                        val res = if (x < 0) -kotlin.math.abs(x).pow(1.0 / n) else x.pow(1.0 / n)
                        if (res.isNaN() || res.isInfinite()) throw ArithmeticException("Risultato radice non valido")
                        BigDecimal(res, mc)
                    }
                }
                "cbrt" -> {
                    val v = evaluatedArgs[0].toDouble()
                    BigDecimal(cbrt(v), mc)
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
                else -> throw UnsupportedOperationException("Funzione non supportata: $name")
            }
        }
    }
}

/**
 * Contesto di valutazione con precisione matematica e variabili.
 */
data class EvaluationContext(
    val variables: Map<String, BigDecimal> = DEFAULT_VARIABLES,
    val isRadians: Boolean = true,
    val mathContext: MathContext = MathContext(34, RoundingMode.HALF_UP) // Precisione 34 cifre decimali (standard IEEE 754r a 128 bit)
) {
    companion object {
        val DEFAULT_VARIABLES: Map<String, BigDecimal> = mapOf(
            "pi" to BigDecimal("3.14159265358979323846264338327950288"),
            "π" to BigDecimal("3.14159265358979323846264338327950288"),
            "e" to BigDecimal("2.71828182845904523536028747135266249"),
            "phi" to BigDecimal("1.61803398874989484820458683436563811")
        )
        val DEFAULT = EvaluationContext(variables = DEFAULT_VARIABLES)
    }
}
