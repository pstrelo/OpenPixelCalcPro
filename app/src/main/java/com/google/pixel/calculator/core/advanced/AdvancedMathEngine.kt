package com.google.pixel.calculator.core.advanced

import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.ExpressionParser
import com.google.pixel.calculator.core.math.MathNode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * Motore matematico ad altissima efficienza per la Calcolatrice Avanzata.
 * Esegue calcolo numerico e simbolico per:
 * - Valutazione di espressioni standard e multi-riga
 * - Ricerca radici ed equazioni f(x) = 0 o f(x) = g(x) (Brent-Dekker)
 * - Risoluzione di disequazioni con intervalli (es. x ∈ [-2, 3))
 * - Calcolo di derivata numerica puntuale f'(x₀) con stencil a 5 punti
 * - Integrale definito con quadratura adattiva di Simpson
 * - Limiti numerici lim(x -> x₀)
 * - Campionamento ad alte prestazioni per grafici 2D e rilevamento zeri/intercette
 */
class AdvancedMathEngine(
    private val parser: ExpressionParser = ExpressionParser()
) {
    private val defaultMc = MathContext(18, RoundingMode.HALF_UP)

    data class Point(val x: Float, val y: Float)

    data class ShadedInterval(val from: Float, val to: Float)

    enum class ResultFormat(val label: String, val description: String) {
        DECIMAL("DEC", "Decimale standard"),
        FRACTION("FRAC", "Frazione esatta S<=>D"),
        ENGINEERING("ENG", "Notazione ingegneristica"),
        DMS("DMS", "Gradi, primi, secondi"),
        POLAR("POL", "Coordinate polari r∠θ")
    }

    sealed class SolveResult {
        data class SingleValue(val formatted: String, val raw: Double) : SolveResult()
        data class EquationRoots(val roots: List<Double>, val formattedText: String) : SolveResult()
        data class InequalityIntervals(val text: String, val intervals: List<ShadedInterval>) : SolveResult()
        data class SystemSolution(val solutions: Map<String, Double>, val formattedText: String) : SolveResult()
        data class Error(val message: String) : SolveResult()

        fun formatAs(format: ResultFormat, isRadians: Boolean = true, engine: AdvancedMathEngine): String {
            return when (this) {
                is SingleValue -> engine.formatNumber(raw, format, isRadians)
                is EquationRoots -> {
                    if (roots.isEmpty()) formattedText
                    else roots.mapIndexed { idx, r ->
                        val formattedRoot = engine.formatNumber(r, format, isRadians)
                        if (roots.size == 1) "x = $formattedRoot"
                        else "x${idx + 1} = $formattedRoot"
                    }.joinToString(", ")
                }
                is InequalityIntervals -> text
                is SystemSolution -> formattedText
                is Error -> message
            }
        }
    }

    // ==========================================
    // 1. VALUTAZIONE GENERALE DI UN'ESPRESSIONE
    // ==========================================

    fun autoCloseParentheses(expression: String): String {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return ""
        val open = trimmed.count { it == '(' }
        val close = trimmed.count { it == ')' }
        return if (open > close) {
            val last = trimmed.last()
            if (last == '(' || last in "+-×÷/*^,ʸ√") trimmed else trimmed + ")".repeat(open - close)
        } else trimmed
    }

    fun evaluateExpression(expr: String, isRadians: Boolean = true): String {
        val clean = autoCloseParentheses(expr)
        if (clean.isBlank()) return ""
        return try {
            val ast = parser.parse(clean)
            val ctx = EvaluationContext(
                isRadians = isRadians,
                mathContext = defaultMc
            )
            val res = ast.evaluate(ctx)
            formatNumber(res.toDouble())
        } catch (e: Exception) {
            "Errore: ${e.message ?: "sintassi non valida"}"
        }
    }

    // ==========================================
    // 2. DISPATCHER RISOLUTORE AUTOMATICO
    // ==========================================

    fun solve(input: String, isRadians: Boolean = true): SolveResult {
        val clean = autoCloseParentheses(input).trim()
        if (clean.isBlank()) return SolveResult.Error("Espressione vuota")

        // 1. Derivata: d/dx o f'(x) o diff
        if (clean.startsWith("d/dx", ignoreCase = true) || clean.startsWith("diff", ignoreCase = true)) {
            return solveDerivative(clean, isRadians)
        }

        // 2. Integrale: ∫ o integral
        if (clean.contains("∫") || clean.startsWith("integral", ignoreCase = true)) {
            return solveIntegral(clean, isRadians)
        }

        // 3. Limite: lim
        if (clean.startsWith("lim", ignoreCase = true)) {
            return solveLimit(clean, isRadians)
        }

        // 4. Disequazione: <=, >=, <, >
        val ineqOps = listOf("<=", ">=", "≤", "≥", "<", ">")
        val foundIneq = ineqOps.firstOrNull { clean.contains(it) }
        if (foundIneq != null) {
            return solveInequality(clean, foundIneq, isRadians)
        }

        // 5. Equazione con '='
        if (clean.contains("=")) {
            return solveEquation(clean, isRadians)
        }

        // 6. Espressione con incognita x non azzerata (es. "x^2 - 4" -> trova zeri f(x)=0)
        if (clean.contains("x", ignoreCase = true)) {
            return findRootsOfFunction(clean, isRadians)
        }

        // 7. Semplice calcolo numerico
        return try {
            val ast = parser.parse(clean)
            val ctx = EvaluationContext(isRadians = isRadians, mathContext = defaultMc)
            val num = ast.evaluate(ctx).toDouble()
            SolveResult.SingleValue(formatNumber(num), num)
        } catch (e: Exception) {
            SolveResult.Error(e.message ?: "Calcolo non valido")
        }
    }

    // ==========================================
    // 3. RISOLUTORE EQUAZIONI: f(x) = 0 o f(x) = g(x)
    // ==========================================

    fun solveEquation(expr: String, isRadians: Boolean = true): SolveResult {
        val parts = expr.split("=")
        if (parts.size != 2) return SolveResult.Error("Formato equazione non valido: usare lhs = rhs")
        val lhs = parts[0].trim()
        val rhs = parts[1].trim()

        val diffExpr = if (rhs == "0" || rhs.isEmpty()) lhs else "($lhs) - ($rhs)"
        return findRootsOfFunction(diffExpr, isRadians)
    }

    fun findRootsOfFunction(functionExpr: String, isRadians: Boolean = true): SolveResult {
        val ast = try {
            parser.parse(functionExpr)
        } catch (e: Exception) {
            return SolveResult.Error("Errore sintassi funzione: ${e.message}")
        }

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                val v = ast.evaluate(ctx).toDouble()
                if (v.isNaN() || v.isInfinite()) Double.NaN else v
            } catch (e: Exception) {
                Double.NaN
            }
        }

        val foundRoots = mutableListOf<Double>()
        val xMin = -50.0
        val xMax = 50.0
        val steps = 1000
        val dx = (xMax - xMin) / steps

        var prevX = xMin
        var prevY = f(prevX)

        for (i in 1..steps) {
            val curX = xMin + i * dx
            val curY = f(curX)

            if (!prevY.isNaN() && !curY.isNaN()) {
                if (abs(prevY) < 1e-10) {
                    addUniqueRoot(foundRoots, prevX)
                } else if (prevY * curY < 0.0) {
                    // Cambio di segno: esegui Brent-Dekker / bisezione rapida
                    val root = brentRootFind(::f, prevX, curX, 1e-8, 100)
                    if (root != null && abs(f(root)) < 1e-4) {
                        addUniqueRoot(foundRoots, root)
                    }
                }
            }
            prevX = curX
            prevY = curY
        }

        return if (foundRoots.isEmpty()) {
            SolveResult.Error("Nessuna radice reale trovata nell'intervallo [-50, 50]")
        } else {
            val formatted = foundRoots.mapIndexed { idx, r ->
                if (foundRoots.size == 1) "x = ${formatNumber(r)}"
                else "x${idx + 1} = ${formatNumber(r)}"
            }.joinToString(", ")
            SolveResult.EquationRoots(foundRoots, formatted)
        }
    }

    private fun addUniqueRoot(list: MutableList<Double>, r: Double) {
        val rounded = (r * 1e7).roundToLong() / 1e7
        if (list.none { abs(it - rounded) < 1e-4 }) {
            list.add(rounded)
        }
    }

    /**
     * Metodo di Brent per la ricerca radici super veloce (superlineare).
     */
    private fun brentRootFind(
        f: (Double) -> Double,
        x1: Double,
        x2: Double,
        tol: Double,
        maxIter: Int
    ): Double? {
        var a = x1
        var b = x2
        var fa = f(a)
        var fb = f(b)

        if (fa * fb > 0.0) return null
        if (abs(fa) < abs(fb)) {
            val t = a; a = b; b = t
            val ft = fa; fa = fb; fb = ft
        }

        var c = a
        var fc = fa
        var mflag = true
        var s = b
        var d = 0.0

        for (iter in 0 until maxIter) {
            if (abs(fb) < tol) return b
            if (abs(b - a) < tol) return b

            if (abs(fa - fc) > 1e-14 && abs(fb - fc) > 1e-14) {
                // Inverse quadratic interpolation
                s = (a * fb * fc) / ((fa - fb) * (fa - fc)) +
                        (b * fa * fc) / ((fb - fa) * (fb - fc)) +
                        (c * fa * fb) / ((fc - fa) * (fc - fb))
            } else {
                // Secant method
                s = b - fb * (b - a) / (fb - fa)
            }

            val cond1 = (s < min((3 * a + b) / 4.0, b) || s > max((3 * a + b) / 4.0, b))
            val cond2 = mflag && (abs(s - b) >= abs(b - c) / 2.0)
            val cond3 = !mflag && (abs(s - b) >= abs(c - d) / 2.0)
            val cond4 = mflag && (abs(b - c) < tol)
            val cond5 = !mflag && (abs(c - d) < tol)

            if (cond1 || cond2 || cond3 || cond4 || cond5) {
                s = (a + b) / 2.0
                mflag = true
            } else {
                mflag = false
            }

            val fs = f(s)
            d = c
            c = b
            fc = fb

            if (fa * fs < 0.0) {
                b = s
                fb = fs
            } else {
                a = s
                fa = fs
            }

            if (abs(fa) < abs(fb)) {
                val t = a; a = b; b = t
                val ft = fa; fa = fb; fb = ft
            }
        }
        return b
    }

    // ==========================================
    // 4. RISOLUTORE DISEQUAZIONI
    // ==========================================

    fun solveInequality(expr: String, op: String, isRadians: Boolean = true): SolveResult {
        val parts = expr.split(op)
        if (parts.size != 2) return SolveResult.Error("Formato disequazione non valido")
        val lhs = parts[0].trim()
        val rhs = parts[1].trim()

        val diffExpr = if (rhs == "0" || rhs.isEmpty()) lhs else "($lhs) - ($rhs)"
        val ast = try { parser.parse(diffExpr) } catch (e: Exception) {
            return SolveResult.Error("Sintassi non valida: ${e.message}")
        }

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                Double.NaN
            }
        }

        // Troviamo i punti critici (zeri)
        val rootsResult = findRootsOfFunction(diffExpr, isRadians)
        val criticalPoints = when (rootsResult) {
            is SolveResult.EquationRoots -> rootsResult.roots.sorted()
            else -> emptyList()
        }

        // Partizioniamo la retta reale [-50, 50]
        val intervals = mutableListOf<ShadedInterval>()
        val textIntervals = mutableListOf<String>()

        val testBounds = mutableListOf<Double>()
        testBounds.add(-50.0)
        testBounds.addAll(criticalPoints)
        testBounds.add(50.0)

        fun satisfies(y: Double): Boolean {
            if (y.isNaN()) return false
            return when (op) {
                ">" -> y > 1e-7
                ">=", "≥" -> y >= -1e-7
                "<" -> y < -1e-7
                "<=", "≤" -> y <= 1e-7
                else -> false
            }
        }

        for (i in 0 until testBounds.size - 1) {
            val left = testBounds[i]
            val right = testBounds[i + 1]
            val mid = (left + right) / 2.0
            val yMid = f(mid)

            if (satisfies(yMid)) {
                intervals.add(ShadedInterval(left.toFloat(), right.toFloat()))
                val lStr = if (left == -50.0) "-∞" else formatNumber(left)
                val rStr = if (right == 50.0) "+∞" else formatNumber(right)
                val lBracket = if (left == -50.0) "(" else if (op in listOf(">=", "≤", "<=", "≥")) "[" else "("
                val rBracket = if (right == 50.0) ")" else if (op in listOf(">=", "≤", "<=", "≥")) "]" else ")"
                textIntervals.add("$lBracket$lStr, $rStr$rBracket")
            }
        }

        val text = if (textIntervals.isEmpty()) {
            "Nessuna soluzione reale (impossibile)"
        } else {
            "x ∈ " + textIntervals.joinToString(" ∪ ")
        }

        return SolveResult.InequalityIntervals(text, intervals)
    }

    // ==========================================
    // 5. DERIVATA NUMERICA PUNTUALE
    // ==========================================

    fun solveDerivative(expr: String, isRadians: Boolean = true): SolveResult {
        // Formato atteso: d/dx(f(x), x0) oppure d/dx(f(x)) con default x0=0 o punto dato
        val content = expr.substringAfter("(").substringBeforeLast(")")
        val parts = content.split(",")
        val funcStr = parts[0].trim()
        val x0 = if (parts.size > 1) parts[1].trim().toDoubleOrNull() ?: 0.0 else 0.0

        val ast = try { parser.parse(funcStr) } catch (e: Exception) {
            return SolveResult.Error("Funzione non valida: ${e.message}")
        }

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                Double.NaN
            }
        }

        val fAtX0 = f(x0)
        if (fAtX0.isNaN() || fAtX0.isInfinite()) {
            return SolveResult.Error("Derivata non definita in x = $x0")
        }

        // Stencil a 5 punti: f'(x) ≈ (-f(x+2h) + 8f(x+h) - 8f(x-h) + f(x-2h)) / 12h
        val h = 1e-5
        val deriv = (-f(x0 + 2 * h) + 8 * f(x0 + h) - 8 * f(x0 - h) + f(x0 - 2 * h)) / (12 * h)

        if (deriv.isNaN() || deriv.isInfinite()) {
            return SolveResult.Error("Derivata non definita in x = $x0")
        }

        val formatted = "f'($x0) = ${formatNumber(deriv)}"
        return SolveResult.SingleValue(formatted, deriv)
    }

    // ==========================================
    // 6. INTEGRALE DEFINITO (QUADRATURA SIMPSON)
    // ==========================================

    fun solveIntegral(expr: String, isRadians: Boolean = true): SolveResult {
        // Formato atteso: ∫(f(x), a, b)
        val content = expr.substringAfter("(").substringBeforeLast(")")
        val parts = content.split(",")
        if (parts.size < 3) {
            return SolveResult.Error("Formato integrale: ∫(f(x), da, a). Es: ∫(x^2, 0, 3)")
        }
        val funcStr = parts[0].trim()
        val a = parts[1].trim().toDoubleOrNull() ?: return SolveResult.Error("Estremo inferiore 'a' non valido")
        val b = parts[2].trim().toDoubleOrNull() ?: return SolveResult.Error("Estremo superiore 'b' non valido")

        val ast = try { parser.parse(funcStr) } catch (e: Exception) {
            return SolveResult.Error("Funzione non valida: ${e.message}")
        }

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                0.0
            }
        }

        val area = adaptiveSimpson(::f, a, b, 1e-6, 15)
        val formatted = "Area = ${formatNumber(area)}"
        return SolveResult.SingleValue(formatted, area)
    }

    private fun adaptiveSimpson(
        f: (Double) -> Double,
        a: Double,
        b: Double,
        eps: Double,
        maxRecursion: Int
    ): Double {
        val c = (a + b) / 2.0
        val fa = f(a); val fb = f(b); val fc = f(c)
        val s = (b - a) / 6.0 * (fa + 4 * fc + fb)
        return adaptiveSimpsonRec(f, a, b, eps, s, fa, fb, fc, maxRecursion)
    }

    private fun adaptiveSimpsonRec(
        f: (Double) -> Double,
        a: Double,
        b: Double,
        eps: Double,
        s: Double,
        fa: Double,
        fb: Double,
        fc: Double,
        depth: Int
    ): Double {
        val c = (a + b) / 2.0
        val d = (a + c) / 2.0
        val e = (c + b) / 2.0
        val fd = f(d)
        val fe = f(e)
        val sLeft = (c - a) / 6.0 * (fa + 4 * fd + fc)
        val sRight = (b - c) / 6.0 * (fc + 4 * fe + fb)
        val s2 = sLeft + sRight
        if (depth <= 0 || abs(s2 - s) <= 15 * eps) {
            return s2 + (s2 - s) / 15.0
        }
        return adaptiveSimpsonRec(f, a, c, eps / 2.0, sLeft, fa, fc, fd, depth - 1) +
                adaptiveSimpsonRec(f, c, b, eps / 2.0, sRight, fc, fb, fe, depth - 1)
    }

    // ==========================================
    // 7. LIMITE NUMERICO
    // ==========================================

    fun solveLimit(expr: String, isRadians: Boolean = true): SolveResult {
        // Formato: lim(f(x), x0)
        val content = expr.substringAfter("(").substringBeforeLast(")")
        val parts = content.split(",")
        val funcStr = parts[0].trim()
        val x0 = if (parts.size > 1) parts[1].trim().toDoubleOrNull() ?: 0.0 else 0.0

        val ast = try { parser.parse(funcStr) } catch (e: Exception) {
            return SolveResult.Error("Funzione non valida: ${e.message}")
        }

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                Double.NaN
            }
        }

        // Valuta lim sinistro e destro
        val eps = 1e-7
        val left = f(x0 - eps)
        val right = f(x0 + eps)

        if (left.isNaN() || right.isNaN()) {
            return SolveResult.Error("Limite indefinito o fuori dal dominio")
        }

        val lim = (left + right) / 2.0
        return SolveResult.SingleValue("lim(x→$x0) = ${formatNumber(lim)}", lim)
    }

    // ==========================================
    // 8. CAMPIONAMENTO AD ALTE PRESTAZIONI PER IL GRAFICO
    // ==========================================

    fun sampleFunction(
        expr: String,
        xMin: Float,
        xMax: Float,
        steps: Int = 300,
        isRadians: Boolean = true
    ): List<Point?> {
        val clean = autoCloseParentheses(expr)
        if (clean.isBlank()) return emptyList()

        val ast = try { parser.parse(clean) } catch (e: Exception) { return emptyList() }
        val points = mutableListOf<Point?>()
        val stepSize = (xMax - xMin) / steps

        for (i in 0..steps) {
            val x = xMin + i * stepSize
            try {
                val ctx = EvaluationContext(
                    variables = mapOf("x" to BigDecimal(x.toDouble().toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                val yVal = ast.evaluate(ctx).toDouble()
                if (yVal.isFinite() && !yVal.isNaN() && abs(yVal) < 1e5) {
                    points.add(Point(x, yVal.toFloat()))
                } else {
                    points.add(null) // Discontinuità o asintoto verticale
                }
            } catch (e: Exception) {
                points.add(null)
            }
        }
        return points
    }

    fun formatNumber(value: Double, format: ResultFormat = ResultFormat.DECIMAL, isRadians: Boolean = true): String {
        return when (format) {
            ResultFormat.DECIMAL -> toDecimal(value)
            ResultFormat.FRACTION -> toFraction(value)
            ResultFormat.ENGINEERING -> toEngineering(value)
            ResultFormat.DMS -> toDms(value)
            ResultFormat.POLAR -> toPolar(value, isRadians)
        }
    }

    fun toDecimal(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "+∞" else "-∞"
        if (abs(value) < 1e-12) return "0"
        val rounded = (value * 1e6).roundToLong() / 1e6
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    /**
     * Sviluppo in frazione continua per trovare la frazione esatta p/q ridotta ai minimi termini (es. 0.375 -> 3/8, 0.33333333 -> 1/3, 2.5 -> 5/2)
     */
    fun toFraction(value: Double, tolerance: Double = 1e-7, maxDenominator: Long = 1000000L): String {
        if (value.isNaN() || value.isInfinite()) return toDecimal(value)
        val sign = if (value < 0) "-" else ""
        val x = abs(value)
        val intPart = x.toLong()
        val fracPart = x - intPart
        if (fracPart < tolerance) return "$sign$intPart"

        var h1 = 1L; var h2 = 0L
        var k1 = 0L; var k2 = 1L
        var b = fracPart

        do {
            val a = b.toLong()
            var aux = h1
            h1 = a * h1 + h2
            h2 = aux
            aux = k1
            k1 = a * k1 + k2
            k2 = aux
            val diff = b - a
            if (abs(diff) < 1e-12) break
            b = 1.0 / diff
        } while (abs(fracPart - h1.toDouble() / k1.toDouble()) > fracPart * tolerance && k1 < maxDenominator && b.isFinite())

        val num = intPart * k1 + h1
        val den = k1
        return if (den == 1L) "$sign$num" else "$sign$num/$den"
    }

    /**
     * Notazione ingegneristica con esponenti multipli di 3 (kilo, mega, micro, nano, ecc.)
     */
    fun toEngineering(value: Double): String {
        if (value == 0.0) return "0"
        if (value.isNaN() || value.isInfinite()) return toDecimal(value)
        val sign = if (value < 0) "-" else ""
        val x = abs(value)
        val exp = floor(log10(x)).toInt()
        val engExp = if (exp >= 0) (exp / 3) * 3 else ((exp - 2) / 3) * 3
        val mantissa = x / 10.0.pow(engExp)
        var finalExp = engExp
        var roundedMantissa = (mantissa * 1e4).roundToLong() / 1e4
        if (roundedMantissa >= 1000.0) {
            roundedMantissa /= 1000.0
            finalExp += 3
        }
        val mantissaStr = if (roundedMantissa == roundedMantissa.toLong().toDouble()) {
            roundedMantissa.toLong().toString()
        } else {
            roundedMantissa.toString()
        }
        return if (finalExp == 0) "$sign$mantissaStr" else "$sign$mantissaStr × 10^$finalExp"
    }

    /**
     * Sessagesimale: Gradi, Primi e Secondi (DMS)
     */
    fun toDms(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return toDecimal(value)
        val sign = if (value < 0) "-" else ""
        val x = abs(value)
        val degrees = floor(x).toLong()
        val remMinutes = (x - degrees) * 60.0
        val minutes = floor(remMinutes).toLong()
        val seconds = ((remMinutes - minutes) * 60.0 * 10.0).roundToLong() / 10.0
        return "$sign${degrees}° ${minutes}' ${seconds}\""
    }

    /**
     * Coordinate polari: r ∠ θ
     */
    fun toPolar(value: Double, isRadians: Boolean): String {
        if (value.isNaN() || value.isInfinite()) return toDecimal(value)
        val r = abs(value)
        val rStr = toDecimal(r)
        val thetaStr = if (value >= 0) {
            if (isRadians) "0 rad" else "0°"
        } else {
            if (isRadians) "π rad" else "180°"
        }
        return "$rStr ∠ $thetaStr"
    }
}
