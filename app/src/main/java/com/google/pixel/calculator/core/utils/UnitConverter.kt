package com.google.pixel.calculator.core.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Motore di conversione unità di misura 100% offline.
 */
class UnitConverter {

    enum class Category(val displayName: String) {
        LENGTH("Lunghezza"),
        MASS("Massa / Peso"),
        TEMPERATURE("Temperatura"),
        AREA("Area"),
        VOLUME("Volume"),
        DATA("Dati Digitali"),
        SPEED("Velocità"),
        TIME("Tempo")
    }

    data class UnitDefinition(
        val code: String,
        val name: String,
        val symbol: String,
        val toBaseRatio: Double = 1.0 // Rapporto rispetto all'unità base del SI
    )

    fun getUnitsForCategory(category: Category): List<UnitDefinition> {
        return when (category) {
            Category.LENGTH -> listOf(
                UnitDefinition("m", "Metri", "m", 1.0),
                UnitDefinition("km", "Chilometri", "km", 1000.0),
                UnitDefinition("cm", "Centimetri", "cm", 0.01),
                UnitDefinition("mm", "Millimetri", "mm", 0.001),
                UnitDefinition("in", "Pollici", "in", 0.0254),
                UnitDefinition("ft", "Piedi", "ft", 0.3048),
                UnitDefinition("yd", "Iarde", "yd", 0.9144),
                UnitDefinition("mi", "Miglia", "mi", 1609.344)
            )
            Category.MASS -> listOf(
                UnitDefinition("kg", "Chilogrammi", "kg", 1.0),
                UnitDefinition("g", "Grammi", "g", 0.001),
                UnitDefinition("mg", "Milligrammi", "mg", 0.000001),
                UnitDefinition("t", "Tonnellate", "t", 1000.0),
                UnitDefinition("lb", "Libbre", "lb", 0.45359237),
                UnitDefinition("oz", "Once", "oz", 0.028349523125)
            )
            Category.TEMPERATURE -> listOf(
                UnitDefinition("C", "Celsius", "°C"),
                UnitDefinition("F", "Fahrenheit", "°F"),
                UnitDefinition("K", "Kelvin", "K")
            )
            Category.AREA -> listOf(
                UnitDefinition("m2", "Metri quadrati", "m²", 1.0),
                UnitDefinition("km2", "Chilometri quadrati", "km²", 1_000_000.0),
                UnitDefinition("cm2", "Centimetri quadrati", "cm²", 0.0001),
                UnitDefinition("ha", "Ettari", "ha", 10_000.0),
                UnitDefinition("ac", "Acri", "ac", 4046.8564224),
                UnitDefinition("ft2", "Piedi quadrati", "ft²", 0.09290304)
            )
            Category.VOLUME -> listOf(
                UnitDefinition("l", "Litri", "L", 0.001),
                UnitDefinition("ml", "Millilitri", "mL", 0.000001),
                UnitDefinition("m3", "Metri cubi", "m³", 1.0),
                UnitDefinition("gal", "Galloni USA", "gal", 0.003785411784),
                UnitDefinition("pt", "Pinte USA", "pt", 0.000473176473)
            )
            Category.DATA -> listOf(
                UnitDefinition("B", "Byte", "B", 1.0),
                UnitDefinition("KB", "Kilobyte (KB)", "KB", 1024.0),
                UnitDefinition("MB", "Megabyte (MB)", "MB", 1048576.0),
                UnitDefinition("GB", "Gigabyte (GB)", "GB", 1073741824.0),
                UnitDefinition("TB", "Terabyte (TB)", "TB", 1099511627776.0)
            )
            Category.SPEED -> listOf(
                UnitDefinition("kmh", "Chilometri orari", "km/h", 1.0 / 3.6),
                UnitDefinition("ms", "Metri al secondo", "m/s", 1.0),
                UnitDefinition("mph", "Miglia orarie", "mph", 0.44704),
                UnitDefinition("kn", "Nodi", "kn", 1852.0 / 3600.0)
            )
            Category.TIME -> listOf(
                UnitDefinition("s", "Secondi", "s", 1.0),
                UnitDefinition("min", "Minuti", "min", 60.0),
                UnitDefinition("h", "Ore", "h", 3600.0),
                UnitDefinition("d", "Giorni", "d", 86400.0),
                UnitDefinition("wk", "Settimane", "wk", 604800.0)
            )
        }
    }

    fun convert(
        value: Double,
        fromCode: String,
        toCode: String,
        category: Category
    ): Double {
        if (fromCode == toCode) return value

        if (category == Category.TEMPERATURE) {
            return convertTemperature(value, fromCode, toCode)
        }

        val units = getUnitsForCategory(category)
        val fromUnit = units.find { it.code == fromCode } ?: return value
        val toUnit = units.find { it.code == toCode } ?: return value

        // Converti a unità base SI e poi a target
        val baseValue = value * fromUnit.toBaseRatio
        return baseValue / toUnit.toBaseRatio
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
}
