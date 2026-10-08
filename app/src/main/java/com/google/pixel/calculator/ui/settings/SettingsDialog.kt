package com.google.pixel.calculator.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.data.AppThemeMode
import com.google.pixel.calculator.ui.theme.PixelAppIconPreview

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemeMode,
    currentAppIcon: AppThemeMode = AppThemeMode.SYSTEM_AUTO,
    syncIconWithTheme: Boolean = false,
    onSelectTheme: (AppThemeMode) -> Unit,
    onSetAppIcon: (AppThemeMode) -> Unit = {},
    onToggleSyncIcon: (Boolean) -> Unit = {},
    onDismiss: () -> Unit
) {
    val themeOptions = listOf(
        ThemeOption(
            mode = AppThemeMode.SYSTEM_AUTO,
            titleRes = R.string.theme_system_auto,
            colors = listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335)),
            isDynamic = true
        ),
        ThemeOption(
            mode = AppThemeMode.SYSTEM_LIGHT,
            titleRes = R.string.theme_system_light,
            colors = listOf(Color(0xFF4285F4), Color(0xFFF1F2F6), Color(0xFFE2E3EC), Color(0xFF041E49)),
            isDynamic = true
        ),
        ThemeOption(
            mode = AppThemeMode.SYSTEM_DARK,
            titleRes = R.string.theme_system_dark,
            colors = listOf(Color(0xFF131418), Color(0xFF26282E), Color(0xFF4285F4), Color(0xFFD3E3FD)),
            isDynamic = true
        ),
        ThemeOption(
            mode = AppThemeMode.STANDARD_LIGHT,
            titleRes = R.string.theme_standard_light,
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F2F6), Color(0xFFE2E3EC), Color(0xFF354558)),
            isDynamic = false
        ),
        ThemeOption(
            mode = AppThemeMode.STANDARD_DARK,
            titleRes = R.string.theme_standard_dark,
            colors = listOf(Color(0xFF131418), Color(0xFF26282E), Color(0xFF373942), Color(0xFFD3E3FD)),
            isDynamic = false
        ),
        ThemeOption(
            mode = AppThemeMode.CANDY_POP,
            titleRes = R.string.theme_candy_pop,
            colors = listOf(Color(0xFFD8F3DC), Color(0xFFFFCCD5), Color(0xFFFF6B8B), Color(0xFFFFD152)),
            isDynamic = false
        ),
        ThemeOption(
            mode = AppThemeMode.RETRO_CYBER,
            titleRes = R.string.theme_retro_cyber,
            colors = listOf(Color(0xFF1E222C), Color(0xFF34D399), Color(0xFFFF3366), Color(0xFFFACC15)),
            isDynamic = false
        )
    )

    // Submenu state: populated when a theme is selected for confirmation
    var selectedThemeForSubmenu by remember { mutableStateOf<AppThemeMode?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
        title = {
            if (selectedThemeForSubmenu == null) {
                Text(
                    text = stringResource(R.string.choose_theme),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { selectedThemeForSubmenu = null },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_themes),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.set_app_icon),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            AnimatedContent(
                targetState = selectedThemeForSubmenu,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "ThemeSubmenuTransition"
            ) { targetSelectedTheme ->
                if (targetSelectedTheme == null) {
                    // 1. MAIN THEME LIST
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeOptions.forEach { option ->
                            val isSelected = option.mode == currentTheme
                            val isIconActive = option.mode == currentAppIcon
                            val itemBg = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            } else Color.Transparent

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(itemBg)
                                    .clickable {
                                        selectedThemeForSubmenu = option.mode
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedThemeForSubmenu = option.mode
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(option.titleRes),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isIconActive) {
                                            Surface(
                                                shape = RoundedCornerShape(percent = 50),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Smartphone,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(10.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Text(
                                                        text = stringResource(R.string.icon_badge),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                PixelAppIconPreview(
                                    themeMode = option.mode,
                                    size = 28.dp,
                                    elevation = 1.dp
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Automatic icon-theme synchronization switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.sync_icon_with_theme),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.sync_icon_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = syncIconWithTheme,
                                onCheckedChange = { onToggleSyncIcon(it) },
                                modifier = Modifier.scale(0.85f)
                            )
                        }
                    }
                } else {
                    // 2. SUBMENU TO SET APP ICON ALONGSIDE THEME
                    val themeOption = themeOptions.first { it.mode == targetSelectedTheme }
                    val isAlreadyIcon = targetSelectedTheme == currentAppIcon

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PixelAppIconPreview(
                                    themeMode = targetSelectedTheme,
                                    size = 80.dp,
                                    elevation = 6.dp
                                )
                                Text(
                                    text = stringResource(themeOption.titleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isAlreadyIcon) {
                                    Surface(
                                        shape = RoundedCornerShape(percent = 50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = stringResource(R.string.icon_currently_active),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (themeOption.isDynamic) {
                                            stringResource(R.string.icon_material_you_desc)
                                        } else {
                                            stringResource(R.string.icon_theme_palette_desc)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.prompt_set_app_icon),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = {
                                onSelectTheme(targetSelectedTheme)
                                onSetAppIcon(targetSelectedTheme)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.btn_set_app_icon),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onToggleSyncIcon(false)
                                onSelectTheme(targetSelectedTheme)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_apply_theme_only),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedThemeForSubmenu == null) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                TextButton(
                    onClick = { selectedThemeForSubmenu = null },
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(
                        text = stringResource(R.string.back),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

private data class ThemeOption(
    val mode: AppThemeMode,
    val titleRes: Int,
    val colors: List<Color>,
    val isDynamic: Boolean
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
                text = stringResource(R.string.clear_history_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(R.string.clear_history_message),
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
                    text = stringResource(R.string.clear),
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
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
