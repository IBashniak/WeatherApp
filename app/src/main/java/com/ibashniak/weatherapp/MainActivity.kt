package com.ibashniak.weatherapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.data.Repository
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.processor.Processor
import com.ibashniak.weatherapp.ui.Animator
import di.BeaufortScaleModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import java.util.*


class MainActivity : AppCompatActivity(), KoinComponent {
    companion object {
        const val CHECK_SETTINGS_CODE = 111
        const val REQUEST_LOCATION_PERMISSION = 222
        private val koin: KoinApplication = startKoin {

            modules(BeaufortScaleModule)
        }
    }

    private var networkProcessor: Processor? = null
    private lateinit var animator: Animator
    private lateinit var locationProvider: LocationProvider
    private val TAG = "MainActivity"

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var repo: Repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = Repository(resources, this)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding.lifecycleOwner = this
        activityMainBinding.viewmodel = repo
        animator = Animator(this, activityMainBinding.ivWindDirection, repo.weatherNow)
        locationProvider = LocationProvider(this)
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
                    Log.d(TAG, "onRequestPermissionsResult: ")
                    Log.d(
                        TAG,
                        "onRequestPermissionsResult() called with: requestCode = $requestCode, permissions = $permissions, grantResults = $grantResults"
                    )
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
        repo.setProgressBarVisibility(true)

        CoroutineScope(Dispatchers.Main + Job()).launch {

            networkProcessor!!.requestWeather(
                lang = Locale.getDefault().language,
                locationProvider = locationProvider
            )

            while (networkProcessor != null)
                for (msg in networkProcessor!!.responseChannel) {
                    repo.onCurrentWeatherResponse(msg)
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

}
