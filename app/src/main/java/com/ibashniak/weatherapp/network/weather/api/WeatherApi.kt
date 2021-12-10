package com.ibashniak.weatherapp.network.weather.api

import android.location.Location
import com.ibashniak.weatherapp.BuildConfig
import okhttp3.HttpUrl
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface WeatherApi {

    @GET
    suspend fun requestWeather(@Url fileUrl: String): Response<ResponseBody>

    companion object {
        private const val CURRENT_WEATHER_METHOD = "weather"
        const val ENDPOINT = "api.openweathermap.org"
        private const val API_KEY_STRING = "APPID"
        private const val API_KEY = BuildConfig.API_KEY
        private const val TIMEOUT_IN_SECONDS = 2

        fun weatherUrl(lang: String, location: Location) =
            HttpUrl.Builder()
                .scheme("https")
                .host(ENDPOINT)
                .addPathSegments("data/2.5")
                .addPathSegment(CURRENT_WEATHER_METHOD)
                .addQueryParameter(API_KEY_STRING, API_KEY)
                .addQueryParameter("lat", "${location.latitude}")
                .addQueryParameter("lon", "${location.longitude}")
                .addQueryParameter("lang", lang)
                .addQueryParameter("units", "metric")
                .build()
    }
}
