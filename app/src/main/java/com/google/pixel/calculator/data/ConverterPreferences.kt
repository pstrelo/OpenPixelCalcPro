package com.google.pixel.calculator.data

import android.content.Context
import android.content.SharedPreferences
import com.google.pixel.calculator.core.utils.UnitConverter

/**
 * Persists the user's last selected unit converter category and units
 * so that they are remembered across app restarts and navigation transitions.
 */
class ConverterPreferences(
    private val context: Context? = null,
    sharedPreferences: SharedPreferences? = null
) {
    companion object {
        const val PREFS_NAME = "pixel_calc_converter_prefs"
        const val KEY_CATEGORY = "converter_selected_category"
        const val KEY_TOP_UNIT = "converter_top_unit_code"
        const val KEY_BOTTOM_UNIT = "converter_bottom_unit_code"

        val DEFAULT_CATEGORY = UnitConverter.Category.MASS
        const val DEFAULT_TOP_UNIT = "g"
        const val DEFAULT_BOTTOM_UNIT = "kg"
    }

    private val prefs: SharedPreferences? = sharedPreferences
        ?: context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class SavedConverterState(
        val category: UnitConverter.Category,
        val topUnitCode: String,
        val bottomUnitCode: String
    )

    fun saveSelection(category: UnitConverter.Category, topUnitCode: String, bottomUnitCode: String) {
        prefs?.edit()
            ?.putString(KEY_CATEGORY, category.name)
            ?.putString(KEY_TOP_UNIT, topUnitCode)
            ?.putString(KEY_BOTTOM_UNIT, bottomUnitCode)
            ?.apply()
    }

    fun loadSelection(converter: UnitConverter = UnitConverter()): SavedConverterState {
        val savedCategoryName = prefs?.getString(KEY_CATEGORY, null)
        val category = savedCategoryName?.let { name ->
            try {
                UnitConverter.Category.valueOf(name)
            } catch (e: Exception) {
                null
            }
        } ?: DEFAULT_CATEGORY

        val units = converter.getUnitsForCategory(category)
        val defaultTop = if (category == DEFAULT_CATEGORY) DEFAULT_TOP_UNIT else (units.firstOrNull()?.code ?: DEFAULT_TOP_UNIT)
        val defaultBottom = if (category == DEFAULT_CATEGORY) DEFAULT_BOTTOM_UNIT else (units.getOrNull(1)?.code ?: defaultTop)

        val savedTop = prefs?.getString(KEY_TOP_UNIT, null)
        val savedBottom = prefs?.getString(KEY_BOTTOM_UNIT, null)

        val validTop = if (savedTop != null && units.any { it.code == savedTop }) {
            savedTop
        } else {
            defaultTop
        }

        val validBottom = if (savedBottom != null && units.any { it.code == savedBottom }) {
            savedBottom
        } else {
            defaultBottom
        }

        return SavedConverterState(
            category = category,
            topUnitCode = validTop,
            bottomUnitCode = validBottom
        )
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
