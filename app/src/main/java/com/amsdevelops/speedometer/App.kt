package com.amsdevelops.speedometer

import android.app.Application
import com.amsdevelops.speedometer.di.AppComponent
import com.amsdevelops.speedometer.di.DaggerAppComponent
import com.amsdevelops.speedometer.di.modules.AppModule
import timber.log.Timber

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this

        initTimber()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule())
            .build()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}