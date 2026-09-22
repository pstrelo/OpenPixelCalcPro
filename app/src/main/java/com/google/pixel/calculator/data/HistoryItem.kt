package com.google.pixel.calculator.data

import java.util.UUID

/**
 * Rappresenta un calcolo archiviato nella cronologia.
 * Utilizza UUID come chiave univoca per evitare conflitti di rendering in LazyColumn.
 */
data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
