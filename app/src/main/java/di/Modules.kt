package di

import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import org.koin.dsl.module

val BeaufortScaleModule = module {
    single { BeaufortScaleTable() }
}