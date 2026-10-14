package com.google.pixel.calculator.core.utils

import com.google.pixel.calculator.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs

/**
 * Advanced unit conversion engine for Pixel Calculator.
 * - Supports 17 categories (fundamental SI dimensions and specialized ones like cooking, typography, frequency).
 * - Handles linear, affine (temperature), and reciprocal (fuel consumption) conversions.
 * - Integrates CurrencyRateRepository for currencies with immediate offline fallback and lightweight sync.
 */
class UnitConverter(
    val currencyRepository: CurrencyRateRepository? = null
) {

    enum class Category(val displayName: String, val nameRes: Int = 0) {
        CURRENCY("Valuta", R.string.category_currency),
        LENGTH("Lunghezza", R.string.category_length),
        MASS("Massa / Peso", R.string.category_mass),
        TEMPERATURE("Temperatura", R.string.category_temperature),
        AREA("Area", R.string.category_area),
        VOLUME("Volume", R.string.category_volume),
        SPEED("Velocità", R.string.category_speed),
        TIME("Tempo", R.string.category_time),
        PRESSURE("Pressione", R.string.category_pressure),
        ENERGY("Energia / Lavoro", R.string.category_energy),
        POWER("Potenza", R.string.category_power),
        DATA("Dati Digitali", R.string.category_data),
        ANGLE("Angolo", R.string.category_angle),
        FUEL("Consumo Carburante", R.string.category_fuel),
        COOKING("Cucina / Ricette", R.string.category_cooking),
        TYPOGRAPHY("Tipografia", R.string.category_typography),
        FREQUENCY("Frequenza", R.string.category_frequency)
    }

    data class UnitDefinition(
        val code: String,
        val name: String,
        val symbol: String,
        val toBaseRatio: Double = 1.0, // Ratio relative to the SI base unit of the category
        val category: Category,
        val nameRes: Int? = null
    )

    fun getUnitsForCategory(category: Category): List<UnitDefinition> {
        return when (category) {
            Category.CURRENCY -> listOf(
                UnitDefinition("EUR", "Euro", "€", 1.0, Category.CURRENCY, R.string.unit_eur),
                UnitDefinition("USD", "Dollaro USA", "$", 1.085, Category.CURRENCY, R.string.unit_usd),
                UnitDefinition("GBP", "Sterlina Britannica", "£", 0.855, Category.CURRENCY, R.string.unit_gbp),
                UnitDefinition("JPY", "Yen Giapponese", "¥", 162.50, Category.CURRENCY, R.string.unit_jpy),
                UnitDefinition("CHF", "Franco Svizzero", "CHF", 0.965, Category.CURRENCY, R.string.unit_chf),
                UnitDefinition("CAD", "Dollaro Canadese", "CA$", 1.480, Category.CURRENCY, R.string.unit_cad),
                UnitDefinition("AUD", "Dollaro Australiano", "AU$", 1.650, Category.CURRENCY, R.string.unit_aud),
                UnitDefinition("CNY", "Yuan Cinese", "CN¥", 7.850, Category.CURRENCY, R.string.unit_cny),
                UnitDefinition("INR", "Rupia Indiana", "₹", 91.20, Category.CURRENCY, R.string.unit_inr),
                UnitDefinition("BRL", "Real Brasiliano", "R$", 5.950, Category.CURRENCY, R.string.unit_brl),
                UnitDefinition("BTC", "Bitcoin", "₿", 0.000016, Category.CURRENCY, R.string.unit_btc),
                UnitDefinition("ETH", "Ethereum", "Ξ", 0.00041, Category.CURRENCY, R.string.unit_eth)
            )

            Category.LENGTH -> listOf(
                UnitDefinition("m", "Metri", "m", 1.0, Category.LENGTH, R.string.unit_m),
                UnitDefinition("km", "Chilometri", "km", 1000.0, Category.LENGTH, R.string.unit_km),
                UnitDefinition("dm", "Decimetri", "dm", 0.1, Category.LENGTH, R.string.unit_dm),
                UnitDefinition("cm", "Centimetri", "cm", 0.01, Category.LENGTH, R.string.unit_cm),
                UnitDefinition("mm", "Millimetri", "mm", 0.001, Category.LENGTH, R.string.unit_mm),
                UnitDefinition("um", "Micrometri", "µm", 1e-6, Category.LENGTH, R.string.unit_um),
                UnitDefinition("nm", "Nanometri", "nm", 1e-9, Category.LENGTH, R.string.unit_nm),
                UnitDefinition("in", "Pollici", "in", 0.0254, Category.LENGTH, R.string.unit_in),
                UnitDefinition("ft", "Piedi", "ft", 0.3048, Category.LENGTH, R.string.unit_ft),
                UnitDefinition("yd", "Iarde", "yd", 0.9144, Category.LENGTH, R.string.unit_yd),
                UnitDefinition("mi", "Miglia terrestri", "mi", 1609.344, Category.LENGTH, R.string.unit_mi),
                UnitDefinition("nm_sea", "Miglia nautiche", "NM", 1852.0, Category.LENGTH, R.string.unit_nm_sea),
                UnitDefinition("ly", "Anni luce", "ly", 9.4607304725808e15, Category.LENGTH, R.string.unit_ly)
            )

            Category.MASS -> listOf(
                UnitDefinition("kg", "Chilogrammi", "kg", 1.0, Category.MASS, R.string.unit_kg),
                UnitDefinition("g", "Grammi", "g", 0.001, Category.MASS, R.string.unit_g),
                UnitDefinition("mg", "Milligrammi", "mg", 1e-6, Category.MASS, R.string.unit_mg),
                UnitDefinition("ug", "Microgrammi", "µg", 1e-9, Category.MASS, R.string.unit_ug),
                UnitDefinition("t", "Tonnellate metriche", "t", 1000.0, Category.MASS, R.string.unit_t),
                UnitDefinition("q", "Quintali", "q", 100.0, Category.MASS, R.string.unit_q),
                UnitDefinition("lb", "Libbre", "lb", 0.45359237, Category.MASS, R.string.unit_lb),
                UnitDefinition("oz", "Once", "oz", 0.028349523125, Category.MASS, R.string.unit_oz),
                UnitDefinition("ct", "Carati", "ct", 0.0002, Category.MASS, R.string.unit_ct),
                UnitDefinition("st", "Stone", "st", 6.35029318, Category.MASS, R.string.unit_st)
            )

            Category.TEMPERATURE -> listOf(
                UnitDefinition("C", "Celsius", "°C", 1.0, Category.TEMPERATURE, R.string.unit_c),
                UnitDefinition("F", "Fahrenheit", "°F", 1.0, Category.TEMPERATURE, R.string.unit_f),
                UnitDefinition("K", "Kelvin", "K", 1.0, Category.TEMPERATURE, R.string.unit_k)
            )

            Category.AREA -> listOf(
                UnitDefinition("m2", "Metri quadrati", "m²", 1.0, Category.AREA, R.string.unit_m2),
                UnitDefinition("km2", "Chilometri quadrati", "km²", 1_000_000.0, Category.AREA, R.string.unit_km2),
                UnitDefinition("ha", "Ettari", "ha", 10_000.0, Category.AREA, R.string.unit_ha),
                UnitDefinition("a", "Are", "a", 100.0, Category.AREA, R.string.unit_a),
                UnitDefinition("ac", "Acri", "ac", 4046.8564224, Category.AREA, R.string.unit_ac),
                UnitDefinition("dm2", "Decimetri quadrati", "dm²", 0.01, Category.AREA, R.string.unit_dm2),
                UnitDefinition("cm2", "Centimetri quadrati", "cm²", 0.0001, Category.AREA, R.string.unit_cm2),
                UnitDefinition("mm2", "Millimetri quadrati", "mm²", 1e-6, Category.AREA, R.string.unit_mm2),
                UnitDefinition("yd2", "Iarde quadrate", "yd²", 0.83612736, Category.AREA, R.string.unit_yd2),
                UnitDefinition("ft2", "Piedi quadrati", "ft²", 0.09290304, Category.AREA, R.string.unit_ft2),
                UnitDefinition("in2", "Pollici quadrati", "in²", 0.00064516, Category.AREA, R.string.unit_in2),
                UnitDefinition("mi2", "Miglia quadrate", "mi²", 2_589_988.110336, Category.AREA, R.string.unit_mi2)
            )

            Category.VOLUME -> listOf(
                UnitDefinition("m3", "Metri cubi", "m³", 1.0, Category.VOLUME, R.string.unit_m3),
                UnitDefinition("dm3", "Decimetri cubi", "dm³", 0.001, Category.VOLUME, R.string.unit_dm3),
                UnitDefinition("cm3", "Centimetri cubi", "cm³", 1e-6, Category.VOLUME, R.string.unit_cm3),
                UnitDefinition("mm3", "Millimetri cubi", "mm³", 1e-9, Category.VOLUME, R.string.unit_mm3),
                UnitDefinition("hl", "Ettolitri", "hL", 0.1, Category.VOLUME, R.string.unit_hl),
                UnitDefinition("l", "Litri", "L", 0.001, Category.VOLUME, R.string.unit_l),
                UnitDefinition("dl", "Decilitri", "dL", 1e-4, Category.VOLUME, R.string.unit_dl),
                UnitDefinition("cl", "Centilitri", "cL", 1e-5, Category.VOLUME, R.string.unit_cl),
                UnitDefinition("ml", "Millilitri", "mL", 1e-6, Category.VOLUME, R.string.unit_ml),
                UnitDefinition("yd3", "Iarde cubiche", "yd³", 0.764554857984, Category.VOLUME, R.string.unit_yd3),
                UnitDefinition("ft3", "Piedi cubi", "ft³", 0.028316846592, Category.VOLUME, R.string.unit_ft3),
                UnitDefinition("in3", "Pollici cubi", "in³", 0.000016387064, Category.VOLUME, R.string.unit_in3),
                UnitDefinition("gal_us", "Galloni USA", "gal US", 0.003785411784, Category.VOLUME, R.string.unit_gal_us),
                UnitDefinition("gal_uk", "Galloni UK (Imperiali)", "gal UK", 0.00454609, Category.VOLUME, R.string.unit_gal_uk),
                UnitDefinition("pt_us", "Pinte USA", "pt US", 0.000473176473, Category.VOLUME, R.string.unit_pt_us),
                UnitDefinition("pt_uk", "Pinte UK", "pt UK", 0.00056826125, Category.VOLUME, R.string.unit_pt_uk),
                UnitDefinition("floz_us", "Once fluide USA", "fl oz US", 0.0000295735295625, Category.VOLUME, R.string.unit_floz_us),
                UnitDefinition("floz_uk", "Once fluide UK", "fl oz UK", 0.0000284130625, Category.VOLUME, R.string.unit_floz_uk)
            )

            Category.SPEED -> listOf(
                UnitDefinition("kmh", "Chilometri orari", "km/h", 1.0 / 3.6, Category.SPEED, R.string.unit_kmh),
                UnitDefinition("ms", "Metri al secondo", "m/s", 1.0, Category.SPEED, R.string.unit_ms),
                UnitDefinition("mph", "Miglia orarie", "mph", 0.44704, Category.SPEED, R.string.unit_mph),
                UnitDefinition("kn", "Nodi", "kn", 1852.0 / 3600.0, Category.SPEED, R.string.unit_kn),
                UnitDefinition("mach", "Mach (a livello del mare)", "Ma", 340.29, Category.SPEED, R.string.unit_mach)
            )

            Category.TIME -> listOf(
                UnitDefinition("s", "Secondi", "s", 1.0, Category.TIME, R.string.unit_s),
                UnitDefinition("ms", "Millisecondi", "ms", 0.001, Category.TIME, R.string.unit_time_ms),
                UnitDefinition("us", "Microsecondi", "µs", 1e-6, Category.TIME, R.string.unit_us),
                UnitDefinition("min", "Minuti", "min", 60.0, Category.TIME, R.string.unit_min),
                UnitDefinition("h", "Ore", "h", 3600.0, Category.TIME, R.string.unit_h),
                UnitDefinition("d", "Giorni", "d", 86400.0, Category.TIME, R.string.unit_d),
                UnitDefinition("wk", "Settimane", "wk", 604800.0, Category.TIME, R.string.unit_wk),
                UnitDefinition("mo", "Mesi (medi)", "mo", 2629800.0, Category.TIME, R.string.unit_mo),
                UnitDefinition("yr", "Anni (gregoriani)", "yr", 31556952.0, Category.TIME, R.string.unit_yr)
            )

            Category.PRESSURE -> listOf(
                UnitDefinition("pa", "Pascal", "Pa", 1.0, Category.PRESSURE, R.string.unit_pa),
                UnitDefinition("hpa", "Ettopascal", "hPa", 100.0, Category.PRESSURE, R.string.unit_hpa),
                UnitDefinition("kpa", "Kilopascal", "kPa", 1000.0, Category.PRESSURE, R.string.unit_kpa),
                UnitDefinition("bar", "Bar", "bar", 100000.0, Category.PRESSURE, R.string.unit_bar),
                UnitDefinition("mbar", "Millibar", "mbar", 100.0, Category.PRESSURE, R.string.unit_mbar),
                UnitDefinition("psi", "Pound per Square Inch", "psi", 6894.757293168, Category.PRESSURE, R.string.unit_psi),
                UnitDefinition("atm", "Atmosfere standard", "atm", 101325.0, Category.PRESSURE, R.string.unit_atm),
                UnitDefinition("mmhg", "Millimetri di mercurio", "mmHg", 133.322387415, Category.PRESSURE, R.string.unit_mmhg)
            )

            Category.ENERGY -> listOf(
                UnitDefinition("j", "Joule", "J", 1.0, Category.ENERGY, R.string.unit_j),
                UnitDefinition("kj", "Kilojoule", "kJ", 1000.0, Category.ENERGY, R.string.unit_kj),
                UnitDefinition("cal", "Calorie", "cal", 4.184, Category.ENERGY, R.string.unit_cal),
                UnitDefinition("kcal", "Kilocalorie", "kcal", 4184.0, Category.ENERGY, R.string.unit_kcal),
                UnitDefinition("wh", "Wattora", "Wh", 3600.0, Category.ENERGY, R.string.unit_wh),
                UnitDefinition("kwh", "Kilowattora", "kWh", 3600000.0, Category.ENERGY, R.string.unit_kwh),
                UnitDefinition("btu", "British Thermal Unit", "BTU", 1055.05585, Category.ENERGY, R.string.unit_btu),
                UnitDefinition("ev", "Elettronvolt", "eV", 1.602176634e-19, Category.ENERGY, R.string.unit_ev)
            )

            Category.POWER -> listOf(
                UnitDefinition("w", "Watt", "W", 1.0, Category.POWER, R.string.unit_w),
                UnitDefinition("kw", "Kilowatt", "kW", 1000.0, Category.POWER, R.string.unit_kw),
                UnitDefinition("cv", "Cavalli Vapore (CV)", "CV", 735.49875, Category.POWER, R.string.unit_cv),
                UnitDefinition("hp", "Horsepower (HP)", "hp", 745.69987, Category.POWER, R.string.unit_hp),
                UnitDefinition("btu_h", "BTU all'ora", "BTU/h", 0.29307107, Category.POWER, R.string.unit_btu_h)
            )

            Category.DATA -> listOf(
                UnitDefinition("b", "Byte", "B", 1.0, Category.DATA, R.string.unit_b),
                UnitDefinition("kb", "Kilobyte (KB)", "KB", 1024.0, Category.DATA, R.string.unit_kb),
                UnitDefinition("mb", "Megabyte (MB)", "MB", 1048576.0, Category.DATA, R.string.unit_mb),
                UnitDefinition("gb", "Gigabyte (GB)", "GB", 1073741824.0, Category.DATA, R.string.unit_gb),
                UnitDefinition("tb", "Terabyte (TB)", "TB", 1099511627776.0, Category.DATA, R.string.unit_tb),
                UnitDefinition("pb", "Petabyte (PB)", "PB", 1125899906842624.0, Category.DATA, R.string.unit_pb),
                UnitDefinition("bit", "Bit", "b", 0.125, Category.DATA, R.string.unit_bit),
                UnitDefinition("kbit", "Kilobit", "kbit", 128.0, Category.DATA, R.string.unit_kbit),
                UnitDefinition("mbit", "Megabit", "Mbit", 131072.0, Category.DATA, R.string.unit_mbit),
                UnitDefinition("gbit", "Gigabit", "Gbit", 134217728.0, Category.DATA, R.string.unit_gbit)
            )

            Category.ANGLE -> listOf(
                UnitDefinition("deg", "Gradi sessagesimali", "°", 1.0, Category.ANGLE, R.string.unit_deg),
                UnitDefinition("rad", "Radianti", "rad", 180.0 / PI, Category.ANGLE, R.string.unit_rad),
                UnitDefinition("grad", "Gradi centesimali (Gon)", "grad", 0.9, Category.ANGLE, R.string.unit_grad)
            )

            Category.FUEL -> listOf(
                UnitDefinition("l100km", "Litri per 100 km", "L/100km", 1.0, Category.FUEL, R.string.unit_l100km),
                UnitDefinition("kml", "Chilometri al litro", "km/L", 1.0, Category.FUEL, R.string.unit_kml),
                UnitDefinition("mpgus", "Miglia per gallone USA", "MPG (US)", 1.0, Category.FUEL, R.string.unit_mpgus),
                UnitDefinition("mpguk", "Miglia per gallone UK", "MPG (UK)", 1.0, Category.FUEL, R.string.unit_mpguk)
            )

            Category.COOKING -> listOf(
                UnitDefinition("cup", "Tazze (Cup standard)", "cup", 240.0, Category.COOKING, R.string.unit_cup),
                UnitDefinition("tbsp", "Cucchiai da tavola", "tbsp", 15.0, Category.COOKING, R.string.unit_tbsp),
                UnitDefinition("tsp", "Cucchiaini da tè", "tsp", 5.0, Category.COOKING, R.string.unit_tsp),
                UnitDefinition("floz_c", "Once fluide da cucina", "fl oz", 29.5735, Category.COOKING, R.string.unit_floz_c),
                UnitDefinition("ml_c", "Millilitri", "mL", 1.0, Category.COOKING, R.string.unit_ml_cook),
                UnitDefinition("pinch", "Pizzichi", "pizzico", 0.36, Category.COOKING, R.string.unit_pinch)
            )

            Category.TYPOGRAPHY -> listOf(
                UnitDefinition("pt", "Punti tipografici (DTP)", "pt", 0.352777778, Category.TYPOGRAPHY, R.string.unit_pt),
                UnitDefinition("pc", "Pica", "pc", 4.23333333, Category.TYPOGRAPHY, R.string.unit_pica),
                UnitDefinition("px", "Pixel (CSS a 96 DPI)", "px", 0.264583333, Category.TYPOGRAPHY, R.string.unit_px),
                UnitDefinition("mm_t", "Millimetri", "mm", 1.0, Category.TYPOGRAPHY, R.string.unit_mm_t),
                UnitDefinition("in_t", "Pollici", "in", 25.4, Category.TYPOGRAPHY, R.string.unit_in_t)
            )

            Category.FREQUENCY -> listOf(
                UnitDefinition("hz", "Hertz", "Hz", 1.0, Category.FREQUENCY, R.string.unit_hz),
                UnitDefinition("khz", "Kilohertz", "kHz", 1000.0, Category.FREQUENCY, R.string.unit_khz),
                UnitDefinition("mhz", "Megahertz", "MHz", 1e6, Category.FREQUENCY, R.string.unit_mhz),
                UnitDefinition("ghz", "Gigahertz", "GHz", 1e9, Category.FREQUENCY, R.string.unit_ghz),
                UnitDefinition("rpm", "Giri al minuto", "RPM", 1.0 / 60.0, Category.FREQUENCY, R.string.unit_rpm)
            )
        }
    }

    /**
     * Finds a unit definition by its code.
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
     * Executes numerical conversion between two units in a given category.
     */
    fun convert(
        value: Double,
        fromCode: String,
        toCode: String,
        category: Category
    ): Double {
        if (fromCode == toCode) return value
        if (value.isNaN() || value.isInfinite()) return value

        // 1. Currency handling
        if (category == Category.CURRENCY) {
            return currencyRepository?.convert(value, fromCode, toCode)
                ?: fallbackCurrencyConvert(value, fromCode, toCode)
        }

        // 2. Temperature handling (affine conversions)
        if (category == Category.TEMPERATURE) {
            return convertTemperature(value, fromCode, toCode)
        }

        // 3. Fuel consumption handling (reciprocal conversions)
        if (category == Category.FUEL) {
            return convertFuel(value, fromCode, toCode)
        }

        // 4. Same category linear conversion based on SI base ratio
        val fromUnit = findUnit(fromCode, category) ?: return value
        val toUnit = findUnit(toCode, category) ?: return value

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
        // Convert everything to base unit: km/L
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
     * Formats the converted value with clean separators and smart precision.
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

        // Scientific notation for astronomical or microscopic values
        if (absVal >= 1e11 || (absVal < 1e-5 && absVal > 0.0)) {
            val expFormat = DecimalFormat("0.####E0", symbols)
            return expFormat.format(value).replace("E", " × 10^")
        }

        // Ordinary numbers with up to 6 significant decimals without trailing zeros
        val df = DecimalFormat("#,##0.######", symbols)
        return df.format(value)
    }
}
