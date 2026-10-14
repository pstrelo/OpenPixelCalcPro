package com.google.pixel.calculator.core.math

import java.math.BigDecimal
import java.util.Stack

/**
 * High-performance mathematical parser based on Dijkstra's Shunting-Yard algorithm.
 * Converts expression strings into an Abstract Syntax Tree (MathNode).
 * Supports:
 * - Implicit multiplication (e.g. 2x, 2(3), (4)sin(30))
 * - 2D fraction notation: frac(num, den) or num/den
 * - Unary positive and negative operators
 * - Extended constants and functions
 */
class ExpressionParser {

    fun parse(expression: String): MathNode {
        val sanitized = sanitize(expression)
        val tokens = tokenize(sanitized)
        return buildAst(tokens)
    }

    private val superscriptMap = mapOf(
        '⁰' to '0', '¹' to '1', '²' to '2', '³' to '3', '⁴' to '4',
        '⁵' to '5', '⁶' to '6', '⁷' to '7', '⁸' to '8', '⁹' to '9'
    )
    private val superscriptRootRegex = Regex("([⁰¹²³⁴⁵⁶⁷⁸⁹]+)√")

    private fun sanitize(expr: String): String {
        var s = expr
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("–", "-")
            .replace("—", "-")
            .replace(";", ",")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("arcsin", "asin")
            .replace("arccos", "acos")
            .replace("arctan", "atan")
            .replace("arccsc", "acsc")
            .replace("arcsec", "asec")
            .replace("arccot", "acot")
            .replace("arsinh", "asinh")
            .replace("arcosh", "acosh")
            .replace("artanh", "atanh")
            .replace("nCr(", "binom(")
            .replace("C(", "binom(")
            .replace("nPr(", "perm(")
            .replace("P(", "perm(")
            .replace("k_B", "kb")
            .replace("N_A", "na")
            .replace("q_e", "qe")
            .replace("m_e", "me")
            .replace("m_p", "mp")
            .replace("ε₀", "eps0")
            .replace("μ₀", "mu0")
            .replace("ħ", "hbar")
            .replace("csc(", "1/sin(")
            .replace("sec(", "1/cos(")
            .replace("cot(", "1/tan(")
            .replace("θ", "x")
            .replace("°", "*(π/180)")
            .replace("defint(", "∫(")
            .replace("σ", "sigma")
            .replace("∛", "(3)ʸ√")
            .replace("∜", "(4)ʸ√")

        val subscriptMap = mapOf(
            '₀' to '0', '₁' to '1', '₂' to '2', '₃' to '3', '₄' to '4',
            '₅' to '5', '₆' to '6', '₇' to '7', '₈' to '8', '₉' to '9'
        )
        // Convert log₍base₎(x) or log_base(x) into logn(base,x)
        s = Regex("""log_([0-9a-zA-Z\.\+]+)\(([^\)]*)\)""").replace(s) { m ->
            "logn(${m.groupValues[1]},${m.groupValues[2]})"
        }
        s = Regex("""log_\{([^}]+)\}\(([^\)]*)\)""").replace(s) { m ->
            "logn(${m.groupValues[1]},${m.groupValues[2]})"
        }
        s = s.map { subscriptMap[it] ?: it }.joinToString("")

        // Handle |expr| -> abs(expr)
        var absIdx = s.indexOf('|')
        while (absIdx != -1) {
            val nextAbsIdx = s.indexOf('|', absIdx + 1)
            if (nextAbsIdx != -1) {
                val inner = s.substring(absIdx + 1, nextAbsIdx)
                s = s.substring(0, absIdx) + "abs(" + inner + ")" + s.substring(nextAbsIdx + 1)
                absIdx = s.indexOf('|')
            } else {
                break
            }
        }

        s = superscriptRootRegex.replace(s) { matchResult ->
            val supDigits = matchResult.groupValues[1]
            val normalDigits = supDigits.map { superscriptMap[it] ?: it }.joinToString("")
            "(${normalDigits})ʸ√"
        }
        return s
    }

    companion object {
        val KNOWN_FUNCTIONS = listOf(
            "binompdf", "binomcdf", "normpdf", "normcdf", "randint", "median",
            "arcsin", "arccos", "arctan", "arccsc", "arcsec", "arccot",
            "arsinh", "arcosh", "artanh",
            "asinh", "acosh", "atanh", "acsch", "asech", "acoth",
            "sinh", "cosh", "tanh", "csch", "sech", "coth",
            "sign", "sqrt", "cbrt", "root", "nroot", "log10", "log2", "logn", "log_n",
            "diff", "integral", "defint", "lim",
            "binom", "perm", "comb", "frac", "mean", "rand",
            "sin", "cos", "tan", "cot", "sec", "csc",
            "asin", "acos", "atan", "acot", "asec", "acsc",
            "log", "ln", "lg", "exp", "abs", "ncr", "npr", "var", "std"
        ).sortedByDescending { it.length }

        val KNOWN_CONSTANTS = listOf(
            "g_grav", "r_gas", "eps_0", "eps0", "mu_0", "mu0",
            "hbar", "sigma", "phi", "pi", "kb", "na", "qe", "me", "mp"
        ).sortedByDescending { it.length }
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val c = expr[i]

            when {
                c.isDigit() || c == '.' -> {
                    // Check implicit multiplication before a number: e.g. (2)3 -> (2)*3 or 5% 3 -> 5% * 3
                    if (tokens.isNotEmpty() && (tokens.last() is Token.CloseParen || (tokens.last() is Token.Operator && (tokens.last() as Token.Operator).symbol in listOf("!", "%")))) {
                        tokens.add(Token.Operator("*", 2))
                    }
                    val sb = StringBuilder()
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    val numStr = sb.toString()
                    val validNum = if (numStr == ".") "0" else if (numStr.count { it == '.' } > 1) {
                        val firstDot = numStr.indexOf('.')
                        numStr.substring(0, firstDot + 1) + numStr.substring(firstDot + 1).replace(".", "")
                    } else numStr
                    tokens.add(Token.Number(validNum))
                    continue
                }
                c == '(' -> {
                    // Check implicit multiplication before '(': e.g. 2(3) -> 2*(3)
                    if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                        tokens.add(Token.Operator("*", 2))
                    }
                    tokens.add(Token.OpenParen)
                    i++
                }
                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    i++
                }
                c == ',' -> {
                    tokens.add(Token.Comma)
                    i++
                }
                c == '%' || c == '!' -> {
                    // High-precedence postfix unary operators (percentage and factorial)
                    tokens.add(Token.Operator(c.toString(), 4, isRightAssociative = false))
                    i++
                }
                c in "+-*/^" -> {
                    val isUnary = (c == '-' || c == '+') && (tokens.isEmpty() || 
                            tokens.last() is Token.OpenParen || 
                            (tokens.last() is Token.Operator && (tokens.last() as Token.Operator).symbol !in listOf("!", "%")) ||
                            tokens.last() is Token.Comma)

                    if (isUnary) {
                        tokens.add(Token.Operator(if (c == '-') "neg" else "pos", 4, isRightAssociative = true))
                    } else {
                        val precedence = when (c) {
                            '+', '-' -> 1
                            '*', '/' -> 2
                            '^' -> 3
                            else -> 1
                        }
                        tokens.add(Token.Operator(c.toString(), precedence, isRightAssociative = c == '^'))
                    }
                    i++
                }
                c == 'ʸ' || c == 'ⁿ' || c == 'ˣ' -> {
                    if (i + 1 < len && expr[i + 1] == '√') {
                        tokens.add(Token.Operator("ʸ√", 3, isRightAssociative = true))
                        i += 2
                    } else {
                        tokens.add(Token.Operator("ʸ√", 3, isRightAssociative = true))
                        i++
                    }
                    continue
                }
                c == '√' -> {
                    if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                        tokens.add(Token.Operator("*", 2))
                    }
                    tokens.add(Token.Function("sqrt"))
                    i++
                    continue
                }
                c.isLetter() || c == 'π' || c in "αβγδεζηθικλμνξοπρστυφχψωΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ" -> {
                    val remaining = expr.substring(i)

                    // 1. Match known multi-character functions (e.g. sin, cos, binom, etc.)
                    val matchedFn = KNOWN_FUNCTIONS.firstOrNull { remaining.startsWith(it, ignoreCase = true) }
                    if (matchedFn != null) {
                        if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                            tokens.add(Token.Operator("*", 2))
                        }
                        tokens.add(Token.Function(matchedFn.lowercase()))
                        i += matchedFn.length
                        continue
                    }

                    // 2. Match known multi-character constants (e.g. pi, phi, hbar, eps0, etc.)
                    val matchedConst = KNOWN_CONSTANTS.firstOrNull { remaining.startsWith(it, ignoreCase = true) }
                    if (matchedConst != null) {
                        if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                            tokens.add(Token.Operator("*", 2))
                        }
                        tokens.add(Token.Variable(matchedConst.lowercase()))
                        i += matchedConst.length
                        continue
                    }

                    // 3. Single letter / algebraic variable (e.g. a, b, c, x, y, z) with subscript support (e.g. v_0, a_1)
                    val sb = StringBuilder()
                    sb.append(c)
                    i++
                    if (i < len && expr[i] == '_') {
                        sb.append(expr[i])
                        i++
                        while (i < len && expr[i].isLetterOrDigit()) {
                            sb.append(expr[i])
                            i++
                        }
                    }

                    val varName = sb.toString()
                    var peek = i
                    while (peek < len && expr[peek].isWhitespace()) peek++
                    if (peek < len && expr[peek] == '(') {
                        if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                            tokens.add(Token.Operator("*", 2))
                        }
                        tokens.add(Token.Function(varName.lowercase()))
                        continue
                    }

                    if (tokens.isNotEmpty() && shouldInsertImplicitMul(tokens.last())) {
                        tokens.add(Token.Operator("*", 2))
                    }
                    tokens.add(Token.Variable(varName))
                    continue
                }
                else -> {
                    i++ // Ignora caratteri non riconosciuti
                }
            }
        }
        return tokens
    }

    private fun shouldInsertImplicitMul(lastToken: Token): Boolean {
        return lastToken is Token.Number || 
               lastToken is Token.CloseParen || 
               lastToken is Token.Variable ||
               (lastToken is Token.Operator && (lastToken.symbol == "!" || lastToken.symbol == "%"))
    }

    private fun buildAst(tokens: List<Token>): MathNode {
        val outputStack = Stack<MathNode>()
        val opStack = Stack<Token>()
        val fnArgCountStack = Stack<Int>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    outputStack.push(MathNode.Number(BigDecimal(token.value)))
                }
                is Token.Variable -> {
                    outputStack.push(MathNode.Variable(token.name))
                }
                is Token.Function -> {
                    opStack.push(token)
                }
                is Token.Comma -> {
                    while (opStack.isNotEmpty() && opStack.peek() !is Token.OpenParen) {
                        popOperatorToOutput(opStack.pop(), outputStack)
                    }
                    if (fnArgCountStack.isNotEmpty()) {
                        val top = fnArgCountStack.pop()
                        fnArgCountStack.push(top + 1)
                    }
                }
                is Token.Operator -> {
                    while (opStack.isNotEmpty() && opStack.peek() is Token.Operator) {
                        val top = opStack.peek() as Token.Operator
                        if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                            (token.isRightAssociative && token.precedence < top.precedence)) {
                            popOperatorToOutput(opStack.pop(), outputStack)
                        } else {
                            break
                        }
                    }
                    opStack.push(token)
                }
                is Token.OpenParen -> {
                    if (opStack.isNotEmpty() && opStack.peek() is Token.Function) {
                        fnArgCountStack.push(1)
                    }
                    opStack.push(token)
                }
                is Token.CloseParen -> {
                    while (opStack.isNotEmpty() && opStack.peek() !is Token.OpenParen) {
                        popOperatorToOutput(opStack.pop(), outputStack)
                    }
                    if (opStack.isNotEmpty() && opStack.peek() is Token.OpenParen) {
                        opStack.pop() // Rimuove OpenParen
                    }
                    if (opStack.isNotEmpty() && opStack.peek() is Token.Function) {
                        val fn = opStack.pop() as Token.Function
                        val count = if (fnArgCountStack.isNotEmpty()) fnArgCountStack.pop() else 1
                        val args = mutableListOf<MathNode>()
                        for (a in 0 until count) {
                            if (outputStack.isNotEmpty()) {
                                args.add(0, outputStack.pop())
                            }
                        }
                        if (fn.name == "frac") {
                            val num = args.getOrElse(0) { MathNode.Number(BigDecimal.ZERO) }
                            val den = args.getOrElse(1) { MathNode.Number(BigDecimal.ONE) }
                            outputStack.push(MathNode.Fraction(num, den))
                        } else {
                            outputStack.push(MathNode.FunctionCall(fn.name, args))
                        }
                    }
                }
                else -> {}
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            if (top !is Token.OpenParen && top !is Token.CloseParen) {
                popOperatorToOutput(top, outputStack)
            }
        }

        if (outputStack.isEmpty()) {
            return MathNode.Number(BigDecimal.ZERO)
        }
        val result = outputStack.pop()
        if (outputStack.isNotEmpty()) {
            throw IllegalArgumentException("Sintassi non valida: operatore mancante")
        }
        return result
    }

    private fun popOperatorToOutput(opToken: Token, outputStack: Stack<MathNode>) {
        if (opToken is Token.Operator) {
            if (opToken.symbol in listOf("neg", "pos", "!", "%")) {
                if (outputStack.isEmpty()) throw IllegalArgumentException("Operando mancante per ${opToken.symbol}")
                val operand = outputStack.pop()
                outputStack.push(MathNode.UnaryOp(opToken.symbol, operand))
            } else {
                if (outputStack.size < 2) {
                    if (opToken.symbol == "ʸ√" && outputStack.isNotEmpty()) {
                        val operand = outputStack.pop()
                        outputStack.push(MathNode.UnaryOp("ʸ√", operand))
                        return
                    }
                    throw IllegalArgumentException("Operandi insufficienti per ${opToken.symbol}")
                }
                val right = outputStack.pop()
                val left = outputStack.pop()
                
                // If it is a division, we can also represent it as an AST fraction for 2D rendering
                if (opToken.symbol == "/") {
                    outputStack.push(MathNode.Fraction(left, right))
                } else {
                    outputStack.push(MathNode.BinaryOp(left, opToken.symbol, right))
                }
            }
        }
    }
}
