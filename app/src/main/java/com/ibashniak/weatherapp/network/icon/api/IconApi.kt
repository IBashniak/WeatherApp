package com.ibashniak.weatherapp.network.icon.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface IconApi {
    @GET("/$CURRENT_WEATHER_METHOD/{iconName}$FILE_NAME_END")
    suspend fun getIcon(@Path("iconName") iconName: String): Response<ResponseBody>

    companion object {
        const val CURRENT_WEATHER_METHOD = "img/wn"
        const val FILE_NAME_END = "@2x.png"
    }
}
