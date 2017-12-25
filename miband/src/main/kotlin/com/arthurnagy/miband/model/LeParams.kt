package com.arthurnagy.miband.model

class LeParams
private constructor(
        val connIntMin: Int,
        val connIntMax: Int,
        val connInt: Int,
        val latency: Int,
        val timeout: Int,
        val advInt: Int) {
    companion object {
        fun create(data: ByteArray) = LeParams(
                connIntMin = ((0xffff and (0xff and data[0].toInt() or (0xff and data[1].toInt() shl 8))) * 1.25).toInt(),
                connIntMax = ((0xffff and (0xff and data[2].toInt() or (0xff and data[3].toInt() shl 8))) * 1.25).toInt(),
                connInt = 0xffff and (0xff and data[8].toInt() or (0xff and data[9].toInt() shl 8)),
                latency = 0xffff and (0xff and data[4].toInt() or (0xff and data[5].toInt() shl 8)),
                timeout = (0xffff and (0xff and data[6].toInt() or (0xff and data[7].toInt() shl 8))) * 10,
                advInt = ((0xffff and (0xff and data[10].toInt() or (0xff and data[11].toInt() shl 8))) * 0.625).toInt()
        )
    }
}