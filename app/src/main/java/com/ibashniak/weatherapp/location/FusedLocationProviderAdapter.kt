package com.ibashniak.weatherapp.location

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class FusedLocationProviderAdapter(
    activity: Activity,
    private val locationRequest: LocationRequest,
    val locationChannel: Channel<Location>
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    private var fusedLocationProviderClient = FusedLocationProviderClient(activity)
    private val TAG = "FusedLocationProvider"

    @DelicateCoroutinesApi
    private val locationCallBack = buildLocationCallBack()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates")
        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                it
            )
        }
    }

    @DelicateCoroutinesApi
    private fun buildLocationCallBack() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(TAG, "onLocationResult")
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            Log.d(
                TAG, "onLocationResult locations.size ${locationResult.locations.size}" +
                        " locationResult.lastLocation ${currentLocation.latitude}"
            )
            GlobalScope.launch {
                locationChannel.send(currentLocation)
            }
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            Log.d(TAG, "onLocationAvailability isLocationAvailable ${p0.isLocationAvailable}")
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }
}