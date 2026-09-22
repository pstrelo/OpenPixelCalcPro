package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import com.google.pixel.calculator.e2e.GoogleMathSolverE2ETest
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Empirical Adversarial Test Suite for UI State Machines, Cursor Navigation & Viewport.
 * Challenger M1-2: Stress testing boundary and failure modes for:
 * 1. Cursor navigation & editing (bounds clamping, rapid backspaces, insertion at edges/middle, fuzzing)
 * 2. '=' insertion vs '➤' execution (strict separation, no auto-eval on '=', execute only on '➤' / 'EXE')
 * 3. Multi-equation list (maximum 4 cap, overflow rejection, active selection, removal to single entry)
 * 4. Graph viewport (scale bounds [10f, 300f], pan offset robustness, 40px magnetic snap tolerance)
 */
class AdversarialUIStateM12Test {

    private lateinit var viewModel: AdvancedCalculatorViewModel

    @Before
    fun setUp() {
        viewModel = AdvancedCalculatorViewModel(dispatcher = Dispatchers.Unconfined)
    }

    // =========================================================================
    // 1. CURSOR NAVIGATION & EDITING STRESS TESTS
    // =========================================================================

    @Test
    fun test_cursor_clamp_negative_out_of_bounds() {
        viewModel.onClearAll()
        viewModel.insertCharacter("12345") // length 5, cursor 5
        assertEquals(5, viewModel.uiState.value.cursorPosition)

        // Attempt setting negative positions
        viewModel.setCursorPosition(-1)
        assertEquals("Cursor must be clamped to 0 on negative input", 0, viewModel.uiState.value.cursorPosition)

        viewModel.setCursorPosition(-99999)
        assertEquals("Cursor must be clamped to 0 on extreme negative input", 0, viewModel.uiState.value.cursorPosition)

        assertEquals("Expression content must remain unchanged", "12345", viewModel.uiState.value.activeEquation?.expression)
    }

    @Test
    fun test_cursor_clamp_positive_out_of_bounds() {
        viewModel.onClearAll()
        viewModel.insertCharacter("12345") // length 5
        val len = viewModel.uiState.value.activeEquation?.expression?.length ?: 0

        viewModel.setCursorPosition(6)
        assertEquals("Cursor must be clamped to length on > length input", len, viewModel.uiState.value.cursorPosition)

        viewModel.setCursorPosition(99999)
        assertEquals("Cursor must be clamped to length on extreme positive input", len, viewModel.uiState.value.cursorPosition)

        assertEquals("Expression content must remain unchanged", "12345", viewModel.uiState.value.activeEquation?.expression)
    }

    @Test
    fun test_cursor_move_left_at_zero_clamped() {
        viewModel.onClearAll()
        viewModel.insertCharacter("ABC")
        viewModel.setCursorPosition(0)
        assertEquals(0, viewModel.uiState.value.cursorPosition)

        // Rapid repetitive left moves at 0
        for (i in 1..100) {
            viewModel.moveCursorLeft()
            assertEquals("Cursor must not decrement below 0 at iteration $i", 0, viewModel.uiState.value.cursorPosition)
        }
    }

    @Test
    fun test_cursor_move_right_at_end_clamped() {
        viewModel.onClearAll()
        viewModel.insertCharacter("XYZ") // len 3, cursor 3
        assertEquals(3, viewModel.uiState.value.cursorPosition)

        // Rapid repetitive right moves at end
        for (i in 1..100) {
            viewModel.moveCursorRight()
            assertEquals("Cursor must not increment beyond length at iteration $i", 3, viewModel.uiState.value.cursorPosition)
        }
    }

    @Test
    fun test_rapid_backspace_on_empty_string() {
        viewModel.onClearAll()
        viewModel.clearActiveEquation()
        assertEquals("", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(0, viewModel.uiState.value.cursorPosition)

        // 200 consecutive deletions on empty string
        for (i in 1..200) {
            viewModel.deleteAtCursor()
            viewModel.onBackspace()
            viewModel.onKeyClick("DEL")
            viewModel.onKeyClick("⌫")
        }

        assertEquals("Expression must still be empty", "", viewModel.uiState.value.activeEquation?.expression)
        assertEquals("Cursor must remain 0", 0, viewModel.uiState.value.cursorPosition)
    }

    @Test
    fun test_backspace_at_beginning_of_non_empty_string() {
        viewModel.onClearAll()
        viewModel.insertCharacter("GoogleMathSolver")
        viewModel.setCursorPosition(0)
        assertEquals(0, viewModel.uiState.value.cursorPosition)

        for (i in 1..20) {
            viewModel.deleteAtCursor()
            assertEquals("0", 0, viewModel.uiState.value.cursorPosition)
            assertEquals("GoogleMathSolver", viewModel.uiState.value.activeEquation?.expression)
        }
    }

    @Test
    fun test_inserting_at_beginning_middle_and_end() {
        viewModel.onClearAll()

        // 1. Insert at end (append)
        viewModel.insertCharacter("AC") // "AC", cursor 2
        assertEquals("AC", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(2, viewModel.uiState.value.cursorPosition)

        // 2. Insert in middle
        viewModel.setCursorPosition(1)
        viewModel.insertCharacter("B") // "ABC", cursor 2
        assertEquals("ABC", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(2, viewModel.uiState.value.cursorPosition)

        // 3. Insert at beginning
        viewModel.setCursorPosition(0)
        viewModel.insertCharacter("PRE_") // "PRE_ABC", cursor 4
        assertEquals("PRE_ABC", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(4, viewModel.uiState.value.cursorPosition)

        // 4. Insert at end
        viewModel.setCursorPosition(viewModel.uiState.value.activeEquation!!.expression.length)
        viewModel.insertCharacter("_POST")
        assertEquals("PRE_ABC_POST", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(12, viewModel.uiState.value.cursorPosition)
    }

    @Test
    fun test_multi_char_token_equals_smart_deletion() {
        viewModel.onClearAll()
        viewModel.insertCharacter("x")
        viewModel.onKeyClick("=") // Appends " = "
        assertEquals("x = ", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(4, viewModel.uiState.value.cursorPosition)

        // Single DEL should remove all 3 characters of " = "
        viewModel.deleteAtCursor()
        assertEquals("x", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(1, viewModel.uiState.value.cursorPosition)
    }

    @Test
    fun test_cursor_random_fuzzing_invariant() {
        viewModel.onClearAll()
        val random = Random(42)
        val tokens = listOf("1", "x", "+", "-", "*", "/", "^", "sin(", "sqrt(", " = ", "y", "(", ")")

        for (step in 1..1000) {
            val op = random.nextInt(6)
            when (op) {
                0 -> viewModel.insertCharacter(tokens[random.nextInt(tokens.size)])
                1 -> viewModel.moveCursorLeft()
                2 -> viewModel.moveCursorRight()
                3 -> viewModel.deleteAtCursor()
                4 -> viewModel.setCursorPosition(random.nextInt(-50, 150))
                5 -> viewModel.onKeyClick("DEL")
            }

            val eq = viewModel.uiState.value.activeEquation!!
            val exprLen = eq.expression.length
            val pos = eq.cursorPosition

            assertTrue(
                "Invariant violated at step $step: cursor $pos not in [0, $exprLen]",
                pos in 0..exprLen
            )
        }
    }

    // =========================================================================
    // 2. EQUALS '=' INSERTION VS EXECUTION '➤' STRESS TESTS
    // =========================================================================

    @Test
    fun test_equals_never_executes_calculation_across_diverse_expressions() {
        val expressionsToTest = listOf(
            "2 + 2",
            "100 / 4",
            "x^2 - 16",
            "sin(30)",
            "d/dx(x^2, 2)",
            "x + 5 < 10",
            "3 * (4 + 5)",
            "5 / 0",
            "",
            "   "
        )

        for (expr in expressionsToTest) {
            viewModel.onClearAll()
            if (expr.isNotEmpty()) {
                viewModel.insertCharacter(expr)
            }

            // Click '='
            viewModel.onKeyClick("=")

            val state = viewModel.uiState.value.activeEquation!!
            val expectedExpr = if (expr.isEmpty()) " = " else "$expr = "
            assertEquals("Expected expression to contain inserted '=' without evaluation", expectedExpr, state.expression)
            assertEquals("formattedResult must be empty upon '=' insertion", "", state.formattedResult)
            assertNull("lastSolveResult must be null upon '=' insertion", state.lastSolveResult)
            assertTrue("roots list must be empty upon '=' insertion", state.roots.isEmpty())
            assertFalse("isError must be false upon '=' insertion", state.isError)
            assertTrue("shadedIntervals must be empty upon '=' insertion", viewModel.uiState.value.shadedIntervals.isEmpty())
        }
    }

    @Test
    fun test_repeated_equals_insertion_never_evaluates() {
        viewModel.onClearAll()
        for (i in 1..10) {
            viewModel.onKeyClick("=")
            val eq = viewModel.uiState.value.activeEquation!!
            assertEquals("Iteration $i: formattedResult must remain empty", "", eq.formattedResult)
            assertNull("Iteration $i: lastSolveResult must remain null", eq.lastSolveResult)
        }
        assertEquals(" =  =  =  =  =  =  =  =  =  = ", viewModel.uiState.value.activeEquation?.expression)
    }

    @Test
    fun test_execute_arrow_and_exe_solve_active_equation() {
        // 1. Arithmetic evaluation via '➤'
        viewModel.onClearAll()
        viewModel.insertCharacter("45 * 2")
        viewModel.onKeyClick("➤")
        val arithResult = viewModel.uiState.value.activeEquation?.formattedResult ?: ""
        assertEquals("= 90", arithResult)
        assertEquals(90.0, (viewModel.uiState.value.activeEquation?.lastSolveResult as AdvancedMathEngine.SolveResult.SingleValue).raw, 1e-5)

        // 2. Polynomial root solving via 'EXE'
        viewModel.onClearAll()
        viewModel.insertCharacter("x^2 - 25 = 0")
        viewModel.onKeyClick("EXE")
        val polyResult = viewModel.uiState.value.activeEquation?.formattedResult ?: ""
        assertTrue("Poly result must have square matrix bracket: $polyResult", polyResult.startsWith("[") && polyResult.endsWith("]"))
        assertTrue("Poly result must contain -5 and 5: $polyResult", polyResult.contains("-5") && polyResult.contains("5"))

        // 3. Inequality solving via direct onExecute()
        viewModel.onClearAll()
        viewModel.insertCharacter("2x - 8 > 0")
        viewModel.onExecute()
        val ineqResult = viewModel.uiState.value.activeEquation?.formattedResult ?: ""
        assertTrue("Inequality result must contain interval: $ineqResult", ineqResult.contains("4"))
        assertTrue("Shaded intervals must be populated", viewModel.uiState.value.shadedIntervals.isNotEmpty())
    }

    @Test
    fun test_execute_on_blank_or_whitespace_is_safe_noop() {
        viewModel.onClearAll()
        viewModel.clearActiveEquation()
        viewModel.onKeyClick("➤")
        assertEquals("", viewModel.uiState.value.activeEquation?.formattedResult)
        assertNull(viewModel.uiState.value.activeEquation?.lastSolveResult)

        viewModel.insertCharacter("   \t  ")
        viewModel.onKeyClick("➤")
        assertEquals("", viewModel.uiState.value.activeEquation?.formattedResult)
        assertNull(viewModel.uiState.value.activeEquation?.lastSolveResult)
    }

    @Test
    fun test_execute_on_syntax_error_handles_cleanly_without_crash() {
        val malformedCases = listOf(
            "5 + * 3",
            "((((12)",
            "/ 5",
            "^ 2",
            "sin(",
            "x = = = 0"
        )

        for (malformed in malformedCases) {
            viewModel.onClearAll()
            viewModel.insertCharacter(malformed)
            viewModel.executeSolve()

            val state = viewModel.uiState.value.activeEquation!!
            assertTrue("Expected error flag or valid AST recovery for '$malformed'", state.isError || state.formattedResult.isNotEmpty())
        }
    }

    // =========================================================================
    // 3. MULTI-EQUATION LIST & MANAGEMENT STRESS TESTS
    // =========================================================================

    @Test
    fun test_add_equations_up_to_maximum_4_with_distinct_badge_colors() {
        viewModel.onClearAll()
        assertEquals(1, viewModel.uiState.value.equations.size)
        assertEquals(Color(0xFF4E75F6), viewModel.uiState.value.equations[0].color)

        // Add 2nd
        viewModel.addEquation()
        assertEquals(2, viewModel.uiState.value.equations.size)
        assertEquals(1, viewModel.uiState.value.activeEquationIndex)
        assertEquals(Color(0xFF10B981), viewModel.uiState.value.equations[1].color)

        // Add 3rd
        viewModel.addEquation()
        assertEquals(3, viewModel.uiState.value.equations.size)
        assertEquals(2, viewModel.uiState.value.activeEquationIndex)
        assertEquals(Color(0xFFF59E0B), viewModel.uiState.value.equations[2].color)

        // Add 4th
        viewModel.addEquation()
        assertEquals(4, viewModel.uiState.value.equations.size)
        assertEquals(3, viewModel.uiState.value.activeEquationIndex)
        assertEquals(Color(0xFFEC4899), viewModel.uiState.value.equations[3].color)
    }

    @Test
    fun test_attempt_exceeding_maximum_4_equations_strictly_rejected() {
        viewModel.onClearAll()
        for (i in 1..3) {
            viewModel.addEquation()
        }
        assertEquals("Must reach exactly 4 equations", 4, viewModel.uiState.value.equations.size)

        // Attempt adding 5th, 6th, ..., 20th equation
        for (i in 5..20) {
            viewModel.addEquation()
            viewModel.addFormula()
            assertEquals("Equation count must be capped strictly at 4 at attempt $i", 4, viewModel.uiState.value.equations.size)
        }
    }

    @Test
    fun test_equation_selection_boundaries_and_invalid_indices() {
        viewModel.onClearAll()
        viewModel.addEquation()
        viewModel.addEquation() // 3 equations (indices 0, 1, 2)
        assertEquals(3, viewModel.uiState.value.equations.size)

        // Valid selections
        viewModel.selectEquation(0)
        assertEquals(0, viewModel.uiState.value.activeEquationIndex)
        viewModel.selectEquation(1)
        assertEquals(1, viewModel.uiState.value.activeEquationIndex)
        viewModel.selectEquation(2)
        assertEquals(2, viewModel.uiState.value.activeEquationIndex)

        // Out-of-bounds selections: index must not change
        viewModel.selectEquation(-1)
        assertEquals("Negative index selection must be ignored", 2, viewModel.uiState.value.activeEquationIndex)

        viewModel.selectEquation(3)
        assertEquals("Index >= size selection must be ignored", 2, viewModel.uiState.value.activeEquationIndex)

        viewModel.selectEquation(999)
        assertEquals("Extreme index selection must be ignored", 2, viewModel.uiState.value.activeEquationIndex)

        // Select by ID
        val eq1Id = viewModel.uiState.value.equations[0].id.toString()
        viewModel.selectEquationById(eq1Id)
        assertEquals(0, viewModel.uiState.value.activeEquationIndex)

        // Select by invalid ID
        viewModel.selectEquationById("invalid_id_xyz")
        assertEquals("Invalid ID selection must leave activeEquationIndex unchanged", 0, viewModel.uiState.value.activeEquationIndex)
    }

    @Test
    fun test_equation_removal_boundary_and_active_index_adjustment() {
        viewModel.onClearAll()
        viewModel.addEquation()
        viewModel.addEquation()
        viewModel.addEquation() // 4 equations: index 0, 1, 2, 3
        assertEquals(4, viewModel.uiState.value.equations.size)

        // Active equation is index 3. Remove index 1
        viewModel.selectEquation(3)
        viewModel.removeEquation(1)
        assertEquals(3, viewModel.uiState.value.equations.size)

        // Remove equation at last index (index 2)
        viewModel.selectEquation(2)
        viewModel.removeEquation(2)
        assertEquals(2, viewModel.uiState.value.equations.size)
        assertTrue("Active equation index must be in valid range [0, 1]", viewModel.uiState.value.activeEquationIndex in 0..1)

        // Remove until only 1 remains
        viewModel.removeEquation(0)
        assertEquals(1, viewModel.uiState.value.equations.size)
        assertEquals(0, viewModel.uiState.value.activeEquationIndex)

        // Removing the only remaining equation must NOT delete the entry, but clear its expression
        viewModel.insertCharacter("x^2 + 1")
        assertEquals("x^2 + 1", viewModel.uiState.value.activeEquation?.expression)
        viewModel.removeEquation(0)
        assertEquals("Must remain at 1 equation", 1, viewModel.uiState.value.equations.size)
        assertEquals("Expression of sole equation must be cleared", "", viewModel.uiState.value.equations[0].expression)
        assertEquals("Cursor must be 0", 0, viewModel.uiState.value.equations[0].cursorPosition)

        // Invalid removal calls must be safe no-ops
        viewModel.removeEquation(-1)
        viewModel.removeEquation(100)
        assertEquals(1, viewModel.uiState.value.equations.size)
    }

    // =========================================================================
    // 4. GRAPH VIEWPORT STRESS TESTS
    // =========================================================================

    @Test
    fun test_viewport_scale_clamp_lower_bound_10f() {
        val initialScale = viewModel.uiState.value.graphViewport.scaleX
        assertEquals(40f, initialScale)

        // Zoom out aggressively
        viewModel.onGraphTransform(0.1f, Offset.Zero) // 40 * 0.1 = 4 -> clamped to 10
        assertEquals(10f, viewModel.uiState.value.graphViewport.scaleX)

        // Extreme zoom out factor
        viewModel.onGraphTransform(0.00001f, Offset.Zero)
        assertEquals(10f, viewModel.uiState.value.graphViewport.scaleX)

        // Zero factor zoom
        viewModel.onGraphTransform(0f, Offset.Zero)
        assertEquals(10f, viewModel.uiState.value.graphViewport.scaleX)

        // Negative zoom factor
        viewModel.onGraphTransform(-5f, Offset.Zero)
        assertEquals("Negative zoom must be clamped to 10f", 10f, viewModel.uiState.value.graphViewport.scaleX)

        // Overload with centroid
        viewModel.onGraphTransform(Offset(100f, 100f), Offset(10f, 10f), 0.001f)
        assertEquals(10f, viewModel.uiState.value.graphViewport.scaleX)
    }

    @Test
    fun test_viewport_scale_clamp_upper_bound_300f() {
        // Zoom in aggressively
        viewModel.onGraphTransform(100f, Offset.Zero) // 40 * 100 = 4000 -> clamped to 300
        assertEquals(300f, viewModel.uiState.value.graphViewport.scaleX)

        // Extreme zoom in factor
        viewModel.onGraphTransform(100000f, Offset.Zero)
        assertEquals(300f, viewModel.uiState.value.graphViewport.scaleX)

        // Centroid-anchored overload
        viewModel.onGraphTransform(Offset(500f, 500f), Offset(0f, 0f), 50f)
        assertEquals(300f, viewModel.uiState.value.graphViewport.scaleX)
    }

    @Test
    fun test_viewport_pan_extreme_offsets_and_round_trip() {
        val canvasSize = Size(1080f, 2400f)

        // Pan with massive offset
        viewModel.onGraphTransform(1.0f, Offset(500_000f, -500_000f))
        val vp = viewModel.uiState.value.graphViewport

        assertFalse("centerMathX must not be NaN", vp.centerMathX.isNaN())
        assertFalse("centerMathX must not be Infinite", vp.centerMathX.isInfinite())
        assertFalse("centerMathY must not be NaN", vp.centerMathY.isNaN())
        assertFalse("centerMathY must not be Infinite", vp.centerMathY.isInfinite())

        // Round-trip coordinate conversion fidelity test
        val testMathCoords = listOf(
            Pair(0.0, 0.0),
            Pair(-5.5, 12.3),
            Pair(100.0, -250.0),
            Pair(-0.001, 0.001),
            Pair(vp.centerMathX, vp.centerMathY)
        )

        for ((mx, my) in testMathCoords) {
            val screen = vp.mathToScreen(mx, my, canvasSize)
            val (backX, backY) = vp.screenToMath(screen, canvasSize)
            assertEquals("X round-trip failed for ($mx, $my)", mx, backX, 5e-4)
            assertEquals("Y round-trip failed for ($mx, $my)", my, backY, 5e-4)
        }
    }

    @Test
    fun test_root_touch_snap_tolerance_40px_boundary() {
        val cX = 500f
        val cY = 500f
        val scale = 40f
        // Root at x = 3.0, y = 0.0
        val rootMathX = 3.0f
        val rootMathY = 0.0f
        val rootScreenX = cX + (rootMathX * scale) // 620f
        val rootScreenY = cY - (rootMathY * scale) // 500f

        val curve = listOf(
            AdvancedMathEngine.Point(2.9f, -0.5f),
            AdvancedMathEngine.Point(rootMathX, rootMathY),
            AdvancedMathEngine.Point(3.1f, 0.6f)
        )

        // 1. Tap within 40px threshold (distance = 39.0px) -> MUST SNAP
        val tapInside = Pair(rootScreenX + 39.0f, rootScreenY)
        val snappedInside = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            tapInside.first, tapInside.second, cX, cY, scale, listOf(curve), 40f
        )
        assertEquals("Must snap to rootMathX when within 40px", rootMathX, snappedInside.first, 1e-4f)
        assertEquals("Must snap to rootMathY when within 40px", rootMathY, snappedInside.second, 1e-4f)

        // 2. Tap at distance = 39.9px -> MUST SNAP
        val tapBorderInside = Pair(rootScreenX + 39.9f, rootScreenY)
        val snappedBorderInside = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            tapBorderInside.first, tapBorderInside.second, cX, cY, scale, listOf(curve), 40f
        )
        assertEquals("Must snap to rootMathX at 39.9px", rootMathX, snappedBorderInside.first, 1e-4f)
        assertEquals("Must snap to rootMathY at 39.9px", rootMathY, snappedBorderInside.second, 1e-4f)

        // 3. Tap at distance = 40.0px (exact threshold, dist < 40f is false) -> MUST NOT SNAP
        val tapExact = Pair(rootScreenX + 40.0f, rootScreenY) // ScreenX = 660f -> MathX = (660 - 500)/40 = 4.0
        val snappedExact = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            tapExact.first, tapExact.second, cX, cY, scale, listOf(curve), 40f
        )
        assertEquals("Must not snap at exact 40px border; returns tapped math X", 4.0f, snappedExact.first, 1e-2f)
        assertEquals("Must not snap at exact 40px border; returns tapped math Y", 0.0f, snappedExact.second, 1e-2f)

        // 4. Tap at distance = 45.0px -> MUST NOT SNAP
        val tapOutside = Pair(rootScreenX + 45.0f, rootScreenY)
        val snappedOutside = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            tapOutside.first, tapOutside.second, cX, cY, scale, listOf(curve), 40f
        )
        assertNotEquals("Must not snap to root when outside 40px threshold", rootMathX, snappedOutside.first)

        // 5. Multiple roots: snaps to Euclidean nearest
        val root2MathX = -3.0f
        val root2MathY = 0.0f
        val curveMulti = listOf(
            AdvancedMathEngine.Point(root2MathX, root2MathY),
            AdvancedMathEngine.Point(rootMathX, rootMathY)
        )
        // Tap close to root2 (dist = 10px from root2, dist = 250px from root1)
        val tapNearRoot2 = Pair(cX + (root2MathX * scale) + 10f, cY)
        val snappedMulti = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            tapNearRoot2.first, tapNearRoot2.second, cX, cY, scale, listOf(curveMulti), 40f
        )
        assertEquals("Must snap to nearest root (-3.0)", root2MathX, snappedMulti.first, 1e-4f)

        // 6. Curve with null discontinuity points (e.g. 1/x or tan(x)) -> safely skips null without crash
        val curveWithNulls = listOf(
            null,
            AdvancedMathEngine.Point(5f, 5f),
            null
        )
        val snappedNullSafe = GoogleMathSolverE2ETest.GraphViewportMath.snapToNearestPoint(
            cX + (5f * scale), cY - (5f * scale), cX, cY, scale, listOf(curveWithNulls), 40f
        )
        assertEquals(5f, snappedNullSafe.first, 1e-4f)
        assertEquals(5f, snappedNullSafe.second, 1e-4f)
    }

    @Test
    fun test_reset_graph_viewport_restores_defaults() {
        viewModel.onGraphTransform(5f, Offset(200f, -150f))
        assertNotEquals(40f, viewModel.uiState.value.graphViewport.scaleX)
        assertNotEquals(0.0, viewModel.uiState.value.graphViewport.centerMathX)

        viewModel.selectGraphPoint(Pair(3.5f, 2.0f))
        assertNotNull(viewModel.uiState.value.selectedPoint)
        assertNotNull(viewModel.uiState.value.selectedGraphPoint)

        viewModel.onResetGraph()
        assertEquals(40f, viewModel.uiState.value.graphViewport.scaleX)
        assertEquals(0.0, viewModel.uiState.value.graphViewport.centerMathX, 1e-5)
        assertEquals(0.0, viewModel.uiState.value.graphViewport.centerMathY, 1e-5)
        assertNull(viewModel.uiState.value.selectedPoint)
        assertNull(viewModel.uiState.value.selectedGraphPoint)
    }
}
