package com.ibashniak.weatherapp.network.icon.api

import okhttp3.HttpUrl
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface IconApi {

    @GET
    suspend fun getIcon(@Url fileUrl: String): Response<ResponseBody>

    companion object {
        const val ENDPOINT = "openweathermap.org"
        const val CURRENT_WEATHER_METHOD = "img/wn"
        const val FILE_NAME_END = "@2x.png"

        fun iconUrl(icon: String) =
            HttpUrl.Builder()
                .scheme("https")
                .host(ENDPOINT)
                .addPathSegments(CURRENT_WEATHER_METHOD)
                .addPathSegments(icon + FILE_NAME_END)
                .build()
    }
}
