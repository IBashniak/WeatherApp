package com.ibashniak.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.network.processor.Processor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var btn: Button
    private lateinit var tv: TextView
    private val neworkProcessor = Processor()

    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btn = binding.btnRequest
        tv = binding.tvResponse

        btn.setOnClickListener {
            Log.d("setOnClickListener", " start")
            neworkProcessor.requestWeather()
            GlobalScope.launch(Dispatchers.Main) {
                Log.d("setOnClickListener", " launch")
//                val coords = neworkProcessor.coordChannel
                val resp = neworkProcessor.responseChannel.receive()
                tv.text = resp
//                val coord = coords.consumeEach {
//                    Log.d("setOnClickListener", "${it.toString()}")
//
//                    tv.text = tv.text.toString()+ " ${it.toString()} "
//                    if(coords.isEmpty)
//                        tv.text = tv.text.toString()+ "_____________"
//
//                }


            }
        }
    }
}
