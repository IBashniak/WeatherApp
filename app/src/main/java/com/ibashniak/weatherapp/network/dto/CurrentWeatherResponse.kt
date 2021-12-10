package com.ibashniak.weatherapp.network.dto

import com.google.gson.Gson
import timber.log.Timber
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
            return timeString(dt, timezone)
        }

    companion object {
        fun timeString(dt: Long, timezone: Int): String {
            val date: LocalDateTime =
                LocalDateTime.ofEpochSecond(dt, 0, ZoneOffset.ofTotalSeconds(timezone))
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            return date.format(formatter)
        }

        fun toObject(stringValue: String): CurrentWeatherResponse {
            Timber.d(stringValue)
            return Gson().fromJson(stringValue, CurrentWeatherResponse::class.java)
        }
    }
}
