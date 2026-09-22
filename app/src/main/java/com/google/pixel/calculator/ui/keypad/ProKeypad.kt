package com.google.pixel.calculator.ui.keypad

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.ui.ActiveScreen

@Composable
fun ProKeypad(
    isRadians: Boolean,
    onToggleAngleUnit: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onInsertFraction: () -> Unit,
    onOperator: (String) -> Unit,
    onParenthesis: (String) -> Unit,
    onNavigate: (ActiveScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // BARRA STRUMENTI PRO: EQUAZIONI, GRAFICI 2D, DEG/RAD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = onToggleAngleUnit,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(text = if (isRadians) "RAD" else "DEG", fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(
                onClick = { onNavigate(ActiveScreen.EQUATION_SOLVER) },
                modifier = Modifier.weight(1.5f),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(text = "📐 Equazioni", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(
                onClick = { onNavigate(ActiveScreen.GRAPH_2D) },
                modifier = Modifier.weight(1.5f),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text(text = "📈 Grafici 2D", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // RIGA 1 PRO: sin, cos, tan, ln, log
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProButton(text = "sin", modifier = Modifier.weight(1f)) { onFunction("sin") }
            ProButton(text = "cos", modifier = Modifier.weight(1f)) { onFunction("cos") }
            ProButton(text = "tan", modifier = Modifier.weight(1f)) { onFunction("tan") }
            ProButton(text = "ln", modifier = Modifier.weight(1f)) { onFunction("ln") }
            ProButton(text = "log", modifier = Modifier.weight(1f)) { onFunction("log") }
        }

        // RIGA 2 PRO: x^y, √, ∛, π, e
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProButton(text = "x^y", modifier = Modifier.weight(1f)) { onOperator("^") }
            ProButton(text = "√x", modifier = Modifier.weight(1f)) { onFunction("sqrt") }
            ProButton(text = "∛x", modifier = Modifier.weight(1f)) { onFunction("cbrt") }
            ProButton(text = "π", modifier = Modifier.weight(1f)) { onConstant("π") }
            ProButton(text = "e", modifier = Modifier.weight(1f)) { onConstant("e") }
        }

        // RIGA 3 PRO: Frazione (a/b), !, (, ), x (variabile)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProButton(text = "a/b ⬒", modifier = Modifier.weight(1.2f)) { onInsertFraction() }
            ProButton(text = "x!", modifier = Modifier.weight(0.9f)) { onOperator("!") }
            ProButton(text = "(", modifier = Modifier.weight(0.9f)) { onParenthesis("(") }
            ProButton(text = ")", modifier = Modifier.weight(0.9f)) { onParenthesis(")") }
            ProButton(text = "x", modifier = Modifier.weight(0.9f)) { onConstant("x") }
        }
    }
}

@Composable
fun ProButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
