package com.ibashniak.weatherapp.network.dto

import android.util.Log
import com.google.gson.Gson

data class CurrentWeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
) {

    companion object {
        fun toObject(stringValue: String): CurrentWeatherResponse {
            Log.d("toObject", stringValue)
            return Gson().fromJson(stringValue, CurrentWeatherResponse::class.java)
        }
    }
}