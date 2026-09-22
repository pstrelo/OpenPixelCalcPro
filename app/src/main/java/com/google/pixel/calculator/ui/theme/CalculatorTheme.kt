package com.google.pixel.calculator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.pixel.calculator.data.AppThemeMode

/**
 * Ruoli cromatici specifici per la calcolatrice che rispettano le regole:
 * - Tasto uguale con colore accentuato
 * - Tasti non numerici (operatori e controlli) con gradazione tonale diversa dai tasti numerici
 * - Tasti numerici con tonalità neutra / morbida
 */
data class CalculatorColors(
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val numericContainer: Color,
    val numericContent: Color,
    val operatorContainer: Color,
    val operatorContent: Color,
    val controlContainer: Color,
    val controlContent: Color,
    val equalsContainer: Color,
    val equalsContent: Color,
    val scientificContainer: Color,
    val scientificContent: Color,
    val historyBackground: Color,
    val historyDivider: Color,
    val isDark: Boolean
)

val LocalCalculatorColors = staticCompositionLocalOf<CalculatorColors> {
    error("Nessun CalculatorColors fornito")
}

object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCalculatorColors.current
}

// 1. TEMA CHIARO CLASSICO PIXEL
private val PixelLightCalculatorColors = CalculatorColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1E),
    onSurfaceVariant = Color(0xFF44474E),
    numericContainer = Color(0xFFF1F2F6),
    numericContent = Color(0xFF191C1E),
    operatorContainer = Color(0xFFE2E3EC),
    operatorContent = Color(0xFF191C1E),
    controlContainer = Color(0xFFE2E3EC),
    controlContent = Color(0xFF191C1E),
    equalsContainer = Color(0xFF354558), // Tasto uguale scuro in risalto stile Google Pixel Light
    equalsContent = Color(0xFFFFFFFF),
    scientificContainer = Color(0xFFE8E9F2),
    scientificContent = Color(0xFF191C1E),
    historyBackground = Color(0xFFF8F9FC),
    historyDivider = Color(0xFFE1E2E8),
    isDark = false
)

// 2. TEMA SCURO CLASSICO PIXEL
private val PixelDarkCalculatorColors = CalculatorColors(
    background = Color(0xFF131418),
    surface = Color(0xFF131418),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFF8E9099),
    numericContainer = Color(0xFF26282E),
    numericContent = Color(0xFFE2E2E6),
    operatorContainer = Color(0xFF373942), // Gradazione leggermente più chiara
    operatorContent = Color(0xFFE2E2E6),
    controlContainer = Color(0xFF373942),
    controlContent = Color(0xFFE2E2E6),
    equalsContainer = Color(0xFFD3E3FD), // Azzurro pastello d'accento Pixel Dark
    equalsContent = Color(0xFF041E49),
    scientificContainer = Color(0xFF2D2F36),
    scientificContent = Color(0xFFE2E2E6),
    historyBackground = Color(0xFF191A20),
    historyDivider = Color(0xFF2B2D35),
    isDark = true
)

// 3. TEMA CANDY POP (VIVACE CHIARO - dalla foto utente)
private val CandyPopCalculatorColors = CalculatorColors(
    background = Color(0xFFFAF7F5),
    surface = Color(0xFFFAF7F5),
    onSurface = Color(0xFF28252C),
    onSurfaceVariant = Color(0xFF6B6572),
    numericContainer = Color(0xFFD8F3DC), // Menta pastello morbido
    numericContent = Color(0xFF1B4332),
    operatorContainer = Color(0xFFFFCCD5), // Rosa pastello fragola
    operatorContent = Color(0xFF590D22),
    controlContainer = Color(0xFFFF6B8B), // Rosso corallo vivace per AC e controlli
    controlContent = Color(0xFFFFFFFF),
    equalsContainer = Color(0xFFFFD152), // Giallo sole caldo a contrasto
    equalsContent = Color(0xFF3D2A00),
    scientificContainer = Color(0xFFE8F5E9),
    scientificContent = Color(0xFF1B4332),
    historyBackground = Color(0xFFF5EFEA),
    historyDivider = Color(0xFFE3D8CE),
    isDark = false
)

// 4. TEMA RETRO CYBER (VIVACE SCURO - dalla foto utente)
private val RetroCyberCalculatorColors = CalculatorColors(
    background = Color(0xFF14161D),
    surface = Color(0xFF14161D),
    onSurface = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF9096A6),
    numericContainer = Color(0xFF1E222C), // Chassis scuro
    numericContent = Color(0xFF34D399),   // Numeri ciano/smeraldo brillante
    operatorContainer = Color(0xFFFF3366), // Rosa neon/magenta vivace
    operatorContent = Color(0xFFFFFFFF),
    controlContainer = Color(0xFF2563EB), // Blu cobalto elettrico per AC e controlli
    controlContent = Color(0xFFFFFFFF),
    equalsContainer = Color(0xFFFACC15), // Giallo agrumi fluo d'accento
    equalsContent = Color(0xFF1C1500),
    scientificContainer = Color(0xFF242937),
    scientificContent = Color(0xFF38BDF8),
    historyBackground = Color(0xFF1B1E28),
    historyDivider = Color(0xFF2D3342),
    isDark = true
)

@Composable
fun PixelCalculatorTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.CANDY_POP -> false
        AppThemeMode.RETRO_CYBER -> true
    }

    val (m3ColorScheme, calcColors) = when (themeMode) {
        AppThemeMode.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Dynamic Color Monet reale di sistema
                val dynamicScheme = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                val colors = if (isDark) {
                    CalculatorColors(
                        background = dynamicScheme.surface,
                        surface = dynamicScheme.surface,
                        onSurface = dynamicScheme.onSurface,
                        onSurfaceVariant = dynamicScheme.onSurfaceVariant,
                        numericContainer = dynamicScheme.surfaceContainer,
                        numericContent = dynamicScheme.onSurface,
                        operatorContainer = dynamicScheme.surfaceContainerHigh,
                        operatorContent = dynamicScheme.onSurface,
                        controlContainer = dynamicScheme.surfaceContainerHigh,
                        controlContent = dynamicScheme.onSurface,
                        equalsContainer = dynamicScheme.primaryContainer,
                        equalsContent = dynamicScheme.onPrimaryContainer,
                        scientificContainer = dynamicScheme.surfaceContainerLowest,
                        scientificContent = dynamicScheme.onSurface,
                        historyBackground = dynamicScheme.surfaceContainerLow,
                        historyDivider = dynamicScheme.outlineVariant,
                        isDark = true
                    )
                } else {
                    CalculatorColors(
                        background = dynamicScheme.surface,
                        surface = dynamicScheme.surface,
                        onSurface = dynamicScheme.onSurface,
                        onSurfaceVariant = dynamicScheme.onSurfaceVariant,
                        numericContainer = dynamicScheme.surfaceContainerHighest,
                        numericContent = dynamicScheme.onSurface,
                        operatorContainer = dynamicScheme.secondaryContainer,
                        operatorContent = dynamicScheme.onSecondaryContainer,
                        controlContainer = dynamicScheme.secondaryContainer,
                        controlContent = dynamicScheme.onSecondaryContainer,
                        equalsContainer = dynamicScheme.primary,
                        equalsContent = dynamicScheme.onPrimary,
                        scientificContainer = dynamicScheme.surfaceContainerHigh,
                        scientificContent = dynamicScheme.onSurface,
                        historyBackground = dynamicScheme.surfaceContainerLow,
                        historyDivider = dynamicScheme.outlineVariant,
                        isDark = false
                    )
                }
                Pair(dynamicScheme, colors)
            } else {
                if (isDark) {
                    Pair(darkColorScheme(), PixelDarkCalculatorColors)
                } else {
                    Pair(lightColorScheme(), PixelLightCalculatorColors)
                }
            }
        }
        AppThemeMode.LIGHT -> Pair(
            lightColorScheme(
                primary = PixelLightCalculatorColors.equalsContainer,
                onPrimary = PixelLightCalculatorColors.equalsContent,
                surface = PixelLightCalculatorColors.surface,
                onSurface = PixelLightCalculatorColors.onSurface
            ),
            PixelLightCalculatorColors
        )
        AppThemeMode.DARK -> Pair(
            darkColorScheme(
                primary = PixelDarkCalculatorColors.equalsContainer,
                onPrimary = PixelDarkCalculatorColors.equalsContent,
                surface = PixelDarkCalculatorColors.surface,
                onSurface = PixelDarkCalculatorColors.onSurface
            ),
            PixelDarkCalculatorColors
        )
        AppThemeMode.CANDY_POP -> Pair(
            lightColorScheme(
                primary = CandyPopCalculatorColors.equalsContainer,
                onPrimary = CandyPopCalculatorColors.equalsContent,
                surface = CandyPopCalculatorColors.surface,
                onSurface = CandyPopCalculatorColors.onSurface
            ),
            CandyPopCalculatorColors
        )
        AppThemeMode.RETRO_CYBER -> Pair(
            darkColorScheme(
                primary = RetroCyberCalculatorColors.equalsContainer,
                onPrimary = RetroCyberCalculatorColors.equalsContent,
                surface = RetroCyberCalculatorColors.surface,
                onSurface = RetroCyberCalculatorColors.onSurface
            ),
            RetroCyberCalculatorColors
        )
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = PixelTypography,
            content = content
        )
    }
}
