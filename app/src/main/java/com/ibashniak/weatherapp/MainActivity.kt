package com.ibashniak.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
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
    private val neworkProcessor = Processor(this as Context)
    private lateinit var context: Context

    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding!!.root)
        btn = activityMainBinding!!.btnRequest
        tv = activityMainBinding!!.tvResponse
        context = this as Context

        neworkProcessor.requestWeather()

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("setOnClickListener", " launch")
            tv.text = neworkProcessor.responseChannel.receive()
        }

        btn.setOnClickListener {
            Log.d("setOnClickListener", " start")

            neworkProcessor.requestWeather()
            GlobalScope.launch(Dispatchers.Main) {
                Log.d("setOnClickListener", " launch")

                tv.text = neworkProcessor.responseChannel.receive()

                val icon = activityMainBinding!!.ivWeatherConditionIconSecondary
                Glide.with(context)
                    .load("https://openweathermap.org/img/wn/10d@2x.png")
                    .into(icon);
                icon.animate().scaleX(2f).scaleY(2f).duration = 2L

            }
        }
    }
}
