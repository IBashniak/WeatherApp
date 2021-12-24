package com.ibashniak.weatherapp.di

import com.ibashniak.weatherapp.network.icon.api.IconApi
import com.ibashniak.weatherapp.network.weather.api.WeatherApi
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS

private const val TIMEOUT_IN_SECONDS = 2
val netModule = module {
    single { HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.HEADERS) } }
    single {
        OkHttpClient.Builder().connectTimeout(
            TIMEOUT_IN_SECONDS.toLong(),
            SECONDS
        ).writeTimeout(TIMEOUT_IN_SECONDS.toLong(), SECONDS)
            .readTimeout(TIMEOUT_IN_SECONDS.toLong(), SECONDS)
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        val ENDPOINT = "api.openweathermap.org"

        val url = HttpUrl.Builder()
            .scheme("https")
            .host(ENDPOINT)
            .addPathSegments("data/2.5/")
            .build()

        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
            .create(WeatherApi::class.java)
    }

    single {
        val ENDPOINT = "openweathermap.org"
        val CURRENT_WEATHER_METHOD = "img/wn/"
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(ENDPOINT)
            .addPathSegments(CURRENT_WEATHER_METHOD)
            .build()

        Retrofit.Builder()
            .baseUrl(url)
            .client(get())
            .build()
            .create(IconApi::class.java)
    }
}
