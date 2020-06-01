package com.ibashniak.weatherapp.network.dto

import android.util.Log
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class CurrentWeatherResponse(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Long,
    val timezone: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
) {
    val description: String
        get() = "$name, $timestamp ${if (weather.isNotEmpty()) weather[0].description else ""}"

    private val timestamp: String
        get() {
            val date: LocalDateTime =
                LocalDateTime.ofEpochSecond(dt, 0, ZoneOffset.ofTotalSeconds(timezone))
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            return date.format(formatter)
        }

    companion object {
        fun toObject(stringValue: String): CurrentWeatherResponse {
            Log.d("toObject", stringValue)
            return Gson().fromJson(stringValue, CurrentWeatherResponse::class.java)
        }
    }
}