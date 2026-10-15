package com.google.pixel.calculator.ui.advanced

/**
 * 2D Interactive Math formula model supporting:
 * - 2D Fractions (numerator over denominator with horizontal fraction bar)
 * - Exponents (elevated superscript with halved height cursor)
 * - Subscripts (lowered pedice with halved height cursor, e.g. x_{1,2} or x_1)
 * - Radicals with optional index (n-th root: [apice n] √ {contenuto sotto radice})
 * - Logarithms with base (log[pedice n] (contenuto logaritmo))
 * - Absolute value (|x|)
 * - Recursive / hierarchical child parsing for nested math structures
 * - Bidirectional 2D cursor navigation across all levels and boundaries
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
        val expSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class SubscriptSegment(
        val baseText: String,
        val baseStart: Int,
        val baseEnd: Int,
        val subText: String,
        val subStart: Int,
        val subEnd: Int,
        val subSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class FractionSegment(
        val numText: String,
        val numStart: Int,
        val numEnd: Int,
        val numSegments: List<FormulaSegment> = emptyList(),
        val slashPos: Int,
        val denText: String,
        val denStart: Int,
        val denEnd: Int,
        val denSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class RadicalSegment(
        val hasIndex: Boolean = false,
        val indexText: String = "",
        val indexStart: Int = -1,
        val indexEnd: Int = -1,
        val indexSegments: List<FormulaSegment> = emptyList(),
        val innerText: String,
        val innerStart: Int,
        val innerEnd: Int,
        val innerSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class LogSegment(
        val baseText: String,
        val baseStart: Int,
        val baseEnd: Int,
        val baseSegments: List<FormulaSegment> = emptyList(),
        val argText: String,
        val argStart: Int,
        val argEnd: Int,
        val argSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()

    data class AbsSegment(
        val innerText: String,
        val innerStart: Int,
        val innerEnd: Int,
        val innerSegments: List<FormulaSegment> = emptyList(),
        override val fullStart: Int,
        override val fullEnd: Int
    ) : FormulaSegment()
}

object Formula2DParser {

    /**
     * Parses an expression string into hierarchical visual segments.
     * [baseOffset] ensures all child segments preserve absolute character indices.
     */
    fun parse(expr: String, baseOffset: Int = 0): List<FormulaSegment> {
        if (expr.isEmpty()) return emptyList()
        val segments = mutableListOf<FormulaSegment>()
        var index = 0
        val len = expr.length

        while (index < len) {
            val globalIdx = baseOffset + index

            // 1. Check for Logarithm with base: log_(base)(arg) or log2(arg) or log[base](arg) or log_()
            val isLogWithBase = expr.startsWith("log_", index) ||
                expr.startsWith("log[", index) ||
                expr.startsWith("log{", index) ||
                (expr.startsWith("log", index) && index + 3 < len && (expr[index + 3].isDigit() || (expr[index + 3].isLetter() && expr[index + 3] != 'e' && expr[index + 3] != 'n' && expr[index + 3] != '(')))

            if (isLogWithBase) {
                val logStart = globalIdx
                var baseStartLocal = index + 3
                var baseEndLocal = index + 3
                var afterBaseLocal = index + 3

                if (expr.startsWith("log_", index)) {
                    val afterLogUnder = index + 4
                    baseStartLocal = afterLogUnder
                    baseEndLocal = afterLogUnder
                    afterBaseLocal = afterLogUnder

                    if (afterLogUnder < len && expr[afterLogUnder] == '(') {
                        val closeParen = findMatchingParen(expr, afterLogUnder)
                        if (closeParen != -1) {
                            baseStartLocal = afterLogUnder + 1
                            baseEndLocal = closeParen
                            afterBaseLocal = closeParen + 1
                        }
                    } else if (afterLogUnder < len && expr[afterLogUnder] == '{') {
                        val closeBrace = expr.indexOf('}', afterLogUnder)
                        if (closeBrace != -1) {
                            baseStartLocal = afterLogUnder + 1
                            baseEndLocal = closeBrace
                            afterBaseLocal = closeBrace + 1
                        }
                    } else {
                        var bEnd = afterLogUnder
                        while (bEnd < len && expr[bEnd].isLetterOrDigit()) {
                            bEnd++
                        }
                        baseStartLocal = afterLogUnder
                        baseEndLocal = bEnd
                        afterBaseLocal = bEnd
                    }
                } else if (expr.startsWith("log[", index) || expr.startsWith("log{", index)) {
                    val closeCh = if (expr[index + 3] == '[') ']' else '}'
                    val closeIdx = expr.indexOf(closeCh, index + 4)
                    val endB = if (closeIdx != -1) closeIdx else len
                    baseStartLocal = index + 4
                    baseEndLocal = endB
                    afterBaseLocal = if (closeIdx != -1) closeIdx + 1 else len
                } else {
                    // Direct token base, e.g. log2(...) or log10(...)
                    val afterLog = index + 3
                    var bEnd = afterLog
                    while (bEnd < len && expr[bEnd].isLetterOrDigit()) {
                        bEnd++
                    }
                    baseStartLocal = afterLog
                    baseEndLocal = bEnd
                    afterBaseLocal = bEnd
                }

                val baseText = expr.substring(baseStartLocal, baseEndLocal)
                val baseStartGlobal = baseOffset + baseStartLocal
                val baseEndGlobal = baseOffset + baseEndLocal

                // Argument of logarithm: optional (arg)
                var argText = ""
                var argStartGlobal = baseEndGlobal
                var argEndGlobal = baseEndGlobal
                var logEndLocal = afterBaseLocal

                if (afterBaseLocal < len && expr[afterBaseLocal] == '(') {
                    val closeParen = findMatchingParen(expr, afterBaseLocal)
                    val argEndLoc = if (closeParen != -1) closeParen else len
                    argText = expr.substring(afterBaseLocal + 1, argEndLoc)
                    argStartGlobal = baseOffset + afterBaseLocal + 1
                    argEndGlobal = baseOffset + argEndLoc
                    logEndLocal = if (closeParen != -1) closeParen + 1 else len
                }

                val logEndGlobal = baseOffset + logEndLocal
                segments.add(
                    FormulaSegment.LogSegment(
                        baseText = baseText,
                        baseStart = baseStartGlobal,
                        baseEnd = baseEndGlobal,
                        baseSegments = parse(baseText, baseStartGlobal),
                        argText = argText,
                        argStart = argStartGlobal,
                        argEnd = argEndGlobal,
                        argSegments = parse(argText, argStartGlobal),
                        fullStart = logStart,
                        fullEnd = logEndGlobal
                    )
                )
                index = logEndLocal
                continue
            }

            // 2. Check for N-th root: ^(index)√(radicand) or ^(index)sqrt(radicand)
            if (expr.startsWith("^", index)) {
                val caretIdx = index
                val afterCaret = caretIdx + 1
                if (afterCaret < len && expr[afterCaret] == '(') {
                    val closeParen = findMatchingParen(expr, afterCaret)
                    if (closeParen != -1 && closeParen + 1 < len) {
                        val afterIndex = closeParen + 1
                        val isSqrt = expr.startsWith("sqrt(", afterIndex) || expr.startsWith("√(", afterIndex)
                        if (isSqrt) {
                            val isSqrtLong = expr.startsWith("sqrt(", afterIndex)
                            val prefixLen = if (isSqrtLong) 5 else 2
                            val innerStartLocal = afterIndex + prefixLen
                            val closeRad = findMatchingParen(expr, innerStartLocal - 1)
                            val innerEndLocal = if (closeRad != -1) closeRad else len
                            val radEndLocal = if (closeRad != -1) closeRad + 1 else len

                            val indexText = expr.substring(afterCaret + 1, closeParen)
                            val innerText = expr.substring(innerStartLocal, innerEndLocal)
                            val idxStartGlobal = baseOffset + afterCaret + 1
                            val idxEndGlobal = baseOffset + closeParen
                            val inStartGlobal = baseOffset + innerStartLocal
                            val inEndGlobal = baseOffset + innerEndLocal

                            segments.add(
                                FormulaSegment.RadicalSegment(
                                    hasIndex = true,
                                    indexText = indexText,
                                    indexStart = idxStartGlobal,
                                    indexEnd = idxEndGlobal,
                                    indexSegments = parse(indexText, idxStartGlobal),
                                    innerText = innerText,
                                    innerStart = inStartGlobal,
                                    innerEnd = inEndGlobal,
                                    innerSegments = parse(innerText, inStartGlobal),
                                    fullStart = baseOffset + caretIdx,
                                    fullEnd = baseOffset + radEndLocal
                                )
                            )
                            index = radEndLocal
                            continue
                        }
                    }
                }
            }

            // 2b. Check for N-th root written with superscript characters: e.g. ²√(6) or ³√(8)
            val isSuperscriptChar = expr[index] in "⁰¹²³⁴⁵⁶⁷⁸⁹ⁿⁱˣᵃᵇ"
            if (isSuperscriptChar) {
                var d = index
                while (d < len && expr[d] in "⁰¹²³⁴⁵⁶⁷⁸⁹ⁿⁱˣᵃᵇ") {
                    d++
                }
                if (d > index && d < len && (expr.startsWith("√(", d) || expr.startsWith("sqrt(", d))) {
                    val isSqrtLong = expr.startsWith("sqrt(", d)
                    val prefixLen = if (isSqrtLong) 5 else 2
                    val innerStartLocal = d + prefixLen
                    val closeRad = findMatchingParen(expr, innerStartLocal - 1)
                    val innerEndLocal = if (closeRad != -1) closeRad else len
                    val radEndLocal = if (closeRad != -1) closeRad + 1 else len

                    val supText = expr.substring(index, d)
                    val supMap = mapOf(
                        '⁰' to '0', '¹' to '1', '²' to '2', '³' to '3', '⁴' to '4',
                        '⁵' to '5', '⁶' to '6', '⁷' to '7', '⁸' to '8', '⁹' to '9',
                        'ⁿ' to 'n', 'ⁱ' to 'i', 'ˣ' to 'x', 'ᵃ' to 'a', 'ᵇ' to 'b'
                    )
                    val normalIndexText = supText.map { supMap[it] ?: it }.joinToString("")
                    val innerText = expr.substring(innerStartLocal, innerEndLocal)
                    val idxStartGlobal = baseOffset + index
                    val idxEndGlobal = baseOffset + d
                    val inStartGlobal = baseOffset + innerStartLocal
                    val inEndGlobal = baseOffset + innerEndLocal

                    segments.add(
                        FormulaSegment.RadicalSegment(
                            hasIndex = true,
                            indexText = normalIndexText,
                            indexStart = idxStartGlobal,
                            indexEnd = idxEndGlobal,
                            indexSegments = parse(normalIndexText, idxStartGlobal),
                            innerText = innerText,
                            innerStart = inStartGlobal,
                            innerEnd = inEndGlobal,
                            innerSegments = parse(innerText, inStartGlobal),
                            fullStart = idxStartGlobal,
                            fullEnd = baseOffset + radEndLocal
                        )
                    )
                    index = radEndLocal
                    continue
                }
            }

            // 3. Check for standard Radical: sqrt(...) or √(...)
            if (expr.startsWith("sqrt(", index) || expr.startsWith("√(", index)) {
                val isSqrtLong = expr.startsWith("sqrt(", index)
                val prefixLen = if (isSqrtLong) 5 else 2
                val innerStartLocal = index + prefixLen
                val closeParen = findMatchingParen(expr, innerStartLocal - 1)
                val innerEndLocal = if (closeParen != -1) closeParen else len
                val fullEndLocal = if (closeParen != -1) closeParen + 1 else len
                val innerText = expr.substring(innerStartLocal, innerEndLocal)
                val inStartGlobal = baseOffset + innerStartLocal
                val inEndGlobal = baseOffset + innerEndLocal

                segments.add(
                    FormulaSegment.RadicalSegment(
                        hasIndex = false,
                        innerText = innerText,
                        innerStart = inStartGlobal,
                        innerEnd = inEndGlobal,
                        innerSegments = parse(innerText, inStartGlobal),
                        fullStart = globalIdx,
                        fullEnd = baseOffset + fullEndLocal
                    )
                )
                index = fullEndLocal
                continue
            }

            // 4. Check for Absolute Value: |...|
            if (expr[index] == '|') {
                val nextPipe = expr.indexOf('|', index + 1)
                if (nextPipe != -1) {
                    val innerStartLocal = index + 1
                    val innerEndLocal = nextPipe
                    val fullEndLocal = nextPipe + 1
                    val innerText = expr.substring(innerStartLocal, innerEndLocal)
                    val inStartGlobal = baseOffset + innerStartLocal
                    val inEndGlobal = baseOffset + innerEndLocal

                    segments.add(
                        FormulaSegment.AbsSegment(
                            innerText = innerText,
                            innerStart = inStartGlobal,
                            innerEnd = inEndGlobal,
                            innerSegments = parse(innerText, inStartGlobal),
                            fullStart = globalIdx,
                            fullEnd = baseOffset + fullEndLocal
                        )
                    )
                    index = fullEndLocal
                    continue
                }
            }

            // 5. Check for Fractions: top-level '/'
            val slashIdx = findNextTopLevelSlash(expr, index)
            if (slashIdx != -1 && slashIdx == index) {
                // Standalone '/' without preceding numerator
                val (denText, denStartLocal, denEndLocal, fullEndLocal) = parseDenominator(expr, slashIdx + 1)
                val denStartGlobal = baseOffset + denStartLocal
                val denEndGlobal = baseOffset + denEndLocal
                segments.add(
                    FormulaSegment.FractionSegment(
                        numText = "",
                        numStart = globalIdx,
                        numEnd = globalIdx,
                        numSegments = emptyList(),
                        slashPos = globalIdx,
                        denText = denText,
                        denStart = denStartGlobal,
                        denEnd = denEndGlobal,
                        denSegments = parse(denText, denStartGlobal),
                        fullStart = globalIdx,
                        fullEnd = baseOffset + fullEndLocal
                    )
                )
                index = fullEndLocal
                continue
            }

            // 6. Check for Subscript: base_(sub) or base_{sub} or base_1 (e.g. x_{1,2} or x_1)
            val underIdx = findNextTopLevelUnderscore(expr, index)
            if (underIdx != -1 && underIdx > index && !expr.startsWith("log_", index)) {
                val prevIdx = underIdx - 1
                var b = prevIdx
                while (b >= index && (expr[b].isLetterOrDigit() || expr[b] == '.')) {
                    b--
                }
                val baseStartLocal = b + 1
                val nextSpecial = findNextSpecialChar(expr, index)
                if (nextSpecial != -1 && nextSpecial < baseStartLocal) {
                    val chunk = expr.substring(index, nextSpecial)
                    if (chunk.isNotEmpty()) {
                        segments.add(
                            FormulaSegment.SimpleText(
                                text = chunk,
                                startOffset = globalIdx,
                                endOffset = baseOffset + nextSpecial
                            )
                        )
                        index = nextSpecial
                        continue
                    }
                }

                if (baseStartLocal > index) {
                    segments.add(
                        FormulaSegment.SimpleText(
                            text = expr.substring(index, baseStartLocal),
                            startOffset = globalIdx,
                            endOffset = baseOffset + baseStartLocal
                        )
                    )
                    index = baseStartLocal
                    continue
                } else if (baseStartLocal == index && baseStartLocal < underIdx) {
                    val candidateBase = expr.substring(baseStartLocal, underIdx)
                    val (subText, subStartLocal, subEndLocal, fullEndLocal) = parseSubscriptRhs(expr, underIdx + 1)
                    val baseStartGlobal = baseOffset + baseStartLocal
                    val baseEndGlobal = baseOffset + underIdx
                    val subStartGlobal = baseOffset + subStartLocal
                    val subEndGlobal = baseOffset + subEndLocal

                    segments.add(
                        FormulaSegment.SubscriptSegment(
                            baseText = candidateBase,
                            baseStart = baseStartGlobal,
                            baseEnd = baseEndGlobal,
                            subText = subText,
                            subStart = subStartGlobal,
                            subEnd = subEndGlobal,
                            subSegments = parse(subText, subStartGlobal),
                            fullStart = baseStartGlobal,
                            fullEnd = baseOffset + fullEndLocal
                        )
                    )
                    index = fullEndLocal
                    continue
                }
            }

            // 7. Check for Power / Exponent: base^(exp) or base^digit or base²
            val caretIdx = findNextTopLevelCaret(expr, index)
            if (caretIdx != -1 && caretIdx == index) {
                // Standalone '^' without preceding base
                val (expText, expStartLocal, expEndLocal, fullEndLocal) = parseExponentRhs(expr, caretIdx + 1)
                val expStartGlobal = baseOffset + expStartLocal
                val expEndGlobal = baseOffset + expEndLocal
                segments.add(
                    FormulaSegment.PowerSegment(
                        baseText = "",
                        baseStart = globalIdx,
                        baseEnd = globalIdx,
                        expText = expText,
                        expStart = expStartGlobal,
                        expEnd = expEndGlobal,
                        expSegments = parse(expText, expStartGlobal),
                        fullStart = globalIdx,
                        fullEnd = baseOffset + fullEndLocal
                    )
                )
                index = fullEndLocal
                continue
            }

            // If a fraction is present starting after this token:
            if (slashIdx != -1 && slashIdx > index) {
                val candidateNum = expr.substring(index, slashIdx)
                val isParenGroup = candidateNum.startsWith("(") && findMatchingParen(candidateNum, 0) == candidateNum.length - 1
                val hasOperatorBefore = findTopLevelOperatorBefore(candidateNum)
                if (hasOperatorBefore != -1 && !isParenGroup) {
                    // Split before operator at depth 0 and parse recursively
                    val textEnd = index + hasOperatorBefore + 1
                    val beforeChunk = expr.substring(index, textEnd)
                    segments.addAll(parse(beforeChunk, globalIdx))
                    index = textEnd
                    continue
                } else {
                    // candidateNum is numerator
                    val (numText, numStartLocal, numEndLocal) = if (isParenGroup) {
                        Triple(candidateNum.substring(1, candidateNum.length - 1), index + 1, slashIdx - 1)
                    } else {
                        Triple(candidateNum, index, slashIdx)
                    }
                    val (denText, denStartLocal, denEndLocal, fullEndLocal) = parseDenominator(expr, slashIdx + 1)
                    val numStartGlobal = baseOffset + numStartLocal
                    val numEndGlobal = baseOffset + numEndLocal
                    val denStartGlobal = baseOffset + denStartLocal
                    val denEndGlobal = baseOffset + denEndLocal

                    segments.add(
                        FormulaSegment.FractionSegment(
                            numText = numText,
                            numStart = numStartGlobal,
                            numEnd = numEndGlobal,
                            numSegments = parse(numText, numStartGlobal),
                            slashPos = baseOffset + slashIdx,
                            denText = denText,
                            denStart = denStartGlobal,
                            denEnd = denEndGlobal,
                            denSegments = parse(denText, denStartGlobal),
                            fullStart = baseOffset + index,
                            fullEnd = baseOffset + fullEndLocal
                        )
                    )
                    index = fullEndLocal
                    continue
                }
            }

            // If a power '^' is present starting after this token:
            if (caretIdx != -1 && caretIdx > index) {
                val prevIdx = caretIdx - 1
                val isParenBase = expr[prevIdx] == ')'
                val baseStartLocal = if (isParenBase) {
                    findMatchingOpenParen(expr, prevIdx)
                } else {
                    var b = prevIdx
                    while (b >= index && (expr[b].isLetterOrDigit() || expr[b] == '.')) {
                        b--
                    }
                    b + 1
                }

                val nextSpecial = findNextSpecialChar(expr, index)
                if (nextSpecial != -1 && nextSpecial < baseStartLocal) {
                    val chunk = expr.substring(index, nextSpecial)
                    if (chunk.isNotEmpty()) {
                        segments.add(
                            FormulaSegment.SimpleText(
                                text = chunk,
                                startOffset = globalIdx,
                                endOffset = baseOffset + nextSpecial
                            )
                        )
                        index = nextSpecial
                        continue
                    }
                }

                if (baseStartLocal > index) {
                    // Split preceding text before the power base
                    segments.add(
                        FormulaSegment.SimpleText(
                            text = expr.substring(index, baseStartLocal),
                            startOffset = globalIdx,
                            endOffset = baseOffset + baseStartLocal
                        )
                    )
                    index = baseStartLocal
                    continue
                } else {
                    val (baseText, bStart, bEnd) = if (isParenBase && baseStartLocal < caretIdx) {
                        Triple(expr.substring(baseStartLocal + 1, caretIdx - 1), baseStartLocal + 1, caretIdx - 1)
                    } else {
                        Triple(expr.substring(baseStartLocal, caretIdx), baseStartLocal, caretIdx)
                    }
                    val (expText, expStartLocal, expEndLocal, fullEndLocal) = parseExponentRhs(expr, caretIdx + 1)
                    val expStartGlobal = baseOffset + expStartLocal
                    val expEndGlobal = baseOffset + expEndLocal

                    segments.add(
                        FormulaSegment.PowerSegment(
                            baseText = baseText,
                            baseStart = baseOffset + bStart,
                            baseEnd = baseOffset + bEnd,
                            expText = expText,
                            expStart = expStartGlobal,
                            expEnd = expEndGlobal,
                            expSegments = parse(expText, expStartGlobal),
                            fullStart = baseOffset + baseStartLocal,
                            fullEnd = baseOffset + fullEndLocal
                        )
                    )
                    index = fullEndLocal
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
                        baseStart = globalIdx,
                        baseEnd = baseOffset + sqIdx,
                        expText = "2",
                        expStart = baseOffset + sqIdx,
                        expEnd = baseOffset + sqIdx + 1,
                        expSegments = listOf(FormulaSegment.SimpleText("2", baseOffset + sqIdx, baseOffset + sqIdx + 1)),
                        fullStart = globalIdx,
                        fullEnd = baseOffset + sqIdx + 1
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
                        startOffset = globalIdx,
                        endOffset = baseOffset + end
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
            if (c == '/' || c == '^' || c == '|' || c == '²' || c == '_') return i
            if (expr.startsWith("sqrt(", i) || expr.startsWith("√(", i) || expr.startsWith("log_", i)) return i
            if (expr.startsWith("log[", i) || expr.startsWith("log{", i)) return i
            if (expr.startsWith("log", i) && i + 3 < len && (expr[i + 3].isDigit() || (expr[i + 3].isLetter() && expr[i + 3] != 'e' && expr[i + 3] != 'n' && expr[i + 3] != '('))) return i
            if (c.isLetterOrDigit()) {
                var d = i
                while (d < len && expr[d].isLetterOrDigit()) d++
                if (d < len && (expr.startsWith("√(", d) || expr.startsWith("sqrt(", d))) return i
            }
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
        return expr.indexOf('^', fromIndex)
    }

    private fun findNextTopLevelUnderscore(expr: String, fromIndex: Int): Int {
        return expr.indexOf('_', fromIndex)
    }

    private fun findTopLevelOperatorBefore(candidate: String): Int {
        var depth = 0
        var lastOp = -1
        for (i in 0 until candidate.length) {
            val c = candidate[i]
            if (c == '(') depth++
            else if (c == ')') depth = (depth - 1).coerceAtLeast(0)
            else if (depth == 0 && c in "+-*×÷=<>;, ") {
                lastOp = i
            }
        }
        return lastOp
    }

    private fun parseDenominator(expr: String, startIdx: Int): Quadruple<String, Int, Int, Int> {
        if (startIdx >= expr.length) {
            return Quadruple("", startIdx, startIdx, startIdx)
        }
        if (expr[startIdx] == '(') {
            val closeParen = findMatchingParen(expr, startIdx)
            if (closeParen != -1) {
                // Check if denominator is followed by another parenthesized group, e.g. (x-3)(x+3)
                var end = closeParen + 1
                while (end < expr.length && expr[end] == '(') {
                    val nextClose = findMatchingParen(expr, end)
                    if (nextClose != -1) {
                        end = nextClose + 1
                    } else break
                }
                val denText = expr.substring(startIdx + 1, closeParen)
                if (end == closeParen + 1) {
                    return Quadruple(denText, startIdx + 1, closeParen, closeParen + 1)
                } else {
                    // Full chain like (x-3)(x+3)
                    val fullDenText = expr.substring(startIdx, end)
                    return Quadruple(fullDenText, startIdx, end, end)
                }
            }
        }
        // Simple token: up to operator +, -, =, <, >, ;, or end at depth 0
        var end = startIdx
        var depth = 0
        while (end < expr.length) {
            val c = expr[end]
            if (c == '(') depth++
            else if (c == ')') depth = (depth - 1).coerceAtLeast(0)
            else if (depth == 0 && c in "+-=<>;, ") break
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

    private fun parseSubscriptRhs(expr: String, startIdx: Int): Quadruple<String, Int, Int, Int> {
        if (startIdx >= expr.length) {
            return Quadruple("", startIdx, startIdx, startIdx)
        }
        if (expr[startIdx] == '(') {
            val closeParen = findMatchingParen(expr, startIdx)
            if (closeParen != -1) {
                val subText = expr.substring(startIdx + 1, closeParen)
                return Quadruple(subText, startIdx + 1, closeParen, closeParen + 1)
            }
        } else if (expr[startIdx] == '{') {
            val closeBrace = expr.indexOf('}', startIdx)
            if (closeBrace != -1) {
                val subText = expr.substring(startIdx + 1, closeBrace)
                return Quadruple(subText, startIdx + 1, closeBrace, closeBrace + 1)
            }
        }
        // Simple token: digits or letters
        var end = startIdx
        while (end < expr.length && (expr[end].isLetterOrDigit() || expr[end] == ',')) {
            end++
        }
        val subText = expr.substring(startIdx, end)
        return Quadruple(subText, startIdx, end, end)
    }

    fun findMatchingParen(expr: String, openIdx: Int): Int {
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

    fun findMatchingOpenParen(expr: String, closeIdx: Int): Int {
        var depth = 0
        for (i in closeIdx downTo 0) {
            if (expr[i] == ')') depth++
            else if (expr[i] == '(') {
                depth--
                if (depth == 0) return i
            }
        }
        return 0
    }

    /**
     * Smart 2D navigation moving RIGHT:
     * - End of Numerator -> jumps to Denominator
     * - End of Denominator -> exits Fraction onto baseline
     * - End of Base -> enters Exponent / Subscript
     * - End of Exponent / Subscript -> exits onto baseline
     * - End of Root Index -> jumps into Radicand
     * - End of Radicand -> exits Radical onto baseline
     * - End of Log Base -> jumps into Log Argument
     * - End of Log Argument -> exits Log onto baseline
     */
    fun getNextCursorPosition(expr: String, currentPos: Int): Int {
        val safePos = currentPos.coerceIn(0, expr.length)
        val segments = parse(expr)

        fun checkSegments(list: List<FormulaSegment>): Int? {
            for (seg in list) {
                when (seg) {
                    is FormulaSegment.FractionSegment -> {
                        if (safePos in seg.numStart until seg.numEnd) {
                            val innerNext = checkSegments(seg.numSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.numEnd) {
                            return seg.denStart
                        }
                        if (safePos in seg.denStart until seg.denEnd) {
                            val innerNext = checkSegments(seg.denSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.denEnd) {
                            return seg.fullEnd
                        }
                    }
                    is FormulaSegment.PowerSegment -> {
                        if (safePos in seg.expStart until seg.expEnd) {
                            val innerNext = checkSegments(seg.expSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.baseEnd) {
                            return seg.expStart
                        }
                        if (safePos == seg.expEnd) {
                            return seg.fullEnd
                        }
                    }
                    is FormulaSegment.SubscriptSegment -> {
                        if (safePos in seg.subStart until seg.subEnd) {
                            val innerNext = checkSegments(seg.subSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.baseEnd) {
                            return seg.subStart
                        }
                        if (safePos == seg.subEnd) {
                            return seg.fullEnd
                        }
                    }
                    is FormulaSegment.RadicalSegment -> {
                        if (seg.hasIndex) {
                            if (safePos in seg.indexStart until seg.indexEnd) {
                                val innerNext = checkSegments(seg.indexSegments)
                                if (innerNext != null) return innerNext
                            }
                            if (safePos == seg.indexEnd) {
                                return seg.innerStart
                            }
                        }
                        if (safePos in seg.innerStart until seg.innerEnd) {
                            val innerNext = checkSegments(seg.innerSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos < seg.innerStart && safePos >= seg.fullStart && !seg.hasIndex) {
                            return seg.innerStart
                        }
                        if (safePos == seg.innerEnd) {
                            return seg.fullEnd
                        }
                    }
                    is FormulaSegment.LogSegment -> {
                        if (safePos in seg.baseStart until seg.baseEnd) {
                            val innerNext = checkSegments(seg.baseSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.baseEnd) {
                            return seg.argStart
                        }
                        if (safePos in seg.argStart until seg.argEnd) {
                            val innerNext = checkSegments(seg.argSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.argEnd) {
                            return seg.fullEnd
                        }
                    }
                    is FormulaSegment.AbsSegment -> {
                        if (safePos in seg.innerStart until seg.innerEnd) {
                            val innerNext = checkSegments(seg.innerSegments)
                            if (innerNext != null) return innerNext
                        }
                        if (safePos == seg.innerEnd) {
                            return seg.fullEnd
                        }
                    }
                    else -> {}
                }
            }
            return null
        }

        val jump = checkSegments(segments)
        if (jump != null) return jump

        return (safePos + 1).coerceAtMost(expr.length)
    }

    /**
     * Smart 2D navigation moving LEFT:
     * - Start of Denominator -> risale al Numeratore (at end of numerator)
     * - Start of Numerator -> exits Fraction to the left
     * - Baseline right after Fraction -> enters Denominator at the end
     * - Start of Exponent / Subscript -> exits to the left
     * - Baseline right after Exponent / Subscript -> enters at the end
     * - Start of Radicand (in N-th root) -> jumps back to index
     * - Baseline right after Radical -> enters Radicand at the end
     * - Start of Log Argument -> jumps back to base
     * - Baseline right after Log -> enters Argument at the end
     */
    fun getPrevCursorPosition(expr: String, currentPos: Int): Int {
        val safePos = currentPos.coerceIn(0, expr.length)
        val segments = parse(expr)

        fun checkSegments(list: List<FormulaSegment>): Int? {
            for (seg in list) {
                when (seg) {
                    is FormulaSegment.FractionSegment -> {
                        if (safePos == seg.denStart) {
                            return seg.numEnd
                        }
                        if (safePos > seg.denStart && safePos <= seg.denEnd) {
                            val innerPrev = checkSegments(seg.denSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.fullEnd && seg.fullEnd > seg.denEnd) {
                            return seg.denEnd
                        }
                        if (safePos > seg.numStart && safePos <= seg.numEnd) {
                            val innerPrev = checkSegments(seg.numSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.numStart) {
                            return seg.fullStart
                        }
                    }
                    is FormulaSegment.PowerSegment -> {
                        if (safePos == seg.expStart) {
                            return seg.baseEnd
                        }
                        if (safePos > seg.expStart && safePos <= seg.expEnd) {
                            val innerPrev = checkSegments(seg.expSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.fullEnd && seg.fullEnd > seg.expEnd) {
                            return seg.expEnd
                        }
                    }
                    is FormulaSegment.SubscriptSegment -> {
                        if (safePos == seg.subStart) {
                            return seg.baseEnd
                        }
                        if (safePos > seg.subStart && safePos <= seg.subEnd) {
                            val innerPrev = checkSegments(seg.subSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.fullEnd && seg.fullEnd > seg.subEnd) {
                            return seg.subEnd
                        }
                    }
                    is FormulaSegment.RadicalSegment -> {
                        if (safePos == seg.innerStart && seg.hasIndex) {
                            return seg.indexEnd
                        }
                        if (safePos > seg.innerStart && safePos <= seg.innerEnd) {
                            val innerPrev = checkSegments(seg.innerSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.innerStart) {
                            return seg.fullStart
                        }
                        if (seg.hasIndex) {
                            if (safePos > seg.indexStart && safePos <= seg.indexEnd) {
                                val innerPrev = checkSegments(seg.indexSegments)
                                if (innerPrev != null) return innerPrev
                            }
                            if (safePos == seg.indexStart) {
                                return seg.fullStart
                            }
                        }
                        if (safePos == seg.fullEnd) {
                            return seg.innerEnd
                        }
                    }
                    is FormulaSegment.LogSegment -> {
                        if (safePos == seg.argStart) {
                            return seg.baseEnd
                        }
                        if (safePos > seg.argStart && safePos <= seg.argEnd) {
                            val innerPrev = checkSegments(seg.argSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.baseStart) {
                            return seg.fullStart
                        }
                        if (safePos > seg.baseStart && safePos <= seg.baseEnd) {
                            val innerPrev = checkSegments(seg.baseSegments)
                            if (innerPrev != null) return innerPrev
                        }
                        if (safePos == seg.fullEnd) {
                            return seg.argEnd
                        }
                    }
                    is FormulaSegment.AbsSegment -> {
                        if (safePos > seg.innerStart && safePos <= seg.innerEnd) {
                            val innerPrev = checkSegments(seg.innerSegments)
                            if (innerPrev != null) return innerPrev
                        }
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
            return null
        }

        val jump = checkSegments(segments)
        if (jump != null) return jump

        return (safePos - 1).coerceAtLeast(0)
    }

    /**
     * Determines whether the cursor at [cursorPosition] is currently inside an elevated or reduced region.
     */
    fun isCursorHalved(expr: String, cursorPosition: Int): Boolean {
        val safePos = cursorPosition.coerceIn(0, expr.length)
        val segments = parse(expr)

        fun check(list: List<FormulaSegment>): Boolean {
            for (seg in list) {
                when (seg) {
                    is FormulaSegment.PowerSegment -> {
                        if (safePos in seg.expStart..seg.expEnd) return true
                    }
                    is FormulaSegment.SubscriptSegment -> {
                        if (safePos in seg.subStart..seg.subEnd) return true
                    }
                    is FormulaSegment.RadicalSegment -> {
                        if (seg.hasIndex && safePos in seg.indexStart..seg.indexEnd) return true
                        if (safePos in seg.innerStart..seg.innerEnd && check(seg.innerSegments)) return true
                    }
                    is FormulaSegment.LogSegment -> {
                        if (safePos in seg.baseStart..seg.baseEnd) return true
                        if (safePos in seg.argStart..seg.argEnd && check(seg.argSegments)) return true
                    }
                    is FormulaSegment.FractionSegment -> {
                        if (safePos in seg.numStart..seg.numEnd || safePos in seg.denStart..seg.denEnd || safePos == seg.slashPos) return true
                    }
                    else -> {}
                }
            }
            return false
        }

        return check(segments)
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
