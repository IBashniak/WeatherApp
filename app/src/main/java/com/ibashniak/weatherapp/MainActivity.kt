package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.dto.CurrentWeatherResponse
import com.ibashniak.weatherapp.network.processor.BeaufortScaleTable
import com.ibashniak.weatherapp.network.processor.IconDownloader
import com.ibashniak.weatherapp.network.processor.Processor
import di.BeaufortScaleModule
import di.iconDownloaderModule
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), KoinComponent {
    companion object {
        const val CHECK_SETTINGS_CODE = 111
        const val REQUEST_LOCATION_PERMISSION = 222
    }

    private val iconDownloader by inject<IconDownloader>()
    private val BeaufortScale by inject<BeaufortScaleTable>()
    private var networkProcessor: Processor? = null
    private lateinit var Beaufort: Array<String>
    private lateinit var humidity: String
    private lateinit var windSpeed: String
    private lateinit var wind: Array<String>
    private lateinit var locationProvider: LocationProvider
    private val TAG = "MainActivity"

    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            modules(iconDownloaderModule)
            modules(BeaufortScaleModule)
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

    @SuppressLint("LongLogTag")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val TAG = TAG + "onRequestPermissionsResult"
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Log.d(TAG, "Permission is granted. Continue the action or workflow in the app.")
                } else {
                    Log.d(
                        TAG, " Explain to the user that the feature is unavailable because" +
                                "the features requires a permission that the user has denied." +
                                "At the same time, respect the user's decision. Don't link to" +
                                "system settings in an effort to convince the user to change" +
                                "their decision."
                    )
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onResume() {
        super.onResume()

        val availability = GoogleApiAvailability.getInstance()

        val isGooglePlayServicesAvailable = availability.isGooglePlayServicesAvailable(this)
        val apkVer = availability.getApkVersion(this)
        val clVer = availability.getClientVersion(this)
        Log.d(
            TAG,
            "onResume GoogleApiAvailability ${isGooglePlayServicesAvailable == ConnectionResult.SUCCESS} apkVer $apkVer clVer $clVer"
        )
        if (networkProcessor == null) {
            networkProcessor = Processor()
        }
        activityMainBinding.progressBar.visibility = View.VISIBLE


        CoroutineScope(Dispatchers.Main + Job()).launch {

            networkProcessor!!.requestWeather(
                lang = Locale.getDefault().language,
                locationProvider = locationProvider
            )

            while (networkProcessor != null)
                for (msg in networkProcessor!!.responseChannel) {
                    responseHandler(msg)
                }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        locationProvider.stopLocationUpdates()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        networkProcessor?.onDestroy()
        locationProvider.onDestroy()
        networkProcessor = null
        super.onDestroy()
    }

    @DelicateCoroutinesApi
    @SuppressLint("SetTextI18n")
    private fun responseHandler(weather: CurrentWeatherResponse) {
        Log.d(TAG, "responseHandler")
//          Debug info
        activityMainBinding.tvResponse.text = "$weather "

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
                " ${BeaufortScale.getBeaufortString(weather.wind.speed, Beaufort)}"
        }
    }

    @DelicateCoroutinesApi
    private fun downloadIcons(weather: CurrentWeatherResponse) {
        if (weather.weather.isNotEmpty()) {
            iconDownloader.getIcon(
                weather.weather[0],
                activityMainBinding.ivWeatherConditionIconPrimary,
                this
            )
            if (weather.weather.size > 1) {
                iconDownloader.getIcon(
                    weather.weather[1],
                    activityMainBinding.ivWeatherConditionIconSecondary,
                    this
                )
            }
        }
    }

}
