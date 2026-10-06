package com.google.pixel.calculator.core.utils

import android.content.Context
import android.content.SharedPreferences
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
 * Repository per i tassi di cambio valuta offline-first a consumo dati ultra-ridotto (~2KB).
 * - Fornisce tassi di default istantanei senza alcuna connessione.
 * - Salva i tassi in SharedPreferences.
 * - Aggiorna in background solo se la cache ha più di 24 ore o su richiesta esplicita.
 */
class CurrencyRateRepository(private val context: Context? = null) {

    companion object {
        private const val PREFS_NAME = "currency_rates_prefs"
        private const val KEY_RATES_JSON = "key_rates_json"
        private const val KEY_LAST_UPDATE_MS = "key_last_update_ms"
        private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 ore

        // Tassi di default robusti (Base: EUR = 1.0) per operatività 100% offline out-of-the-box
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
     * Converte un importo tra due valute utilizzando i tassi attualmente disponibili.
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
     * Restituisce la stringa descrittiva dell'ultimo aggiornamento (es. "Aggiornato: 06/10 12:30" o "Predefiniti offline").
     */
    fun getLastUpdateText(): String {
        val ts = _lastUpdateTimestamp.value
        if (ts <= 0L) return "Tassi offline di base"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return "Aggiornato al: ${sdf.format(Date(ts))}"
    }

    /**
     * Esegue l'aggiornamento se necessario (cache > 24h) o forzato.
     * Consumo dati limitato: ~2KB per chiamata con compressione GZIP automatica.
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
                // Silenzioso: se offline o errore di rete, l'app continua ad usare i tassi cached/default senza crash
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Effettua la richiesta HTTP nativa a bassissimo consumo.
     */
    private fun fetchRemoteRates(): Map<String, Double>? {
        var conn: HttpURLConnection? = null
        return try {
            // Endpoint pubblico gratuito e leggero (~2KB)
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
