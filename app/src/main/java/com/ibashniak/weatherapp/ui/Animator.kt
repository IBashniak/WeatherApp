package com.ibashniak.weatherapp.ui

import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.ibashniak.weatherapp.data.WeatherNow

class Animator(lifecycleOwner: LifecycleOwner, arrow: ImageView, weatherNow: LiveData<WeatherNow>) {

    init {
        weatherNow.observe(lifecycleOwner,
            { weather ->
                if (weather != null) {
                    arrow.rotation = 0F
                    arrow.animate().rotation(360 + weather.windDegree).duration = 1500L
                }
            })
    }
}