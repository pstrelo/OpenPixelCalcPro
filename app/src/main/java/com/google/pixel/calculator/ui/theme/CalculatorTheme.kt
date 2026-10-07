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
 * Specific chromatic roles for the calculator adhering to rules:
 * - Equals key with emphasized accent color
 * - Non-numeric keys (operators and controls) with tonal gradation distinct from numeric keys
 * - Numeric keys with neutral / soft tone
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
    error("No CalculatorColors provided")
}

object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCalculatorColors.current
}

// 1. CLASSIC STANDARD LIGHT THEME
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
    equalsContainer = Color(0xFF354558), // Distinct dark equals key in Google Pixel Light style
    equalsContent = Color(0xFFFFFFFF),
    scientificContainer = Color(0xFFF1F2F6),
    scientificContent = Color(0xFF191C1E),
    historyBackground = Color(0xFFF8F9FC),
    historyDivider = Color(0xFFE1E2E8),
    isDark = false
)

// 2. CLASSIC STANDARD DARK THEME
private val PixelDarkCalculatorColors = CalculatorColors(
    background = Color(0xFF131418),
    surface = Color(0xFF131418),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFF8E9099),
    numericContainer = Color(0xFF26282E),
    numericContent = Color(0xFFE2E2E6),
    operatorContainer = Color(0xFF373942), // Slightly lighter gradation
    operatorContent = Color(0xFFE2E2E6),
    controlContainer = Color(0xFF373942),
    controlContent = Color(0xFFE2E2E6),
    equalsContainer = Color(0xFFD3E3FD), // Pixel Dark pastel accent blue
    equalsContent = Color(0xFF041E49),
    scientificContainer = Color(0xFF26282E),
    scientificContent = Color(0xFFE2E2E6),
    historyBackground = Color(0xFF191A20),
    historyDivider = Color(0xFF2B2D35),
    isDark = true
)

// 3. CANDY POP THEME (VIBRANT LIGHT)
private val CandyPopCalculatorColors = CalculatorColors(
    background = Color(0xFFFAF7F5),
    surface = Color(0xFFFAF7F5),
    onSurface = Color(0xFF28252C),
    onSurfaceVariant = Color(0xFF6B6572),
    numericContainer = Color(0xFFD8F3DC), // Soft pastel mint
    numericContent = Color(0xFF1B4332),
    operatorContainer = Color(0xFFFFCCD5), // Strawberry pastel pink
    operatorContent = Color(0xFF590D22),
    controlContainer = Color(0xFFFF6B8B), // Vibrant coral red for AC and controls
    controlContent = Color(0xFFFFFFFF),
    equalsContainer = Color(0xFFFFD152), // Contrasting warm sunshine yellow
    equalsContent = Color(0xFF3D2A00),
    scientificContainer = Color(0xFFD8F3DC),
    scientificContent = Color(0xFF1B4332),
    historyBackground = Color(0xFFF5EFEA),
    historyDivider = Color(0xFFE3D8CE),
    isDark = false
)

// 4. RETRO CYBER THEME (VIBRANT DARK)
private val RetroCyberCalculatorColors = CalculatorColors(
    background = Color(0xFF14161D),
    surface = Color(0xFF14161D),
    onSurface = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF9096A6),
    numericContainer = Color(0xFF1E222C), // Dark chassis
    numericContent = Color(0xFF34D399),   // Bright cyan/emerald numbers
    operatorContainer = Color(0xFFFF3366), // Vibrant neon magenta
    operatorContent = Color(0xFFFFFFFF),
    controlContainer = Color(0xFF2563EB), // Electric cobalt blue for AC and controls
    controlContent = Color(0xFFFFFFFF),
    equalsContainer = Color(0xFFFACC15), // Fluorescent citrus yellow accent
    equalsContent = Color(0xFF1C1500),
    scientificContainer = Color(0xFF1E222C),
    scientificContent = Color(0xFF34D399),
    historyBackground = Color(0xFF1B1E28),
    historyDivider = Color(0xFF2D3342),
    isDark = true
)

@Composable
fun PixelCalculatorTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM_AUTO,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> systemInDark
        AppThemeMode.SYSTEM_LIGHT -> false
        AppThemeMode.SYSTEM_DARK -> true
        AppThemeMode.STANDARD_LIGHT -> false
        AppThemeMode.STANDARD_DARK -> true
        AppThemeMode.CANDY_POP -> false
        AppThemeMode.RETRO_CYBER -> true
    }

    val isSystemDynamic = themeMode == AppThemeMode.SYSTEM_AUTO ||
            themeMode == AppThemeMode.SYSTEM_LIGHT ||
            themeMode == AppThemeMode.SYSTEM_DARK

    val (m3ColorScheme, calcColors) = when {
        isSystemDynamic -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Real system Monet Dynamic Color with coherent accents
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
                        scientificContainer = dynamicScheme.surfaceContainer,
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
                        scientificContainer = dynamicScheme.surfaceContainerHighest,
                        scientificContent = dynamicScheme.onSurface,
                        historyBackground = dynamicScheme.surfaceContainerLow,
                        historyDivider = dynamicScheme.outlineVariant,
                        isDark = false
                    )
                }
                Pair(dynamicScheme, colors)
            } else {
                if (isDark) {
                    Pair(
                        darkColorScheme(
                            primary = PixelDarkCalculatorColors.equalsContainer,
                            onPrimary = PixelDarkCalculatorColors.equalsContent,
                            primaryContainer = PixelDarkCalculatorColors.equalsContainer,
                            onPrimaryContainer = PixelDarkCalculatorColors.equalsContent,
                            secondaryContainer = PixelDarkCalculatorColors.operatorContainer,
                            onSecondaryContainer = PixelDarkCalculatorColors.operatorContent,
                            surface = PixelDarkCalculatorColors.surface,
                            onSurface = PixelDarkCalculatorColors.onSurface,
                            surfaceVariant = PixelDarkCalculatorColors.operatorContainer,
                            onSurfaceVariant = PixelDarkCalculatorColors.onSurfaceVariant
                        ),
                        PixelDarkCalculatorColors
                    )
                } else {
                    Pair(
                        lightColorScheme(
                            primary = PixelLightCalculatorColors.equalsContainer,
                            onPrimary = PixelLightCalculatorColors.equalsContent,
                            primaryContainer = Color(0xFFD3E3FD),
                            onPrimaryContainer = Color(0xFF041E49),
                            secondaryContainer = PixelLightCalculatorColors.operatorContainer,
                            onSecondaryContainer = PixelLightCalculatorColors.operatorContent,
                            surface = PixelLightCalculatorColors.surface,
                            onSurface = PixelLightCalculatorColors.onSurface,
                            surfaceVariant = PixelLightCalculatorColors.operatorContainer,
                            onSurfaceVariant = PixelLightCalculatorColors.onSurfaceVariant
                        ),
                        PixelLightCalculatorColors
                    )
                }
            }
        }
        themeMode == AppThemeMode.STANDARD_LIGHT -> Pair(
            lightColorScheme(
                primary = PixelLightCalculatorColors.equalsContainer,
                onPrimary = PixelLightCalculatorColors.equalsContent,
                primaryContainer = Color(0xFFD3E3FD),
                onPrimaryContainer = Color(0xFF041E49),
                secondaryContainer = PixelLightCalculatorColors.operatorContainer,
                onSecondaryContainer = PixelLightCalculatorColors.operatorContent,
                surface = PixelLightCalculatorColors.surface,
                onSurface = PixelLightCalculatorColors.onSurface,
                surfaceVariant = PixelLightCalculatorColors.operatorContainer,
                onSurfaceVariant = PixelLightCalculatorColors.onSurfaceVariant
            ),
            PixelLightCalculatorColors
        )
        themeMode == AppThemeMode.STANDARD_DARK -> Pair(
            darkColorScheme(
                primary = PixelDarkCalculatorColors.equalsContainer,
                onPrimary = PixelDarkCalculatorColors.equalsContent,
                primaryContainer = PixelDarkCalculatorColors.equalsContainer,
                onPrimaryContainer = PixelDarkCalculatorColors.equalsContent,
                secondaryContainer = PixelDarkCalculatorColors.operatorContainer,
                onSecondaryContainer = PixelDarkCalculatorColors.operatorContent,
                surface = PixelDarkCalculatorColors.surface,
                onSurface = PixelDarkCalculatorColors.onSurface,
                surfaceVariant = PixelDarkCalculatorColors.operatorContainer,
                onSurfaceVariant = PixelDarkCalculatorColors.onSurfaceVariant
            ),
            PixelDarkCalculatorColors
        )
        themeMode == AppThemeMode.CANDY_POP -> Pair(
            lightColorScheme(
                primary = CandyPopCalculatorColors.equalsContainer,
                onPrimary = CandyPopCalculatorColors.equalsContent,
                primaryContainer = CandyPopCalculatorColors.equalsContainer,
                onPrimaryContainer = CandyPopCalculatorColors.equalsContent,
                secondaryContainer = CandyPopCalculatorColors.operatorContainer,
                onSecondaryContainer = CandyPopCalculatorColors.operatorContent,
                surface = CandyPopCalculatorColors.surface,
                onSurface = CandyPopCalculatorColors.onSurface,
                surfaceVariant = CandyPopCalculatorColors.operatorContainer,
                onSurfaceVariant = CandyPopCalculatorColors.onSurfaceVariant
            ),
            CandyPopCalculatorColors
        )
        themeMode == AppThemeMode.RETRO_CYBER -> Pair(
            darkColorScheme(
                primary = RetroCyberCalculatorColors.equalsContainer,
                onPrimary = RetroCyberCalculatorColors.equalsContent,
                primaryContainer = RetroCyberCalculatorColors.equalsContainer,
                onPrimaryContainer = RetroCyberCalculatorColors.equalsContent,
                secondaryContainer = RetroCyberCalculatorColors.operatorContainer,
                onSecondaryContainer = RetroCyberCalculatorColors.operatorContent,
                surface = RetroCyberCalculatorColors.surface,
                onSurface = RetroCyberCalculatorColors.onSurface,
                surfaceVariant = RetroCyberCalculatorColors.operatorContainer,
                onSurfaceVariant = RetroCyberCalculatorColors.onSurfaceVariant
            ),
            RetroCyberCalculatorColors
        )
        else -> Pair(lightColorScheme(), PixelLightCalculatorColors)
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = PixelTypography,
            content = content
        )
    }
}
