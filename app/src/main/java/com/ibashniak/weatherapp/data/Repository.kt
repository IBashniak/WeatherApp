package com.ibashniak.weatherapp.data


import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ibashniak.weatherapp.R
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.icon.api.DownloadClient
import com.ibashniak.weatherapp.network.icon.api.IconApi
import com.ibashniak.weatherapp.network.icon.api.IconApi.Companion.FILE_NAME_END
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.*


class Repository(private val res: Resources, private val cntxt: Context) : KoinComponent {
    private var coroutineScope = createCoroutineScope()
    private fun createCoroutineScope() = CoroutineScope(Job() + Dispatchers.IO)

    private val tableBeaufortScale: BeaufortScaleTable by inject()
    private val downloadClient: DownloadClient by inject()

    private val _weatherNow = MutableLiveData<CurrentWeather>(null)
    private val _progressBarVisibility =
        MutableLiveData(1).apply { value = android.view.View.VISIBLE }
    private val TAG = "Repository"

    val currentWeather: LiveData<CurrentWeather> = _weatherNow
    val progressBarVisibility: LiveData<Int> = _progressBarVisibility

    private fun iconFileName(weatherIcon: String): String =
        cntxt.filesDir.toString() + File.separator + weatherIcon + FILE_NAME_END


    fun onCurrentWeatherResponse(weather: CurrentWeatherResponse) {
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
                val resp = downloadClient.client()
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

    fun setProgressBarVisibility(visible: Boolean) {
        _progressBarVisibility.value =
            if (visible) android.view.View.VISIBLE else android.view.View.GONE
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
}