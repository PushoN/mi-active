package com.arthurnagy.miband

import java.util.UUID

@JvmField internal val BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb" //this is common for all BTLE devices. see http://stackoverflow.com/questions/18699251/finding-out-android-bluetooth-le-gatt-profiles

@JvmField internal val UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEE1"))
@JvmField internal val UUID_SERVICE_WEIGHT_SERVICE = "00001530-0000-3512-2118-0009af100700"
@JvmField internal val UUID_SERVICE_HEART_RATE = UUID.fromString(String.format(BASE_UUID, "180D"))

@JvmField internal val UUID_CHARACTERISTIC_DEVICE_INFO = UUID.fromString(String.format(BASE_UUID, "FF01"))

@JvmField internal val UUID_CHARACTERISTIC_DEVICE_NAME = UUID.fromString(String.format(BASE_UUID, "FF02"))

@JvmField internal val UUID_CHARACTERISTIC_NOTIFICATION = UUID.fromString(String.format(BASE_UUID, "FF03"))

@JvmField internal val UUID_CHARACTERISTIC_USER_INFO = UUID.fromString(String.format(BASE_UUID, "FF04"))

@JvmField internal val UUID_CHARACTERISTIC_CONTROL_POINT = UUID.fromString(String.format(BASE_UUID, "FF05"))

@JvmField internal val UUID_CHARACTERISTIC_REALTIME_STEPS = UUID.fromString(String.format(BASE_UUID, "FF06"))

@JvmField internal val UUID_CHARACTERISTIC_ACTIVITY_DATA = UUID.fromString(String.format(BASE_UUID, "FF07"))

@JvmField internal val UUID_CHARACTERISTIC_FIRMWARE_DATA = UUID.fromString(String.format(BASE_UUID, "FF08"))

@JvmField internal val UUID_CHARACTERISTIC_LE_PARAMS = UUID.fromString(String.format(BASE_UUID, "FF09"))

@JvmField internal val UUID_CHARACTERISTIC_DATE_TIME = UUID.fromString(String.format(BASE_UUID, "FF0A"))

@JvmField internal val UUID_CHARACTERISTIC_STATISTICS = UUID.fromString(String.format(BASE_UUID, "FF0B"))

@JvmField internal val UUID_CHARACTERISTIC_BATTERY = UUID.fromString(String.format(BASE_UUID, "FF0C"))

@JvmField internal val UUID_CHARACTERISTIC_TEST = UUID.fromString(String.format(BASE_UUID, "FF0D"))

@JvmField internal val UUID_CHARACTERISTIC_SENSOR_DATA = UUID.fromString(String.format(BASE_UUID, "FF0E"))

@JvmField internal val UUID_CHARACTERISTIC_PAIR = UUID.fromString(String.format(BASE_UUID, "FF0F"))

@JvmField internal val UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT = UUID.fromString(String.format(BASE_UUID, "2A39"))
@JvmField internal val UUID_CHARACTERISTIC_HEART_RATE_MAX = UUID.fromString(String.format(BASE_UUID, "2A8D"))
@JvmField internal val UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT = UUID.fromString(String.format(BASE_UUID, "2A37"))

@JvmField internal val UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
