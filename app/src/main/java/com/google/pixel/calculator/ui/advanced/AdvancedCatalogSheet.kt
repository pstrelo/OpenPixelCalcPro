package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CatalogItem(
    val title: String,
    val insertion: String,
    val subtitle: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedCatalogSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectItem: (String) -> Unit
) {
    if (!isOpen) return

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("📐 Goniometria", "📈 Analisi", "⚡ Iperboliche", "🔢 Algebra", "⚛️ Costanti")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF1E212D),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF64748B))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header del catalogo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Libreria Funzioni Scientifiche",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Tab Row in stile Pill Expressive
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF262A3B),
                contentColor = Color(0xFF60A5FA),
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF60A5FA),
                        height = 3.dp
                    )
                },
                modifier = Modifier.background(Color(0xFF262A3B), RoundedCornerShape(percent = 50))
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Griglia di elementi della categoria selezionata
            val items = when (selectedTabIndex) {
                0 -> listOf(
                    CatalogItem("sin(x)", "sin(", "Seno"),
                    CatalogItem("cos(x)", "cos(", "Coseno"),
                    CatalogItem("tan(x)", "tan(", "Tangente"),
                    CatalogItem("cot(x)", "cot(", "Cotangente"),
                    CatalogItem("asin(x)", "asin(", "Arco seno (sin⁻¹)"),
                    CatalogItem("acos(x)", "acos(", "Arco coseno (cos⁻¹)"),
                    CatalogItem("atan(x)", "atan(", "Arco tangente (tan⁻¹)")
                )
                1 -> listOf(
                    CatalogItem("d/dx", "d/dx(x^2, 1)", "Derivata numerica f'(x₀)"),
                    CatalogItem("∫(f, a, b)", "∫(x^2, 0, 3)", "Integrale definito"),
                    CatalogItem("lim(x→x₀)", "lim(x, 0)", "Limite numerico"),
                    CatalogItem("Σ aᵢ", "Σ(k, 1, 10)", "Sommatoria discreta"),
                    CatalogItem("xⁿ", "^", "Esponente generico"),
                    CatalogItem("ⁿ√x", "ʸ√", "Radice n-esima")
                )
                2 -> listOf(
                    CatalogItem("sinh(x)", "sinh(", "Seno iperbolico"),
                    CatalogItem("cosh(x)", "cosh(", "Coseno iperbolico"),
                    CatalogItem("tanh(x)", "tanh(", "Tangente iperbolica")
                )
                3 -> listOf(
                    CatalogItem("|x|", "abs(", "Valore assoluto"),
                    CatalogItem("n!", "!", "Fattoriale"),
                    CatalogItem("sign(x)", "sign(", "Segno (+1, -1, 0)"),
                    CatalogItem("ln(x)", "ln(", "Logaritmo naturale"),
                    CatalogItem("log₁₀(x)", "log(", "Logaritmo decimale"),
                    CatalogItem("logₙ(b, x)", "logn(", "Logaritmo base arbitraria")
                )
                else -> listOf(
                    CatalogItem("c", "299792458", "Velocità luce (2.99×10⁸ m/s)"),
                    CatalogItem("G", "6.6743e-11", "Costante gravitazionale"),
                    CatalogItem("h", "6.62607e-34", "Costante di Planck"),
                    CatalogItem("k_B", "1.3806e-23", "Costante di Boltzmann"),
                    CatalogItem("N_A", "6.02214e23", "Numero di Avogadro"),
                    CatalogItem("π", "π", "Pi Greco (3.14159...)"),
                    CatalogItem("e", "e", "Numero di Eulero (2.71828...)")
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3040)),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectItem(item.insertion)
                                onDismiss()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                            if (item.subtitle.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
