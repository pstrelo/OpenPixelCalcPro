package com.google.pixel.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.pixel.calculator.data.HistoryStorage
import com.google.pixel.calculator.data.ThemePreferences
import com.google.pixel.calculator.ui.CalculatorApp
import com.google.pixel.calculator.ui.CalculatorViewModel
import com.google.pixel.calculator.ui.theme.PixelCalculatorTheme

class MainActivity : ComponentActivity() {

    private val historyStorage by lazy { HistoryStorage(applicationContext) }
    private val themePreferences by lazy { ThemePreferences(applicationContext) }

    private val viewModel: CalculatorViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalculatorViewModel(historyStorage, themePreferences) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsState()

            PixelCalculatorTheme(themeMode = state.themeMode) {
                CalculatorApp(viewModel = viewModel, historyStorage = historyStorage)
            }
        }
    }
}
