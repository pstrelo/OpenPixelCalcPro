package com.google.pixel.calculator.data

import java.util.UUID

/**
 * Represents a calculation stored in history.
 * Uses UUID as unique key to prevent rendering conflicts in LazyColumn.
 */
data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
