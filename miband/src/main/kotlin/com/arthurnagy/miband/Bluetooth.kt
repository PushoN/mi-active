package com.arthurnagy.miband

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.arthurnagy.miband.Bluetooth.Event.CharacteristicChanged
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.UUID

class Bluetooth {

    val bluetoothEvents: FlowableProcessor<Event> = PublishProcessor.create()
    private var bluetoothGatt: BluetoothGatt? = null
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else {
                gatt.close()
                bluetoothEvents.onNext(Event.Disconnected)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt
                bluetoothEvents.onNext(Event.Connected)
            } else {
                bluetoothEvents.onNext(Event.Error(ErrorType.CONNECTION_FAILED, "onServicesDiscovered fail: " + status.toString()))
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bluetoothEvents.onNext(Event.Success(characteristic))
            } else {
                val serviceId = characteristic.service.uuid
                val characteristicId = characteristic.uuid
                bluetoothEvents.onNext(Event.Failure(serviceId, characteristicId, "onCharacteristicRead fail"))
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bluetoothEvents.onNext(Event.Success(characteristic))
            } else {
                val serviceId = characteristic.service.uuid
                val characteristicId = characteristic.uuid
                bluetoothEvents.onNext(Event.Failure(serviceId, characteristicId, "onCharacteristicWrite fail"))
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            bluetoothEvents.onNext(CharacteristicChanged(characteristic.uuid, characteristic.value))
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bluetoothEvents.onNext(Event.SuccessRssi(rssi))
            } else {
                bluetoothEvents.onNext(Event.Error(ErrorType.READ_RSSI_FAILED, "onCharacteristicRead fail: " + status.toString()))
            }
        }
    }

    private fun errorCharacteristicNotExisting(characteristicId: UUID) = "BluetoothGattCharacteristic $characteristicId does not exist"

    private fun errorServiceNotExisting(serviceUUID: UUID) = "BluetoothGattService $serviceUUID does not exist"

    /**
     * Connects to the Bluetooth device
     * @param context Context
     * *
     * @param device  Device to connect
     */
    fun connect(context: Context, device: BluetoothDevice) {
        device.connectGatt(context, false, bluetoothGattCallback)
    }

    /**
     * Gets remote connected device

     * @return Connected device or null
     */
    fun getConnectedDevice(): BluetoothDevice? {
        return bluetoothGatt?.device
    }

    /**
     * Writes data to the service
     * @param serviceUUID      Service UUID
     * *
     * @param characteristicId Characteristic UUID
     * *
     * @param value            Value to write
     */
    fun writeCharacteristic(serviceUUID: UUID, characteristicId: UUID, value: ByteArray) {
        checkConnectionState()

        val service = bluetoothGatt?.getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicId)
            if (characteristic != null) {
                characteristic.value = value
                val writeResult = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
                if (!writeResult) {
                    bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, "BluetoothGatt write operation failed"))
                }
            } else {
                bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorCharacteristicNotExisting(characteristicId)))
            }
        } else {
            bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorServiceNotExisting(serviceUUID)))
        }
    }

    /**
     * Reads data from the service
     * @param serviceUUID      Service UUID
     * *
     * @param characteristicId Characteristic UUID
     */
    fun readCharacteristic(serviceUUID: UUID, characteristicId: UUID) {
        checkConnectionState()

        val service = bluetoothGatt?.getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicId)
            if (characteristic != null) {
                val readResult = bluetoothGatt?.readCharacteristic(characteristic) ?: false
                if (readResult) {
                    bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, "BluetoothGatt read operation failed"))
                }
            } else {
                bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorCharacteristicNotExisting(characteristicId)))
            }
        } else {
            bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorServiceNotExisting(serviceUUID)))
        }
    }

    /**
     * Reads Received Signal Strength Indication (RSSI)
     */
    fun readRssi() {
        checkConnectionState()
        val readResult = bluetoothGatt?.readRemoteRssi() ?: false
        if (!readResult) {
            bluetoothEvents.onNext(Event.Error(ErrorType.READ_RSSI_FAILED, "Request RSSI value failed"))
        }
    }

    /**
     * Checks connection state.
     * @throws IllegalStateException if device is not connected
     */
    @Throws(IllegalStateException::class) private fun checkConnectionState() {
        if (bluetoothGatt == null) {
            throw IllegalStateException("Device is not connected")
        }
    }

    /**
     * Sets notification listener for specific service and specific characteristic

     * @param serviceUUID      Service UUID
     * *
     * @param characteristicId Characteristic UUID
     * *
     * @param listener         New listener
     */
    fun subscribeToServiceCharacteristic(serviceUUID: UUID, characteristicId: UUID) {
        checkConnectionState()
        val service = bluetoothGatt?.getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicId)
            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION)
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
            } else {
                bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorCharacteristicNotExisting(characteristicId)))
            }
        } else {
            bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorServiceNotExisting(serviceUUID)))
        }
    }

    /**
     * Removes notification listener for the service and characteristic

     * @param serviceUUID      Service UUID
     * *
     * @param characteristicId Characteristic UUID
     */
    fun unsubscribeFromServiceCharacteristic(serviceUUID: UUID, characteristicId: UUID) {
        checkConnectionState()
        val service = bluetoothGatt?.getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicId)
            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, false)
                val descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION)
                descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
            } else {
                bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorCharacteristicNotExisting(characteristicId)))
            }
        } else {
            bluetoothEvents.onNext(Event.Failure(serviceUUID, characteristicId, errorServiceNotExisting(serviceUUID)))
        }
    }

    enum class ErrorType {
        CONNECTION_FAILED, READ_RSSI_FAILED
    }

    sealed class Event {
        data class CharacteristicChanged(val characteristicId: UUID, val data: ByteArray) : Event()
        object Disconnected : Event()
        object Connected : Event()
        data class Success(val data: BluetoothGattCharacteristic) : Event()
        data class SuccessRssi(val data: Int) : Event()
        data class Failure(val serviceUUID: UUID, val characteristicId: UUID, val msg: String) : Event()
        data class Error(val errorType: ErrorType, val msg: String) : Event()
    }

}