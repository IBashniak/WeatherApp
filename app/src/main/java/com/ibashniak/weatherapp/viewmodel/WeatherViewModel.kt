package com.ibashniak.weatherapp.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ibashniak.weatherapp.data.CurrentWeather
import com.ibashniak.weatherapp.data.Repository
import com.ibashniak.weatherapp.location.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class WeatherViewModel(
    appContext: Context,
    private val coroutineScope: CoroutineScope,
    var locationProvider: LocationProvider,
) : ViewModel(), KoinComponent {
    private val repo: Repository by inject()

    val currentWeather: LiveData<CurrentWeather> = repo.currentWeather
    val isLoading = repo.isLoading
    val rotation = 0F

    init {
        val availability = GoogleApiAvailability.getInstance()

        val isGooglePlayServicesAvailable = availability.isGooglePlayServicesAvailable(appContext)
        val apkVer = availability.getApkVersion(appContext)
        val clVer = availability.getClientVersion(appContext)
        Timber.d(
            "onResume: GoogleApiAvailability" +
                " ${isGooglePlayServicesAvailable == ConnectionResult.SUCCESS} " +
                "apkVer $apkVer clVer $clVer"
        )
    }

    private val _isNetworkMissed = MutableLiveData(true)
    val isNetworkMissed: LiveData<Boolean> = _isNetworkMissed

    fun startUpdate() = coroutineScope.launch(Dispatchers.Main) {
        _isNetworkMissed.value = false
        locationProvider.startLocationUpdates()
        repo.startUpdate(locationProvider)
    }

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

    private var connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun checkNetwork() {
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
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                networkCallback
            )
        }
    }
}
