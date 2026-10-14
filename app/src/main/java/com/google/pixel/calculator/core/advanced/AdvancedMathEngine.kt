package com.google.pixel.calculator.core.advanced

import com.google.pixel.calculator.core.math.EvaluationContext
import com.google.pixel.calculator.core.math.ExpressionParser
import com.google.pixel.calculator.core.math.MathNode
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

/**
 * High-performance mathematical engine for the Advanced Calculator.
 * Performs numerical and symbolic computation for:
 * - Evaluation of standard and multi-line expressions
 * - Root and equation finding f(x) = 0 or f(x) = g(x) (Brent-Dekker)
 * - Inequality solving with intervals (e.g. x ∈ [-2, 3))
 * - Point numerical derivative computation f'(x₀) with 5-point stencil
 * - Definite integral with Simpson adaptive quadrature
 * - Numerical limits lim(x -> x₀)
 * - High-performance sampling for 2D graphs and zero/intercept detection
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
        data class Inequality2D(
            val text: String,
            val boundaryExpr: String,
            val op: String,
            val isAbove: Boolean,
            val isStrict: Boolean
        ) : SolveResult()
        data class SystemSolution(val solutions: Map<String, Double>, val formattedText: String) : SolveResult()
        data class VariableAssignment(val variableName: String, val value: Double, val formattedText: String) : SolveResult()
        data class Error(val message: String) : SolveResult()

        fun formatAs(format: ResultFormat, isRadians: Boolean = true, engine: AdvancedMathEngine): String {
            return when (this) {
                is SingleValue -> engine.formatNumber(raw, format, isRadians)
                is VariableAssignment -> {
                    val formattedVal = engine.formatNumber(value, format, isRadians)
                    "$variableName = $formattedVal"
                }
                is EquationRoots -> {
                    if (roots.isEmpty()) formattedText
                    else roots.mapIndexed { idx, r ->
                        val formattedRoot = engine.formatNumber(r, format, isRadians)
                        if (roots.size == 1) "x = $formattedRoot"
                        else "x${idx + 1} = $formattedRoot"
                    }.joinToString(", ")
                }
                is InequalityIntervals -> text
                is Inequality2D -> text
                is SystemSolution -> formattedText
                is Error -> message
            }
        }
    }

    // ==========================================
    // 1. GENERAL EXPRESSION EVALUATION
    // ==========================================

    fun isVariableAssignment(expr: String): Boolean {
        val clean = expr.trim()
        if (!clean.contains("=")) return false
        val parts = clean.split("=")
        if (parts.size != 2) return false
        val lhs = parts[0].trim()
        val isSimpleVar = Regex("^[a-zA-Zα-ωΑ-Ω][a-zA-Z0-9_]*$").matches(lhs)
        val isFunctionLhs = lhs.equals("x", ignoreCase = true) || 
                            lhs.equals("y", ignoreCase = true) || 
                            Regex("^y[0-9]*$", RegexOption.IGNORE_CASE).matches(lhs) ||
                            Regex("^[fghpqruwv](\\([a-zA-Z]\\))?$", RegexOption.IGNORE_CASE).matches(lhs)
        return isSimpleVar && !isFunctionLhs
    }

    fun isFunctionDefinition(expr: String): Boolean {
        val clean = expr.trim()
        if (!clean.contains("=")) return false
        val parts = clean.split("=")
        if (parts.size != 2) return false
        val lhs = parts[0].trim()
        return lhs.equals("y", ignoreCase = true) || 
               Regex("^y[0-9]*$", RegexOption.IGNORE_CASE).matches(lhs) ||
               Regex("^[fghpqruwv](\\([a-zA-Z]\\))?$", RegexOption.IGNORE_CASE).matches(lhs)
    }

    fun stripFunctionDefinition(expr: String): String {
        val clean = expr.trim()
        return if (isFunctionDefinition(clean)) clean.substringAfter("=").trim() else clean
    }

    fun autoCloseParentheses(expression: String): String {
        var trimmed = expression.trim()
        if (trimmed.isEmpty()) return ""
        val barCount = trimmed.count { it == '|' }
        if (barCount % 2 != 0) {
            val last = trimmed.last()
            if (last != '|') {
                trimmed += "|"
            }
        }
        val open = trimmed.count { it == '(' }
        val close = trimmed.count { it == ')' }
        return if (open > close) {
            val last = trimmed.last()
            if (last == '(' || last in "+-×÷/*^,ʸ√") trimmed else trimmed + ")".repeat(open - close)
        } else trimmed
    }

    fun evaluateExpression(
        expr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): String {
        val clean = autoCloseParentheses(expr)
        if (clean.isBlank()) return ""
        return try {
            val ast = parser.parse(clean)
            val ctx = EvaluationContext(
                variables = EvaluationContext.DEFAULT_VARIABLES + customVariables,
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
    // 2. AUTOMATIC SOLVER DISPATCHER
    // ==========================================

    fun solve(
        input: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap(),
        customFunctions: Map<String, (List<BigDecimal>, EvaluationContext) -> BigDecimal> = emptyMap()
    ): SolveResult {
        val clean = autoCloseParentheses(input).trim()
        if (clean.isBlank()) return SolveResult.Error("Espressione vuota")

        // 1. Derivative: d/dx or f'(x) or diff
        if (clean.startsWith("d/dx", ignoreCase = true) || clean.startsWith("diff", ignoreCase = true)) {
            return solveDerivative(clean, isRadians, customVariables)
        }

        // 2. Integral: ∫ or integral
        if (clean.contains("∫") || clean.startsWith("integral", ignoreCase = true)) {
            return solveIntegral(clean, isRadians, customVariables)
        }

        // 3. Limit: lim
        if (clean.startsWith("lim", ignoreCase = true)) {
            return solveLimit(clean, isRadians, customVariables)
        }

        // 4. Inequality: <=, >=, <, >
        val ineqOps = listOf("<=", ">=", "≤", "≥", "<", ">")
        val foundIneq = ineqOps.firstOrNull { clean.contains(it) }
        if (foundIneq != null) {
            return solveInequality(clean, foundIneq, isRadians, customVariables)
        }

        // 5. Constant Assignment or Equation with '='
        if (clean.contains("=")) {
            val parts = clean.split("=")
            if (parts.size == 2 && isVariableAssignment(clean)) {
                val lhs = parts[0].trim()
                val rhs = parts[1].trim()
                return try {
                    val ast = parser.parse(rhs)
                    val ctx = EvaluationContext(
                        variables = EvaluationContext.DEFAULT_VARIABLES + customVariables,
                        isRadians = isRadians,
                        mathContext = defaultMc,
                        customFunctions = customFunctions
                    )
                    val resNum = ast.evaluate(ctx).toDouble()
                    val formatted = "$lhs = ${formatNumber(resNum)}"
                    SolveResult.VariableAssignment(lhs, resNum, formatted)
                } catch (e: Exception) {
                    SolveResult.Error("Impossibile definire $lhs: ${e.message ?: "sintassi non valida"}")
                }
            }
            return solveEquation(clean, isRadians, customVariables, customFunctions)
        }

        // 6. AST Parsing and variable 'x' presence check
        val ast = try {
            parser.parse(clean)
        } catch (e: Exception) {
            return SolveResult.Error(e.message ?: "Sintassi non valida")
        }

        if (hasVariableInAst(ast, "x")) {
            return findRootsOfFunction(clean, isRadians, customVariables, customFunctions)
        }

        // 7. Simple numeric calculation
        return try {
            val ctx = EvaluationContext(
                variables = EvaluationContext.DEFAULT_VARIABLES + customVariables,
                isRadians = isRadians,
                mathContext = defaultMc,
                customFunctions = customFunctions
            )
            val num = ast.evaluate(ctx).toDouble()
            SolveResult.SingleValue(formatNumber(num), num)
        } catch (e: Exception) {
            SolveResult.Error(e.message ?: "Calcolo non valido")
        }
    }

    private fun hasVariableInAst(node: MathNode, varName: String = "x"): Boolean {
        return when (node) {
            is MathNode.Variable -> node.name.equals(varName, ignoreCase = true)
            is MathNode.BinaryOp -> hasVariableInAst(node.left, varName) || hasVariableInAst(node.right, varName)
            is MathNode.UnaryOp -> hasVariableInAst(node.operand, varName)
            is MathNode.Fraction -> hasVariableInAst(node.numerator, varName) || hasVariableInAst(node.denominator, varName)
            is MathNode.FunctionCall -> node.arguments.any { hasVariableInAst(it, varName) }
            else -> false
        }
    }

    // ==========================================
    // 3. EQUATION SOLVER: f(x) = 0 or f(x) = g(x)
    // ==========================================

    fun solveEquation(
        expr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap(),
        customFunctions: Map<String, (List<BigDecimal>, EvaluationContext) -> BigDecimal> = emptyMap()
    ): SolveResult {
        val parts = expr.split("=")
        if (parts.size != 2) return SolveResult.Error("Formato equazione non valido: usare lhs = rhs")
        val lhs = parts[0].trim()
        val rhs = parts[1].trim()

        val isLhsFunc = lhs.equals("y", ignoreCase = true) || 
                        Regex("^y[0-9]*$", RegexOption.IGNORE_CASE).matches(lhs) ||
                        Regex("^[fghpqruwv](\\([a-zA-Z]\\))?$", RegexOption.IGNORE_CASE).matches(lhs)
        val diffExpr = if (isLhsFunc) {
            rhs
        } else if (rhs == "0" || rhs.isEmpty()) {
            lhs
        } else {
            "($lhs) - ($rhs)"
        }
        return findRootsOfFunction(diffExpr, isRadians, customVariables, customFunctions)
    }

    fun findRootsOfFunction(
        functionExpr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap(),
        customFunctions: Map<String, (List<BigDecimal>, EvaluationContext) -> BigDecimal> = emptyMap()
    ): SolveResult {
        val ast = try {
            parser.parse(functionExpr)
        } catch (e: Exception) {
            return SolveResult.Error("Errore sintassi funzione: ${e.message}")
        }

        val baseVariables = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVariables + ("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc,
                    customFunctions = customFunctions
                )
                val v = ast.evaluate(ctx).toDouble()
                if (v.isNaN() || v.isInfinite()) Double.NaN else v
            } catch (e: Exception) {
                Double.NaN
            }
        }

        val foundRoots = mutableListOf<Double>()
        val isTrig = functionExpr.contains("sin") || functionExpr.contains("cos") || functionExpr.contains("tan")
        val xMin = -50.0
        val xMax = 50.0
        val steps = 1000
        val dx = (xMax - xMin) / steps

        var prevPrevX = xMin
        var prevPrevY = Double.NaN
        var prevX = xMin
        var prevY = f(prevX)

        for (i in 1..steps) {
            val curX = xMin + i * dx
            val curY = f(curX)

            if (!prevY.isNaN() && !curY.isNaN()) {
                if (abs(prevY) < 1e-10) {
                    addUniqueRoot(foundRoots, prevX)
                } else if (prevY * curY < 0.0) {
                    // Sign change: perform Brent-Dekker / fast bisection
                    val root = brentRootFind(::f, prevX, curX, 1e-8, 100)
                    if (root != null && abs(f(root)) < 1e-4) {
                        addUniqueRoot(foundRoots, root)
                    }
                } else if (!prevPrevY.isNaN() && prevY <= prevPrevY && prevY <= curY && prevY < 0.2) {
                    // Tangential / non-negative zero (e.g. |cos(x)|, abs(x - 2), (x - 3)^2)
                    val root = findLocalMinimum(::f, prevPrevX, curX)
                    if (abs(f(root)) < 1e-4) {
                        addUniqueRoot(foundRoots, root)
                    }
                }
            }
            prevPrevX = prevX
            prevPrevY = prevY
            prevX = curX
            prevY = curY
        }

        // Sort roots by proximity to origin |x|
        foundRoots.sortBy { abs(it) }
        // For periodic/trigonometric functions (such as cos(x)), limit to principal roots (max 2 near 0),
        // for polynomials limit to max 4 roots. Avoids chaotic explosion of 32 roots!
        val maxRoots = if (isTrig) 2 else 4
        val cappedRoots = foundRoots.distinctBy { (it * 1000).roundToLong() }.take(maxRoots).sorted()

        return if (cappedRoots.isEmpty()) {
            SolveResult.Error("Nessuna radice reale trovata nell'intervallo [-50, 50]")
        } else {
            val formatted = cappedRoots.mapIndexed { idx, r ->
                if (cappedRoots.size == 1) "x = ${formatNumber(r)}"
                else "x${idx + 1} = ${formatNumber(r)}"
            }.joinToString(", ")
            SolveResult.EquationRoots(cappedRoots, formatted)
        }
    }

    private fun findLocalMinimum(f: (Double) -> Double, xA: Double, xB: Double): Double {
        var a = xA
        var b = xB
        val phi = (sqrt(5.0) - 1.0) / 2.0
        var c = b - phi * (b - a)
        var d = a + phi * (b - a)
        for (i in 0..45) {
            val fc = abs(f(c))
            val fd = abs(f(d))
            if (fc < fd) {
                b = d
                d = c
                c = b - phi * (b - a)
            } else {
                a = c
                c = d
                d = a + phi * (b - a)
            }
        }
        return (a + b) / 2.0
    }

    private fun addUniqueRoot(list: MutableList<Double>, r: Double) {
        val rounded = (r * 1e7).roundToLong() / 1e7
        if (list.none { abs(it - rounded) < 1e-4 }) {
            list.add(rounded)
        }
    }

    /**
     * Brent's method for super-fast (superlinear) root finding.
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
    // 4. INEQUALITY SOLVER
    // ==========================================

    data class Inequality2DInfo(
        val boundaryExpr: String,
        val readableRhs: String,
        val op: String,
        val isAbove: Boolean,
        val isStrict: Boolean
    )

    fun parseInequality2D(
        clean: String,
        op: String,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): Inequality2DInfo? {
        val hasY = Regex("(?<![a-zA-Z])y(?![a-zA-Z])", RegexOption.IGNORE_CASE).containsMatchIn(clean)
        if (!hasY) return null

        val parts = clean.split(op)
        if (parts.size != 2) return null
        val lhs = parts[0].trim()
        val rhs = parts[1].trim()

        val isStrict = op == "<" || op == ">"

        // Replace variable y with 0 in lhs and rhs
        val lhsZeroY = replaceVarWith(lhs, "y", "0")
        val rhsZeroY = replaceVarWith(rhs, "y", "0")

        // Estimate coefficient A of y in diffExpr = lhs - rhs
        val diffExpr = "($lhs) - ($rhs)"
        val ast = try { parser.parse(diffExpr) } catch (_: Exception) { return null }
        val baseVars = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun evalDiff(yVal: Double, xVal: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVars + mapOf(
                        "y" to BigDecimal(yVal.toString(), defaultMc),
                        "x" to BigDecimal(xVal.toString(), defaultMc)
                    ),
                    isRadians = true,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (_: Exception) {
                Double.NaN
            }
        }

        var diff0 = evalDiff(0.0, 1.0)
        var diff1 = evalDiff(1.0, 1.0)
        if (diff0.isNaN() || diff1.isNaN()) {
            diff0 = evalDiff(0.0, 0.0)
            diff1 = evalDiff(1.0, 0.0)
        }
        if (diff0.isNaN() || diff1.isNaN()) {
            diff0 = evalDiff(0.0, 2.0)
            diff1 = evalDiff(1.0, 2.0)
        }
        if (diff0.isNaN() || diff1.isNaN()) return null
        val A = diff1 - diff0
        if (abs(A) < 1e-9) return null

        val finalOp = if (A > 0) {
            op
        } else {
            when (op) {
                ">" -> "<"
                ">=", "≥" -> "≤"
                "<" -> ">"
                "<=", "≤" -> "≥"
                else -> op
            }
        }

        val isAbove = finalOp == ">" || finalOp == ">=" || finalOp == "≥"

        val boundaryExpr = if (lhs.trim().equals("y", ignoreCase = true)) {
            rhs
        } else if (abs(A - 1.0) < 1e-7) {
            "($rhsZeroY) - ($lhsZeroY)"
        } else {
            "(($rhsZeroY) - ($lhsZeroY)) / ${formatNumber(A)}"
        }

        val readableRhs = if (lhs.trim().equals("y", ignoreCase = true)) {
            rhs
        } else {
            simplifyReadableRhs(lhs, rhs, A)
        }

        return Inequality2DInfo(
            boundaryExpr = boundaryExpr,
            readableRhs = readableRhs,
            op = finalOp,
            isAbove = isAbove,
            isStrict = isStrict
        )
    }

    private fun replaceVarWith(expr: String, varName: String, replacement: String): String {
        return expr.replace(Regex("(?<![a-zA-Z_])${varName}(?![a-zA-Z0-9_])"), "($replacement)")
    }

    private fun simplifyReadableRhs(lhs: String, rhs: String, A: Double): String {
        val cleanLhs = lhs.trim()
        val cleanRhs = rhs.trim()
        if (cleanLhs.equals("y + x", ignoreCase = true) || cleanLhs.equals("x + y", ignoreCase = true)) {
            return if (cleanRhs == "0") "-x" else "$cleanRhs - x"
        }
        if (cleanLhs.equals("y - x", ignoreCase = true)) {
            return if (cleanRhs == "0") "x" else "$cleanRhs + x"
        }
        if (cleanLhs.equals("x - y", ignoreCase = true)) {
            return if (cleanRhs == "0") "x" else "x - $cleanRhs"
        }
        val withoutY = cleanLhs.replace(Regex("(?<![a-zA-Z_])y(?![a-zA-Z0-9_])"), "0")
            .replace("+ 0", "")
            .replace("- 0", "")
            .replace("0 +", "")
            .trim()
        val baseStr = if (cleanRhs == "0") "-($withoutY)" else "$cleanRhs - ($withoutY)"
        return if (abs(A - 1.0) < 1e-7) {
            baseStr
        } else if (abs(A - (-1.0)) < 1e-7) {
            "($withoutY) - $cleanRhs"
        } else {
            "($baseStr) / ${formatNumber(A)}"
        }
    }

    fun solveInequality(
        expr: String,
        op: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): SolveResult {
        // First check if this is a 2D inequality involving y (e.g. y >= x, y + x > 2)
        val ineq2D = parseInequality2D(expr, op, customVariables)
        if (ineq2D != null) {
            val opSymbol = when (ineq2D.op) {
                ">" -> ">"
                "<" -> "<"
                ">=", "≥" -> "≥"
                "<=", "≤" -> "≤"
                else -> ineq2D.op
            }
            val regionDesc = if (ineq2D.isAbove) "Semipiano superiore" else "Semipiano inferiore"
            val text = "y $opSymbol ${ineq2D.readableRhs}  ($regionDesc)"
            return SolveResult.Inequality2D(
                text = text,
                boundaryExpr = ineq2D.boundaryExpr,
                op = ineq2D.op,
                isAbove = ineq2D.isAbove,
                isStrict = ineq2D.isStrict
            )
        }

        val parts = expr.split(op)
        if (parts.size != 2) return SolveResult.Error("Formato disequazione non valido")
        val lhs = parts[0].trim()
        val rhs = parts[1].trim()

        val diffExpr = if (rhs == "0" || rhs.isEmpty()) lhs else "($lhs) - ($rhs)"
        val ast = try { parser.parse(diffExpr) } catch (e: Exception) {
            return SolveResult.Error("Sintassi non valida: ${e.message}")
        }

        val baseVariables = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVariables + ("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                Double.NaN
            }
        }

        // Find critical points (zeros)
        val rootsResult = findRootsOfFunction(diffExpr, isRadians, customVariables)
        val criticalPoints = when (rootsResult) {
            is SolveResult.EquationRoots -> rootsResult.roots.sorted()
            else -> emptyList()
        }

        // Partition the real line [-50, 50]
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
    // 5. POINT NUMERICAL DERIVATIVE
    // ==========================================

    fun solveDerivative(
        expr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): SolveResult {
        // Expected format: d/dx(f(x), x0) or d/dx(f(x)) with default x0=0 or given point
        val content = expr.substringAfter("(").substringBeforeLast(")")
        val parts = content.split(",")
        val funcStr = parts[0].trim()
        val x0 = if (parts.size > 1) parts[1].trim().toDoubleOrNull() ?: 0.0 else 0.0

        val ast = try { parser.parse(funcStr) } catch (e: Exception) {
            return SolveResult.Error("Funzione non valida: ${e.message}")
        }

        val baseVariables = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVariables + ("x" to BigDecimal(x.toString(), defaultMc)),
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

        // 5-point stencil: f'(x) ≈ (-f(x+2h) + 8f(x+h) - 8f(x-h) + f(x-2h)) / 12h
        val h = 1e-5
        val deriv = (-f(x0 + 2 * h) + 8 * f(x0 + h) - 8 * f(x0 - h) + f(x0 - 2 * h)) / (12 * h)

        if (deriv.isNaN() || deriv.isInfinite()) {
            return SolveResult.Error("Derivata non definita in x = $x0")
        }

        val formatted = "f'($x0) = ${formatNumber(deriv)}"
        return SolveResult.SingleValue(formatted, deriv)
    }

    // ==========================================
    // 6. DEFINITE INTEGRAL (SIMPSON QUADRATURE)
    // ==========================================

    fun solveIntegral(
        expr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): SolveResult {
        // Expected format: ∫(f(x), a, b)
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

        val baseVariables = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVariables + ("x" to BigDecimal(x.toString(), defaultMc)),
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
    // 7. NUMERICAL LIMIT
    // ==========================================

    fun solveLimit(
        expr: String,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap()
    ): SolveResult {
        // Format: lim(f(x), x0)
        val content = expr.substringAfter("(").substringBeforeLast(")")
        val parts = content.split(",")
        val funcStr = parts[0].trim()
        val x0 = if (parts.size > 1) parts[1].trim().toDoubleOrNull() ?: 0.0 else 0.0

        val ast = try { parser.parse(funcStr) } catch (e: Exception) {
            return SolveResult.Error("Funzione non valida: ${e.message}")
        }

        val baseVariables = EvaluationContext.DEFAULT_VARIABLES + customVariables

        fun f(x: Double): Double {
            return try {
                val ctx = EvaluationContext(
                    variables = baseVariables + ("x" to BigDecimal(x.toString(), defaultMc)),
                    isRadians = isRadians,
                    mathContext = defaultMc
                )
                ast.evaluate(ctx).toDouble()
            } catch (e: Exception) {
                Double.NaN
            }
        }

        // Evaluate left and right limits
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
    // 8. HIGH-PERFORMANCE SAMPLING FOR GRAPH (GEOGEBRA / PYPLOT STYLE)
    // ==========================================

    fun sampleFunction(
        expr: String,
        xMin: Float,
        xMax: Float,
        steps: Int = 1000,
        isRadians: Boolean = true,
        customVariables: Map<String, BigDecimal> = emptyMap(),
        customFunctions: Map<String, (List<BigDecimal>, EvaluationContext) -> BigDecimal> = emptyMap(),
        fastCustomFunctions: Map<String, (Double) -> Double> = emptyMap()
    ): List<Point?> {
        val stripped = stripFunctionDefinition(expr)
        val clean = autoCloseParentheses(stripped)
        if (clean.isBlank()) return emptyList()

        val ast = try { parser.parse(clean) } catch (e: Exception) { return emptyList() }
        val points = mutableListOf<Point?>()
        val totalSteps = steps.coerceIn(100, 2000)
        val stepSize = ((xMax - xMin) / totalSteps).toDouble()
        if (stepSize <= 0.0) return emptyList()

        // Prepare Double precision fast variables & constants
        val doubleVars = mutableMapOf<String, Double>()
        for ((k, v) in EvaluationContext.DEFAULT_VARIABLES) {
            doubleVars[k] = v.toDouble()
        }
        for ((k, v) in customVariables) {
            doubleVars[k] = v.toDouble()
        }

        // Fast custom functions map
        val doubleFns = fastCustomFunctions.toMutableMap()

        fun evalFast(xVal: Double): Double {
            return com.google.pixel.calculator.core.math.FastDoubleEvaluator.evaluate(
                node = ast,
                x = xVal,
                isRadians = isRadians,
                customVars = doubleVars,
                customFns = doubleFns
            )
        }

        fun findAsymptoteInInterval(
            x0: Double, y0: Double,
            x1: Double, y1: Double
        ): Double? {
            val mid = (x0 + x1) * 0.5
            val yMid = evalFast(mid)
            val hasPole = yMid.isNaN() || yMid.isInfinite() ||
                (y0 * y1 < 0 && (abs(yMid) > maxOf(abs(y0), abs(y1)) * 1.35 + 2.0)) ||
                (y0 * y1 > 0 && (abs(yMid) > maxOf(abs(y0), abs(y1)) * 2.8 + 8.0))

            if (!hasPole) {
                // Large delta with opposite signs: verify if values blow up or cross zero smoothly
                if (y0 * y1 < 0 && abs(y1 - y0) > 25.0) {
                    var l = x0; var r = x1; var vL = y0; var vR = y1
                    for (step in 0 until 4) {
                        val m = (l + r) * 0.5
                        val vM = evalFast(m)
                        if (vM.isNaN() || vM.isInfinite() || abs(vM) > 100.0) return m
                        if (vL * vM < 0) { r = m; vR = vM } else { l = m; vL = vM }
                    }
                    if (abs(vL) > 40.0 || abs(vR) > 40.0) return (l + r) * 0.5
                }
                return null
            }

            // Refine pole position using bisection
            var left = x0
            var right = x1
            var bestPole = mid
            for (iter in 0 until 6) {
                val m = (left + right) * 0.5
                val v = evalFast(m)
                if (v.isNaN() || v.isInfinite()) {
                    bestPole = m
                    break
                }
                if (abs(v) > 150.0) {
                    bestPole = m
                }
                val leftVal = evalFast((left + m) * 0.5)
                val rightVal = evalFast((m + right) * 0.5)
                if (leftVal.isNaN() || leftVal.isInfinite() || abs(leftVal) > abs(rightVal)) {
                    right = m
                } else {
                    left = m
                }
            }
            return bestPole
        }

        val yCutoff = 250.0 // Smooth asymptote extension bound
        var prevX: Double? = null
        var prevY: Double? = null
        var prevWasUndefined = true

        for (i in 0..totalSteps) {
            val rawX = xMin.toDouble() + i * stepSize
            val x = if (abs(rawX) < 1e-11) 0.0 else rawX
            val yVal = evalFast(x)

            if (yVal.isNaN()) {
                // Domain boundary exit check
                if (!prevWasUndefined && prevX != null && prevY != null) {
                    val boundaryPoint = findDomainBoundary(
                        ast = ast,
                        validX = prevX,
                        invalidX = x,
                        isRadians = isRadians,
                        customVars = doubleVars,
                        customFns = doubleFns
                    )
                    if (boundaryPoint != null) {
                        points.add(boundaryPoint)
                    }
                    points.add(null) // Discontinuity / gap
                }
                prevWasUndefined = true
                prevY = null
                prevX = x
                continue
            }

            if (yVal.isInfinite()) {
                // Vertical asymptote hit directly on sample
                val lastY = prevY
                val lastX = prevX
                if (lastY != null && lastX != null) {
                    val exitSign = if (lastY > 0) 1.0 else -1.0
                    points.add(Point((x - 0.001 * stepSize).toFloat(), (exitSign * yCutoff).toFloat()))
                }
                points.add(null) // Hard path break: do NOT connect across pole!
                prevWasUndefined = true
                prevY = null
                prevX = null
                continue
            }

            // Point is finite real number
            if (abs(yVal) >= yCutoff) {
                val lastY = prevY
                val lastX = prevX
                if (lastY != null && lastX != null && abs(lastY) < yCutoff) {
                    val exitSign = if (lastY > 0) 1.0 else -1.0
                    points.add(Point((lastX + 0.35 * stepSize).toFloat(), (exitSign * yCutoff).toFloat()))
                    points.add(null)
                }
                prevY = null
                prevX = null
                continue
            }

            // Function is defined and inside visible y bounds (|yVal| < yCutoff)
            if (prevWasUndefined) {
                // Domain boundary entry check (e.g. sqrt(x) entering at x >= 0)
                if (prevX != null) {
                    val boundaryPoint = findDomainBoundary(
                        ast = ast,
                        validX = x,
                        invalidX = prevX,
                        isRadians = isRadians,
                        customVars = doubleVars,
                        customFns = doubleFns
                    )
                    if (boundaryPoint != null) {
                        points.add(boundaryPoint)
                    }
                }
                prevWasUndefined = false
            }

            val lastY = prevY
            val lastX = prevX

            if (lastY == null) {
                if (abs(yVal) > 20.0) {
                    val enterSign = if (yVal > 0) 1.0 else -1.0
                    points.add(Point((x - 0.35 * stepSize).toFloat(), (enterSign * yCutoff).toFloat()))
                }
            } else if (lastX != null) {
                // Check if there is an asymptote or singularity in the interval between lastX and x
                val pole = findAsymptoteInInterval(lastX, lastY, x, yVal)
                if (pole != null) {
                    val exitSign = if (lastY > 0) 1.0 else -1.0
                    points.add(Point((pole - 0.001 * stepSize).toFloat(), (exitSign * yCutoff).toFloat()))
                    points.add(null) // Break path: NEVER draw line across asymptote!
                    val enterSign = if (yVal > 0) 1.0 else -1.0
                    points.add(Point((pole + 0.001 * stepSize).toFloat(), (enterSign * yCutoff).toFloat()))
                }
            }

            points.add(Point(x.toFloat(), yVal.toFloat()))
            prevX = x
            prevY = yVal
        }

        return points
    }

    private fun findDomainBoundary(
        ast: MathNode,
        validX: Double,
        invalidX: Double,
        isRadians: Boolean,
        customVars: Map<String, Double>,
        customFns: Map<String, (Double) -> Double>,
        iterations: Int = 6
    ): Point? {
        var low = validX
        var high = invalidX
        var lastValidX = validX
        var lastValidY = com.google.pixel.calculator.core.math.FastDoubleEvaluator.evaluate(
            node = ast,
            x = validX,
            isRadians = isRadians,
            customVars = customVars,
            customFns = customFns
        )
        if (lastValidY.isNaN()) return null

        for (k in 0 until iterations) {
            val mid = (low + high) * 0.5
            val y = com.google.pixel.calculator.core.math.FastDoubleEvaluator.evaluate(
                node = ast,
                x = mid,
                isRadians = isRadians,
                customVars = customVars,
                customFns = customFns
            )
            if (!y.isNaN() && y.isFinite()) {
                lastValidX = mid
                lastValidY = y
                low = mid
            } else {
                high = mid
            }
        }
        return Point(lastValidX.toFloat(), lastValidY.toFloat())
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
        if (abs(value) < 1e-4 || abs(value) >= 1e10) {
            return BigDecimal(value, MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros().toString()
        }
        val rounded = (value * 1e8).roundToLong() / 1e8
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    /**
     * Continued fraction expansion to find the exact reduced fraction p/q in lowest terms (e.g. 0.375 -> 3/8, 0.33333333 -> 1/3, 2.5 -> 5/2)
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
     * Engineering notation with exponents multiples of 3 (kilo, mega, micro, nano, etc.)
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
     * Sexagesimal: Degrees, Minutes, and Seconds (DMS)
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
     * Polar coordinates: r ∠ θ
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
