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
        currentWeather.observe(
            lifecycleOwner,
            { weather ->
                if (weather != null) {
                    arrow.rotation = 0F
                    arrow.animate().rotation(CIRCLE + weather.windDegree).duration = DURATION
                }
            }
        )
    }
    companion object {
        const val CIRCLE = 360
        const val DURATION = 1500L
    }
}
