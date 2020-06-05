package com.ibashniak.weatherapp.location

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.ui.RequestPermissionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class LocationProvider(private val activity: Activity) {
    private val MILLISECONDS_PER_SECOND = 1000
    val TAG = "LocationProvider"

    // колличество секунд для обновелния
    private val UPDATE_INTERVAL_IN_SECONDS = 60 * 5

    // интервал обновления в милисекундах
    private val UPDATE_INTERVAL_IN_MILLISECONDS =
        MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS.toLong()
    private var GPSaccessGranted: Boolean = false
    private val locationRequest: LocationRequest
    val coordinates: Location? = null
        get() = field

    init {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS / 2
        }
        val LocationSettingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(LocationSettingsBuilder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            locationSettingsResponse.toString()
            Log.d(
                TAG,
                "addOnSuccessListener isGpsPresent ${locationSettingsResponse.locationSettingsStates.isGpsPresent}"
            )

            val checkSelfPermission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            Log.d(
                TAG,
                " PERMISSION_GRANTED ? ${checkSelfPermission == PackageManager.PERMISSION_GRANTED}"
            )
            GPSaccessGranted = (checkSelfPermission == PackageManager.PERMISSION_GRANTED)
            if (!GPSaccessGranted) {
                // Check Permissions Now
                Log.d(TAG, " Check Permissions Now")
                val shouldShowRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                if (shouldShowRequestPermissionRationale) {
                    CoroutineScope(Dispatchers.Main + Job()).launch {
                        runCatching {
                            GPSaccessGranted = RequestPermissionDialog.requestPermission(activity)
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                MainActivity.REQUEST_LOCATION_PERMISSION
                            )
                        }.onFailure {
                            when (it) {
                                is Exception -> Log.d(TAG, "${it.message}")
                                else -> throw it
                            }
                        }
                    }
                }
            }
        }

        task.addOnFailureListener { exception ->
            Log.d(TAG, "Task<LocationSettingsResponse> addOnFailureListener ${exception.message}")
            if (exception is ResolvableApiException) {
                Log.d(TAG, "ResolvableApiException ${exception.resolution}")
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

    suspend fun getLocation() = suspendCoroutine<Location>() { continuation ->
        // permission has been granted, continue as usual
        if (coordinates != null) {
            continuation.resumeWith(Result.success(coordinates!!))
        }

        Log.d(TAG, " permission has been granted, continue as usual")
        val checkSelfPermission = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (checkSelfPermission) {
            FusedLocationProviderClient(activity).lastLocation.addOnCompleteListener { task ->
                Log.d(
                    TAG,
                    "provider ${task.result?.provider} latitude ${task.result?.latitude}  longitude ${task.result?.longitude} "
                )
                if (task.result == null) {
                    FusedLocationProviderClient(activity).requestLocationUpdates(
                        locationRequest,
                        buildLocationCallBack(continuation),
                        Looper.myLooper()
                    )
                } else {
                    continuation.resumeWith(Result.success(task.result!!))
                }
            }.addOnFailureListener {
                Log.d(
                    TAG,
                    "FusedLocationProviderClient addOnFailureListener ${it.message.toString()}"
                )
                continuation.resumeWithException(Exception("GPS not available"))
            }
        }
    }


    private fun buildLocationCallBack(continuation: Continuation<Location>) = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            Log.d(
                TAG,
                "onLocationResult locations.size ${locationResult.locations.size} locationResult.lastLocation ${currentLocation.latitude}"
            )
            continuation.resumeWith(Result.success(currentLocation))
        }
    }
}
