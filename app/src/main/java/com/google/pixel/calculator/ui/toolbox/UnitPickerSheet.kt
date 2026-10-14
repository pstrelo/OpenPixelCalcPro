package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.ui.theme.CalculatorTheme

enum class UnitPickerTarget {
    FROM,
    TO
}

/**
 * Returns the specific Material 3 vector icon for each of the 17 categories.
 */
fun getCategoryMaterialIcon(category: UnitConverter.Category): ImageVector = when (category) {
    UnitConverter.Category.CURRENCY -> Icons.Default.Payments
    UnitConverter.Category.LENGTH -> Icons.Default.Straighten
    UnitConverter.Category.MASS -> Icons.Default.Scale
    UnitConverter.Category.TEMPERATURE -> Icons.Default.Thermostat
    UnitConverter.Category.AREA -> Icons.Default.GridOn
    UnitConverter.Category.VOLUME -> Icons.Default.WaterDrop
    UnitConverter.Category.COMPLEX -> Icons.Default.Transform
    UnitConverter.Category.SPEED -> Icons.Default.Speed
    UnitConverter.Category.TIME -> Icons.Default.Schedule
    UnitConverter.Category.PRESSURE -> Icons.Default.Compress
    UnitConverter.Category.ENERGY -> Icons.Default.Bolt
    UnitConverter.Category.POWER -> Icons.Default.Lightbulb
    UnitConverter.Category.DATA -> Icons.Default.Storage
    UnitConverter.Category.ANGLE -> Icons.Default.ChangeHistory
    UnitConverter.Category.FUEL -> Icons.Default.LocalGasStation
    UnitConverter.Category.COOKING -> Icons.Default.Restaurant
    UnitConverter.Category.TYPOGRAPHY -> Icons.Default.TextFields
    UnitConverter.Category.FREQUENCY -> Icons.Default.GraphicEq
}

/**
 * Material 3 Expressive ModalBottomSheet for fast unit selection.
 * - Instant search with filtering across all dimensions.
 * - Scrollable tabs with pure text (no emojis next to text).
 * - Material 3 icons inside menu circular containers.
 * - Clear checkmark highlight for active unit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPickerSheet(
    converter: UnitConverter,
    initialCategory: UnitConverter.Category,
    currentUnitCode: String,
    target: UnitPickerTarget,
    onDismiss: () -> Unit,
    onSelectUnit: (category: UnitConverter.Category, unitCode: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    val allCategories = remember { UnitConverter.Category.values() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer ?: MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Target header title
            Text(
                text = if (target == UnitPickerTarget.FROM) {
                    stringResource(R.string.unit_picker_from)
                } else {
                    stringResource(R.string.unit_picker_to)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            // Quick search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_units_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_text)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable category tabs when search query is empty
            if (searchQuery.isEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory.ordinal,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategory.ordinal]),
                            color = CalculatorTheme.colors.equalsContainer,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Tab(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            text = {
                                Text(
                                    text = if (cat.nameRes != 0) stringResource(cat.nameRes) else cat.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (isSelected) CalculatorTheme.colors.onSurface else CalculatorTheme.colors.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Filtered unit list
            val unitsToDisplay = remember(searchQuery, selectedCategory) {
                if (searchQuery.isBlank()) {
                    converter.getUnitsForCategory(selectedCategory)
                } else {
                    val query = searchQuery.trim().lowercase()
                    allCategories.flatMap { cat ->
                        val catLocalized = if (cat.nameRes != 0) context.getString(cat.nameRes).lowercase() else cat.displayName.lowercase()
                        converter.getUnitsForCategory(cat).filter { unit ->
                            val unitLocalized = unit.nameRes?.let { context.getString(it).lowercase() } ?: ""
                            unit.name.lowercase().contains(query) ||
                                    unitLocalized.contains(query) ||
                                    unit.code.lowercase().contains(query) ||
                                    unit.symbol.lowercase().contains(query) ||
                                    cat.displayName.lowercase().contains(query) ||
                                    catLocalized.contains(query)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (unitsToDisplay.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_units_found, searchQuery),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(unitsToDisplay, key = { "${it.category.name}_${it.code}" }) { unit ->
                        val isCurrent = unit.code == currentUnitCode && (searchQuery.isNotEmpty() || unit.category == selectedCategory)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) {
                                CalculatorTheme.colors.equalsContainer.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val catToPass = if (selectedCategory == UnitConverter.Category.COMPLEX) UnitConverter.Category.COMPLEX else unit.category
                                    onSelectUnit(catToPass, unit.code)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Circular menu badge with Material 3 icon
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCurrent) {
                                            CalculatorTheme.colors.equalsContainer.copy(alpha = 0.25f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = getCategoryMaterialIcon(unit.category),
                                                contentDescription = null,
                                                tint = if (isCurrent) {
                                                    CalculatorTheme.colors.equalsContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = unit.nameRes?.let { stringResource(it) } ?: unit.name,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val catText = if (unit.category.nameRes != 0) stringResource(unit.category.nameRes) else unit.category.displayName
                                        Text(
                                            text = if (searchQuery.isNotEmpty()) {
                                                "$catText • ${unit.symbol}"
                                            } else {
                                                unit.symbol
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest ?: MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = unit.symbol,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = CalculatorTheme.colors.equalsContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
