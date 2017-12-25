package com.arthurnagy.miband

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.arthurnagy.miband.Bluetooth.Event.Failure
import com.arthurnagy.miband.Bluetooth.Event.Success
import com.arthurnagy.miband.MiBand.Event.Error
import com.arthurnagy.miband.model.BatteryInfo
import com.arthurnagy.miband.model.HeartRate
import com.arthurnagy.miband.model.UserInfo
import io.reactivex.Flowable

class MiBand(private val bluetooth: Bluetooth) {

    private val dataEvents: Flowable<Event>
//    private val miBandDevice: BluetoothDevice?
//        get() = bluetooth.getConnectedDevice()

    init {
        dataEvents = bluetooth.bluetoothEvents.map { bluetoothEvent ->
            when (bluetoothEvent) {
                is Success -> {
                    val serviceId = bluetoothEvent.data.service.uuid
                    val characteristicId = bluetoothEvent.data.uuid
                    if (serviceId == UUID_SERVICE_MI_BAND) {
                        if (characteristicId == UUID_CHAR_BATTERY) Event.Battery(BatteryInfo.create(bluetoothEvent.data.value))
                        if (characteristicId == UUID_CHAR_USER_INFO) Event.User(UserInfo.create(bluetoothEvent.data.value))
                        if (characteristicId == UUID_CHAR_HEARTRATE) Event.HeartRateScan(HeartRate.create(bluetoothEvent.data.value))
                    }
                }
                is com.arthurnagy.miband.Bluetooth.Event.CharacteristicChanged -> {
                    if (bluetoothEvent.characteristicId == UUID_CHAR_HEARTRATE || bluetoothEvent.characteristicId == UUID_CHAR_NOTIFICATION_HEARTRATE) {
                        Event.HeartRateScan(HeartRate.create(bluetoothEvent.data))
                    }
                }
                is Failure -> Error
                is com.arthurnagy.miband.Bluetooth.Event.Error -> Error
            }
            Event.Error
        }
    }

    fun connect(context: Context, miDevice: BluetoothDevice) {
        bluetooth.connect(context, miDevice)
    }

    fun readBatteryInfo() {
        bluetooth.readCharacteristic(UUID_SERVICE_MI_BAND, UUID_CHAR_BATTERY)
    }

    fun readUserInfo() {
        bluetooth.readCharacteristic(UUID_SERVICE_MI_BAND, UUID_CHAR_USER_INFO)
    }

    fun readHeartRate() {
        bluetooth.writeCharacteristic(UUID_SERVICE_HEARTRATE, UUID_CHAR_HEARTRATE, START_HEART_RATE_SCAN)
    }

    fun subscribeToHeartRate() {
        bluetooth.subscribeToServiceCharacteristic(UUID_SERVICE_HEARTRATE, UUID_CHAR_NOTIFICATION_HEARTRATE)
    }

    fun unsubscribeFromHeartRate() {
        bluetooth.unsubscribeFromServiceCharacteristic(UUID_SERVICE_HEARTRATE, UUID_CHAR_NOTIFICATION_HEARTRATE)
    }

    sealed class Event {
        data class User(val userInfo: UserInfo) : Event()
        data class Battery(val batteryInfo: BatteryInfo) : Event()
        data class HeartRateScan(val heartRate: HeartRate) : Event()
        object Error : Event()
    }

}