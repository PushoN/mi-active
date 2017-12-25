package com.arthurnagy.miband.model

import java.nio.ByteBuffer
import java.nio.charset.Charset

class UserInfo
private constructor(
        val id: Int,
        val gender: Byte,
        val age: Byte,
        val height: Byte,
        val weight: Byte,
        val type: Byte,
        val alias: String) {

    fun toBytes(bluetoothAddress: String): ByteArray {
        val aliasBytes = this.alias.toByteArray(charset("UTF-8"))
        val byteBuffer = ByteBuffer.allocate(20)
        byteBuffer.put((id and 0xff).toByte())
        byteBuffer.put((id shr 8 and 0xff).toByte())
        byteBuffer.put((id shr 16 and 0xff).toByte())
        byteBuffer.put((id shr 24 and 0xff).toByte())
        byteBuffer.put(gender)
        byteBuffer.put(age)
        byteBuffer.put(height)
        byteBuffer.put(weight)
        byteBuffer.put(type)
        byteBuffer.put(4.toByte())
        byteBuffer.put(0.toByte())

        if (aliasBytes.size <= 8) {
            byteBuffer.put(aliasBytes)
            byteBuffer.put(ByteArray(8 - aliasBytes.size))
        } else {
            byteBuffer.put(aliasBytes, 0, 8)
        }

        val crcSequence = ByteArray(19)
        for (i in crcSequence.indices) crcSequence[i] = byteBuffer.array()[i]

        val crcb = (getCRC8(crcSequence) xor Integer.parseInt(bluetoothAddress.substring(bluetoothAddress.length - 2), 16) and 0xff).toByte()
        byteBuffer.put(crcb)
        return byteBuffer.array()
    }

    private fun getCRC8(seq: ByteArray): Int {
        var len = seq.size
        var i = 0
        var crc: Byte = 0x00

        while (len-- > 0) {
            var extract = seq[i++]
            for (tempI in 8 downTo 1) {
                var sum = (crc.toInt() and 0xff xor (extract.toInt() and 0xff)).toByte()
                sum = (sum.toInt() and 0xff and 0x01).toByte()
                crc = (crc.toInt() and 0xff).ushr(1).toByte()
                if (sum.toInt() != 0) {
                    crc = (crc.toInt() and 0xff xor 0x8c).toByte()
                }
                extract = (extract.toInt() and 0xff).ushr(1).toByte()
            }
        }
        return (crc.toInt() and 0xff)
    }

    companion object {
        fun create(data: ByteArray) = UserInfo(
                id = data[3].toInt() shl 24 or (data[2].toInt() and 0xFF shl 16) or (data[1].toInt() and 0xFF shl 8) or (data[0].toInt() and 0xFF),
                gender = data[4],
                age = data[5],
                height = data[6],
                weight = data[7],
                type = data[8],
                alias = String(data, 9, 8, Charset.forName("UTF-8"))
        )
    }

}