package com.google.pixel.calculator

import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class EasterEggTest {

    @Test
    fun testEasterEggTriggeredWith26Plus04() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigit("2")
        viewModel.onDigit("6")
        viewModel.onOperator("+")
        viewModel.onDigit("0")
        viewModel.onDigit("4")
        
        assertEquals("26+04", viewModel.uiState.value.expression)
        
        viewModel.onEquals()
        
        assertEquals(ActiveScreen.EASTER_EGG, viewModel.uiState.value.activeScreen)
    }

    @Test
    fun testEasterEggTriggeredWith26Plus4() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigit("2")
        viewModel.onDigit("6")
        viewModel.onOperator("+")
        viewModel.onDigit("4")
        
        viewModel.onEquals()
        
        assertEquals(ActiveScreen.EASTER_EGG, viewModel.uiState.value.activeScreen)
    }

    @Test
    fun testNonEasterEggExpressionDoesNotTriggerEasterEgg() {
        val viewModel = CalculatorViewModel()
        viewModel.onDigit("2")
        viewModel.onDigit("5")
        viewModel.onOperator("+")
        viewModel.onDigit("4")
        
        assertEquals("25+4", viewModel.uiState.value.expression)
        assertEquals(ActiveScreen.MAIN_CALCULATOR, viewModel.uiState.value.activeScreen)
    }

    @Test
    fun testBackFromEasterEggReturnsToMainCalculator() {
        val viewModel = CalculatorViewModel()
        viewModel.navigateTo(ActiveScreen.EASTER_EGG)
        assertEquals(ActiveScreen.EASTER_EGG, viewModel.uiState.value.activeScreen)

        viewModel.navigateTo(ActiveScreen.MAIN_CALCULATOR)
        assertEquals(ActiveScreen.MAIN_CALCULATOR, viewModel.uiState.value.activeScreen)
    }
}
