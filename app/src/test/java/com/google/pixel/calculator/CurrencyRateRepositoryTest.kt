package com.google.pixel.calculator

import com.google.pixel.calculator.core.utils.CurrencyRateRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CurrencyRateRepositoryTest {

    private lateinit var repository: CurrencyRateRepository

    @Before
    fun setup() {
        // Without context to test default offline fallback
        repository = CurrencyRateRepository(null)
    }

    @Test
    fun testDefaultRatesLoaded() {
        val rates = repository.rates.value
        assertTrue("I tassi di base devono includere EUR", rates.containsKey("EUR"))
        assertTrue("I tassi di base devono includere USD", rates.containsKey("USD"))
        assertTrue("I tassi di base devono includere GBP", rates.containsKey("GBP"))
        assertTrue("I tassi di base devono includere BTC", rates.containsKey("BTC"))
        assertEquals(1.0, rates["EUR"] ?: 0.0, 1e-6)
    }

    @Test
    fun testCurrencyConversionCalculation() {
        // EUR to USD: 100 EUR * 1.085 = 108.5 USD
        val usd = repository.convert(100.0, "EUR", "USD")
        assertEquals(108.5, usd, 1e-2)

        // Same currency
        val same = repository.convert(50.0, "EUR", "EUR")
        assertEquals(50.0, same, 1e-6)

        // Cross currency: USD -> GBP
        // 100 USD in EUR = 100 / 1.085 ~= 92.165 EUR
        // 92.165 EUR in GBP = 92.165 * 0.855 ~= 78.80 GBP
        val gbp = repository.convert(100.0, "USD", "GBP")
        assertTrue(gbp > 70.0 && gbp < 85.0)
    }

    @Test
    fun testLastUpdateText() {
        val text = repository.getLastUpdateText()
        assertNotNull(text)
        assertTrue(text.isNotEmpty())
    }
}
