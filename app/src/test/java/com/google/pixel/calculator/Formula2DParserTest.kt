package com.google.pixel.calculator

import com.google.pixel.calculator.ui.advanced.Formula2DParser
import com.google.pixel.calculator.ui.advanced.FormulaSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Formula2DParserTest {

    @Test
    fun testParseImage1QuadraticFormula() {
        val expr = "x_(1,2) = (-b ± sqrt(b^(2) - 4ac))/(2a)"
        val segments = Formula2DParser.parse(expr)

        assertTrue(segments.isNotEmpty())
        val firstSeg = segments[0]
        assertTrue("First segment should be SubscriptSegment for x_(1,2)", firstSeg is FormulaSegment.SubscriptSegment)
        val subSeg = firstSeg as FormulaSegment.SubscriptSegment
        assertEquals("x", subSeg.baseText)
        assertEquals("1,2", subSeg.subText)

        val fracSeg = segments.filterIsInstance<FormulaSegment.FractionSegment>().firstOrNull()
        assertTrue("Should contain a FractionSegment", fracSeg != null)
        assertEquals("-b ± sqrt(b^(2) - 4ac)", fracSeg!!.numText)
        assertEquals("2a", fracSeg.denText)

        // Nested radical inside numerator
        val radSeg = fracSeg.numSegments.filterIsInstance<FormulaSegment.RadicalSegment>().firstOrNull()
        assertTrue("Numerator should contain a RadicalSegment", radSeg != null)
        assertEquals("b^(2) - 4ac", radSeg!!.innerText)

        // Nested power inside radical
        val powSeg = radSeg.innerSegments.filterIsInstance<FormulaSegment.PowerSegment>().firstOrNull()
        assertTrue("Radical should contain PowerSegment b^2", powSeg != null)
        assertEquals("b", powSeg!!.baseText)
        assertEquals("2", powSeg.expText)
    }

    @Test
    fun testParseImage2ExponentialEquations() {
        // Equation (a): 3^(x-1) + 3 = 3^(2x)
        val exprA = "3^(x-1) + 3 = 3^(2x)"
        val segsA = Formula2DParser.parse(exprA)
        val powersA = segsA.filterIsInstance<FormulaSegment.PowerSegment>()
        assertEquals(2, powersA.size)
        assertEquals("3", powersA[0].baseText)
        assertEquals("x-1", powersA[0].expText)
        assertEquals("3", powersA[1].baseText)
        assertEquals("2x", powersA[1].expText)

        // Equation (b): 2^(2) + 3^(x) - 1 = 0
        val exprB = "2^(2) + 3^(x) - 1 = 0"
        val segsB = Formula2DParser.parse(exprB)
        val powersB = segsB.filterIsInstance<FormulaSegment.PowerSegment>()
        assertEquals(2, powersB.size)
        assertEquals("2", powersB[0].baseText)
        assertEquals("2", powersB[0].expText)
        assertEquals("3", powersB[1].baseText)
        assertEquals("x", powersB[1].expText)
    }

    @Test
    fun testParseImage3RationalEquations() {
        // (x(x-3) + 3(x+3) + (x^(2) - 12))/((x-3)(x+3)) = 0
        val expr = "(x(x-3) + 3(x+3) + (x^(2) - 12))/((x-3)(x+3)) = 0"
        val segments = Formula2DParser.parse(expr)

        val fracSeg = segments.filterIsInstance<FormulaSegment.FractionSegment>().firstOrNull()
        assertTrue("Should contain a FractionSegment", fracSeg != null)
        assertEquals("x(x-3) + 3(x+3) + (x^(2) - 12)", fracSeg!!.numText)
        assertEquals("(x-3)(x+3)", fracSeg.denText)

        // Nested power inside numerator
        val powSeg = fracSeg.numSegments.filterIsInstance<FormulaSegment.PowerSegment>().firstOrNull()
        assertTrue("Numerator should contain PowerSegment x^2", powSeg != null)
        assertEquals("x", powSeg!!.baseText)
        assertEquals("2", powSeg.expText)
    }

    @Test
    fun testNthRootParsingAndNavigation() {
        // ^(3)√(27) -> N-th root with index 3 and radicand 27
        val expr = "^(3)√(27)"
        val segments = Formula2DParser.parse(expr)
        assertEquals(1, segments.size)

        val radSeg = segments[0] as FormulaSegment.RadicalSegment
        assertTrue("Should have index", radSeg.hasIndex)
        assertEquals("3", radSeg.indexText)
        assertEquals("27", radSeg.innerText)

        // Navigation: from end of index (pos 3, after '3') right jumps to innerStart (pos 6, start of 27)
        val afterIndexPos = radSeg.indexEnd
        val nextPos = Formula2DParser.getNextCursorPosition(expr, afterIndexPos)
        assertEquals(radSeg.innerStart, nextPos)

        // From end of radicand (pos 8), right exits to fullEnd (pos 9)
        val exitPos = Formula2DParser.getNextCursorPosition(expr, radSeg.innerEnd)
        assertEquals(radSeg.fullEnd, exitPos)

        // Moving left from start of radicand (pos 6) jumps back to indexEnd (pos 3)
        val prevPos = Formula2DParser.getPrevCursorPosition(expr, radSeg.innerStart)
        assertEquals(radSeg.indexEnd, prevPos)
    }

    @Test
    fun testLogWithBaseParsingAndNavigation() {
        // log_(2)(8) -> base 2, argument 8
        val expr = "log_(2)(8)"
        val segments = Formula2DParser.parse(expr)
        assertEquals(1, segments.size)

        val logSeg = segments[0] as FormulaSegment.LogSegment
        assertEquals("2", logSeg.baseText)
        assertEquals("8", logSeg.argText)

        // Navigation: from end of base right jumps to argStart
        val nextFromBase = Formula2DParser.getNextCursorPosition(expr, logSeg.baseEnd)
        assertEquals(logSeg.argStart, nextFromBase)

        // From end of argument right exits to baseline fullEnd
        val nextFromArg = Formula2DParser.getNextCursorPosition(expr, logSeg.argEnd)
        assertEquals(logSeg.fullEnd, nextFromArg)

        // From start of argument left jumps back to baseEnd
        val prevFromArg = Formula2DParser.getPrevCursorPosition(expr, logSeg.argStart)
        assertEquals(logSeg.baseEnd, prevFromArg)
    }

    @Test
    fun testSubscriptNavigation() {
        // x_(1,2) + 5
        val expr = "x_(1,2) + 5"
        val segments = Formula2DParser.parse(expr)
        val subSeg = segments[0] as FormulaSegment.SubscriptSegment

        assertEquals("x", subSeg.baseText)
        assertEquals("1,2", subSeg.subText)

        // From end of base 'x' right enters subStart
        val enterSub = Formula2DParser.getNextCursorPosition(expr, subSeg.baseEnd)
        assertEquals(subSeg.subStart, enterSub)

        // From end of subText right exits to fullEnd
        val exitSub = Formula2DParser.getNextCursorPosition(expr, subSeg.subEnd)
        assertEquals(subSeg.fullEnd, exitSub)

        // Cursor halved inside subscript
        assertTrue(Formula2DParser.isCursorHalved(expr, subSeg.subStart))
    }

    @Test
    fun testFractionSmartNavigation() {
        // (1)/(x)
        val expr = "(1)/(x)"
        val segments = Formula2DParser.parse(expr)
        val frac = segments[0] as FormulaSegment.FractionSegment

        // Moving right at numEnd jumps to denStart
        val jumpToDen = Formula2DParser.getNextCursorPosition(expr, frac.numEnd)
        assertEquals(frac.denStart, jumpToDen)

        // Moving right at denEnd exits fraction to fullEnd
        val exitFrac = Formula2DParser.getNextCursorPosition(expr, frac.denEnd)
        assertEquals(frac.fullEnd, exitFrac)

        // Moving left at denStart risale al numeratore (numEnd)
        val jumpToNum = Formula2DParser.getPrevCursorPosition(expr, frac.denStart)
        assertEquals(frac.numEnd, jumpToNum)
    }

    @Test
    fun testUserGoalTargetEquation() {
        // Target equation from user request: (2^(3x+2))*2^4X- 1/(2+x) + log2(20)
        val expr = "(2^(3x+2))*2^4X- 1/(2+x) + log2(20)"
        val segments = Formula2DParser.parse(expr)

        assertTrue("Segments should not be empty", segments.isNotEmpty())

        // 1. First segment: SimpleText("(")
        val seg0 = segments[0] as FormulaSegment.SimpleText
        assertEquals("(", seg0.text)

        // 2. Second segment: PowerSegment for 2^(3x+2)
        val pow1 = segments[1] as FormulaSegment.PowerSegment
        assertEquals("2", pow1.baseText)
        assertEquals("3x+2", pow1.expText)

        // 3. Third segment: SimpleText(")*")
        val seg2 = segments[2] as FormulaSegment.SimpleText
        assertEquals(")*", seg2.text)

        // 4. Fourth segment: PowerSegment for 2^4X
        val pow2 = segments[3] as FormulaSegment.PowerSegment
        assertEquals("2", pow2.baseText)
        assertEquals("4X", pow2.expText)

        // 5. Fifth segment: SimpleText("- ")
        val seg4 = segments[4] as FormulaSegment.SimpleText
        assertEquals("- ", seg4.text)

        // 6. Sixth segment: FractionSegment for 1/(2+x)
        val frac = segments[5] as FormulaSegment.FractionSegment
        assertEquals("1", frac.numText)
        assertEquals("2+x", frac.denText)

        // 7. Seventh segment: SimpleText(" + ")
        val seg6 = segments[6] as FormulaSegment.SimpleText
        assertEquals(" + ", seg6.text)

        // 8. Eighth segment: LogSegment for log2(20)
        val log = segments[7] as FormulaSegment.LogSegment
        assertEquals("2", log.baseText)
        assertEquals("20", log.argText)

        // Verify Cursor Halving:
        // Inside power 1 exponent: halved
        assertTrue("Cursor should be halved in 3x+2", Formula2DParser.isCursorHalved(expr, pow1.expStart))
        // Outside power on baseline: full
        org.junit.Assert.assertFalse("Cursor should be full on baseline", Formula2DParser.isCursorHalved(expr, pow1.fullEnd))
        // Inside fraction denominator: halved
        assertTrue("Cursor should be halved in fraction denominator", Formula2DParser.isCursorHalved(expr, frac.denStart))
        // Inside log base: halved
        assertTrue("Cursor should be halved in log base", Formula2DParser.isCursorHalved(expr, log.baseStart))

        // Verify 2D Navigation across all compartments:
        // Exponent end -> baseline
        assertEquals(pow1.fullEnd, Formula2DParser.getNextCursorPosition(expr, pow1.expEnd))
        // Fraction numerator end -> drops to denominator start
        assertEquals(frac.denStart, Formula2DParser.getNextCursorPosition(expr, frac.numEnd))
        // Fraction denominator start -> risale al numeratore end
        assertEquals(frac.numEnd, Formula2DParser.getPrevCursorPosition(expr, frac.denStart))
        // Fraction denominator end -> exits to baseline
        assertEquals(frac.fullEnd, Formula2DParser.getNextCursorPosition(expr, frac.denEnd))
        // Log base end -> jumps to argument start
        assertEquals(log.argStart, Formula2DParser.getNextCursorPosition(expr, log.baseEnd))
        // Log argument start -> jumps back to base end
        assertEquals(log.baseEnd, Formula2DParser.getPrevCursorPosition(expr, log.argStart))
        // Log argument end -> exits to baseline
        assertEquals(log.fullEnd, Formula2DParser.getNextCursorPosition(expr, log.argEnd))
    }

    @Test
    fun testDirectNthRootAndLogNavigation() {
        // Direct N-th root written with superscript index: ³√(8)
        val expr = "³√(8)"
        val segments = Formula2DParser.parse(expr)
        assertEquals(1, segments.size)

        val rad = segments[0] as FormulaSegment.RadicalSegment
        assertTrue("³√(8) must have hasIndex=true", rad.hasIndex)
        assertEquals("3", rad.indexText)
        assertEquals("8", rad.innerText)

        // Cursor halved inside index apice
        assertTrue(Formula2DParser.isCursorHalved(expr, rad.indexStart))

        // Moving right from index end jumps into radicand start
        assertEquals(rad.innerStart, Formula2DParser.getNextCursorPosition(expr, rad.indexEnd))

        // Moving left from radicand start jumps back to index end
        assertEquals(rad.indexEnd, Formula2DParser.getPrevCursorPosition(expr, rad.innerStart))

        // Moving right from radicand end exits to fullEnd
        assertEquals(rad.fullEnd, Formula2DParser.getNextCursorPosition(expr, rad.innerEnd))
    }

    @Test
    fun testNormalDigitsBeforeRootIsMultiplication() {
        // Normal digits like 5√(2) are multiplication, not n-th root index
        val expr = "5√(2)"
        val segments = Formula2DParser.parse(expr)
        assertEquals(2, segments.size)

        val simpleText = segments[0] as FormulaSegment.SimpleText
        assertEquals("5", simpleText.text)

        val rad = segments[1] as FormulaSegment.RadicalSegment
        org.junit.Assert.assertFalse("5√(2) must have hasIndex=false", rad.hasIndex)
        assertEquals("2", rad.innerText)
    }
}
