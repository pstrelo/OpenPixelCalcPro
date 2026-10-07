package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.ActiveScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxModal(
    onDismiss: () -> Unit,
    onSelectTool: (ActiveScreen) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.toolbox_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.toolbox_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            ToolboxItem(
                icon = "⚖️",
                title = stringResource(R.string.tool_converter),
                description = stringResource(R.string.tool_unit_converter_summary),
                itemShape = RoundedCornerShape(20.dp),
                iconShape = RoundedCornerShape(14.dp),
                iconBg = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    onSelectTool(ActiveScreen.TOOLBOX_CONVERTER)
                    onDismiss()
                }
            )

            ToolboxItem(
                icon = "🟢",
                title = stringResource(R.string.tool_level),
                description = stringResource(R.string.tool_bubble_level_summary),
                itemShape = RoundedCornerShape(topStart = 20.dp, topEnd = 12.dp, bottomEnd = 20.dp, bottomStart = 12.dp),
                iconShape = CircleShape,
                iconBg = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    onSelectTool(ActiveScreen.TOOLBOX_LEVEL)
                    onDismiss()
                }
            )

            ToolboxItem(
                icon = "🧭",
                title = stringResource(R.string.tool_compass),
                description = stringResource(R.string.tool_compass_summary),
                itemShape = RoundedCornerShape(20.dp),
                iconShape = CircleShape,
                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = {
                    onSelectTool(ActiveScreen.TOOLBOX_COMPASS)
                    onDismiss()
                }
            )

            ToolboxItem(
                icon = "📏",
                title = stringResource(R.string.tool_ruler),
                description = stringResource(R.string.tool_ruler_summary),
                itemShape = RoundedCornerShape(topStart = 12.dp, topEnd = 20.dp, bottomEnd = 12.dp, bottomStart = 20.dp),
                iconShape = RoundedCornerShape(14.dp),
                iconBg = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    onSelectTool(ActiveScreen.TOOLBOX_RULER)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ToolboxItem(
    icon: String,
    title: String,
    description: String,
    itemShape: Shape = RoundedCornerShape(16.dp),
    iconShape: Shape = RoundedCornerShape(12.dp),
    iconBg: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(itemShape)
            .clickable { onClick() },
        shape = itemShape,
        color = MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = iconShape,
                color = iconBg,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
