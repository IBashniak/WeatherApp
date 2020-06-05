package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable.Companion.getBeaufortString
import com.ibashniak.weatherapp.network.processor.IconDownloader
import com.ibashniak.weatherapp.network.processor.Processor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    companion object {
        const val CHECK_SETTINGS_CODE = 111
        const val REQUEST_LOCATION_PERMISSION = 222
    }

    private var networkProcessor: Processor? = null
    private lateinit var Beaufort: Array<String>
    private lateinit var humidity: String
    private lateinit var windSpeed: String
    private lateinit var wind: Array<String>
    private lateinit var locationProvider: LocationProvider
    private val TAG = "MainActivity"
    private var location: Location? = null

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main + Job()).launch {
            location = locationProvider.getLocation()
        }
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        humidity = resources.getString(R.string.humidity)
        windSpeed = resources.getString(R.string.speed)
        Beaufort = resources.getStringArray(R.array.Beaufort)
        wind = resources.getStringArray(R.array.wind_direction)

        locationProvider = LocationProvider(this)


        with(activityMainBinding) {
            btnRequest.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()

        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        Log.d(TAG, "onResume GoogleApiAvailability ${availability == ConnectionResult.SUCCESS}")
        if (networkProcessor == null) {
            networkProcessor = Processor()
        }
        activityMainBinding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main + Job()).launch {

            Log.d(TAG, " locationProvider ->longitude ${location?.longitude} latitude ${location?.latitude}")

            location = locationProvider.getLocation()

            if (location != null) {
                networkProcessor?.requestWeather(location!!, lang = Locale.getDefault().language)
            } else {
                throw Exception()
            }
            while (networkProcessor != null)
                for (msg in networkProcessor!!.responseChannel) {
                    responseHandler(msg)
                }
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        networkProcessor?.responseChannel?.close()
        networkProcessor = null
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun responseHandler(weather: CurrentWeatherResponse) {
        Log.d(TAG, "responseHandler")
//          Debug info

        activityMainBinding.tvResponse.text = "${location?.toString()} \n$weather "

        downloadIcons(weather)

        val index = (weather.wind.deg / 22.5).roundToInt()

        with(activityMainBinding)
        {
            tvDescription.text = weather.description
            progressBar.visibility = View.GONE
            tvWind.text = " ${weather.wind.speed.toInt()} $windSpeed\n ${wind[index]} "
            etTemperature.text =
                "%.1f".format(weather.main.temp) + "°C" + " \n${"%.1f".format(weather.main.feels_like)} °C"
            ivWindDirection.rotation = 0F
            ivWindDirection.animate().rotation(360 + weather.wind.deg.toFloat()).duration = 1500L
            tvTempRange.text = "${weather.main.temp_min} ${weather.main.temp_max}"
            tvHumidity.text = "$humidity ${weather.main.humidity}%"
            tvWindScale.text =
                " ${getBeaufortString(weather.wind.speed, Beaufort)}"
        }
    }

    private fun downloadIcons(weather: CurrentWeatherResponse) {
        if (weather.weather.isNotEmpty()) {
            IconDownloader.getIcon(
                weather.weather[0],
                activityMainBinding.ivWeatherConditionIconPrimary,
                this
            )
            if (weather.weather.size > 1) {
                IconDownloader.getIcon(
                    weather.weather[1],
                    activityMainBinding.ivWeatherConditionIconSecondary,
                    this
                )
            }
        }
    }

}
