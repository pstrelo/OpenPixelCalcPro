package com.google.pixel.calculator.ui.theme

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.pixel.calculator.data.AppIconManager
import com.google.pixel.calculator.data.AppThemeMode

data class ThemeIconColors(
    val background: Color,
    val flower: Color,
    val symbols: Color,
    val equalsCircle: Color,
    val equalsSign: Color
)

@Composable
fun getThemeIconColors(themeMode: AppThemeMode): ThemeIconColors {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()

    return when (themeMode) {
        AppThemeMode.SYSTEM_AUTO -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val scheme = if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                if (isSystemDark) {
                    ThemeIconColors(
                        background = scheme.surface,
                        flower = scheme.surfaceContainerHigh,
                        symbols = scheme.onSurface,
                        equalsCircle = scheme.primaryContainer,
                        equalsSign = scheme.onPrimaryContainer
                    )
                } else {
                    ThemeIconColors(
                        background = scheme.surface,
                        flower = scheme.secondaryContainer,
                        symbols = scheme.onSecondaryContainer,
                        equalsCircle = scheme.primary,
                        equalsSign = scheme.onPrimary
                    )
                }
            } else {
                ThemeIconColors(
                    background = Color(0xFFFFFFFF),
                    flower = Color(0xFFDBE2FB),
                    symbols = Color(0xFF4A5166),
                    equalsCircle = Color(0xFF495F8E),
                    equalsSign = Color.White
                )
            }
        }
        AppThemeMode.SYSTEM_LIGHT -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val scheme = dynamicLightColorScheme(context)
                ThemeIconColors(
                    background = scheme.surface,
                    flower = scheme.secondaryContainer,
                    symbols = scheme.onSecondaryContainer,
                    equalsCircle = scheme.primary,
                    equalsSign = scheme.onPrimary
                )
            } else {
                ThemeIconColors(
                    background = Color(0xFFF8F9FA),
                    flower = Color(0xFFD3E3FD),
                    symbols = Color(0xFF44474E),
                    equalsCircle = Color(0xFF00639B),
                    equalsSign = Color.White
                )
            }
        }
        AppThemeMode.SYSTEM_DARK -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val scheme = dynamicDarkColorScheme(context)
                ThemeIconColors(
                    background = scheme.surface,
                    flower = scheme.surfaceContainerHigh,
                    symbols = scheme.onSurface,
                    equalsCircle = scheme.primaryContainer,
                    equalsSign = scheme.onPrimaryContainer
                )
            } else {
                ThemeIconColors(
                    background = Color(0xFF131418),
                    flower = Color(0xFF26282E),
                    symbols = Color(0xFFE2E2E6),
                    equalsCircle = Color(0xFFA8C7FA),
                    equalsSign = Color(0xFF041E49)
                )
            }
        }
        AppThemeMode.STANDARD_LIGHT -> ThemeIconColors(
            background = Color(0xFFFFFFFF),
            flower = Color(0xFFE2E3EC),
            symbols = Color(0xFF191C1E),
            equalsCircle = Color(0xFF354558),
            equalsSign = Color(0xFFFFFFFF)
        )
        AppThemeMode.STANDARD_DARK -> ThemeIconColors(
            background = Color(0xFF131418),
            flower = Color(0xFF26282E),
            symbols = Color(0xFFE2E2E6),
            equalsCircle = Color(0xFFD3E3FD),
            equalsSign = Color(0xFF041E49)
        )
        AppThemeMode.CANDY_POP -> ThemeIconColors(
            background = Color(0xFFFAF7F5),
            flower = Color(0xFFD8F3DC),
            symbols = Color(0xFFFF6B8B),
            equalsCircle = Color(0xFFFFD152),
            equalsSign = Color(0xFF3D2A00)
        )
        AppThemeMode.RETRO_CYBER -> ThemeIconColors(
            background = Color(0xFF14161D),
            flower = Color(0xFF1E222C),
            symbols = Color(0xFF34D399),
            equalsCircle = Color(0xFFFACC15),
            equalsSign = Color(0xFF1C1500)
        )
    }
}

/**
 * Anteprima grafica dell'icona dell'applicazione per un dato tema.
 */
@Composable
fun PixelAppIconPreview(
    themeMode: AppThemeMode,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    elevation: Dp = 4.dp
) {
    val cornerRadius = size * 0.23f
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val colors = getThemeIconColors(themeMode)

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .background(colors.background)
            .border(0.75.dp, Color.Black.copy(alpha = 0.08f), shape),
        contentAlignment = Alignment.Center
    ) {
        val fgRes = AppIconManager.getForegroundDrawableRes(themeMode)
        Image(
            painter = painterResource(id = fgRes),
            contentDescription = "Anteprima icona tema",
            modifier = Modifier.fillMaxSize()
        )
    }
}
