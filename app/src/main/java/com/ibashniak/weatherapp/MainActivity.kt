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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

//    private var _activityMainBinding: ActivityMainBinding

    companion object {
//        private var activityMainBinding: ActivityMainBinding? = null
    }


    private lateinit var networkProcessor: Processor
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
//            btnRequest.setOnClickListener {
//                Log.d("setOnClickListener", " start")
//                networkProcessor.requestWeather()
//            }
            btnRequest.visibility = View.GONE
        }

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("setOnClickListener", " launch")
            networkProcessor.responseChannel.consumeEach(responseHandler())
        }


    }

    @ExperimentalCoroutinesApi
    @ImplicitReflectionSerializer
    override fun onStart() {
        Log.d("onStart", "onStart")
        super.onStart()
        networkProcessor = Processor(this)
        networkProcessor.requestWeather(lang = Locale.getDefault().language)
    }

    override fun onStop() {
        Log.d("onStop", "onStop")
        super.onStop()
        networkProcessor.responseChannel.close()
    }

    @SuppressLint("SetTextI18n")
    private fun responseHandler(): (CurrentWeatherResponse) -> Unit {
        return {
//          Debug info
            activityMainBinding.tvResponse.text = "$it "

            if (it.weather.isNotEmpty()) {
                activityMainBinding.tvDescription.text =
                    "${it.name}, ${it.timeStamp} ${it.weather[0].description}"
                IconDownloader.getIcon(
                    it.weather[0],
                    activityMainBinding.ivWeatherConditionIconPrimary,
                    this
                )
                if (it.weather.size > 1) {
                    IconDownloader.getIcon(
                        it.weather[1],
                        activityMainBinding.ivWeatherConditionIconSecondary,
                        this
                    )
                } else {
                    activityMainBinding.ivWeatherConditionIconSecondary.visibility = View.GONE
                }

            }

            val index = (it.wind.deg / 22.5).roundToInt()

            with(activityMainBinding)
            {
                tvWind.text = " ${it.wind.speed.toInt()} $windSpeed\n ${wind[index]} "
                etTemperature.text = it.main.temp.toString() + "°C" + " \n ${it.main.feels_like} °C"
                ivWindDirection.rotation = it.wind.deg.toFloat()
                tvTempRange.text = "${it.main.temp_min} ${it.main.temp_max}"
                tvHumidity.text = "$humidity ${it.main.humidity}%"
                tvWindScale.text =
                    " ${BeaufortScaleTable.getBeaufortString(it.wind.speed, Beaufort)}"
            }
        }
    }
}
