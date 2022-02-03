package com.ibashniak.weatherapp

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.data.Repository
import com.ibashniak.weatherapp.databinding.ActivityMainBinding
import com.ibashniak.weatherapp.location.LocationChannel
import com.ibashniak.weatherapp.location.LocationProvider
import com.ibashniak.weatherapp.location.PermissionChecker
import com.ibashniak.weatherapp.ui.Animator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MainActivity : AppCompatActivity(), KoinComponent {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 222
        fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.activeNetwork?.let { activeNetwork ->
                    connectivityManager.getNetworkCapabilities(activeNetwork)
                        ?.let { networkCapabilities ->
                            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) or
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) or
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) or
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                        }
                }
                return false
            } else {
                val nwInfo = connectivityManager.activeNetworkInfo ?: return false
                return nwInfo.isConnected
            }
        }
    }

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var animator: Animator
    private lateinit var locationProvider: LocationProvider
    private val coroutineScope = lifecycle.coroutineScope

    private lateinit var activityMainBinding: ActivityMainBinding
    private val repo: Repository by inject()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted)
                coroutineScope.launch {
                    PermissionChecker.checkPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } else {
                checkNetwork()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        locationProvider = LocationProvider(this, LocationChannel(coroutineScope))
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding.lifecycleOwner = this
        activityMainBinding.repository = repo
        animator = Animator(this, activityMainBinding.ivWindDirection, repo.currentWeather)

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        val availability = GoogleApiAvailability.getInstance()

        val isGooglePlayServicesAvailable = availability.isGooglePlayServicesAvailable(this)
        val apkVer = availability.getApkVersion(this)
        val clVer = availability.getClientVersion(this)
        Timber.d(
            "onResume: GoogleApiAvailability" +
                " ${isGooglePlayServicesAvailable == ConnectionResult.SUCCESS} " +
                "apkVer $apkVer clVer $clVer"
        )
    }
    override fun onResume() {
        super.onResume()
        checkNetwork()
    }

    private fun checkNetwork() {
        if (isNetworkAvailable(connectivityManager)) {
            startUpdate()
        } else {
            Timber.d("Network is not available !!")
            val networkCallback: ConnectivityManager.NetworkCallback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        Timber.d(" ")
                        startUpdate()
                        connectivityManager.unregisterNetworkCallback(this)
                    }
                }
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    private fun startUpdate() = coroutineScope.launch(Dispatchers.Main) {
        activityMainBinding.tvWarning.visibility = View.GONE
        activityMainBinding.tvHumidity.setTextColor(Color.BLACK)
        locationProvider.startLocationUpdates()
        repo.startUpdate(locationProvider)
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()
        locationProvider.stopLocationUpdates()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }
}
