package com.arthurnagy.miactive

import android.app.Application
import com.arthurnagy.miactive.di.appModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class MiActiveApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

}