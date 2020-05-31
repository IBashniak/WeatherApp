package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import com.ibashniak.weatherapp.network.processor.IconDownloader
import com.ibashniak.weatherapp.network.processor.Processor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private var networkProcessor: Processor? = null
    private lateinit var Beaufort: Array<String>
    private lateinit var humidity: String
    private lateinit var windSpeed: String
    private lateinit var wind: Array<String>

    private lateinit var activityMainBinding: ActivityMainBinding

    @ImplicitReflectionSerializer
    @ExperimentalCoroutinesApi
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        Beaufort = resources.getStringArray(R.array.Beaufort)
        humidity = resources.getString(R.string.humidity)
        windSpeed = resources.getString(R.string.speed)
        wind = resources.getStringArray(R.array.wind_direction)

        with(activityMainBinding) {
            btnRequest.visibility = View.GONE
        }

    }

    @ExperimentalCoroutinesApi
    @ImplicitReflectionSerializer
    override fun onResume() {
        super.onResume()
        Log.d("onResume", "onResume")
        super.onStart()
        networkProcessor = Processor(this)
        activityMainBinding.progressBar.visibility = View.VISIBLE
        networkProcessor?.requestWeather(lang = Locale.getDefault().language)
        GlobalScope.launch(Dispatchers.Main) {

            while (networkProcessor != null)
                for (msg in networkProcessor!!.responseChannel) {
                    responseHandler(msg)
                }
        }
    }

    override fun onStop() {
        Log.d("onStop", "onStop")
        super.onStop()
        networkProcessor?.responseChannel?.close()
        networkProcessor = null
    }

    @SuppressLint("SetTextI18n")
    private fun responseHandler(weather: CurrentWeatherResponse) {
        Log.d("responseHandler", "responseHandler")
//          Debug info

        activityMainBinding.tvResponse.text = "$weather "

        if (weather.weather.isNotEmpty()) {
            activityMainBinding.tvDescription.text =
                "${weather.name}, ${weather.timeStamp} ${weather.weather[0].description}"
            IconDownloader.getIcon(
                weather.weather[0],
                activityMainBinding.ivWeatherConditionIconPrimary,
                this
            )
            if (weather.weather.size > 1) {
                IconDownloader.getIcon(
                    weather.weather[1],
                    activityMainBinding.ivWeatherConditionIconSecondary,
                    this
                )
            } else {
                activityMainBinding.ivWeatherConditionIconSecondary.visibility = View.GONE
            }

        }

        val index = (weather.wind.deg / 22.5).roundToInt()

        with(activityMainBinding)
        {
            progressBar.visibility = View.GONE
            tvWind.text = " ${weather.wind.speed.toInt()} $windSpeed\n ${wind[index]} "
            etTemperature.text =
                "%.1f".format(weather.main.temp) + "°C" + " \n${"%.1f".format(weather.main.feels_like)} °C"
            ivWindDirection.rotation = weather.wind.deg.toFloat()
            tvTempRange.text = "${weather.main.temp_min} ${weather.main.temp_max}"
            tvHumidity.text = "$humidity ${weather.main.humidity}%"
            tvWindScale.text =
                " ${BeaufortScaleTable.getBeaufortString(weather.wind.speed, Beaufort)}"
        }
    }

}
