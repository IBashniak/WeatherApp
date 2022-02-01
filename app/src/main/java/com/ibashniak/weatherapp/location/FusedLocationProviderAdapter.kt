package com.ibashniak.weatherapp.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Looper
import com.google.android.gms.location.*
import timber.log.Timber

class FusedLocationProviderAdapter(
    activity: Activity,
    private val locationRequest: LocationRequest,
    val locationChannel: LocationChannel,
    @SuppressLint("VisibleForTests")
    private var fusedLocationProviderClient: FusedLocationProviderClient = FusedLocationProviderClient(activity)
) {

    private val locationCallBack = buildLocationCallBack()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
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
}
