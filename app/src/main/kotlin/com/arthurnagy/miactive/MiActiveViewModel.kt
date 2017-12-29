package com.arthurnagy.miactive

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import org.koin.standalone.KoinComponent

open class MiActiveViewModel : ViewModel(), KoinComponent {

    protected val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}