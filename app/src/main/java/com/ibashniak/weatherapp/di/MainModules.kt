package com.ibashniak.weatherapp.di

import com.ibashniak.weatherapp.data.BeaufortScaleTable
import com.ibashniak.weatherapp.data.Repository
import org.koin.dsl.module

val mainModule = module {
    single { BeaufortScaleTable() }
    single { Repository(get(), get(), get(), get()) }
}
