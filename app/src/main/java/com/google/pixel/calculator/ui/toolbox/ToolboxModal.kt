package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.ui.ActiveScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxModal(
    onDismiss: () -> Unit,
    onSelectTool: (ActiveScreen) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Strumenti Pixel Utility",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Funzionalità avanzate offline integrate con i sensori del Pixel",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            ToolboxItem(
                icon = "⚖️",
                title = "Convertitore di Unità",
                description = "Lunghezza, peso, temperatura, volume, velocità e dati offline",
                onClick = { onSelectTool(ActiveScreen.TOOLBOX_CONVERTER) }
            )

            ToolboxItem(
                icon = "🟢",
                title = "Livella a Bolla Digitale",
                description = "Misura l'inclinazione su due assi con l'accelerometro del Pixel",
                onClick = { onSelectTool(ActiveScreen.TOOLBOX_LEVEL) }
            )

            ToolboxItem(
                icon = "🧭",
                title = "Bussola Digitale",
                description = "Orientamento magnetico e punti cardinali a 360°",
                onClick = { onSelectTool(ActiveScreen.TOOLBOX_COMPASS) }
            )

            ToolboxItem(
                icon = "📏",
                title = "Righello su Schermo",
                description = "Scala millimetrica e pollici calibrata fisicamente sul display Pixel",
                onClick = { onSelectTool(ActiveScreen.TOOLBOX_RULER) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ToolboxItem(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = icon, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
