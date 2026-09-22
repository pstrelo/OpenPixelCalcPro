package com.google.pixel.calculator.ui.equations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.math.EquationSolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquationSolverScreen(
    onBack: () -> Unit
) {
    var aStr by remember { mutableStateOf("1") }
    var bStr by remember { mutableStateOf("-5") }
    var cStr by remember { mutableStateOf("6") }
    var solution by remember { mutableStateOf<EquationSolver.Solution?>(null) }

    val solver = remember { EquationSolver() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risolutore di Equazioni") },
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Forma canonica: ax² + bx + c = 0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Inserisci i coefficienti per ottenere radici reali/complesse e passaggi dettagliati.",
                        fontSize = 14.sp
                    )
                }
            }

            // Input coefficienti a, b, c
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("a") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("b") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("c") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val a = aStr.toDoubleOrNull() ?: 1.0
                    val b = bStr.toDoubleOrNull() ?: 0.0
                    val c = cStr.toDoubleOrNull() ?: 0.0
                    solution = solver.solveQuadratic(a, b, c)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Risolvi Equazione", fontWeight = FontWeight.Bold)
            }

            // Risultato e Passaggi
            solution?.let { sol ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Risultato:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        when (sol) {
                            is EquationSolver.Solution.QuadraticReal -> {
                                Text("x₁ = ${sol.x1}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("x₂ = ${sol.x2}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            is EquationSolver.Solution.QuadraticComplex -> {
                                Text(
                                    "x = ${sol.realPart} ± ${sol.imagPart} i",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            is EquationSolver.Solution.Linear -> {
                                Text("x = ${sol.x}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            is EquationSolver.Solution.InfiniteSolutions -> {
                                Text(sol.message, fontSize = 16.sp)
                            }
                            is EquationSolver.Solution.NoSolution -> {
                                Text(sol.message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                            }
                            is EquationSolver.Solution.Error -> {
                                Text(sol.message, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                            }
                            else -> {}
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Passaggi di risoluzione:", fontWeight = FontWeight.SemiBold)
                        val steps = when (sol) {
                            is EquationSolver.Solution.QuadraticReal -> sol.steps
                            is EquationSolver.Solution.QuadraticComplex -> sol.steps
                            is EquationSolver.Solution.Linear -> sol.steps
                            else -> emptyList()
                        }
                        for (step in steps) {
                            Text(
                                text = "• $step",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
