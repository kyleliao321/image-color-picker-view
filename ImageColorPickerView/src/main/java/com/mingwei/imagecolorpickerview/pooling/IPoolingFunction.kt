package com.mingwei.imagecolorpickerview.pooling

/**
 * Pooling function will be used to get color from given pixel array.
 */
interface IPoolingFunction {

    /**
     * @param pixels Pixels that contains colors in ARGB_8888 format.
     * @return Color in ARGB_8888 format.
     */
    fun exec(pixels: IntArray): Int
}