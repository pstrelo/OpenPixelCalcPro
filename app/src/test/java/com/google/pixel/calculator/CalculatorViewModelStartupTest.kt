package com.google.pixel.calculator

import com.google.pixel.calculator.ui.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorViewModelStartupTest {

    @Test
    fun testStartupDefaultsExpressionEmptyAndSessionHistoryEmpty() {
        val viewModel = CalculatorViewModel()
        val state = viewModel.uiState.value

        // On fresh startup, expression should be empty (rendered as "0" in UI)
        assertEquals("", state.expression)

        // Session history must be empty so no previous calculations are displayed above the 0
        assertTrue("sessionHistory deve essere vuoto al riavvio dell'app", state.sessionHistory.isEmpty())
    }

    @Test
    fun testCalculationAddsToSessionHistory() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("5")
        viewModel.onOperator("+")
        viewModel.onDigit("3")
        viewModel.onEquals()

        val state = viewModel.uiState.value
        assertEquals("8", state.fullResult)
        assertEquals(1, state.sessionHistory.size)
        assertEquals("5+3", state.sessionHistory.first().first)
        assertEquals("8", state.sessionHistory.first().second)
    }

    @Test
    fun testClearResetsSessionHistoryAndExpression() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("7")
        viewModel.onOperator("×")
        viewModel.onDigit("6")
        viewModel.onEquals()

        assertEquals(1, viewModel.uiState.value.sessionHistory.size)

        viewModel.onClearAll()

        assertEquals("", viewModel.uiState.value.expression)
        assertTrue(viewModel.uiState.value.sessionHistory.isEmpty())
    }
}
