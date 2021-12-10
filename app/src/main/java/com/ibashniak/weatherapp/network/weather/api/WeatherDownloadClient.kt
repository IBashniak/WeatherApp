package com.ibashniak.weatherapp.network.weather.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class WeatherDownloadClient {
    fun client(): WeatherApi {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(WeatherApi.ENDPOINT)
            .addPathSegments("data/2.5/")
            .build()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.HEADERS)
        }

        val httpClient =
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor) //  okhttp3.OkHttpClient
                .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .build()
            .create(WeatherApi::class.java)
    }

    companion object {
        const val TIMEOUT_IN_SECONDS = 2
    }
}
