package com.google.pixel.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.pixel.calculator.data.HistoryStorage
import com.google.pixel.calculator.data.ThemePreferences
import com.google.pixel.calculator.ui.CalculatorApp
import com.google.pixel.calculator.ui.CalculatorViewModel
import com.google.pixel.calculator.ui.theme.PixelCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = this
            val historyStorage = remember { HistoryStorage(context) }
            val themePreferences = remember { ThemePreferences(context) }
            val viewModel = remember { CalculatorViewModel(historyStorage, themePreferences) }
            val state by viewModel.uiState.collectAsState()

            PixelCalculatorTheme(themeMode = state.themeMode) {
                CalculatorApp(viewModel = viewModel, historyStorage = historyStorage)
            }
        }
    }
}
