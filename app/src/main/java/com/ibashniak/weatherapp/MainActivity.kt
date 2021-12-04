package com.ibashniak.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.data.BeaufortScaleTable
import com.ibashniak.weatherapp.data.Repository
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationChaneel
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.network.icon.api.IconDownloadClient
import com.ibashniak.weatherapp.ui.Animator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class MainActivity : AppCompatActivity(), KoinComponent {
    companion object {
        const val CHECK_SETTINGS_CODE = 111
        const val REQUEST_LOCATION_PERMISSION = 222
        private const val TAG = "MainActivity"
    }

    private lateinit var animator: Animator
    private lateinit var locationProvider: LocationProvider
    private val coroutineScope = lifecycle.coroutineScope

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var repo: Repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tableBeaufortScale: BeaufortScaleTable by inject()
        val iconDownloadClient: IconDownloadClient by inject()

        locationProvider = LocationProvider(this, LocationChaneel(coroutineScope))
        repo = Repository(
            resources,
            this,
            tableBeaufortScale,
            iconDownloadClient,
            locationProvider,
            coroutineScope
        )
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding.lifecycleOwner = this
        activityMainBinding.repository = repo
        animator = Animator(this, activityMainBinding.ivWindDirection, repo.currentWeather)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Log.d(
                        TAG, "onRequestPermissionsResult: Permission is granted. " +
                                "Continue the action or workflow in the app."
                    )
                } else {
                    Log.d(
                        TAG, "onRequestPermissionsResult: called with: requestCode =" +
                                " $requestCode,permissions = $permissions, grantResults = $grantResults"
                    )
                    Log.d(
                        TAG, "onRequestPermissionsResult: Explain to the user that the feature" +
                                " is unavailable because" +
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
        coroutineScope.launch(Dispatchers.Main) {
            locationProvider.startLocationUpdates()
        }
        val availability = GoogleApiAvailability.getInstance()

        val isGooglePlayServicesAvailable = availability.isGooglePlayServicesAvailable(this)
        val apkVer = availability.getApkVersion(this)
        val clVer = availability.getClientVersion(this)
        Log.d(
            TAG, "onResume: GoogleApiAvailability" +
                    " ${isGooglePlayServicesAvailable == ConnectionResult.SUCCESS} " +
                    "apkVer $apkVer clVer $clVer"
        )
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        locationProvider.stopLocationUpdates()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

}
