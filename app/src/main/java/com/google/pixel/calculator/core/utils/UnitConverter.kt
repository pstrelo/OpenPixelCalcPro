package com.google.pixel.calculator.core.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs

/**
 * Motore di conversione unità di misura avanzato per Calcolatrice-Pixel.
 * - Supporta 17 categorie (fondamentali ed extra come cucina, tipografia, frequenza).
 * - Gestisce conversioni lineari, affini (temperatura) e reciproche (consumo carburante).
 * - Integra CurrencyRateRepository per valute con fallback offline immediato e sync leggero.
 */
class UnitConverter(
    val currencyRepository: CurrencyRateRepository? = null
) {

    enum class Category(val displayName: String) {
        CURRENCY("Valuta"),
        LENGTH("Lunghezza"),
        MASS("Massa / Peso"),
        TEMPERATURE("Temperatura"),
        AREA("Area"),
        VOLUME("Volume"),
        SPEED("Velocità"),
        TIME("Tempo"),
        PRESSURE("Pressione"),
        ENERGY("Energia / Lavoro"),
        POWER("Potenza"),
        DATA("Dati Digitali"),
        ANGLE("Angolo"),
        FUEL("Consumo Carburante"),
        COOKING("Cucina / Ricette"),
        TYPOGRAPHY("Tipografia"),
        FREQUENCY("Frequenza")
    }

    data class UnitDefinition(
        val code: String,
        val name: String,
        val symbol: String,
        val toBaseRatio: Double = 1.0, // Rapporto rispetto all'unità base SI della categoria
        val category: Category
    )

    fun getUnitsForCategory(category: Category): List<UnitDefinition> {
        return when (category) {
            Category.CURRENCY -> listOf(
                UnitDefinition("EUR", "Euro", "€", 1.0, Category.CURRENCY),
                UnitDefinition("USD", "Dollaro USA", "$", 1.085, Category.CURRENCY),
                UnitDefinition("GBP", "Sterlina Britannica", "£", 0.855, Category.CURRENCY),
                UnitDefinition("JPY", "Yen Giapponese", "¥", 162.50, Category.CURRENCY),
                UnitDefinition("CHF", "Franco Svizzero", "CHF", 0.965, Category.CURRENCY),
                UnitDefinition("CAD", "Dollaro Canadese", "CA$", 1.480, Category.CURRENCY),
                UnitDefinition("AUD", "Dollaro Australiano", "AU$", 1.650, Category.CURRENCY),
                UnitDefinition("CNY", "Yuan Cinese", "CN¥", 7.850, Category.CURRENCY),
                UnitDefinition("INR", "Rupia Indiana", "₹", 91.20, Category.CURRENCY),
                UnitDefinition("BRL", "Real Brasiliano", "R$", 5.950, Category.CURRENCY),
                UnitDefinition("BTC", "Bitcoin", "₿", 0.000016, Category.CURRENCY),
                UnitDefinition("ETH", "Ethereum", "Ξ", 0.00041, Category.CURRENCY)
            )

            Category.LENGTH -> listOf(
                UnitDefinition("m", "Metri", "m", 1.0, Category.LENGTH),
                UnitDefinition("km", "Chilometri", "km", 1000.0, Category.LENGTH),
                UnitDefinition("dm", "Decimetri", "dm", 0.1, Category.LENGTH),
                UnitDefinition("cm", "Centimetri", "cm", 0.01, Category.LENGTH),
                UnitDefinition("mm", "Millimetri", "mm", 0.001, Category.LENGTH),
                UnitDefinition("um", "Micrometri", "µm", 1e-6, Category.LENGTH),
                UnitDefinition("nm", "Nanometri", "nm", 1e-9, Category.LENGTH),
                UnitDefinition("in", "Pollici", "in", 0.0254, Category.LENGTH),
                UnitDefinition("ft", "Piedi", "ft", 0.3048, Category.LENGTH),
                UnitDefinition("yd", "Iarde", "yd", 0.9144, Category.LENGTH),
                UnitDefinition("mi", "Miglia terrestri", "mi", 1609.344, Category.LENGTH),
                UnitDefinition("nm_sea", "Miglia nautiche", "NM", 1852.0, Category.LENGTH),
                UnitDefinition("ly", "Anni luce", "ly", 9.4607304725808e15, Category.LENGTH)
            )

            Category.MASS -> listOf(
                UnitDefinition("kg", "Chilogrammi", "kg", 1.0, Category.MASS),
                UnitDefinition("g", "Grammi", "g", 0.001, Category.MASS),
                UnitDefinition("mg", "Milligrammi", "mg", 1e-6, Category.MASS),
                UnitDefinition("ug", "Microgrammi", "µg", 1e-9, Category.MASS),
                UnitDefinition("t", "Tonnellate metriche", "t", 1000.0, Category.MASS),
                UnitDefinition("q", "Quintali", "q", 100.0, Category.MASS),
                UnitDefinition("lb", "Libbre", "lb", 0.45359237, Category.MASS),
                UnitDefinition("oz", "Once", "oz", 0.028349523125, Category.MASS),
                UnitDefinition("ct", "Carati", "ct", 0.0002, Category.MASS),
                UnitDefinition("st", "Stone", "st", 6.35029318, Category.MASS)
            )

            Category.TEMPERATURE -> listOf(
                UnitDefinition("C", "Celsius", "°C", 1.0, Category.TEMPERATURE),
                UnitDefinition("F", "Fahrenheit", "°F", 1.0, Category.TEMPERATURE),
                UnitDefinition("K", "Kelvin", "K", 1.0, Category.TEMPERATURE)
            )

            Category.AREA -> listOf(
                UnitDefinition("m2", "Metri quadrati", "m²", 1.0, Category.AREA),
                UnitDefinition("km2", "Chilometri quadrati", "km²", 1_000_000.0, Category.AREA),
                UnitDefinition("ha", "Ettari", "ha", 10_000.0, Category.AREA),
                UnitDefinition("ac", "Acri", "ac", 4046.8564224, Category.AREA),
                UnitDefinition("cm2", "Centimetri quadrati", "cm²", 0.0001, Category.AREA),
                UnitDefinition("mm2", "Millimetri quadrati", "mm²", 1e-6, Category.AREA),
                UnitDefinition("ft2", "Piedi quadrati", "ft²", 0.09290304, Category.AREA),
                UnitDefinition("in2", "Pollici quadrati", "in²", 0.00064516, Category.AREA),
                UnitDefinition("mi2", "Miglia quadrate", "mi²", 2_589_988.110336, Category.AREA)
            )

            Category.VOLUME -> listOf(
                UnitDefinition("l", "Litri", "L", 0.001, Category.VOLUME),
                UnitDefinition("ml", "Millilitri", "mL", 1e-6, Category.VOLUME),
                UnitDefinition("cl", "Centilitri", "cL", 1e-5, Category.VOLUME),
                UnitDefinition("dl", "Decilitri", "dL", 1e-4, Category.VOLUME),
                UnitDefinition("m3", "Metri cubi", "m³", 1.0, Category.VOLUME),
                UnitDefinition("cm3", "Centimetri cubi", "cm³", 1e-6, Category.VOLUME),
                UnitDefinition("gal_us", "Galloni USA", "gal US", 0.003785411784, Category.VOLUME),
                UnitDefinition("gal_uk", "Galloni UK (Imperiali)", "gal UK", 0.00454609, Category.VOLUME),
                UnitDefinition("pt_us", "Pinte USA", "pt US", 0.000473176473, Category.VOLUME),
                UnitDefinition("pt_uk", "Pinte UK", "pt UK", 0.00056826125, Category.VOLUME),
                UnitDefinition("floz_us", "Once fluide USA", "fl oz US", 0.0000295735295625, Category.VOLUME),
                UnitDefinition("floz_uk", "Once fluide UK", "fl oz UK", 0.0000284130625, Category.VOLUME)
            )

            Category.SPEED -> listOf(
                UnitDefinition("kmh", "Chilometri orari", "km/h", 1.0 / 3.6, Category.SPEED),
                UnitDefinition("ms", "Metri al secondo", "m/s", 1.0, Category.SPEED),
                UnitDefinition("mph", "Miglia orarie", "mph", 0.44704, Category.SPEED),
                UnitDefinition("kn", "Nodi", "kn", 1852.0 / 3600.0, Category.SPEED),
                UnitDefinition("mach", "Mach (a livello del mare)", "Ma", 340.29, Category.SPEED)
            )

            Category.TIME -> listOf(
                UnitDefinition("s", "Secondi", "s", 1.0, Category.TIME),
                UnitDefinition("ms", "Millisecondi", "ms", 0.001, Category.TIME),
                UnitDefinition("us", "Microsecondi", "µs", 1e-6, Category.TIME),
                UnitDefinition("min", "Minuti", "min", 60.0, Category.TIME),
                UnitDefinition("h", "Ore", "h", 3600.0, Category.TIME),
                UnitDefinition("d", "Giorni", "d", 86400.0, Category.TIME),
                UnitDefinition("wk", "Settimane", "wk", 604800.0, Category.TIME),
                UnitDefinition("mo", "Mesi (medi)", "mo", 2629800.0, Category.TIME),
                UnitDefinition("yr", "Anni (gregoriani)", "yr", 31556952.0, Category.TIME)
            )

            Category.PRESSURE -> listOf(
                UnitDefinition("pa", "Pascal", "Pa", 1.0, Category.PRESSURE),
                UnitDefinition("hpa", "Ettopascal", "hPa", 100.0, Category.PRESSURE),
                UnitDefinition("kpa", "Kilopascal", "kPa", 1000.0, Category.PRESSURE),
                UnitDefinition("bar", "Bar", "bar", 100000.0, Category.PRESSURE),
                UnitDefinition("mbar", "Millibar", "mbar", 100.0, Category.PRESSURE),
                UnitDefinition("psi", "Pound per Square Inch", "psi", 6894.757293168, Category.PRESSURE),
                UnitDefinition("atm", "Atmosfere standard", "atm", 101325.0, Category.PRESSURE),
                UnitDefinition("mmhg", "Millimetri di mercurio", "mmHg", 133.322387415, Category.PRESSURE)
            )

            Category.ENERGY -> listOf(
                UnitDefinition("j", "Joule", "J", 1.0, Category.ENERGY),
                UnitDefinition("kj", "Kilojoule", "kJ", 1000.0, Category.ENERGY),
                UnitDefinition("cal", "Calorie", "cal", 4.184, Category.ENERGY),
                UnitDefinition("kcal", "Kilocalorie", "kcal", 4184.0, Category.ENERGY),
                UnitDefinition("wh", "Wattora", "Wh", 3600.0, Category.ENERGY),
                UnitDefinition("kwh", "Kilowattora", "kWh", 3600000.0, Category.ENERGY),
                UnitDefinition("btu", "British Thermal Unit", "BTU", 1055.05585, Category.ENERGY),
                UnitDefinition("ev", "Elettronvolt", "eV", 1.602176634e-19, Category.ENERGY)
            )

            Category.POWER -> listOf(
                UnitDefinition("w", "Watt", "W", 1.0, Category.POWER),
                UnitDefinition("kw", "Kilowatt", "kW", 1000.0, Category.POWER),
                UnitDefinition("cv", "Cavalli Vapore (CV)", "CV", 735.49875, Category.POWER),
                UnitDefinition("hp", "Horsepower (HP)", "hp", 745.69987, Category.POWER),
                UnitDefinition("btu_h", "BTU all'ora", "BTU/h", 0.29307107, Category.POWER)
            )

            Category.DATA -> listOf(
                UnitDefinition("b", "Byte", "B", 1.0, Category.DATA),
                UnitDefinition("kb", "Kilobyte (KB)", "KB", 1024.0, Category.DATA),
                UnitDefinition("mb", "Megabyte (MB)", "MB", 1048576.0, Category.DATA),
                UnitDefinition("gb", "Gigabyte (GB)", "GB", 1073741824.0, Category.DATA),
                UnitDefinition("tb", "Terabyte (TB)", "TB", 1099511627776.0, Category.DATA),
                UnitDefinition("pb", "Petabyte (PB)", "PB", 1125899906842624.0, Category.DATA),
                UnitDefinition("bit", "Bit", "b", 0.125, Category.DATA),
                UnitDefinition("kbit", "Kilobit", "kbit", 128.0, Category.DATA),
                UnitDefinition("mbit", "Megabit", "Mbit", 131072.0, Category.DATA),
                UnitDefinition("gbit", "Gigabit", "Gbit", 134217728.0, Category.DATA)
            )

            Category.ANGLE -> listOf(
                UnitDefinition("deg", "Gradi sessagesimali", "°", 1.0, Category.ANGLE),
                UnitDefinition("rad", "Radianti", "rad", 180.0 / PI, Category.ANGLE),
                UnitDefinition("grad", "Gradi centesimali (Gon)", "grad", 0.9, Category.ANGLE)
            )

            Category.FUEL -> listOf(
                UnitDefinition("l100km", "Litri per 100 km", "L/100km", 1.0, Category.FUEL),
                UnitDefinition("kml", "Chilometri al litro", "km/L", 1.0, Category.FUEL),
                UnitDefinition("mpgus", "Miglia per gallone USA", "MPG (US)", 1.0, Category.FUEL),
                UnitDefinition("mpguk", "Miglia per gallone UK", "MPG (UK)", 1.0, Category.FUEL)
            )

            Category.COOKING -> listOf(
                UnitDefinition("cup", "Tazze (Cup standard)", "cup", 240.0, Category.COOKING),
                UnitDefinition("tbsp", "Cucchiai da tavola", "tbsp", 15.0, Category.COOKING),
                UnitDefinition("tsp", "Cucchiaini da tè", "tsp", 5.0, Category.COOKING),
                UnitDefinition("floz_c", "Once fluide da cucina", "fl oz", 29.5735, Category.COOKING),
                UnitDefinition("ml_c", "Millilitri", "mL", 1.0, Category.COOKING),
                UnitDefinition("pinch", "Pizzichi", "pizzico", 0.36, Category.COOKING)
            )

            Category.TYPOGRAPHY -> listOf(
                UnitDefinition("pt", "Punti tipografici (DTP)", "pt", 0.352777778, Category.TYPOGRAPHY),
                UnitDefinition("pc", "Pica", "pc", 4.23333333, Category.TYPOGRAPHY),
                UnitDefinition("px", "Pixel (CSS a 96 DPI)", "px", 0.264583333, Category.TYPOGRAPHY),
                UnitDefinition("mm_t", "Millimetri", "mm", 1.0, Category.TYPOGRAPHY),
                UnitDefinition("in_t", "Pollici", "in", 25.4, Category.TYPOGRAPHY)
            )

            Category.FREQUENCY -> listOf(
                UnitDefinition("hz", "Hertz", "Hz", 1.0, Category.FREQUENCY),
                UnitDefinition("khz", "Kilohertz", "kHz", 1000.0, Category.FREQUENCY),
                UnitDefinition("mhz", "Megahertz", "MHz", 1e6, Category.FREQUENCY),
                UnitDefinition("ghz", "Gigahertz", "GHz", 1e9, Category.FREQUENCY),
                UnitDefinition("rpm", "Giri al minuto", "RPM", 1.0 / 60.0, Category.FREQUENCY)
            )
        }
    }

    /**
     * Trova la definizione di un'unità tramite il suo codice.
     */
    fun findUnit(code: String, category: Category? = null): UnitDefinition? {
        if (category != null) {
            val found = getUnitsForCategory(category).find { it.code == code }
            if (found != null) return found
        }
        for (cat in Category.values()) {
            val found = getUnitsForCategory(cat).find { it.code == code }
            if (found != null) return found
        }
        return null
    }

    /**
     * Esegue la conversione numerica tra due unità di una data categoria.
     */
    fun convert(
        value: Double,
        fromCode: String,
        toCode: String,
        category: Category
    ): Double {
        if (fromCode == toCode) return value
        if (value.isNaN() || value.isInfinite()) return value

        // 1. Gestione Valute
        if (category == Category.CURRENCY) {
            return currencyRepository?.convert(value, fromCode, toCode)
                ?: fallbackCurrencyConvert(value, fromCode, toCode)
        }

        // 2. Gestione Temperature (conversioni affini)
        if (category == Category.TEMPERATURE) {
            return convertTemperature(value, fromCode, toCode)
        }

        // 3. Gestione Consumo Carburante (conversioni reciproche)
        if (category == Category.FUEL) {
            return convertFuel(value, fromCode, toCode)
        }

        // 4. Conversioni lineari standard su rapporto base SI
        val units = getUnitsForCategory(category)
        val fromUnit = units.find { it.code == fromCode } ?: return value
        val toUnit = units.find { it.code == toCode } ?: return value

        val baseValue = value * fromUnit.toBaseRatio
        return baseValue / toUnit.toBaseRatio
    }

    private fun fallbackCurrencyConvert(value: Double, fromCode: String, toCode: String): Double {
        val rates = CurrencyRateRepository.DEFAULT_RATES
        val fromRate = rates[fromCode] ?: 1.0
        val toRate = rates[toCode] ?: 1.0
        if (fromRate <= 0.0) return 0.0
        return (value / fromRate) * toRate
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        val celsius = when (from) {
            "C" -> value
            "F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }

        return when (to) {
            "C" -> celsius
            "F" -> (celsius * 9.0 / 5.0) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }

    private fun convertFuel(value: Double, from: String, to: String): Double {
        if (value <= 0.0) return 0.0
        // Converti tutto nell'unità base: km/L
        val kmPerLiter = when (from) {
            "kml" -> value
            "l100km" -> 100.0 / value
            "mpgus" -> value * 0.425143707
            "mpguk" -> value * 0.35400619
            else -> value
        }

        return when (to) {
            "kml" -> kmPerLiter
            "l100km" -> if (kmPerLiter > 0.0) 100.0 / kmPerLiter else 0.0
            "mpgus" -> kmPerLiter / 0.425143707
            "mpguk" -> kmPerLiter / 0.35400619
            else -> kmPerLiter
        }
    }

    /**
     * Formatta il valore convertito con eleganza, separatori e precisione intelligente.
     */
    fun formatValue(value: Double): String {
        if (value.isNaN()) return "0"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        if (value == 0.0) return "0"

        val absVal = abs(value)
        val symbols = DecimalFormatSymbols(Locale.ITALIAN).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }

        // Notazione scientifica per valori astronomici o microscopici
        if (absVal >= 1e11 || (absVal < 1e-5 && absVal > 0.0)) {
            val expFormat = DecimalFormat("0.####E0", symbols)
            return expFormat.format(value).replace("E", " × 10^")
        }

        // Numeri ordinari con fino a 6 decimali significativi senza zeri inutili
        val df = DecimalFormat("#,##0.######", symbols)
        return df.format(value)
    }
}
