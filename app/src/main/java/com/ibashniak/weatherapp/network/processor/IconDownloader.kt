package com.ibashniak.weatherapp.network.processor

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ibashniak.weatherapp.network.dto.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl

@GlideModule
class MyAppGlideModule : AppGlideModule()


class IconDownloader {

    companion object {
        // http://openweathermap.org/img/wn/10d@2x.png
        private const val ENDPOINT = "openweathermap.org"
        private const val CURRENT_WEATHER_METHOD = "img/wn"
        private const val FILE_NAME_END = "@2x.png"

        fun getIcon(weather: Weather, icon: ImageView, context: Context) {
            val TAG = "getIcon"

            val url = HttpUrl.Builder()
                .scheme("https")
                .host(ENDPOINT)
                .addPathSegments(CURRENT_WEATHER_METHOD)
                .addPathSegments(weather.icon + FILE_NAME_END)
                .build()
            Log.d(TAG, "https://openweathermap.org/img/wn/04d@2x.png")
            Log.d(TAG, url.toString())

            GlobalScope.launch(Dispatchers.Main) {


                Glide.with(context)
                    .load(url.toString())
                    .listener(object : RequestListener<Drawable> { //9
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (e != null) {
                                Log.d(
                                    TAG,
                                    "onLoadFailed ${e.message}  ${e.cause}  $e.logRootCauses(TAG)"
                                )
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?, model: Any?, target: Target<Drawable>?,
                            dataSource: DataSource?, isFirstResource: Boolean
                        ): Boolean {
                            Log.d(TAG, "onResourceReady ")
                            icon.animate().scaleX(2f).scaleY(2f).duration = 2L
                            return false
                        }
                    })
                    .into(icon)

            }
        }
    }
}