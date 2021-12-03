package di

import com.ibashniak.weatherapp.network.icon.api.DownloadClient
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import org.koin.dsl.module

val appModules = module {
    single { BeaufortScaleTable() }
    single { DownloadClient() }
}