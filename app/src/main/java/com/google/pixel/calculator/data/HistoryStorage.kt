package com.google.pixel.calculator.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Storage snello, thread-safe e reattivo per la cronologia con limite a 200 elementi.
 * Utilizza Mutex per prevenire race conditions e parsing difensivo per record corrotti.
 */
class HistoryStorage(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("pixel_calc_history", Context.MODE_PRIVATE)
    private val maxItems = 200
    private val mutex = Mutex()

    private val _historyFlow = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyFlow: StateFlow<List<HistoryItem>> = _historyFlow.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val rawJson = prefs.getString("items", "[]") ?: "[]"
        try {
            val array = JSONArray(rawJson)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    HistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _historyFlow.value = list
        } catch (e: Exception) {
            _historyFlow.value = emptyList()
        }
    }

    suspend fun addItem(expression: String, result: String) = withContext(Dispatchers.IO) {
        if (expression.isBlank() || result.isBlank()) return@withContext

        val safeExpr = expression.take(500)
        val safeResult = result.take(200)

        mutex.withLock {
            val currentList = _historyFlow.value.toMutableList()
            // Inserisci in cima con ID univoco
            currentList.add(0, HistoryItem(id = UUID.randomUUID().toString(), expression = safeExpr, result = safeResult))

            // Mantieni massimo 200 elementi
            val trimmed = currentList.take(maxItems)
            saveList(trimmed)
            _historyFlow.value = trimmed
        }
    }

    suspend fun deleteItem(id: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val currentList = _historyFlow.value.toMutableList()
            currentList.removeAll { it.id == id }
            saveList(currentList)
            _historyFlow.value = currentList
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        mutex.withLock {
            prefs.edit().remove("items").apply()
            _historyFlow.value = emptyList()
        }
    }

    private fun saveList(list: List<HistoryItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("items", array.toString()).apply()
    }
}
