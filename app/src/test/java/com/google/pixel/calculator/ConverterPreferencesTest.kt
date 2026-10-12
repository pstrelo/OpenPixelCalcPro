package com.google.pixel.calculator

import android.content.SharedPreferences
import com.google.pixel.calculator.core.utils.UnitConverter
import com.google.pixel.calculator.data.ConverterPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ConverterPreferencesTest {

    private class FakeSharedPreferences : SharedPreferences {
        val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        override fun edit(): SharedPreferences.Editor = FakeEditor(this)

        class FakeEditor(private val parent: FakeSharedPreferences) : SharedPreferences.Editor {
            private val tempMap = mutableMapOf<String, Any?>()
            private var clearCalled = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) tempMap[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) tempMap[key] = null
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearCalled = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearCalled) parent.map.clear()
                tempMap.forEach { (k, v) ->
                    if (v == null) parent.map.remove(k) else parent.map[k] = v
                }
            }
        }
    }

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var converter: UnitConverter

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        converter = UnitConverter()
    }

    @Test
    fun testDefaultSelectionWhenEmpty() {
        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)
        val loaded = prefs.loadSelection(converter)

        assertEquals(ConverterPreferences.DEFAULT_CATEGORY, loaded.category)
        assertEquals(ConverterPreferences.DEFAULT_TOP_UNIT, loaded.topUnitCode)
        assertEquals(ConverterPreferences.DEFAULT_BOTTOM_UNIT, loaded.bottomUnitCode)
    }

    @Test
    fun testSaveAndLoadSelection() {
        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)

        prefs.saveSelection(UnitConverter.Category.SPEED, "kmh", "mph")
        val loaded = prefs.loadSelection(converter)

        assertEquals(UnitConverter.Category.SPEED, loaded.category)
        assertEquals("kmh", loaded.topUnitCode)
        assertEquals("mph", loaded.bottomUnitCode)
    }

    @Test
    fun testSaveAndLoadCurrency() {
        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)

        prefs.saveSelection(UnitConverter.Category.CURRENCY, "USD", "EUR")
        val loaded = prefs.loadSelection(converter)

        assertEquals(UnitConverter.Category.CURRENCY, loaded.category)
        assertEquals("USD", loaded.topUnitCode)
        assertEquals("EUR", loaded.bottomUnitCode)
    }

    @Test
    fun testSwapAndPersist() {
        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)

        // Initial save
        prefs.saveSelection(UnitConverter.Category.TEMPERATURE, "C", "F")
        var state = prefs.loadSelection(converter)
        assertEquals("C", state.topUnitCode)
        assertEquals("F", state.bottomUnitCode)

        // Swapping units
        prefs.saveSelection(state.category, state.bottomUnitCode, state.topUnitCode)
        state = prefs.loadSelection(converter)
        assertEquals("F", state.topUnitCode)
        assertEquals("C", state.bottomUnitCode)
    }

    @Test
    fun testFallbackOnCorruptedCategory() {
        fakePrefs.map[ConverterPreferences.KEY_CATEGORY] = "INVALID_CATEGORY_NAME_XYZ"
        fakePrefs.map[ConverterPreferences.KEY_TOP_UNIT] = "km"
        fakePrefs.map[ConverterPreferences.KEY_BOTTOM_UNIT] = "m"

        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)
        val loaded = prefs.loadSelection(converter)

        // Should fallback to default category MASS
        assertEquals(ConverterPreferences.DEFAULT_CATEGORY, loaded.category)
        // Since "km" is not in MASS, it should safely fallback to default mass units
        assertEquals(ConverterPreferences.DEFAULT_TOP_UNIT, loaded.topUnitCode)
        assertEquals(ConverterPreferences.DEFAULT_BOTTOM_UNIT, loaded.bottomUnitCode)
    }

    @Test
    fun testFallbackOnInvalidUnitCode() {
        fakePrefs.map[ConverterPreferences.KEY_CATEGORY] = UnitConverter.Category.LENGTH.name
        fakePrefs.map[ConverterPreferences.KEY_TOP_UNIT] = "non_existent_unit"
        fakePrefs.map[ConverterPreferences.KEY_BOTTOM_UNIT] = "m"

        val prefs = ConverterPreferences(sharedPreferences = fakePrefs)
        val loaded = prefs.loadSelection(converter)

        assertEquals(UnitConverter.Category.LENGTH, loaded.category)
        // Top unit fell back to the first unit of LENGTH ("m")
        val lengthUnits = converter.getUnitsForCategory(UnitConverter.Category.LENGTH)
        assertEquals(lengthUnits.first().code, loaded.topUnitCode)
        assertEquals("m", loaded.bottomUnitCode)
    }

    @Test
    fun testNullSharedPreferencesSafeFallback() {
        val prefs = ConverterPreferences(context = null, sharedPreferences = null)
        val loaded = prefs.loadSelection(converter)

        assertEquals(ConverterPreferences.DEFAULT_CATEGORY, loaded.category)
        assertEquals(ConverterPreferences.DEFAULT_TOP_UNIT, loaded.topUnitCode)
        assertEquals(ConverterPreferences.DEFAULT_BOTTOM_UNIT, loaded.bottomUnitCode)

        // Should not crash on save or clear
        prefs.saveSelection(UnitConverter.Category.COOKING, "tbsp", "tsp")
        prefs.clear()
    }
}
