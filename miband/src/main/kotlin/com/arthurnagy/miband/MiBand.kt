package com.arthurnagy.miband

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.arthurnagy.miband.Bluetooth.Event.CharacteristicChanged
import com.arthurnagy.miband.Bluetooth.Event.Connected
import com.arthurnagy.miband.Bluetooth.Event.Disconnected
import com.arthurnagy.miband.Bluetooth.Event.Failure
import com.arthurnagy.miband.Bluetooth.Event.Scan
import com.arthurnagy.miband.Bluetooth.Event.Success
import com.arthurnagy.miband.Bluetooth.Event.SuccessRssi
import com.arthurnagy.miband.MiBand.Event.Error
import com.arthurnagy.miband.model.BatteryInfo
import com.arthurnagy.miband.model.HeartRate
import com.arthurnagy.miband.model.UserInfo
import io.reactivex.Flowable
import timber.log.Timber

class MiBand constructor(val bluetooth: Bluetooth) {

    val dataEvents: Flowable<Event>
    val device: BluetoothDevice?
        get() = bluetooth.getConnectedDevice()

    init {
        dataEvents = bluetooth.bluetoothEvents.map { bluetoothEvent ->
            when (bluetoothEvent) {
                is CharacteristicChanged -> {
                    Timber.d("CharacteristicChanged: $bluetoothEvent")
                    if (bluetoothEvent.characteristicId == UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT || bluetoothEvent.characteristicId == UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT) {
                        Event.HeartRateScan(HeartRate.create(bluetoothEvent.data))
                    } else if (bluetoothEvent.characteristicId == UUID_CHARACTERISTIC_PAIR) {
                        Timber.d("PAIR: ${bluetoothEvent.data}")
                    } else if (bluetoothEvent.characteristicId == UUID_CHARACTERISTIC_USER_INFO) {
                        Event.User(UserInfo.create(bluetoothEvent.data))
                    }
                    // TODO: handle other characteristics
                    Error
                }
                is Disconnected -> {
                    Timber.d("Disconnected: $bluetoothEvent")
                    Event.BandDisconnected
                }
                is Connected -> {
                    Timber.d("Connected: $bluetoothEvent")
                    Event.BandConnected
                }
                is Scan -> {
                    Timber.d("Scan: $bluetoothEvent")
                    if (bluetoothEvent.scanResult.device.name.contains("mi")) {
                        Event.BandScanned(bluetoothEvent.scanResult.device)
                    }
                    // TODO: handle
                    Error
                }
                is Success -> {
                    Timber.d("Success: $bluetoothEvent")
                    val serviceId = bluetoothEvent.data.service.uuid
                    val characteristicId = bluetoothEvent.data.uuid
                    if (serviceId == UUID_SERVICE_MIBAND2_SERVICE) {
                        if (characteristicId == UUID_CHARACTERISTIC_BATTERY) Event.Battery(BatteryInfo.create(bluetoothEvent.data.value))
                        if (characteristicId == UUID_CHARACTERISTIC_USER_INFO) Event.User(UserInfo.create(bluetoothEvent.data.value))
                        if (characteristicId == UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT || characteristicId == UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT)
                            Event.HeartRateScan(HeartRate.create(bluetoothEvent.data.value))
                    } else if (serviceId == UUID_SERVICE_HEART_RATE) {
                        if (characteristicId == UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT || characteristicId == UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT)
                            Event.HeartRateScan(HeartRate.create(bluetoothEvent.data.value))
                    }
                    // TODO: handle
                    Error
                }
            // TODO: handle
                is SuccessRssi -> {
                    Timber.d("SuccessRssi: $bluetoothEvent")
                    Error
                }
            // TODO: handle
                is Failure -> {
                    Timber.d("Failure: $bluetoothEvent")
                    Error
                }
                is com.arthurnagy.miband.Bluetooth.Event.Error -> {
                    Timber.d("Error: $bluetoothEvent")
                    Error
                }
            }
        }
    }

    fun connect(context: Context, miDevice: BluetoothDevice) {
        Timber.d("connect ${miDevice.name} on address: ${miDevice.address}")
        bluetooth.connect(context, miDevice)
    }

    fun pair() {
        Timber.d("pair")
        bluetooth.writeCharacteristic(UUID_SERVICE_MIBAND2_SERVICE, UUID_CHARACTERISTIC_PAIR, PAIR)
    }

    fun startScanning() = bluetooth.scanDevices()

    fun stopScanning() = bluetooth.stopDeviceScan()

    fun readBatteryInfo() {
        bluetooth.readCharacteristic(UUID_SERVICE_MIBAND2_SERVICE, UUID_CHARACTERISTIC_BATTERY)
    }

    fun readUserInfo() {
        Timber.d("readUserInfo")
        bluetooth.readCharacteristic(UUID_SERVICE_MIBAND2_SERVICE, UUID_CHARACTERISTIC_USER_INFO)
    }

    fun readHeartRate() {
        Timber.d("readHeartRate")
        bluetooth.writeCharacteristic(UUID_SERVICE_HEART_RATE, UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT, START_HEART_RATE_SCAN)
    }

    fun subscribeToHeartRate() {
        Timber.d("subscribeToHeartRate")
        bluetooth.subscribeToServiceCharacteristic(UUID_SERVICE_HEART_RATE, UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT)
    }

    fun unsubscribeFromHeartRate() {
        Timber.d("unsubscribeFromHeartRate")
        bluetooth.unsubscribeFromServiceCharacteristic(UUID_SERVICE_HEART_RATE, UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT)
    }

    sealed class Event {
        data class User(val userInfo: UserInfo) : Event()
        data class Battery(val batteryInfo: BatteryInfo) : Event()
        data class HeartRateScan(val heartRate: HeartRate?) : Event()
        data class BandScanned(val device: BluetoothDevice) : Event()
        object BandConnected : Event()
        object BandDisconnected : Event()
        object Error : Event()
    }

}