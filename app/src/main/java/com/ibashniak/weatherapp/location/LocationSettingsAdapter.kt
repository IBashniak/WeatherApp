package com.ibashniak.weatherapp.location

import android.app.Activity
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationSettingsAdapter(activity: Activity, locationRequest: LocationRequest) {
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private val locationSettingsBuilder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    suspend fun getLocationSettingsAsync() = suspendCoroutine<LocationSettingsResponse> { continuation ->
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsBuilder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            continuation.resumeWith(Result.success(locationSettingsResponse))
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

}
