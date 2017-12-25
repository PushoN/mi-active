package com.arthurnagy.miband.model

import java.util.Calendar
import java.util.Date

/**
 * MiBand's battery information, contains the current battery level, charging cycle number, current status and last day when the band was charged
 */
class BatteryInfo
private constructor(val level: Int, val cycles: Int, val status: Status, val lastChargedDate: Date) {

    enum class Status {
        UNKNOWN, LOW, FULL, CHARGING, NOT_CHARGING;

        companion object {
            fun fromByte(status: Byte): Status = when (status) {
                1.toByte() -> LOW
                2.toByte() -> CHARGING
                3.toByte() -> FULL
                4.toByte() -> NOT_CHARGING
                else -> UNKNOWN
            }

        }
    }

    companion object {

        /**
         * Creates an instance of the battery info from byte data

         * @param data Byte data
         * *
         * @return Battery info instance
         */
        fun create(data: ByteArray): BatteryInfo {
            val level = data[0].toInt()
            val status = Status.fromByte(data[9])
            val cycles = 0xffff and (0xff and data[7].toInt() or (0xff and data[8].toInt() shl 8))

            val lastChargeDay = Calendar.getInstance()
            lastChargeDay.set(Calendar.YEAR, data[1] + 2000)
            lastChargeDay.set(Calendar.MONTH, data[2].toInt())
            lastChargeDay.set(Calendar.DATE, data[3].toInt())

            lastChargeDay.set(Calendar.HOUR_OF_DAY, data[4].toInt())
            lastChargeDay.set(Calendar.MINUTE, data[5].toInt())
            lastChargeDay.set(Calendar.SECOND, data[6].toInt())

            return BatteryInfo(level, cycles, status, lastChargeDay.time)
        }
    }

}