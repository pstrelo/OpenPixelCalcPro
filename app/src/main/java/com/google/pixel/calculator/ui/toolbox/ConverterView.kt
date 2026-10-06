package com.google.pixel.calculator.ui.toolbox

import androidx.compose.runtime.Composable

/**
 * Schermata di conversione unità.
 * Reindirizzata alla nuova versione in stile iPhone (ConverterCalculatorView).
 */
@Composable
fun ConverterView(
    onBack: () -> Unit
) {
    ConverterCalculatorView(onBack = onBack)
}
