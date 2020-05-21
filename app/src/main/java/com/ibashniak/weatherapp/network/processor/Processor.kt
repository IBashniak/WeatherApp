package com.ibashniak.weatherapp.network.processor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.R
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ImplicitReflectionSerializer
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class Processor(val context: Context) {
    private val wind: Array<String>
    companion object {

        private const val ONE_CALL_METHOD = "onecall"
        private const val CURRENT_WEATHER_METHOD = "weather"
        private const val ENDPOINT = "api.openweathermap.org"
        private const val PATH = "/data/2.5"
        private const val API_KEY_STRING = "APPID"
        private const val API_KEY = "0fd732b2980dcc11b580078dfee4aea9"
        private const val TIMEOUT_IN_SECONDS = 2
    }

    private val client: OkHttpClient
    val responseChannel = Channel<CurrentWeatherResponse>()

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        wind = context.resources.getStringArray(R.array.wind_direction)
        client =
            OkHttpClient.Builder().connectTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)  //  okhttp3.OkHttpClient
                .build()

    }

    @SuppressLint("ResourceType", "SetTextI18n")
    @ImplicitReflectionSerializer
    fun requestWeather(city: String = "Odessa", country: String = "UA", lang: String = "ru") {
        val TAG = "requestWeather"
        Log.d("$TAG '0' ", ENDPOINT)
        GlobalScope.launch(Dispatchers.IO) {

            //https://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=0fd732b2980dcc11b580078dfee4aea9
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

            Log.d("$TAG '0' ", "body  ${url}")
            val request: Request = url.let {
                Request.Builder()
                    .url(url)
                    .build()
            }

            val response = client.newCall(request).execute()
            val resp = response.body?.string()
            val data = CurrentWeatherResponse.toObject(resp.toString())


            IconDownloader.getIcon(
                data.weather[0],
                MainActivity.activityMainBinding!!.ivWeatherConditionIconPrimary,
                context
            )
            withContext(Dispatchers.Main)

            {
                val index = (data.wind.deg / 22.5).roundToInt()
                val binding = MainActivity.activityMainBinding!!

                binding.tvWind.text = " ${data.wind.speed}\n ${wind[index]} "

                binding.etTemperature.text = data.main.temp.toString() + "°C"

                binding.ivWindDirection.rotation = data.wind.deg.toFloat()

            }

            Log.d("$TAG '0' ", "body ${data.toString()}  ")
            Log.d(TAG, "message resp __ ${resp}  ")
            Log.d(TAG, "networkResponse ${response.networkResponse.toString()}  ")
            Log.d(TAG, "isSuccessful ${response.isSuccessful}  ")


            responseChannel.send(data)
        }
    }

}