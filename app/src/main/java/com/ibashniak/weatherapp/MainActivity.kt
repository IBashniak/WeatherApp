package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

//    private var _activityMainBinding: ActivityMainBinding

    companion object {
        var activityMainBinding: ActivityMainBinding? = null
    }


    private lateinit var networkProcessor: Processor


    @ExperimentalCoroutinesApi
    @SuppressLint("SetTextI18n")
    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding!!.root)

        networkProcessor = Processor(this)
        networkProcessor.requestWeather()

        activityMainBinding!!.btnRequest.setOnClickListener {
            Log.d("setOnClickListener", " start")
            networkProcessor.requestWeather()
        }

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("setOnClickListener", " launch")
            networkProcessor.responseChannel.consumeEach(responseHandler())
        }


    }

    @SuppressLint("SetTextI18n")
    private fun responseHandler(): (CurrentWeatherResponse) -> Unit {
        return {
            val binding = activityMainBinding!!

            binding.tvResponse.text = "${BeaufortScaleTable.getBeaufortString(
                it.wind.speed,
                this
            )}\n$it "
            if (it.weather.isNotEmpty()) {
                binding.tvDescription.text = it.weather[0].description
                IconDownloader.getIcon(
                    it.weather[0],
                    binding.ivWeatherConditionIconPrimary,
                    this
                )

            }

            val index = (it.wind.deg / 22.5).roundToInt()

            val wind = resources.getStringArray(R.array.wind_direction)
            binding.tvWind.text = " ${it.wind.speed}\n ${wind[index]} "
            binding.etTemperature.text = it.main.temp.toString() + "°C"
            binding.ivWindDirection.rotation = it.wind.deg.toFloat()
            binding.tvTempRange.text = "${it.main.temp_min} ${it.main.temp_max}"
            binding.tvHumidity.text = "Humidity ${it.main.humidity}"
            binding.tvWindScale.text = BeaufortScaleTable.getBeaufortString(
                it.wind.speed,
                this
            )

        }
    }
}
