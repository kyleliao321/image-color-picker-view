package com.mingwei.imagecolorpickerview.pooling

class MaxPooling internal constructor() : IPoolingFunction {

    override fun exec(pixels: IntArray): Int {
        var max = Int.MIN_VALUE

        for (pixel in pixels) {
            max = pixel.coerceAtLeast(max)
        }

        return max
    }
}