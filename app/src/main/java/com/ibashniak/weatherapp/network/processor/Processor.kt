package com.ibashniak.weatherapp.network.processor

import android.location.Location
import android.util.Log
import com.ibashniak.weatherapp.BuildConfig
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit

class Processor {

    companion object {
        //private const val ONE_CALL_METHOD = "onecall"
        //private const val PATH = "/data/2.5"
        private const val CURRENT_WEATHER_METHOD = "weather"
        private const val ENDPOINT = "api.openweathermap.org"
        private const val API_KEY_STRING = "APPID"
        private const val API_KEY = BuildConfig.API_KEY
        private const val TIMEOUT_IN_SECONDS = 2
    }

    val TAG = "Network Processor"
    private val client: OkHttpClient
    val responseChannel: Channel<CurrentWeatherResponse> = Channel()

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        client =
            OkHttpClient.Builder().connectTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)  //  okhttp3.OkHttpClient
                .build()
    }


    fun requestWeather(city: String = "Odessa", country: String = "UA", lang: String = "ru") {
        val TAG = "$TAG requestWeather"
        Log.d("$TAG  ", ENDPOINT)
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(ENDPOINT)
            .addPathSegments("data/2.5")
            .addPathSegment(CURRENT_WEATHER_METHOD)
            .addQueryParameter(API_KEY_STRING, API_KEY)
            .addQueryParameter("q", "$city,$country")
            .addQueryParameter("lang", lang)
            .addQueryParameter("units", "metric")
            .build()

        requestWeather(url)
    }

    fun requestWeather(location: Location, lang: String) {
        val TAG = "$TAG requestWeather"
        Log.d("$TAG  ", ENDPOINT)
        val url = HttpUrl.Builder()
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
        Log.d("$TAG  ", url.toString())
        requestWeather(url)
    }

    private fun requestWeather(url: HttpUrl) {
        val TAG = "$TAG requestWeather"
        GlobalScope.launch(Dispatchers.IO) {
            repeat(5) {
                //https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=

                if (BuildConfig.BUILD_TYPE == "debug") {
                    Log.d(TAG, "body  $url")
                }

                val request: Request = url.let {
                    Request.Builder()
                        .url(url)
                        .build()
                }

                try {
                    val response = client.newCall(request).execute()
                    with(response) {
                        val resp = body?.string()
                        val data = CurrentWeatherResponse.toObject(resp.toString())

                        Log.d(
                            "$TAG ",
                            "body $data \nmessage resp __ $resp \n" +
                                    "isSuccessful $isSuccessful  BuildConfig.BUILD_TYPE ${BuildConfig.BUILD_TYPE} "
                        )
                        if (BuildConfig.BUILD_TYPE == "debug") {
                            Log.d(
                                "$TAG ", "networkResponse ${networkResponse.toString()}"
                            )
                        }

                        if (isSuccessful && networkResponse?.code == 200) {
                            Log.d(TAG, "responseChannel.send")
                            responseChannel.send(data)
                            return@launch
                        }
                    }
                } catch (e: IOException) {
                    Log.d("$TAG request failed", " ${e.localizedMessage} ")
                    sleep(5000)
                }
            }
        }
    }
}