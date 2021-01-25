package com.mingwei.imagecolorpickerview.pooling

object MaxPooling : PoolingFunction {

    override fun exec(pixels: IntArray): Int {
        var max = Int.MIN_VALUE

        for (pixel in pixels) {
            max = pixel.coerceAtLeast(max)
        }

        return max
    }
}