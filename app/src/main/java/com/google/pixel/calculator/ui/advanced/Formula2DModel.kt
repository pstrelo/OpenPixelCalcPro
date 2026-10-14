package com.google.pixel.calculator.ui.advanced

/**
 * 2D Interactive Math formula model supporting:
 * - 2D Fractions (numerator over denominator with horizontal fraction bar)
 * - Exponents (elevated superscript with reduced height cursor)
 * - Radicals (square root with overbar)
 * - Absolute value (|x|)
 * - Bidirectional 2D cursor navigation (jumping between numerator/denominator, entering/exiting exponents)
 */
sealed class FormulaSegment {
    abstract val fullStart: Int
    abstract val fullEnd: Int

    data class SimpleText(
        val text: String,
        val startOffset: Int,
        val endOffset: Int
    ) : FormulaSegment() {
        override val fullStart: Int get() = startOffset
        override val fullEnd: Int get() = endOffset
    }

    data class PowerSegment(
        val baseText: String,
        val baseStart: Int,
        val baseEnd: Int,
        val expText: String,
        val expStart: Int,
        val expEnd: Int,
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class FractionSegment(
        val numText: String,
        val numStart: Int,
        val numEnd: Int,
        val slashPos: Int,
        val denText: String,
        val denStart: Int,
        val denEnd: Int,
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class RadicalSegment(
        val innerText: String,
        val innerStart: Int,
        val innerEnd: Int,
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class AbsSegment(
        val innerText: String,
        val innerStart: Int,
        val innerEnd: Int,
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()
}

object Formula2DParser {

    /**
     * Parses an expression string into high-level visual segments.
     */
    fun parse(expr: String): List<FormulaSegment> {
        if (expr.isEmpty()) return emptyList()
        val segments = mutableListOf<FormulaSegment>()
        var index = 0
        val len = expr.length

        while (index < len) {
            // 1. Check for Radicals: sqrt(...) or √(...)
            if (expr.startsWith("sqrt(", index) || expr.startsWith("√(", index)) {
                val isSqrtLong = expr.startsWith("sqrt(", index)
                val prefixLen = if (isSqrtLong) 5 else 2
                val innerStart = index + prefixLen
                val closeParen = findMatchingParen(expr, innerStart - 1)
                val innerEnd = if (closeParen != -1) closeParen else len
                val fullEnd = if (closeParen != -1) closeParen + 1 else len
                val innerText = expr.substring(innerStart, innerEnd)
                segments.add(
                    FormulaSegment.RadicalSegment(
                        innerText = innerText,
                        innerStart = innerStart,
                        innerEnd = innerEnd,
                        fullStart = index,
                        fullEnd = fullEnd
                    )
                )
                index = fullEnd
                continue
            }

            // 2. Check for Absolute Value: |...|
            if (expr[index] == '|') {
                val nextPipe = expr.indexOf('|', index + 1)
                if (nextPipe != -1) {
                    val innerStart = index + 1
                    val innerEnd = nextPipe
                    val fullEnd = nextPipe + 1
                    val innerText = expr.substring(innerStart, innerEnd)
                    segments.add(
                        FormulaSegment.AbsSegment(
                            innerText = innerText,
                            innerStart = innerStart,
                            innerEnd = innerEnd,
                            fullStart = index,
                            fullEnd = fullEnd
                        )
                    )
                    index = fullEnd
                    continue
                }
            }

            // 3. Check for Fractions: either (num)/(den) or num/den
            // Check if upcoming token or preceding item connects via '/'
            val slashIdx = findNextTopLevelSlash(expr, index)
            if (slashIdx != -1 && slashIdx == index) {
                // Standalone '/' without numerator
                val (denText, denStart, denEnd, fullEnd) = parseDenominator(expr, slashIdx + 1)
                segments.add(
                    FormulaSegment.FractionSegment(
                        numText = "",
                        numStart = slashIdx,
                        numEnd = slashIdx,
                        slashPos = slashIdx,
                        denText = denText,
                        denStart = denStart,
                        denEnd = denEnd,
                        fullStart = slashIdx,
                        fullEnd = fullEnd
                    )
                )
                index = fullEnd
                continue
            }

            // 4. Check for Power / Exponent: base^(exp) or base^digit or base²
            val caretIdx = findNextTopLevelCaret(expr, index)
            if (caretIdx != -1 && caretIdx == index) {
                // Standalone '^' without preceding base
                val (expText, expStart, expEnd, fullEnd) = parseExponentRhs(expr, caretIdx + 1)
                segments.add(
                    FormulaSegment.PowerSegment(
                        baseText = "",
                        baseStart = caretIdx,
                        baseEnd = caretIdx,
                        expText = expText,
                        expStart = expStart,
                        expEnd = expEnd,
                        fullStart = caretIdx,
                        fullEnd = fullEnd
                    )
                )
                index = fullEnd
                continue
            }

            // If a fraction is present starting after this token:
            if (slashIdx != -1 && slashIdx > index) {
                // Check if the portion from [index..slashIdx) is the numerator
                val candidateNum = expr.substring(index, slashIdx)
                val isParenGroup = candidateNum.startsWith("(") && candidateNum.endsWith(")")
                val hasOperatorBefore = candidateNum.lastIndexOfAny(charArrayOf('+', '-', '=', '<', '>', ';'))
                if (hasOperatorBefore != -1 && !isParenGroup) {
                    // Split before operator
                    val textEnd = index + hasOperatorBefore + 1
                    segments.add(
                        FormulaSegment.SimpleText(
                            text = expr.substring(index, textEnd),
                            startOffset = index,
                            endOffset = textEnd
                        )
                    )
                    index = textEnd
                    continue
                } else {
                    // candidateNum is numerator
                    val (numText, numStart, numEnd) = if (isParenGroup) {
                        Triple(candidateNum.substring(1, candidateNum.length - 1), index + 1, slashIdx - 1)
                    } else {
                        Triple(candidateNum, index, slashIdx)
                    }
                    val (denText, denStart, denEnd, fullEnd) = parseDenominator(expr, slashIdx + 1)
                    segments.add(
                        FormulaSegment.FractionSegment(
                            numText = numText,
                            numStart = numStart,
                            numEnd = numEnd,
                            slashPos = slashIdx,
                            denText = denText,
                            denStart = denStart,
                            denEnd = denEnd,
                            fullStart = index,
                            fullEnd = fullEnd
                        )
                    )
                    index = fullEnd
                    continue
                }
            }

            // If a power '^' is present starting after this token:
            if (caretIdx != -1 && caretIdx > index) {
                val candidateBase = expr.substring(index, caretIdx)
                val isParenGroup = candidateBase.startsWith("(") && candidateBase.endsWith(")")
                val hasOperatorBefore = candidateBase.lastIndexOfAny(charArrayOf('+', '-', '=', '<', '>', ';'))
                if (hasOperatorBefore != -1 && !isParenGroup) {
                    val textEnd = index + hasOperatorBefore + 1
                    segments.add(
                        FormulaSegment.SimpleText(
                            text = expr.substring(index, textEnd),
                            startOffset = index,
                            endOffset = textEnd
                        )
                    )
                    index = textEnd
                    continue
                } else {
                    val (baseText, baseStart, baseEnd) = if (isParenGroup) {
                        Triple(candidateBase.substring(1, candidateBase.length - 1), index + 1, caretIdx - 1)
                    } else {
                        Triple(candidateBase, index, caretIdx)
                    }
                    val (expText, expStart, expEnd, fullEnd) = parseExponentRhs(expr, caretIdx + 1)
                    segments.add(
                        FormulaSegment.PowerSegment(
                            baseText = baseText,
                            baseStart = baseStart,
                            baseEnd = baseEnd,
                            expText = expText,
                            expStart = expStart,
                            expEnd = expEnd,
                            fullStart = index,
                            fullEnd = fullEnd
                        )
                    )
                    index = fullEnd
                    continue
                }
            }

            // Check for unicode superscript '²' attached to a base
            val sqIdx = expr.indexOf('²', index)
            if (sqIdx != -1 && sqIdx > index && sqIdx == index + 1) {
                val base = expr.substring(index, sqIdx)
                segments.add(
                    FormulaSegment.PowerSegment(
                        baseText = base,
                        baseStart = index,
                        baseEnd = sqIdx,
                        expText = "2",
                        expStart = sqIdx,
                        expEnd = sqIdx + 1,
                        fullStart = index,
                        fullEnd = sqIdx + 1
                    )
                )
                index = sqIdx + 1
                continue
            }

            // Normal text block up to the next special operator or end of string
            val nextSpecial = findNextSpecialChar(expr, index)
            val end = if (nextSpecial != -1) nextSpecial else len
            val chunk = expr.substring(index, end)
            if (chunk.isNotEmpty()) {
                segments.add(
                    FormulaSegment.SimpleText(
                        text = chunk,
                        startOffset = index,
                        endOffset = end
                    )
                )
            }
            index = end
        }

        return segments
    }

    private fun findNextSpecialChar(expr: String, fromIndex: Int): Int {
        var i = fromIndex
        val len = expr.length
        while (i < len) {
            val c = expr[i]
            if (c == '/' || c == '^' || c == '|' || c == '²') return i
            if (expr.startsWith("sqrt(", i) || expr.startsWith("√(", i)) return i
            i++
        }
        return -1
    }

    private fun findNextTopLevelSlash(expr: String, fromIndex: Int): Int {
        var depth = 0
        for (i in fromIndex until expr.length) {
            val c = expr[i]
            if (c == '(') depth++
            else if (c == ')') depth = (depth - 1).coerceAtLeast(0)
            else if (c == '/' && depth == 0) {
                // Ensure not d/dx
                if (i >= 1 && expr[i - 1] == 'd' && i + 2 < expr.length && expr.substring(i, i + 3) == "/dx") {
                    continue
                }
                return i
            }
        }
        return -1
    }

    private fun findNextTopLevelCaret(expr: String, fromIndex: Int): Int {
        var depth = 0
        for (i in fromIndex until expr.length) {
            val c = expr[i]
            if (c == '(') depth++
            else if (c == ')') depth = (depth - 1).coerceAtLeast(0)
            else if (c == '^' && depth == 0) return i
        }
        return -1
    }

    private fun parseDenominator(expr: String, startIdx: Int): Quadruple<String, Int, Int, Int> {
        if (startIdx >= expr.length) {
            return Quadruple("", startIdx, startIdx, startIdx)
        }
        if (expr[startIdx] == '(') {
            val closeParen = findMatchingParen(expr, startIdx)
            if (closeParen != -1) {
                val denText = expr.substring(startIdx + 1, closeParen)
                return Quadruple(denText, startIdx + 1, closeParen, closeParen + 1)
            }
        }
        // Simple token: up to operator +, -, =, <, >, ;, or end
        var end = startIdx
        while (end < expr.length && expr[end] !in "+-=<>;, ") {
            end++
        }
        val denText = expr.substring(startIdx, end)
        return Quadruple(denText, startIdx, end, end)
    }

    private fun parseExponentRhs(expr: String, startIdx: Int): Quadruple<String, Int, Int, Int> {
        if (startIdx >= expr.length) {
            return Quadruple("", startIdx, startIdx, startIdx)
        }
        if (expr[startIdx] == '(') {
            val closeParen = findMatchingParen(expr, startIdx)
            if (closeParen != -1) {
                val expText = expr.substring(startIdx + 1, closeParen)
                return Quadruple(expText, startIdx + 1, closeParen, closeParen + 1)
            }
        }
        // Simple token: single variable or digits
        var end = startIdx
        if (end < expr.length && (expr[end] == '-' || expr[end] == '+')) {
            end++
        }
        while (end < expr.length && (expr[end].isLetterOrDigit() || expr[end] == '.')) {
            end++
        }
        val expText = expr.substring(startIdx, end)
        return Quadruple(expText, startIdx, end, end)
    }

    private fun findMatchingParen(expr: String, openIdx: Int): Int {
        var depth = 0
        for (i in openIdx until expr.length) {
            if (expr[i] == '(') depth++
            else if (expr[i] == ')') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }

    /**
     * Smart 2D navigation moving RIGHT:
     * - End of Numerator -> jumps to Denominator
     * - End of Denominator -> exits Fraction onto baseline
     * - End of Base -> enters Exponent
     * - End of Exponent -> exits Exponent onto baseline
     * - End of Radicand -> exits Radical onto baseline
     */
    fun getNextCursorPosition(expr: String, currentPos: Int): Int {
        val safePos = currentPos.coerceIn(0, expr.length)
        val segments = parse(expr)

        for (seg in segments) {
            when (seg) {
                is FormulaSegment.FractionSegment -> {
                    // At end of numerator: drop down into denominator!
                    if (safePos == seg.numEnd) {
                        return seg.denStart
                    }
                    // At end of denominator: exit the fraction to baseline!
                    if (safePos == seg.denEnd) {
                        return seg.fullEnd
                    }
                }
                is FormulaSegment.PowerSegment -> {
                    // At end of base: enter exponent!
                    if (safePos == seg.baseEnd) {
                        return seg.expStart
                    }
                    // At end of exponent: exit to baseline!
                    if (safePos == seg.expEnd) {
                        return seg.fullEnd
                    }
                }
                is FormulaSegment.RadicalSegment -> {
                    if (safePos < seg.innerStart && safePos >= seg.fullStart) {
                        return seg.innerStart
                    }
                    if (safePos == seg.innerEnd) {
                        return seg.fullEnd
                    }
                }
                is FormulaSegment.AbsSegment -> {
                    if (safePos == seg.innerEnd) {
                        return seg.fullEnd
                    }
                }
                else -> {}
            }
        }

        return (safePos + 1).coerceAtMost(expr.length)
    }

    /**
     * Smart 2D navigation moving LEFT:
     * - Start of Denominator -> risale al Numeratore (at end of numerator)
     * - Start of Numerator -> exits Fraction to the left
     * - Baseline right after Fraction -> enters Denominator at the end
     * - Start of Exponent -> exits Exponent to the left (before ^)
     * - Baseline right after Exponent -> enters Exponent at the end
     */
    fun getPrevCursorPosition(expr: String, currentPos: Int): Int {
        val safePos = currentPos.coerceIn(0, expr.length)
        val segments = parse(expr)

        for (seg in segments) {
            when (seg) {
                is FormulaSegment.FractionSegment -> {
                    // At start of denominator: "quando arriva in battuta sul denominatore risale al numeratore"
                    if (safePos == seg.denStart) {
                        return seg.numEnd
                    }
                    // If on baseline right after the fraction: enter denominator at its end!
                    if (safePos == seg.fullEnd && seg.fullEnd > seg.denEnd) {
                        return seg.denEnd
                    }
                    // At start of numerator: exit fraction to the left
                    if (safePos == seg.numStart) {
                        return seg.fullStart
                    }
                }
                is FormulaSegment.PowerSegment -> {
                    // At start of exponent: exit to the left before ^
                    if (safePos == seg.expStart) {
                        return seg.baseEnd
                    }
                    // If on baseline right after exponent: enter exponent at its end!
                    if (safePos == seg.fullEnd && seg.fullEnd > seg.expEnd) {
                        return seg.expEnd
                    }
                }
                is FormulaSegment.RadicalSegment -> {
                    if (safePos == seg.innerStart) {
                        return seg.fullStart
                    }
                    if (safePos == seg.fullEnd) {
                        return seg.innerEnd
                    }
                }
                is FormulaSegment.AbsSegment -> {
                    if (safePos == seg.innerStart) {
                        return seg.fullStart
                    }
                    if (safePos == seg.fullEnd) {
                        return seg.innerEnd
                    }
                }
                else -> {}
            }
        }

        return (safePos - 1).coerceAtLeast(0)
    }

    /**
     * Determines whether the cursor at [cursorPosition] is currently inside an elevated exponent or child region.
     * Returns true if cursor height should be halved.
     */
    fun isCursorHalved(expr: String, cursorPosition: Int): Boolean {
        val safePos = cursorPosition.coerceIn(0, expr.length)
        val segments = parse(expr)
        for (seg in segments) {
            when (seg) {
                is FormulaSegment.PowerSegment -> {
                    if (safePos in seg.expStart..seg.expEnd) return true
                }
                is FormulaSegment.FractionSegment -> {
                    if (safePos in seg.numStart..seg.numEnd || safePos in seg.denStart..seg.denEnd || safePos == seg.slashPos) return true
                }
                else -> {}
            }
        }
        return false
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
