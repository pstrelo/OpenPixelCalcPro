package com.google.pixel.calculator

import com.google.pixel.calculator.core.utils.CurrencyRateRepository
import com.google.pixel.calculator.core.utils.UnitConverter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class UnitConverterTest {

    private lateinit var converter: UnitConverter

    @Before
    fun setup() {
        converter = UnitConverter()
    }

    @Test
    fun testAllCategoriesHaveUnits() {
        for (category in UnitConverter.Category.values()) {
            val units = converter.getUnitsForCategory(category)
            assertTrue("La categoria $category deve avere almeno 2 unità", units.size >= 2)
            // Verifica che ogni unità abbia codice, nome e simbolo non vuoti
            units.forEach { unit ->
                assertTrue("Codice unità non vuoto in $category", unit.code.isNotEmpty())
                assertTrue("Nome unità non vuoto in $category", unit.name.isNotEmpty())
                assertTrue("Simbolo non vuoto in $category", unit.symbol.isNotEmpty())
            }
        }
    }

    @Test
    fun testLengthConversion() {
        // 1 km = 1000 m
        val meters = converter.convert(1.0, "km", "m", UnitConverter.Category.LENGTH)
        assertEquals(1000.0, meters, 1e-4)

        // 1 pollice = 2.54 cm = 0.0254 m
        val cm = converter.convert(1.0, "in", "cm", UnitConverter.Category.LENGTH)
        assertEquals(2.54, cm, 1e-4)

        // 1 miglio = 1609.344 m
        val milesInKm = converter.convert(1.0, "mi", "km", UnitConverter.Category.LENGTH)
        assertEquals(1.609344, milesInKm, 1e-4)

        // Inversione: 1000 m = 1 km
        val km = converter.convert(1000.0, "m", "km", UnitConverter.Category.LENGTH)
        assertEquals(1.0, km, 1e-4)
    }

    @Test
    fun testMassConversion() {
        // 1 kg = 1000 g
        assertEquals(1000.0, converter.convert(1.0, "kg", "g", UnitConverter.Category.MASS), 1e-4)
        // 1 lb = 0.45359237 kg
        assertEquals(0.45359237, converter.convert(1.0, "lb", "kg", UnitConverter.Category.MASS), 1e-4)
        // 1 t = 1000 kg
        assertEquals(1000.0, converter.convert(1.0, "t", "kg", UnitConverter.Category.MASS), 1e-4)
        // 1 q = 100 kg
        assertEquals(100.0, converter.convert(1.0, "q", "kg", UnitConverter.Category.MASS), 1e-4)
    }

    @Test
    fun testTemperatureConversion() {
        // 0 °C = 32 °F
        assertEquals(32.0, converter.convert(0.0, "C", "F", UnitConverter.Category.TEMPERATURE), 1e-4)
        // 100 °C = 212 °F
        assertEquals(212.0, converter.convert(100.0, "C", "F", UnitConverter.Category.TEMPERATURE), 1e-4)
        // 0 °C = 273.15 K
        assertEquals(273.15, converter.convert(0.0, "C", "K", UnitConverter.Category.TEMPERATURE), 1e-4)
        // 32 °F = 0 °C
        assertEquals(0.0, converter.convert(32.0, "F", "C", UnitConverter.Category.TEMPERATURE), 1e-4)
        // -40 °C = -40 °F
        assertEquals(-40.0, converter.convert(-40.0, "C", "F", UnitConverter.Category.TEMPERATURE), 1e-4)
    }

    @Test
    fun testFuelConversion() {
        // 5 L/100km -> 20 km/L (formula reciproca: 100 / 5 = 20)
        val kml = converter.convert(5.0, "l100km", "kml", UnitConverter.Category.FUEL)
        assertEquals(20.0, kml, 1e-2)

        // 20 km/L -> 5 L/100km
        val l100 = converter.convert(20.0, "kml", "l100km", UnitConverter.Category.FUEL)
        assertEquals(5.0, l100, 1e-2)

        // Casi limite zero / negativi
        assertEquals(0.0, converter.convert(0.0, "l100km", "kml", UnitConverter.Category.FUEL), 1e-4)
    }

    @Test
    fun testCurrencyOfflineConversion() {
        // Con i tassi offline di base: EUR = 1.0, USD = 1.085
        // 100 EUR -> 108.5 USD
        val usd = converter.convert(100.0, "EUR", "USD", UnitConverter.Category.CURRENCY)
        assertEquals(108.5, usd, 1e-2)

        // 108.5 USD -> 100 EUR
        val eur = converter.convert(108.5, "USD", "EUR", UnitConverter.Category.CURRENCY)
        assertEquals(100.0, eur, 1e-2)

        // USD -> JPY
        val jpy = converter.convert(10.0, "USD", "JPY", UnitConverter.Category.CURRENCY)
        assertTrue(jpy > 0)
    }

    @Test
    fun testExtraCategories() {
        // Cucina: 1 tazza (cup = 240 mL) in cucchiai (tbsp = 15 mL) -> 16 cucchiai
        val tbsp = converter.convert(1.0, "cup", "tbsp", UnitConverter.Category.COOKING)
        assertEquals(16.0, tbsp, 1e-2)

        // Tipografia: 72 pt = 1 pollice = 25.4 mm
        val mm = converter.convert(72.0, "pt", "mm_t", UnitConverter.Category.TYPOGRAPHY)
        assertEquals(25.4, mm, 1e-1)

        // Frequenza: 60 RPM = 1 Hz
        val hz = converter.convert(60.0, "rpm", "hz", UnitConverter.Category.FREQUENCY)
        assertEquals(1.0, hz, 1e-4)

        // Pressione: 1 bar = 100000 Pa = 1000 hPa
        val hpa = converter.convert(1.0, "bar", "hpa", UnitConverter.Category.PRESSURE)
        assertEquals(1000.0, hpa, 1e-2)

        // Dati Digitali: 1 GB = 1024 MB
        val mb = converter.convert(1.0, "gb", "mb", UnitConverter.Category.DATA)
        assertEquals(1024.0, mb, 1e-4)
    }

    @Test
    fun testValueFormatting() {
        assertEquals("0", converter.formatValue(0.0))
        assertEquals("1.000", converter.formatValue(1000.0))
        assertEquals("12,5", converter.formatValue(12.5))
        assertEquals("0,123456", converter.formatValue(0.123456))

        // Notazione scientifica per numeri astronomici
        val formattedHuge = converter.formatValue(1e12)
        assertTrue("Dovrebbe contenere notazione scientifica", formattedHuge.contains("10^") || formattedHuge.contains("E"))
    }

    @Test
    fun testFindUnit() {
        val meter = converter.findUnit("m", UnitConverter.Category.LENGTH)
        assertNotNull(meter)
        assertEquals("Metri", meter?.name)

        val usd = converter.findUnit("USD")
        assertNotNull(usd)
        assertEquals(UnitConverter.Category.CURRENCY, usd?.category)
    }

    @Test
    fun testUserSwapScenarioGramsAndKg() {
        // Iniziale: 100g -> 0.1 kg
        val initialConverted = converter.convert(100.0, "g", "kg", UnitConverter.Category.MASS)
        assertEquals(0.1, initialConverted, 1e-4)
        assertEquals("0,1", converter.formatValue(initialConverted))

        // Al click di Swap: le unità si scambiano e il valore 100 inserito diventa 100 kg -> 100.000 g (x g)
        val swappedConverted = converter.convert(100.0, "kg", "g", UnitConverter.Category.MASS)
        assertEquals(100000.0, swappedConverted, 1e-4)
        assertEquals("100.000", converter.formatValue(swappedConverted))
    }

    @Test
    fun testUserSelectionScenarioEditingBottomValue() {
        // Selezionando la casella dei kg e digitando 10: 10 kg -> 10.000 g
        val convertedFromTenKg = converter.convert(10.0, "kg", "g", UnitConverter.Category.MASS)
        assertEquals(10000.0, convertedFromTenKg, 1e-4)
        assertEquals("10.000", converter.formatValue(convertedFromTenKg))
    }

    @Test
    fun testNoEmojisInCategoriesAndUnits() {
        // Verifica che non ci siano emoji nei nomi delle categorie
        val emojiRegex = Regex("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")
        for (category in UnitConverter.Category.values()) {
            assertFalse(
                "La categoria ${category.name} non deve contenere emoji nel displayName: ${category.displayName}",
                emojiRegex.containsMatchIn(category.displayName)
            )
            for (unit in converter.getUnitsForCategory(category)) {
                assertFalse(
                    "L'unità ${unit.name} non deve contenere emoji nel nome",
                    emojiRegex.containsMatchIn(unit.name)
                )
            }
        }
    }

    @Test
    fun testAllCategoriesHaveMaterialIcons() {
        for (category in UnitConverter.Category.values()) {
            val icon = com.google.pixel.calculator.ui.toolbox.getCategoryMaterialIcon(category)
            assertNotNull("La categoria ${category.name} deve avere un'icona Material", icon)
        }
    }
}
