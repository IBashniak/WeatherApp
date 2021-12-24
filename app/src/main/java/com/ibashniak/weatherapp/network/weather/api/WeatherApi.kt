package com.ibashniak.weatherapp.network.weather.api

import com.ibashniak.weatherapp.BuildConfig
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface WeatherApi {

    @GET("/data/2.5/$CURRENT_WEATHER_METHOD?&l&lang=ru&units=metric")
    suspend fun requestWeather(
        @Query("lat") location: Double,
        @Query("lon") longitude: Double,
        @Query(API_KEY_STRING) appId: String = BuildConfig.API_KEY,
        @Query("lang") lang: String = Locale.getDefault().language,
        @Query("units") units: String = "metric"
    ): CurrentWeatherResponse

    companion object {
        private const val CURRENT_WEATHER_METHOD = "weather"
        private const val API_KEY_STRING = "APPID"
    }
}
