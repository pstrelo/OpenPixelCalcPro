package com.google.pixel.calculator.ui.advanced

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.google.pixel.calculator.core.advanced.AdvancedMathEngine
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive Unit Test Suite for AdvancedCalculatorViewModel in Google Math Solver Redesign.
 * Verifies R1 (thematic tabs, edit bar), R2 ('=' vs '➤' functional decoupling),
 * R3 (FORMAT instant cycling), R4 (Desmos multi-equation & root brackets), and R5 (Graph viewport).
 */
class AdvancedCalculatorViewModelTest {

    private lateinit var viewModel: AdvancedCalculatorViewModel

    @Before
    fun setUp() {
        viewModel = AdvancedCalculatorViewModel(dispatcher = Dispatchers.Unconfined)
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertEquals(1, state.equations.size)
        assertEquals(0, state.activeEquationIndex)
        assertEquals(MathSolverTab.ALGEBRA, state.activeTab)
        assertEquals(AdvancedMathEngine.ResultFormat.DECIMAL, state.activeEquation?.formatMode)
        assertEquals(ScreenViewMode.CALCULATOR_ONLY, state.screenViewMode)
    }

    @Test
    fun testThematicTabsSelection() {
        viewModel.selectTab(MathSolverTab.TRIGONOMETRY)
        assertEquals(MathSolverTab.TRIGONOMETRY, viewModel.uiState.value.activeTab)
        assertEquals(Color(0xFF10B981), viewModel.uiState.value.activeTabAccentColor)

        viewModel.selectTab(MathSolverTab.CALCULUS)
        assertEquals(MathSolverTab.CALCULUS, viewModel.uiState.value.activeTab)
        assertEquals(Color(0xFFEC4899), viewModel.uiState.value.activeTabAccentColor)

        viewModel.selectTab(MathSolverTab.ALPHABET)
        assertEquals(MathSolverTab.ALPHABET, viewModel.uiState.value.activeTab)
        assertEquals(Color(0xFFF97316), viewModel.uiState.value.activeTabAccentColor)

        viewModel.selectTab(MathSolverTab.ALGEBRA)
        assertEquals(MathSolverTab.ALGEBRA, viewModel.uiState.value.activeTab)
        assertEquals(Color(0xFFA855F7), viewModel.uiState.value.activeTabAccentColor)
    }

    @Test
    fun testFunctionalDecoupling_EqualsDoesNotEvaluate() {
        viewModel.onClearAll()
        viewModel.insertCharacter("x^2 - 4")
        viewModel.onKeyClick("=") // Inserts " = "

        val stateAfterEquals = viewModel.uiState.value
        assertEquals("x^2 - 4 = ", stateAfterEquals.activeEquation?.expression)
        assertEquals("", stateAfterEquals.activeEquation?.formattedResult) // Must NOT solve automatically!
        assertNull(stateAfterEquals.activeEquation?.lastSolveResult)

        viewModel.insertCharacter("0")
        assertEquals("x^2 - 4 = 0", viewModel.uiState.value.activeEquation?.expression)
        assertEquals("", viewModel.uiState.value.activeEquation?.formattedResult)

        // Only executes upon pressing '➤'
        viewModel.onKeyClick("➤")
        val solvedState = viewModel.uiState.value
        val result = solvedState.activeEquation?.formattedResult ?: ""
        assertTrue("Expected roots -2 and 2 in $result", result.contains("-2") && result.contains("2"))
        assertTrue("Expected square bracket notation in $result", result.startsWith("[") && result.endsWith("]"))
    }

    @Test
    fun testCursorNavigationAndInsertion() {
        viewModel.onClearAll()
        viewModel.insertCharacter("24")
        assertEquals(2, viewModel.uiState.value.activeEquation?.cursorPosition)

        viewModel.moveCursorLeft()
        assertEquals(1, viewModel.uiState.value.activeEquation?.cursorPosition)

        viewModel.insertCharacter("+")
        assertEquals("2+4", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(2, viewModel.uiState.value.activeEquation?.cursorPosition)

        // Move to start and insert
        viewModel.setCursorPosition(0)
        assertEquals(0, viewModel.uiState.value.activeEquation?.cursorPosition)
        viewModel.insertCharacter("1")
        assertEquals("12+4", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(1, viewModel.uiState.value.activeEquation?.cursorPosition)
    }

    @Test
    fun testDeleteAtCursor() {
        viewModel.onClearAll()
        viewModel.insertCharacter("123")
        viewModel.deleteAtCursor()
        assertEquals("12", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(2, viewModel.uiState.value.activeEquation?.cursorPosition)

        // Deleting character in middle
        viewModel.insertCharacter("3") // "123"
        viewModel.moveCursorLeft() // cursor at 2 (before '3')
        viewModel.deleteAtCursor() // deletes '2'
        assertEquals("13", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(1, viewModel.uiState.value.activeEquation?.cursorPosition)

        // Deleting multi-char token " = "
        viewModel.insertCharacter(" = ")
        assertEquals("1 = 3", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(4, viewModel.uiState.value.activeEquation?.cursorPosition)
        viewModel.deleteAtCursor() // Smart deletion of entire " = "
        assertEquals("13", viewModel.uiState.value.activeEquation?.expression)
        assertEquals(1, viewModel.uiState.value.activeEquation?.cursorPosition)
    }

    @Test
    fun testInstantFormatCycling() {
        viewModel.onClearAll()
        viewModel.insertCharacter("0.375")
        viewModel.executeSolve()

        assertEquals("= 0.375", viewModel.uiState.value.activeEquation?.formattedResult)

        // Cycle to FRACTION
        viewModel.cycleFormat()
        assertEquals(AdvancedMathEngine.ResultFormat.FRACTION, viewModel.uiState.value.activeEquation?.formatMode)
        assertEquals("= 3/8", viewModel.uiState.value.activeEquation?.formattedResult)

        // Cycle to ENGINEERING
        viewModel.cycleFormat()
        assertEquals(AdvancedMathEngine.ResultFormat.ENGINEERING, viewModel.uiState.value.activeEquation?.formatMode)
        assertTrue(viewModel.uiState.value.activeEquation?.formattedResult?.contains("375") == true)

        // Cycle to DMS
        viewModel.cycleFormat()
        assertEquals(AdvancedMathEngine.ResultFormat.DMS, viewModel.uiState.value.activeEquation?.formatMode)
        assertTrue(viewModel.uiState.value.activeEquation?.formattedResult?.contains("°") == true)

        // Cycle to POLAR
        viewModel.cycleFormat()
        assertEquals(AdvancedMathEngine.ResultFormat.POLAR, viewModel.uiState.value.activeEquation?.formatMode)
        assertTrue(viewModel.uiState.value.activeEquation?.formattedResult?.contains("∠") == true)

        // Cycle back to DECIMAL
        viewModel.cycleFormat()
        assertEquals(AdvancedMathEngine.ResultFormat.DECIMAL, viewModel.uiState.value.activeEquation?.formatMode)
        assertEquals("= 0.375", viewModel.uiState.value.activeEquation?.formattedResult)
    }

    @Test
    fun testMultiEquationManagement() {
        viewModel.onClearAll()
        assertEquals(1, viewModel.uiState.value.equations.size)

        viewModel.addEquation()
        viewModel.addEquation()
        viewModel.addEquation()
        assertEquals(4, viewModel.uiState.value.equations.size)

        // Cap at 4 equations
        viewModel.addEquation()
        assertEquals(4, viewModel.uiState.value.equations.size)

        // Select equation
        viewModel.selectEquation(1)
        assertEquals(1, viewModel.uiState.value.activeEquationIndex)

        // Remove equation
        viewModel.removeEquation(1)
        assertEquals(3, viewModel.uiState.value.equations.size)
        assertEquals(1, viewModel.uiState.value.activeEquationIndex)

        // Remove until only 1 remains
        viewModel.removeEquation(0)
        viewModel.removeEquation(0)
        assertEquals(1, viewModel.uiState.value.equations.size)

        // Removing the last single equation clears it without removing it
        viewModel.insertCharacter("x^2")
        viewModel.removeEquation(0)
        assertEquals(1, viewModel.uiState.value.equations.size)
        assertEquals("", viewModel.uiState.value.equations[0].expression)
    }

    @Test
    fun testMatrixSolutionBracketsForMultipleRoots() {
        viewModel.onClearAll()
        viewModel.insertCharacter("x^2 - 9 = 0")
        viewModel.executeSolve()

        val resultText = viewModel.uiState.value.activeEquation?.formattedResult ?: ""
        assertTrue("Contains square bracket start", resultText.startsWith("["))
        assertTrue("Contains square bracket end", resultText.endsWith("]"))
        assertTrue("Contains semicolon delimiter", resultText.contains(";"))
        assertTrue("Contains root -3", resultText.contains("-3"))
        assertTrue("Contains root 3", resultText.contains("3"))
    }

    @Test
    fun testGraphViewportPanAndZoom() {
        val initialVp = viewModel.uiState.value.graphViewport
        assertEquals(40f, initialVp.scaleX)

        // Pan Right (Offset +40px) -> moves centerMathX to negative (leftward in math space)
        viewModel.onGraphTransform(1f, Offset(40f, 0f))
        assertEquals(-1.0, viewModel.uiState.value.graphViewport.centerMathX, 1e-3)

        // Pan Left (Offset -80px) -> moves centerMathX to +1.0
        viewModel.onGraphTransform(1f, Offset(-80f, 0f))
        assertEquals(1.0, viewModel.uiState.value.graphViewport.centerMathX, 1e-3)

        // Pan Down (Offset +40px) -> moves centerMathY to positive (upward in math space)
        viewModel.onGraphTransform(1f, Offset(0f, 40f))
        assertEquals(1.0, viewModel.uiState.value.graphViewport.centerMathY, 1e-3)

        // Zoom In
        viewModel.onGraphTransform(2f, Offset.Zero)
        val updatedVp = viewModel.uiState.value.graphViewport
        assertEquals(80f, updatedVp.scaleX)

        // Zoom clamping test
        viewModel.onGraphTransform(10f, Offset.Zero) // 80 * 10 = 800 -> clamped to 300f
        assertEquals(300f, viewModel.uiState.value.graphViewport.scaleX)

        viewModel.onGraphTransform(0.01f, Offset.Zero) // clamped to 10f
        assertEquals(10f, viewModel.uiState.value.graphViewport.scaleX)

        // Reset
        viewModel.onResetGraph()
        assertEquals(40f, viewModel.uiState.value.graphViewport.scaleX)
        assertEquals(0.0, viewModel.uiState.value.graphViewport.centerMathX, 1e-5)
        assertEquals(0.0, viewModel.uiState.value.graphViewport.centerMathY, 1e-5)
    }

    @Test
    fun testToggleGraphViewModes() {
        assertEquals(ScreenViewMode.CALCULATOR_ONLY, viewModel.uiState.value.screenViewMode)

        viewModel.toggleGraphView()
        assertEquals(ScreenViewMode.SPLIT_SCREEN, viewModel.uiState.value.screenViewMode)

        viewModel.toggleGraphView()
        assertEquals(ScreenViewMode.CALCULATOR_ONLY, viewModel.uiState.value.screenViewMode)

        viewModel.setScreenViewMode(ScreenViewMode.FULLSCREEN_GRAPH)
        assertEquals(ScreenViewMode.FULLSCREEN_GRAPH, viewModel.uiState.value.screenViewMode)

        viewModel.toggleGraphView()
        assertEquals(ScreenViewMode.SPLIT_SCREEN, viewModel.uiState.value.screenViewMode)
    }

    @Test
    fun testToggleRadiansViaKeypadKey() {
        assertTrue(viewModel.uiState.value.isRadians)

        // Press DEG key incorporated in keypad
        viewModel.onKeyClick("DEG")
        assertFalse(viewModel.uiState.value.isRadians)

        // Test trig evaluation in DEG mode: sin(90) = 1
        viewModel.onClearAll()
        viewModel.insertText("sin(90)")
        viewModel.executeSolve()
        val eqDeg = viewModel.uiState.value.activeEquation!!
        assertTrue("Expected sin(90) = 1 in DEG mode", eqDeg.formattedResult.contains("1"))

        // Press RAD key in keypad to switch back to radians
        viewModel.onKeyClick("RAD")
        assertTrue(viewModel.uiState.value.isRadians)
    }

    @Test
    fun testCycleEquationColors() {
        val initialColor = viewModel.uiState.value.equations[0].color
        assertEquals(Color(0xFF4E75F6), initialColor)

        // Cycle color once
        viewModel.cycleEquationColor(0)
        val secondColor = viewModel.uiState.value.equations[0].color
        assertEquals(Color(0xFF10B981), secondColor)

        // Cycle through all 6 colors and ensure it wraps around
        repeat(5) {
            viewModel.cycleEquationColor(0)
        }
        assertEquals(initialColor, viewModel.uiState.value.equations[0].color)
    }

    @Test
    fun testMultiEquationVariableDefinitionAndGraphUpdate() {
        viewModel.onClearAll()

        // Equation 1: 5ax
        viewModel.insertText("5ax")

        // Add Equation 2: a = 5
        viewModel.addEquation()
        viewModel.insertText("a = 5")
        viewModel.executeSolve()

        val eq2 = viewModel.uiState.value.equations[1]
        assertEquals("a = 5", eq2.formattedResult)
        assertFalse(eq2.isError)

        // Select Equation 1 and solve
        viewModel.selectEquation(0)
        viewModel.executeSolve()

        val eq1 = viewModel.uiState.value.equations[0]
        assertFalse("Equation 1 should not error when a is defined", eq1.isError)
        assertTrue("Equation 1 roots should be x = 0", eq1.roots.contains(0.0))
        assertTrue("Equation 1 should sample curve points", eq1.sampledPoints.isNotEmpty())
    }

    @Test
    fun testInequality2D_ViewModelExecutionAndGraphing() {
        viewModel.onClearAll()
        viewModel.insertCharacter("y >= x")
        viewModel.onKeyClick("➤")

        val state = viewModel.uiState.value
        val eq = state.activeEquation
        assertNotNull(eq)
        assertTrue("Result should mention semipiano", eq!!.formattedResult.contains("Semipiano"))
        assertNotNull(eq.inequality2D)
        assertEquals("x", eq.inequality2D?.boundaryExpr)
        assertTrue(eq.inequality2D?.isAbove == true)
        assertFalse(eq.inequality2D?.isStrict == true)
        assertTrue(eq.sampledPoints.isNotEmpty())

        // Test strict 2D inequality: y + x > 2
        viewModel.onClearAll()
        viewModel.insertCharacter("y + x > 2")
        viewModel.onKeyClick("➤")

        val state2 = viewModel.uiState.value
        val eq2 = state2.activeEquation
        assertNotNull(eq2)
        assertTrue(eq2!!.inequality2D?.isStrict == true)
        assertTrue(eq2.inequality2D?.isAbove == true)
        assertTrue(eq2.formattedResult.contains("y > 2 - x"))
    }
}
