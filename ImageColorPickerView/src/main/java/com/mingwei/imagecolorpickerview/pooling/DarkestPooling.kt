package com.mingwei.imagecolorpickerview.pooling

import android.graphics.Color
import androidx.annotation.ColorInt

class DarkestPooling internal constructor() : IPoolingFunction {

    override fun exec(pixels: IntArray): Int {
        var darkest = Double.MAX_VALUE
        var darkestIndex = -1

        for ((index, pixel) in pixels.withIndex()) {
            darkest = darkest.coerceAtMost(luminance(pixel))
            darkestIndex = index
        }

        return darkestIndex.takeIf { it >= 0 }?.let {
            pixels[it]
        } ?: run {
            pixels.first()
        }
    }

    private fun luminance(@ColorInt color: Int): Double {
        return Color.red(color) * 0.2126 + Color.green(color) * 0.7152 + Color.blue(color) * 0.0722
    }
}