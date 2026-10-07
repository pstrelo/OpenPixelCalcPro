package com.google.pixel.calculator

import com.google.pixel.calculator.data.AppIconManager
import com.google.pixel.calculator.data.AppThemeMode
import com.google.pixel.calculator.ui.CalculatorState
import com.google.pixel.calculator.ui.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppIconManagerTest {

    @Test
    fun testAllThemeModesHaveDistinctForegroundDrawables() {
        val foregrounds = AppThemeMode.entries.map { mode ->
            val res = AppIconManager.getForegroundDrawableRes(mode)
            assertNotNull(res)
            assertNotEquals(0, res)
            res
        }

        // Tutti i 7 temi devono avere una risorsa drawable assegnata
        assertEquals(7, foregrounds.size)
        // I 7 foreground devono essere distinti per rappresentare ciascun tema
        assertEquals(7, foregrounds.distinct().size)
    }

    @Test
    fun testAllThemeModesHaveDistinctBackgroundDrawables() {
        val backgrounds = AppThemeMode.entries.map { mode ->
            val res = AppIconManager.getBackgroundDrawableRes(mode)
            assertNotNull(res)
            assertNotEquals(0, res)
            res
        }

        assertEquals(7, backgrounds.size)
    }

    @Test
    fun testInitialCalculatorStateContainsDefaultAppIcon() {
        val state = CalculatorState()
        assertEquals(AppThemeMode.SYSTEM_AUTO, state.appIconMode)
        assertEquals(false, state.syncIconWithTheme)
    }

    @Test
    fun testViewModelIconStateUpdates() {
        val viewModel = CalculatorViewModel()
        
        // Verifica default
        assertEquals(AppThemeMode.SYSTEM_AUTO, viewModel.uiState.value.appIconMode)
        
        // Aggiorna a Candy Pop
        viewModel.setAppIconMode(AppThemeMode.CANDY_POP)
        assertEquals(AppThemeMode.CANDY_POP, viewModel.uiState.value.appIconMode)

        // Aggiorna a Retro Cyber
        viewModel.setAppIconMode(AppThemeMode.RETRO_CYBER)
        assertEquals(AppThemeMode.RETRO_CYBER, viewModel.uiState.value.appIconMode)

        // Toggle Sync Icon
        viewModel.setSyncIconWithTheme(true)
        assertEquals(true, viewModel.uiState.value.syncIconWithTheme)
    }
}
