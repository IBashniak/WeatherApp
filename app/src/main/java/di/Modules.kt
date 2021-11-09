package di

import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import com.ibashniak.weatherapp.network.processor.IconDownloader
import org.koin.dsl.module

val iconDownloaderModule = module {
    single { IconDownloader() }
}

val BeaufortScaleModule = module {
    single { BeaufortScaleTable() }
}