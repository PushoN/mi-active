package com.arthurnagy.miactive.di

import android.bluetooth.BluetoothManager
import android.content.Context
import com.arthurnagy.miband.Bluetooth
import com.arthurnagy.miband.MiBand
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext

val appModule = applicationContext {
    provide { MiBand(get()) }
    provide { Bluetooth(get()) }
    provide { androidApplication().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
}