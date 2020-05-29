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
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

//    private var _activityMainBinding: ActivityMainBinding

    companion object {
//        private var activityMainBinding: ActivityMainBinding? = null
    }


    private lateinit var networkProcessor: Processor
    private lateinit var BeaufortEn: Array<String>
    private lateinit var BeaufortRu: Array<String>
    private lateinit var activityMainBinding: ActivityMainBinding

    @ImplicitReflectionSerializer
    @ExperimentalCoroutinesApi
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)


        BeaufortEn = resources.getStringArray(R.array.Beaufort_en)
        BeaufortRu = resources.getStringArray(R.array.Beaufort_ru)

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

    @ImplicitReflectionSerializer
    override fun onStart() {
        Log.d("onStart", "onStart")
        super.onStart()
        networkProcessor = Processor(this)
        networkProcessor.requestWeather()
    }

    override fun onStop() {
        Log.d("onStop", "onStop")
        super.onStop()
        networkProcessor.responseChannel.close()
    }

    @SuppressLint("SetTextI18n")
    private fun responseHandler(): (CurrentWeatherResponse) -> Unit {
        return {

            activityMainBinding.tvResponse.text = "${BeaufortScaleTable.getBeaufortString(
                it.wind.speed,
                BeaufortEn
            )}\n$it "

            if (it.weather.isNotEmpty()) {
                activityMainBinding.tvDescription.text = "${it.name}  ${it.weather[0].description}"
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

            val wind = resources.getStringArray(R.array.wind_direction)
            activityMainBinding.tvWind.text = " ${it.wind.speed}\n ${wind[index]} "
            activityMainBinding.etTemperature.text = it.main.temp.toString() + "°C"
            activityMainBinding.ivWindDirection.rotation = it.wind.deg.toFloat()
            activityMainBinding.tvTempRange.text = "${it.main.temp_min} ${it.main.temp_max}"
            activityMainBinding.tvHumidity.text = "Humidity ${it.main.humidity}"
            activityMainBinding.tvWindScale.text = " ${BeaufortScaleTable.getBeaufortString(
                it.wind.speed, BeaufortEn
            )} \n ${BeaufortScaleTable.getBeaufortString(
                it.wind.speed, BeaufortRu
            )}"

        }
    }
}
