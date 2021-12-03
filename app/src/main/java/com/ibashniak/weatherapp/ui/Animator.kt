package com.ibashniak.weatherapp.ui

import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.ibashniak.weatherapp.data.CurrentWeather

class Animator(
    lifecycleOwner: LifecycleOwner,
    arrow: ImageView,
    currentWeather: LiveData<CurrentWeather>
) {
    init {
        currentWeather.observe(lifecycleOwner,
            { weather ->
                if (weather != null) {
                    arrow.rotation = 0F
                    val circle = 360
                    val duration = 1500L
                    arrow.animate().rotation(circle + weather.windDegree).duration = duration
                }
            })
    }
}