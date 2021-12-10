package com.ibashniak.weatherapp

import android.app.Application
import android.util.Log
import com.ibashniak.weatherapp.di.appModules
import com.ibashniak.weatherapp.log.LogTree
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupLogging()
        startKoin {
            androidContext(this@WeatherApp)
            modules(appModules)
        }
    }

    private fun setupLogging() {
        val logLevel =
            if (BuildConfig.DEBUG) Log.VERBOSE else Log.WARN

        Timber.plant(LogTree(logLevel))
    }
}
