package com.google.pixel.calculator.ui.advanced

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdvancedKeypad(
    state: AdvancedCalculatorState,
    onKeyClick: (String) -> Unit,
    onExecute: () -> Unit,
    onCycleFormat: () -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onOpenCatalog: () -> Unit,
    onToggleGraph: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Barra superiore di accesso rapido: CATALOG / f(x), Mostra Grafico, Simboli Disequazione
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tasto CATALOG / f(x) per aprire la libreria funzioni completa (stile Casio ClassWiz)
            Button(
                onClick = onOpenCatalog,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33384C)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1.3f)
            ) {
                Text(
                    text = "CATALOG  f(x)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD)
                )
            }

            // Tasto rapido Grafico
            Button(
                onClick = onToggleGraph,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.screenViewMode != ScreenViewMode.CALCULATOR_ONLY) Color(0xFF2563EB) else Color(0xFF33384C)
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (state.screenViewMode) {
                        ScreenViewMode.CALCULATOR_ONLY -> "Grafico 📈"
                        ScreenViewMode.SPLIT_SCREEN -> "Nascondi ✕"
                        ScreenViewMode.FULLSCREEN_GRAPH -> "Calcolatore"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Pulsante per strip rapida relazioni disequazioni (≤, ≥, <, >)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF33384C), RoundedCornerShape(12.dp))
                    .clickable { onKeyClick("≤") },
                contentAlignment = Alignment.Center
            ) {
                Text("≤", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF33384C), RoundedCornerShape(12.dp))
                    .clickable { onKeyClick("≥") },
                contentAlignment = Alignment.Center
            ) {
                Text("≥", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            }
        }

        // =========================================================================
        // TASTIERA SCIENTIFICA PRINCIPALE (LAYOUT CASIO fx-991CW / EXPRESSIVE)
        // =========================================================================

        // Riga 1: x, y, □/□ (frazione), √□, ⁿ√□
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey("x", Color(0xFF3B4158), Color(0xFF93C5FD), Modifier.weight(1f), isBold = true) { onKeyClick("x") }
            SciKey("y", Color(0xFF3B4158), Color(0xFF93C5FD), Modifier.weight(1f), isBold = true) { onKeyClick("y") }
            SciKey("□/□", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("/") }
            SciKey("√□", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("sqrt(") }
            SciKey("ⁿ√□", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("ʸ√") }
        }

        // Riga 2: x², x□, ln, log, π
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey("x²", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("^2") }
            SciKey("x□", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("^") }
            SciKey("ln", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("ln(") }
            SciKey("log", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("log(") }
            SciKey("π", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("π") }
        }

        // Riga 3: (, ), DEL, AC, e
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SciKey("(", Color(0xFF2E3346), Color(0xFFCBD5E1), Modifier.weight(1f)) { onKeyClick("(") }
            SciKey(")", Color(0xFF2E3346), Color(0xFFCBD5E1), Modifier.weight(1f)) { onKeyClick(")") }
            SciKey("DEL", Color(0xFF474E66), Color(0xFFFCA5A5), Modifier.weight(1f), isBold = true) { onBackspace() }
            SciKey("AC", Color(0xFF7F1D1D), Color(0xFFFECACA), Modifier.weight(1f), isBold = true) { onClearAll() }
            SciKey("e", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f)) { onKeyClick("e") }
        }

        // Riga 4: 7, 8, 9, ÷, Ans
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumKey("7", Modifier.weight(1f)) { onKeyClick("7") }
            NumKey("8", Modifier.weight(1f)) { onKeyClick("8") }
            NumKey("9", Modifier.weight(1f)) { onKeyClick("9") }
            OpKey("÷", Modifier.weight(1f)) { onKeyClick("/") }
            SciKey("Ans", Color(0xFF33384C), Color(0xFF93C5FD), Modifier.weight(1f)) { onKeyClick("Ans") }
        }

        // Riga 5: 4, 5, 6, ×, = (Uguale per scrivere le equazioni!)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumKey("4", Modifier.weight(1f)) { onKeyClick("4") }
            NumKey("5", Modifier.weight(1f)) { onKeyClick("5") }
            NumKey("6", Modifier.weight(1f)) { onKeyClick("6") }
            OpKey("×", Modifier.weight(1f)) { onKeyClick("*") }
            // TASTO '=': Inserisce l'uguale nella formula (NON calcola!)
            SciKey("=", Color(0xFF3E455E), Color(0xFF60A5FA), Modifier.weight(1f), isBold = true, fontSize = 20.sp) {
                onKeyClick(" = ")
            }
        }

        // Riga 6: 1, 2, 3, -, +
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumKey("1", Modifier.weight(1f)) { onKeyClick("1") }
            NumKey("2", Modifier.weight(1f)) { onKeyClick("2") }
            NumKey("3", Modifier.weight(1f)) { onKeyClick("3") }
            OpKey("-", Modifier.weight(1f)) { onKeyClick("-") }
            OpKey("+", Modifier.weight(1f)) { onKeyClick("+") }
        }

        // Riga 7: 0, ., ×10ˣ, FORMAT, EXE (Calcola)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumKey("0", Modifier.weight(1f)) { onKeyClick("0") }
            NumKey(".", Modifier.weight(1f)) { onKeyClick(".") }
            SciKey("×10ˣ", Color(0xFF2E3346), Color(0xFFE2E8F0), Modifier.weight(1f), fontSize = 12.sp) { onKeyClick("*10^(") }

            // TASTO 'FORMAT': Cambia la visualizzazione del risultato (Dec, Frac, Eng, DMS, Polar)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Color(0xFF33384C), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(16.dp))
                    .clickable { onCycleFormat() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FORMAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF93C5FD))
                    Text(
                        text = state.formulas.getOrNull(state.activeFormulaIndex)?.activeFormat?.label ?: "DEC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // TASTO 'EXE': Esecuzione e Calcolo (Accent Color Pixel Blue)
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .height(48.dp)
                    .background(Color(0xFF2563EB), RoundedCornerShape(16.dp))
                    .clickable { onExecute() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EXE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun NumKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(Color(0xFF383D50), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Normal, color = Color.White)
    }
}

@Composable
private fun OpKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(Color(0xFF2E3346), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFFE2E8F0))
    }
}

@Composable
private fun SciKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
