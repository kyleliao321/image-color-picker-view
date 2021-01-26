package com.mingwei.imagecolorpickerview.pooling

class PoolingFunction private constructor() {

    companion object {
        val MAX_POOLING = MaxPooling()
        val AVERAGE_POOLING = AveragePooling()
        val BRIGHTEST_POOLING = BrightestPooling()
        val DARKEST_POOLING = DarkestPooling()
    }

}