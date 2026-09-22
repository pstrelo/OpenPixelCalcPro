package com.google.pixel.calculator.ui.toolbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.utils.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterView(
    onBack: () -> Unit
) {
    val converter = remember { UnitConverter() }
    var selectedCategory by remember { mutableStateOf(UnitConverter.Category.LENGTH) }

    val units = remember(selectedCategory) { converter.getUnitsForCategory(selectedCategory) }
    var fromUnitCode by remember(selectedCategory) { mutableStateOf(units.first().code) }
    var toUnitCode by remember(selectedCategory) { mutableStateOf(units.getOrNull(1)?.code ?: units.first().code) }

    var inputValueStr by remember { mutableStateOf("1") }
    val inputValue = inputValueStr.toDoubleOrNull() ?: 0.0

    val convertedValue = remember(inputValue, fromUnitCode, toUnitCode, selectedCategory) {
        converter.convert(inputValue, fromUnitCode, toUnitCode, selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convertitore di Unità") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selettore Categoria
            Text(text = "Categoria:", fontWeight = FontWeight.Bold)
            ScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                edgePadding = 0.dp
            ) {
                UnitConverter.Category.values().forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text(cat.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Input valore da convertire
            OutlinedTextField(
                value = inputValueStr,
                onValueChange = { inputValueStr = it },
                label = { Text("Valore di partenza") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Da unità a unità
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Da:", fontWeight = FontWeight.SemiBold)
                    UnitDropdown(
                        units = units,
                        selectedCode = fromUnitCode,
                        onSelect = { fromUnitCode = it }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("A:", fontWeight = FontWeight.SemiBold)
                    UnitDropdown(
                        units = units,
                        selectedCode = toUnitCode,
                        onSelect = { toUnitCode = it }
                    )
                }
            }

            // Card Risultato
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Risultato:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$convertedValue ${units.find { it.code == toUnitCode }?.symbol ?: ""}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    units: List<UnitConverter.UnitDefinition>,
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = units.find { it.code == selectedCode } ?: units.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = current.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text("${unit.name} (${unit.symbol})") },
                    onClick = {
                        onSelect(unit.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
