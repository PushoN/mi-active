package com.arthurnagy.miband.model

class HeartRate private constructor(val value: Int?) {
    companion object {
        fun create(data: ByteArray): HeartRate =
                HeartRate(if (data.size == 2 && data[0].toInt() == 6) data[1].toInt() and 0xFF else null)
    }
}