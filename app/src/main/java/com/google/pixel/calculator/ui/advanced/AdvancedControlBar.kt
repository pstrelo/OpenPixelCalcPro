package com.google.pixel.calculator.ui.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R

@Composable
fun AdvancedControlBar(
    state: AdvancedCalculatorState,
    onSelectKeypadMode: (KeypadMode) -> Unit,
    onToggleDropdown: () -> Unit,
    onToggleGraph: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Dropdown button to switch keypad mode: Operations / Functions
        Box {
            Row(
                modifier = Modifier
                    .background(Color(0xFF383C4E), RoundedCornerShape(20.dp))
                    .clickable { onToggleDropdown() }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (state.keypadMode == KeypadMode.OPERATIONS) stringResource(R.string.keypad_operations) else stringResource(R.string.keypad_functions),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
                Icon(
                    imageVector = if (state.showModeDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.switch_keypad),
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = state.showModeDropdown,
                onDismissRequest = { onToggleDropdown() },
                modifier = Modifier.background(Color(0xFF2C3040), RoundedCornerShape(14.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.keypad_operations),
                            fontSize = 17.sp,
                            fontWeight = if (state.keypadMode == KeypadMode.OPERATIONS) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.keypadMode == KeypadMode.OPERATIONS) Color(0xFF60A5FA) else Color.White
                        )
                    },
                    onClick = { onSelectKeypadMode(KeypadMode.OPERATIONS) }
                )
                HorizontalDivider(color = Color(0xFF3B4156), thickness = 1.dp)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.keypad_functions),
                            fontSize = 17.sp,
                            fontWeight = if (state.keypadMode == KeypadMode.FUNCTIONS) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.keypadMode == KeypadMode.FUNCTIONS) Color(0xFF60A5FA) else Color.White
                        )
                    },
                    onClick = { onSelectKeypadMode(KeypadMode.FUNCTIONS) }
                )
            }
        }

        // 2. Pill button Show Graph / Hide Graph
        Row(
            modifier = Modifier
                .background(
                    if (state.screenViewMode != ScreenViewMode.CALCULATOR_ONLY) Color(0xFF2563EB) else Color(0xFF383C4E),
                    RoundedCornerShape(20.dp)
                )
                .clickable { onToggleGraph() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (state.screenViewMode) {
                    ScreenViewMode.CALCULATOR_ONLY -> stringResource(R.string.show_graph)
                    ScreenViewMode.SPLIT_SCREEN -> stringResource(R.string.hide_graph)
                    ScreenViewMode.FULLSCREEN_GRAPH -> stringResource(R.string.calculator_label)
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
