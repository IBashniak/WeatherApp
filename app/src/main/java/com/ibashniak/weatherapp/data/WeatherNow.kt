package com.ibashniak.weatherapp.data

data class WeatherNow(
    val windSpeed: String,
    val description: String,
    val temperature: String,
    val tempRange: String,
    val humidity: String,
    val windScale: String,
    val windDegree:Float,
    val icon:String
)