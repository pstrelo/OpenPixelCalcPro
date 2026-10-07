package com.google.pixel.calculator.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.pixel.calculator.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Offline-first repository for currency exchange rates with ultra-low data consumption (~2KB).
 * - Provides instant default rates without network connectivity.
 * - Saves cached rates in SharedPreferences.
 * - Updates in background only when cache is older than 24 hours or on explicit user request.
 */
class CurrencyRateRepository(private val context: Context? = null) {

    companion object {
        private const val PREFS_NAME = "currency_rates_prefs"
        private const val KEY_RATES_JSON = "key_rates_json"
        private const val KEY_LAST_UPDATE_MS = "key_last_update_ms"
        private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 hours

        // Robust default rates (Base: EUR = 1.0) for 100% offline out-of-the-box readiness
        val DEFAULT_RATES: Map<String, Double> = mapOf(
            "EUR" to 1.0,
            "USD" to 1.085,
            "GBP" to 0.855,
            "JPY" to 162.50,
            "CHF" to 0.965,
            "CAD" to 1.480,
            "AUD" to 1.650,
            "CNY" to 7.850,
            "INR" to 91.20,
            "BRL" to 5.950,
            "BTC" to 0.000016,
            "ETH" to 0.00041
        )
    }

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _rates = MutableStateFlow<Map<String, Double>>(loadCachedRates())
    val rates: StateFlow<Map<String, Double>> = _rates.asStateFlow()

    private val _lastUpdateTimestamp = MutableStateFlow<Long>(loadLastUpdateTimestamp())
    val lastUpdateTimestamp: StateFlow<Long> = _lastUpdateTimestamp.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private fun loadCachedRates(): Map<String, Double> {
        val jsonStr = prefs?.getString(KEY_RATES_JSON, null) ?: return DEFAULT_RATES
        return try {
            val json = JSONObject(jsonStr)
            val map = HashMap<String, Double>(DEFAULT_RATES)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.optDouble(key, DEFAULT_RATES[key] ?: 1.0)
            }
            map
        } catch (_: Exception) {
            DEFAULT_RATES
        }
    }

    private fun loadLastUpdateTimestamp(): Long {
        return prefs?.getLong(KEY_LAST_UPDATE_MS, 0L) ?: 0L
    }

    /**
     * Converts an amount between two currencies using currently available rates.
     */
    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (fromCode == toCode) return amount
        val currentRates = _rates.value
        val fromRate = currentRates[fromCode] ?: DEFAULT_RATES[fromCode] ?: 1.0
        val toRate = currentRates[toCode] ?: DEFAULT_RATES[toCode] ?: 1.0

        if (fromRate <= 0.0) return 0.0
        val baseEur = amount / fromRate
        return baseEur * toRate
    }

    /**
     * Returns a human-readable description of the last rate update (e.g. "Updated on: 06/10 12:30" or localized default).
     */
    fun getLastUpdateText(customContext: Context? = null): String {
        val effectiveContext = customContext ?: context
        val ts = _lastUpdateTimestamp.value
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        if (effectiveContext != null) {
            return if (ts <= 0L) {
                effectiveContext.getString(R.string.currency_offline_default)
            } else {
                effectiveContext.getString(R.string.currency_updated_at, sdf.format(Date(ts)))
            }
        }

        return if (ts <= 0L) {
            "Default offline rates"
        } else {
            "Updated on: ${sdf.format(Date(ts))}"
        }
    }

    /**
     * Performs update if needed (cache > 24h) or forced.
     * Lightweight network overhead: ~2KB per call with automatic GZIP compression.
     */
    fun updateRatesIfNeeded(scope: CoroutineScope, force: Boolean = false) {
        val now = System.currentTimeMillis()
        val elapsed = now - _lastUpdateTimestamp.value
        if (!force && elapsed < CACHE_VALIDITY_MS && _lastUpdateTimestamp.value > 0L) {
            return
        }

        if (_isUpdating.value) return

        scope.launch {
            _isUpdating.value = true
            try {
                val fetchedRates = withContext(Dispatchers.IO) {
                    fetchRemoteRates()
                }
                if (fetchedRates != null && fetchedRates.isNotEmpty()) {
                    val merged = HashMap(DEFAULT_RATES)
                    merged.putAll(fetchedRates)
                    _rates.value = merged
                    _lastUpdateTimestamp.value = now

                    prefs?.edit()?.apply {
                        val json = JSONObject(merged as Map<*, *>)
                        putString(KEY_RATES_JSON, json.toString())
                        putLong(KEY_LAST_UPDATE_MS, now)
                        apply()
                    }
                }
            } catch (_: Exception) {
                // Silent fallback: if offline or network error occurs, keep cached/default rates without crashing
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Performs low-overhead native HTTP request to fetch exchange rates.
     */
    private fun fetchRemoteRates(): Map<String, Double>? {
        var conn: HttpURLConnection? = null
        return try {
            // Free and lightweight public endpoint (~2KB)
            val url = URL("https://open.er-api.com/v6/latest/EUR")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "PixelCalculator/1.0")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val root = JSONObject(sb.toString())
                if (root.optString("result") == "success" || root.has("rates")) {
                    val ratesObj = root.getJSONObject("rates")
                    val result = mutableMapOf<String, Double>()
                    for (curr in DEFAULT_RATES.keys) {
                        if (ratesObj.has(curr)) {
                            result[curr] = ratesObj.getDouble(curr)
                        }
                    }
                    result
                } else null
            } else null
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
