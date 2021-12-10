package com.ibashniak.weatherapp

import android.app.Application
import android.util.Log
import com.ibashniak.weatherapp.di.appModules
import org.koin.core.context.startKoin

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("WeatherApp", "onCreate: ")
        startKoin {
            modules(appModules)
        }
    }
}
