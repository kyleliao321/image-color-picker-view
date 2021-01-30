package com.colorgraph.utils.imagecolorpickerview.pooling

import android.graphics.Color

class AveragePooling internal constructor() : IPoolingFunction {

    override fun exec(pixels: IntArray): Int {
        val pixelNum = pixels.size

        var rSum = 0f
        var bSum = 0f
        var gSum = 0f
        var aSum = 0f

        for (pixel in pixels) {
            rSum += Color.red(pixel)
            bSum += Color.blue(pixel)
            gSum += Color.green(pixel)
            aSum += Color.alpha(pixel)
        }

        val rAvg = rSum / pixelNum
        val gAvg = gSum / pixelNum
        val bAvg = bSum / pixelNum
        val aAvg = aSum / pixelNum

        return Color.argb(aAvg.toInt(), rAvg.toInt(), gAvg.toInt(), bAvg.toInt())
    }
}