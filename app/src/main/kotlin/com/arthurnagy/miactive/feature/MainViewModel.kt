package com.arthurnagy.miactive.feature

import android.arch.lifecycle.MutableLiveData
import android.bluetooth.BluetoothDevice
import com.arthurnagy.miactive.MiActiveViewModel
import com.arthurnagy.miband.MiBand
import com.arthurnagy.miband.MiBand.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.inject
import timber.log.Timber

class MainViewModel : MiActiveViewModel() {

    val miBand by inject<MiBand>()
    val bandDevice = MutableLiveData<BluetoothDevice>()

    init {
        disposables.add(miBand.dataEvents.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bandEvent ->
                    when (bandEvent) {
                        is Event.BandConnected -> {
                            bandDevice.value = miBand.device
                            setup()
                        }
                        is Event.User -> {

                        }
                        is Event.HeartRateScan -> Timber.d("Heart rate: ${bandEvent.heartRate?.value}")
                    }
                })
    }

    private fun setup() {
        miBand.readHeartRate()
    }

}