package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.ui.ActiveScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen(
    onBack: () -> Unit,
    onSelectTool: (ActiveScreen) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna alla calcolatrice"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Strumenti e Funzioni",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val isLandscape = androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isLandscape) 20.dp else 16.dp, vertical = if (isLandscape) 8.dp else 12.dp)
        ) {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // SEZIONE 1: CALCOLO AVANZATO (Colonna Sinistra)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Calcolo Avanzato",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        ToolCard(
                            icon = "🧮",
                            title = "Calcolatrice Avanzata & Grafica",
                            description = "Calcolo scientifico, equazioni, derivate, integrali e grafici 2D interattivi",
                            cardShape = RoundedCornerShape(22.dp),
                            iconContainerShape = RoundedCornerShape(16.dp),
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onSelectTool(ActiveScreen.ADVANCED_CALCULATOR) }
                        )
                    }

                    // SEZIONE 2: SENSORI E STRUMENTI FISICI PIXEL (Colonna Destra)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Utilità e Sensori Pixel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        ToolCard(
                            icon = "⚖️",
                            title = "Convertitore di Unità",
                            description = "Valute, grandezze fisiche, dosaggi e oltre 17 categorie offline con calcolo in tempo reale",
                            cardShape = RoundedCornerShape(20.dp),
                            iconContainerShape = RoundedCornerShape(14.dp),
                            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_CONVERTER) }
                        )

                        ToolCard(
                            icon = "🟢",
                            title = "Livella a Bolla Digitale",
                            description = "Misurazione dell'inclinazione su due assi con feedback haptic e accelerometro",
                            cardShape = RoundedCornerShape(topStart = 20.dp, topEnd = 14.dp, bottomEnd = 20.dp, bottomStart = 14.dp),
                            iconContainerShape = CircleShape,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_LEVEL) }
                        )

                        ToolCard(
                            icon = "🧭",
                            title = "Bussola Digitale",
                            description = "Orientamento magnetico 360°, calcolo azimut e indicazione punti cardinali",
                            cardShape = RoundedCornerShape(20.dp),
                            iconContainerShape = CircleShape,
                            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_COMPASS) }
                        )

                        ToolCard(
                            icon = "📏",
                            title = "Righello su Schermo",
                            description = "Scala millimetrica e pollici calibrata fisicamente sui display Pixel",
                            cardShape = RoundedCornerShape(topStart = 14.dp, topEnd = 20.dp, bottomEnd = 14.dp, bottomStart = 20.dp),
                            iconContainerShape = RoundedCornerShape(14.dp),
                            iconBgColor = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_RULER) }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // SEZIONE 1: CALCOLO AVANZATO
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Calcolo Avanzato",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // 1. Calcolatrice Avanzata & Grafica (Squircle Expressive Shape: 24dp, container accent)
                        ToolCard(
                            icon = "🧮",
                            title = "Calcolatrice Avanzata & Grafica",
                            description = "Calcolo scientifico, equazioni, derivate, integrali e grafici 2D interattivi",
                            cardShape = RoundedCornerShape(26.dp),
                            iconContainerShape = RoundedCornerShape(18.dp),
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onSelectTool(ActiveScreen.ADVANCED_CALCULATOR) }
                        )
                    }

                    // SEZIONE 2: SENSORI E STRUMENTI FISICI PIXEL
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Utilità e Sensori Pixel",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // 4. Convertitore di Unità (Pill rounded 22dp)
                        ToolCard(
                            icon = "⚖️",
                            title = "Convertitore di Unità",
                            description = "Valute, grandezze fisiche, dosaggi e oltre 17 categorie offline con calcolo in tempo reale",
                            cardShape = RoundedCornerShape(22.dp),
                            iconContainerShape = RoundedCornerShape(16.dp),
                            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_CONVERTER) }
                        )

                        // 5. Livella a Bolla Digitale (Organic Curvature: topStart/bottomEnd 24dp, topEnd/bottomStart 16dp)
                        ToolCard(
                            icon = "🟢",
                            title = "Livella a Bolla Digitale",
                            description = "Misurazione dell'inclinazione su due assi con feedback haptic e accelerometro",
                            cardShape = RoundedCornerShape(topStart = 24.dp, topEnd = 16.dp, bottomEnd = 24.dp, bottomStart = 16.dp),
                            iconContainerShape = CircleShape,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_LEVEL) }
                        )

                        // 6. Bussola Digitale (Circle icon container & 22dp card)
                        ToolCard(
                            icon = "🧭",
                            title = "Bussola Digitale",
                            description = "Orientamento magnetico 360°, calcolo azimut e indicazione punti cardinali",
                            cardShape = RoundedCornerShape(22.dp),
                            iconContainerShape = CircleShape,
                            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_COMPASS) }
                        )

                        // 7. Righello su Schermo (Asymmetric: topStart 16dp, topEnd 24dp, bottomEnd 16dp, bottomStart 24dp)
                        ToolCard(
                            icon = "📏",
                            title = "Righello su Schermo",
                            description = "Scala millimetrica e pollici calibrata fisicamente sui display Pixel",
                            cardShape = RoundedCornerShape(topStart = 16.dp, topEnd = 24.dp, bottomEnd = 16.dp, bottomStart = 24.dp),
                            iconContainerShape = RoundedCornerShape(14.dp),
                            iconBgColor = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onSelectTool(ActiveScreen.TOOLBOX_RULER) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ToolCard(
    icon: String,
    title: String,
    description: String,
    cardShape: Shape,
    iconContainerShape: Shape,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable { onClick() },
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = iconContainerShape,
                color = iconBgColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 26.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
