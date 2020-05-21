package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import com.ibashniak.weatherapp.network.processor.Processor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer


class MainActivity : AppCompatActivity() {

//    private var _activityMainBinding: ActivityMainBinding

    companion object {
        var activityMainBinding: ActivityMainBinding? = null
            get() = field
    }

    private lateinit var btn: Button
    private lateinit var tv: TextView
    private lateinit var networkProcessor: Processor
    private lateinit var context: Context

    @SuppressLint("SetTextI18n")
    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding!!.root)
        btn = activityMainBinding!!.btnRequest
        tv = activityMainBinding!!.tvResponse
        context = this as Context
        networkProcessor = Processor(context)
        networkProcessor.requestWeather()

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("setOnClickListener", " launch")
            tv.text = networkProcessor.responseChannel.receive().toString()
        }

        btn.setOnClickListener {
            Log.d("setOnClickListener", " start")

            networkProcessor.requestWeather()
            GlobalScope.launch(Dispatchers.Main) {
                Log.d("setOnClickListener", " launch")
                val currentWeatherResponse = networkProcessor.responseChannel.receive()
                tv.text = "${BeaufortScaleTable.getBeaufortString(
                    currentWeatherResponse.wind.speed,
                    context
                )}\n$currentWeatherResponse"

                val icon = activityMainBinding!!.ivWeatherConditionIconSecondary
                Glide.with(context)
                    .load("https://openweathermap.org/img/wn/10d@2x.png")
                    .into(icon)
                icon.animate().scaleX(2f).scaleY(2f).duration = 2L

            }
        }
    }
}
