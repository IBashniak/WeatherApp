package com.ibashniak.weatherapp.data

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ibashniak.weatherapp.BuildConfig
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.R
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.icon.api.IconApi
import com.ibashniak.weatherapp.network.icon.api.IconApi.Companion.FILE_NAME_END
import com.ibashniak.weatherapp.network.weather.api.WeatherApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*

class Repository(
    private val context: Context,
    private val tableBeaufortScale: BeaufortScaleTable,
    private val weatherApi: WeatherApi,
    private val iconApi: IconApi
) {
    private val res: Resources = context.resources
    private val _weatherNow = MutableLiveData<CurrentWeather>(null)
    val currentWeather: LiveData<CurrentWeather> = _weatherNow

    private fun iconFileName(weatherIcon: String): String =
        context.filesDir.toString() + File.separator + weatherIcon + FILE_NAME_END

    private fun CoroutineScope.onCurrentWeatherResponse(weather: CurrentWeatherResponse) {
        Timber.d("")

        val windSpeedUnits = res.getString(R.string.speed)
        val humidity = res.getString(R.string.humidity)
        val real = res.getString(R.string.real)
        val comfort = res.getString(R.string.comfort)
        val min = res.getString(R.string.min)
        val max = res.getString(R.string.max)

        val beaufort = res.getStringArray(R.array.Beaufort)
        val icon = weather.weather[0].icon

        launch {
            if (!checkWeatherIconFile(icon)) {
                val resp = iconApi.getIcon(icon)
                Timber.d("resp.isSuccessful  = ${resp.isSuccessful} ")
                resp.body()?.let { writeIconFileToDisk(it, icon) }
            }
            withContext(Dispatchers.Main) {
                _weatherNow.value =
                    CurrentWeather(
                        " ${weather.wind.speed.toInt()}\n$windSpeedUnits",
                        weather.description,
                        "%.1f".format(weather.main.temp) + "°C $real" + " \n${"%.1f".format(weather.main.feels_like)}°C  $comfort",
                        "${weather.main.temp_min}$min ${weather.main.temp_max}$max",
                        "$humidity ${weather.main.humidity}%",
                        " ${tableBeaufortScale.getBeaufortString(weather.wind.speed, beaufort)}",
                        weather.wind.deg.toFloat(),
                        iconFileName(icon)
                    )
            }
        }
    }

    private fun checkWeatherIconFile(weatherIcon: String): Boolean {
        val fileName = iconFileName(weatherIcon)
        val futureStudioIconFile = File(fileName)

        val fileExists = futureStudioIconFile.exists()

        if (fileExists) {
            Timber.d("$fileName does exist.")
        } else {
            Timber.d("$fileName does NOT exist.")
        }
        return fileExists
    }

    private fun writeIconFileToDisk(body: ResponseBody, weatherIcon: String): Boolean {
        return try {
            val futureStudioIconFile = File(iconFileName(weatherIcon))

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Timber.d("$fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()
                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun startUpdate(locationProvider: LocationProvider, mainActivity: MainActivity) =
        with(Dispatchers.Default) {
            mainActivity.isLoading = true
            Timber.d("")
            locationProvider.locationChannel.getLocation().also { location ->
                Timber.d("$location")

                val response: CurrentWeatherResponse = weatherApi.requestWeather(location.latitude, location.longitude)

                Timber.d(
                    "$response " +
                        "BuildConfig.BUILD_TYPE ${BuildConfig.BUILD_TYPE} "
                )

                withContext(Dispatchers.Main) {
                    mainActivity.isLoading = false
                    onCurrentWeatherResponse(response)
                }
            }
        }
}
