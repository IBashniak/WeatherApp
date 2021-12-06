package com.ibashniak.weatherapp.data


import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ibashniak.weatherapp.BuildConfig
import com.ibashniak.weatherapp.R
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.icon.api.IconApi
import com.ibashniak.weatherapp.network.icon.api.IconApi.Companion.FILE_NAME_END
import com.ibashniak.weatherapp.network.icon.api.IconDownloadClient
import com.ibashniak.weatherapp.network.weather.api.WeatherApi
import com.ibashniak.weatherapp.network.weather.api.WeatherDownloadClient
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import java.io.*
import java.util.*


class Repository(
    private val res: Resources,
    private val cntxt: Context,
    private val tableBeaufortScale: BeaufortScaleTable,
    private val iconDownloadClient: IconDownloadClient,
    private val locationProvider: LocationProvider,
    private var coroutineScope: CoroutineScope
) : KoinComponent {
    private val weatherDownloadClient = WeatherDownloadClient()

    private val _weatherNow = MutableLiveData<CurrentWeather>(null)
    private val _progressBarVisibility =
        MutableLiveData(1).apply { value = android.view.View.VISIBLE }
    private val TAG = "Repository"

    val currentWeather: LiveData<CurrentWeather> = _weatherNow
    val progressBarVisibility: LiveData<Int> = _progressBarVisibility


    private fun iconFileName(weatherIcon: String): String =
        cntxt.filesDir.toString() + File.separator + weatherIcon + FILE_NAME_END


    private fun onCurrentWeatherResponse(weather: CurrentWeatherResponse) {
        Log.d(TAG, "responseHandler")

        _progressBarVisibility.value = android.view.View.GONE
        val windSpeedUnits = res.getString(R.string.speed)
        val humidity = res.getString(R.string.humidity)
        val real = res.getString(R.string.real)
        val comfort = res.getString(R.string.comfort)
        val min = res.getString(R.string.min)
        val max = res.getString(R.string.max)

        val Beaufort = res.getStringArray(R.array.Beaufort)
        val icon = weather.weather[0].icon


        coroutineScope.launch() {
            if (!checkWeatherIconFile(icon)) {
                val resp = iconDownloadClient.client()
                    .getIcon(IconApi.iconUrl(icon).toString())
                Log.d(TAG, "resp.isSuccessful  = ${resp.isSuccessful} ")
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
                        " ${tableBeaufortScale.getBeaufortString(weather.wind.speed, Beaufort)}",
                        weather.wind.deg.toFloat(),
                        iconFileName(icon)
                    )
                Log.d(TAG, "_progressBarVisibility.value  = ${progressBarVisibility.value} ")
            }
        }
    }

    private fun checkWeatherIconFile(weatherIcon: String): Boolean {
        val fileName = iconFileName(weatherIcon)
        val futureStudioIconFile = File(fileName)

        val fileExists = futureStudioIconFile.exists()

        if (fileExists) {
            Log.d(TAG, "$fileName does exist.")
        } else {
            Log.d(TAG, "$fileName does NOT exist.")
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
                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
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

    fun startUpdate() =
        coroutineScope.launch() {
            Log.d(TAG, "start: ")
            locationProvider.locationChannel.getLocation().also { location ->
                val lang = Locale.getDefault().language
                Log.d(TAG, "ff: location")
                val response = weatherDownloadClient.client()
                    .requestWeather(WeatherApi.weatherUrl(lang, location).toString())
                _progressBarVisibility.value = android.view.View.VISIBLE

                with(response) {
                    val resp = body()?.string()

                    val data = CurrentWeatherResponse.toObject(resp.toString())
                    Log.d(
                        TAG,
                        "requestWeather: body $data \nmessage resp __ $resp \n" +
                                "isSuccessful $isSuccessful  " +
                                "BuildConfig.BUILD_TYPE ${BuildConfig.BUILD_TYPE} "
                    )
                    if (BuildConfig.BUILD_TYPE == "debug") {
                        Log.d(
                            TAG,
                            "requestWeather: networkResponse ${toString()}"
                        )
                    }

                    if (isSuccessful && code() == 200) {
                        Log.d(TAG, "requestWeather: responseChannel.send")
                        withContext(Dispatchers.Main) {
                            _progressBarVisibility.value = android.view.View.GONE
                            onCurrentWeatherResponse(data)
                        }
                        return@launch
                    }
                }
            }
        }
}