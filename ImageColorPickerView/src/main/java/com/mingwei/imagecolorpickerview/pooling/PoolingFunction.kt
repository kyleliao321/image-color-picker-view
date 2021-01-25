package com.mingwei.imagecolorpickerview.pooling

interface PoolingFunction {
    fun exec(pixels: IntArray): Int
}