package com.colorgraph.utils.imagecolorpickerview.pooling

class PoolingFunction private constructor() {

    companion object {
        val AVERAGE_POOLING = AveragePooling()
        val BRIGHTEST_POOLING = BrightestPooling()
        val DARKEST_POOLING = DarkestPooling()
    }

}