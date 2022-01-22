package com.ibashniak.weatherapp.location

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.location.PermissionChecker.Companion.checkPermission
import timber.log.Timber

class LocationProvider(private val activity: Activity, val locationChannel: LocationChannel) {
    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000
        private const val UPDATE_INTERVAL_IN_SECONDS = 60 * 5
        private const val UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS.toLong()
    }
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
        interval = UPDATE_INTERVAL_IN_MILLISECONDS
        fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    private val locationSettingsAdapter = LocationSettingsAdapter(activity, locationRequest)
    private val fusedLocationProviderAdapter =
        FusedLocationProviderAdapter(activity, locationRequest, locationChannel)

    suspend fun startLocationUpdates() {
        try {
            val locationSettingsResponse = locationSettingsAdapter.getLocationSettingsAsync()
            Timber.d(
                "isGpsPresent ${locationSettingsResponse.locationSettingsStates?.isGpsPresent}"
            )
            if (checkPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Timber.d("requestLocationUpdates")
                fusedLocationProviderAdapter.requestLocationUpdates()
            }
        } catch (exception: Exception) {
            Timber.d("Task<LocationSettingsResponse> addOnFailureListener ${exception.message}")
            if (exception is ResolvableApiException) {
                Timber.d("ResolvableApiException ${exception.resolution}")
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activity,
                        MainActivity.REQUEST_LOCATION_PERMISSION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun stopLocationUpdates() {
        fusedLocationProviderAdapter.stopLocationUpdates()
    }
}
