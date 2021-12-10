package com.ibashniak.weatherapp.network.dto

data class Main(
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double
)
