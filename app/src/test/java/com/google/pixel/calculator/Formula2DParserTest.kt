package com.google.pixel.calculator

import com.google.pixel.calculator.ui.advanced.Formula2DParser
import com.google.pixel.calculator.ui.advanced.FormulaSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Formula2DParserTest {

    @Test
    fun testFractionParsing() {
        val segments = Formula2DParser.parse("1/x")
        assertEquals(1, segments.size)
        val frac = segments[0] as FormulaSegment.FractionSegment
        assertEquals("1", frac.numText)
        assertEquals("x", frac.denText)
        assertEquals(0, frac.numStart)
        assertEquals(1, frac.numEnd)
        assertEquals(2, frac.denStart)
        assertEquals(3, frac.denEnd)
    }

    @Test
    fun testFractionEmptyDenominator() {
        val segments = Formula2DParser.parse("1/")
        assertEquals(1, segments.size)
        val frac = segments[0] as FormulaSegment.FractionSegment
        assertEquals("1", frac.numText)
        assertEquals("", frac.denText)
        assertEquals(2, frac.denStart)
    }

    @Test
    fun testFractionCursorNavigation() {
        // "1/x" has length 3: '1' at 0..1, '/' at 1, 'x' at 2..3
        // Moving right from end of numerator (1) should drop into denominator (2)
        val nextFromNum = Formula2DParser.getNextCursorPosition("1/x", 1)
        assertEquals(2, nextFromNum)

        // Moving left from start of denominator (2) should rise up to end of numerator (1)
        // "quando arriva in battuta sul denominatore risale al numeratore"
        val prevFromDen = Formula2DParser.getPrevCursorPosition("1/x", 2)
        assertEquals(1, prevFromDen)

        // Moving right from end of denominator (3) should exit the fraction (3)
        val nextFromDen = Formula2DParser.getNextCursorPosition("1/x", 3)
        assertEquals(3, nextFromDen)
    }

    @Test
    fun testExponentParsingAndNavigation() {
        // "x^(3+2)+5":
        // 'x' at 0..1, '^(' at 1..3, '3+2' at 3..6, ')' at 6..7, '+5' at 7..9
        val segments = Formula2DParser.parse("x^(3+2)+5")
        assertEquals(2, segments.size)
        assertTrue(segments[0] is FormulaSegment.PowerSegment)
        val power = segments[0] as FormulaSegment.PowerSegment
        assertEquals("x", power.baseText)
        assertEquals("3+2", power.expText)

        // Moving right from base (1) enters exponent at 3
        val nextFromBase = Formula2DParser.getNextCursorPosition("x^(3+2)+5", 1)
        assertEquals(3, nextFromBase)

        // Moving right from end of exponent (6) exits to baseline (7)
        val nextFromExp = Formula2DParser.getNextCursorPosition("x^(3+2)+5", 6)
        assertEquals(7, nextFromExp)

        // Moving left from baseline right after exponent (7) enters exponent (6)
        val prevFromBase = Formula2DParser.getPrevCursorPosition("x^(3+2)+5", 7)
        assertEquals(6, prevFromBase)

        // Moving left from start of exponent (3) exits to base (1)
        val prevFromExpStart = Formula2DParser.getPrevCursorPosition("x^(3+2)+5", 3)
        assertEquals(1, prevFromExpStart)
    }
}
