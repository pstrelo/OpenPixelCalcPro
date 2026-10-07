package com.google.pixel.calculator.ui.toolbox

import androidx.compose.runtime.Composable

/**
 * Unit converter screen.
 * Delegates to ConverterCalculatorView.
 */
@Composable
fun ConverterView(
    onBack: () -> Unit
) {
    ConverterCalculatorView(onBack = onBack)
}
