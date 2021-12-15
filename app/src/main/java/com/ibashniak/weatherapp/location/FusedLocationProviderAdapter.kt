package com.ibashniak.weatherapp.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.*
import timber.log.Timber

class FusedLocationProviderAdapter(
    activity: Activity,
    private val locationRequest: LocationRequest,
    val locationChannel: LocationChannel
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    private var fusedLocationProviderClient = FusedLocationProviderClient(activity)
    private val locationCallBack = buildLocationCallBack()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        Timber.d("requestLocationUpdates")
        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                it
            )
        }
    }

    private fun buildLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Timber.d("")
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            Timber.d(
                "locations.size ${locationResult.locations.size}" +
                    " locationResult.lastLocation ${currentLocation.latitude}"
            )
            locationChannel.send(currentLocation)
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            Timber.d("isLocationAvailable ${p0.isLocationAvailable}")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }
}
