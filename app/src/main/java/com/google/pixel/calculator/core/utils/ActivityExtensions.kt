package com.google.pixel.calculator.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Traverses context wrappers to find the host Activity.
 */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
