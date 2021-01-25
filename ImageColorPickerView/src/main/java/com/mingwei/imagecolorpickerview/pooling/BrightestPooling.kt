package com.mingwei.imagecolorpickerview.pooling

import android.graphics.Color
import androidx.annotation.ColorInt

object BrightestPooling : PoolingFunction {

    override fun exec(pixels: IntArray): Int {
        var brightest = Double.MIN_VALUE
        var brightestIndex = -1

        for ((index, pixel) in pixels.withIndex()) {
            brightest = brightest.coerceAtLeast(luminance(pixel))
            brightestIndex = index
        }

        return brightestIndex.takeIf { it >= 0 }?.let {
            pixels[it]
        } ?: run {
            pixels.first()
        }
    }

    private fun luminance(@ColorInt color: Int): Double {
        return Color.red(color) * 0.2126 + Color.green(color) * 0.7152 + Color.blue(color) * 0.0722
    }
}