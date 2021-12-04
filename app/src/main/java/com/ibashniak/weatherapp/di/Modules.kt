package com.ibashniak.weatherapp.di

import com.ibashniak.weatherapp.data.BeaufortScaleTable
import com.ibashniak.weatherapp.network.icon.api.IconDownloadClient
import org.koin.dsl.module

val appModules = module {
    single { BeaufortScaleTable() }
    single { IconDownloadClient() }
}