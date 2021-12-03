package com.ibashniak.weatherapp.network.icon.api


import okhttp3.HttpUrl
import retrofit2.Retrofit

class DownloadClient {

    fun client(): IconApi {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(IconApi.ENDPOINT)
            .addPathSegments(IconApi.CURRENT_WEATHER_METHOD+"/")
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .build()
            .create(IconApi::class.java)
    }
}