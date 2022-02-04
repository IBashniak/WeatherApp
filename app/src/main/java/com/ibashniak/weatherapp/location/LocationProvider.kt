package com.ibashniak.weatherapp.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class LocationProvider(
    activity: Activity,
    scope: CoroutineScope,
    val locationChannel: LocationChannel = LocationChannel(scope),
    private var fusedLocationProviderClient: FusedLocationProviderClient =
        FusedLocationProviderClient(activity),

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
        interval = UPDATE_INTERVAL_IN_MILLISECONDS
        fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
) {

    private val locationCallBack = buildLocationCallBack()

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Timber.d(" ")
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    fun buildLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val currentLocation = locationResult.lastLocation
            Timber.d(
                "locations.size ${locationResult.locations.size}" +
                    " locationResult.lastLocation ${currentLocation.latitude}"
            )
            locationChannel.send(currentLocation)
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            Timber.d("isLocationAvailable ${p0.isLocationAvailable}")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000
        private const val UPDATE_INTERVAL_IN_SECONDS = 60 * 5
        private const val UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS.toLong()
    }
}
