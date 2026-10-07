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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.theme.CalculatorTheme

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

    val isDark = CalculatorTheme.colors.isDark
    val sheetBg = if (isDark) Color(0xFF1E212D) else CalculatorTheme.colors.surface
    val tabRowBg = if (isDark) Color(0xFF262A3B) else CalculatorTheme.colors.operatorContainer
    val titleColor = if (isDark) Color.White else CalculatorTheme.colors.onSurface

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.catalog_tab_trig),
        stringResource(R.string.catalog_tab_calculus),
        stringResource(R.string.catalog_tab_hyperbolic),
        stringResource(R.string.catalog_tab_algebra),
        stringResource(R.string.catalog_tab_constants)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetBg,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = if (isDark) Color(0xFF64748B) else CalculatorTheme.colors.onSurfaceVariant)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Catalog header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.catalog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = if (isDark) Color(0xFF94A3B8) else CalculatorTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Tab Row in Expressive Pill style
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = tabRowBg,
                contentColor = Color(0xFF60A5FA),
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF60A5FA),
                        height = 3.dp
                    )
                },
                modifier = Modifier.background(tabRowBg, RoundedCornerShape(percent = 50))
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
                                color = if (isSelected) Color(0xFF60A5FA) else if (isDark) Color(0xFF94A3B8) else CalculatorTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of items for selected category
            val items = when (selectedTabIndex) {
                0 -> listOf(
                    CatalogItem("sin(x)", "sin(", "sin(x)"),
                    CatalogItem("cos(x)", "cos(", "cos(x)"),
                    CatalogItem("tan(x)", "tan(", "tan(x)"),
                    CatalogItem("cot(x)", "cot(", "cot(x)"),
                    CatalogItem("asin(x)", "asin(", "arcsin(x)"),
                    CatalogItem("acos(x)", "acos(", "arccos(x)"),
                    CatalogItem("atan(x)", "atan(", "arctan(x)")
                )
                1 -> listOf(
                    CatalogItem("d/dx", "d/dx(x^2, 1)", "d/dx(f(x), x₀)"),
                    CatalogItem("∫(f, a, b)", "∫(x^2, 0, 3)", "∫(f, a, b)"),
                    CatalogItem("lim(x→x₀)", "lim(x, 0)", "lim(f, x₀)"),
                    CatalogItem("Σ aᵢ", "Σ(k, 1, 10)", "Σ(expr, from, to)"),
                    CatalogItem("xⁿ", "^", "x^n"),
                    CatalogItem("ⁿ√x", "ʸ√", "ⁿ√x")
                )
                2 -> listOf(
                    CatalogItem("sinh(x)", "sinh(", "sinh(x)"),
                    CatalogItem("cosh(x)", "cosh(", "cosh(x)"),
                    CatalogItem("tanh(x)", "tanh(", "tanh(x)")
                )
                3 -> listOf(
                    CatalogItem("|x|", "abs(", "abs(x)"),
                    CatalogItem("n!", "!", "n!"),
                    CatalogItem("sign(x)", "sign(", "sign(x)"),
                    CatalogItem("ln(x)", "ln(", "ln(x)"),
                    CatalogItem("log₁₀(x)", "log(", "log₁₀(x)"),
                    CatalogItem("logₙ(b, x)", "logn(", "log_b(x)")
                )
                else -> listOf(
                    CatalogItem("c", "299792458", "2.99792458×10⁸ m/s"),
                    CatalogItem("G", "6.6743e-11", "6.6743×10⁻¹¹ N⋅m²/kg²"),
                    CatalogItem("h", "6.62607e-34", "6.62607×10⁻³⁴ J⋅s"),
                    CatalogItem("k_B", "1.3806e-23", "1.3806×10⁻²³ J/K"),
                    CatalogItem("N_A", "6.02214e23", "6.02214×10²³ mol⁻¹"),
                    CatalogItem("π", "π", "3.1415926535..."),
                    CatalogItem("e", "e", "2.7182818284...")
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                items(items) { item ->
                    val cardBg = if (isDark) Color(0xFF2C3040) else CalculatorTheme.colors.operatorContainer
                    val itemTitleColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    val itemSubtitleColor = if (isDark) Color(0xFF94A3B8) else CalculatorTheme.colors.onSurfaceVariant

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
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
                                color = itemTitleColor
                            )
                            if (item.subtitle.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = itemSubtitleColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
