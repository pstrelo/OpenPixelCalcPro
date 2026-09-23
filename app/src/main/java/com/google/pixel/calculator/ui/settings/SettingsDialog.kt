package com.google.pixel.calculator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.data.AppThemeMode
import com.google.pixel.calculator.ui.theme.CalculatorTheme

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val themeOptions = listOf(
        ThemeOption(
            mode = AppThemeMode.SYSTEM_AUTO,
            title = "Predefinito di sistema (Auto)",
            subtitle = "Segue chiaro/scuro e colori dinamici del dispositivo",
            colors = listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335))
        ),
        ThemeOption(
            mode = AppThemeMode.SYSTEM_LIGHT,
            title = "Colori di sistema (Sempre chiaro)",
            subtitle = "Accenti dinamici di sistema, forzato su tema chiaro",
            colors = listOf(Color(0xFF4285F4), Color(0xFFF1F2F6), Color(0xFFE2E3EC), Color(0xFF041E49))
        ),
        ThemeOption(
            mode = AppThemeMode.SYSTEM_DARK,
            title = "Colori di sistema (Sempre scuro)",
            subtitle = "Accenti dinamici di sistema, forzato su tema scuro",
            colors = listOf(Color(0xFF131418), Color(0xFF26282E), Color(0xFF4285F4), Color(0xFFD3E3FD))
        ),
        ThemeOption(
            mode = AppThemeMode.STANDARD_LIGHT,
            title = "Chiaro Standard",
            subtitle = "Google Pixel Light con tasto uguale ardesia",
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F2F6), Color(0xFFE2E3EC), Color(0xFF354558))
        ),
        ThemeOption(
            mode = AppThemeMode.STANDARD_DARK,
            title = "Scuro Standard",
            subtitle = "Google Pixel Dark con tasto uguale azzurro",
            colors = listOf(Color(0xFF131418), Color(0xFF26282E), Color(0xFF373942), Color(0xFFD3E3FD))
        ),
        ThemeOption(
            mode = AppThemeMode.CANDY_POP,
            title = "Candy Pop (Vivace)",
            subtitle = "Menta pastello, rosa corallo e giallo sole (dalla foto)",
            colors = listOf(Color(0xFFD8F3DC), Color(0xFFFFCCD5), Color(0xFFFF6B8B), Color(0xFFFFD152))
        ),
        ThemeOption(
            mode = AppThemeMode.RETRO_CYBER,
            title = "Retro Cyber (Vivace Scuro)",
            subtitle = "Chassis grafite, ciano neon, fucsia e giallo fluo (dalla foto)",
            colors = listOf(Color(0xFF1E222C), Color(0xFF34D399), Color(0xFFFF3366), Color(0xFFFACC15))
        )
    )

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Scegli tema",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { option ->
                    val isSelected = option.mode == currentTheme
                    val itemBg = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    } else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(itemBg)
                            .clickable {
                                onSelectTheme(option.mode)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelectTheme(option.mode)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Swatches circolari con i colori di anteprima in pillola
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                option.colors.forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(col, CircleShape)
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = "Chiudi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

private data class ThemeOption(
    val mode: AppThemeMode,
    val title: String,
    val subtitle: String,
    val colors: List<Color>
)

@Composable
fun ClearHistoryConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Cancella cronologia?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Questa operazione rimuoverà tutti i calcoli salvati nella cronologia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = "Cancella",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = "Annulla",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
