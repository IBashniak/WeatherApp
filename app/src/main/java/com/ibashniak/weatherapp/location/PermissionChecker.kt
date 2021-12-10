package com.ibashniak.weatherapp.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.ui.RequestPermissionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PermissionChecker {

    companion object {
        private const val TAG = "PermissionChecker"

        suspend fun checkPermission(activity: Activity, permission: String) =
            suspendCoroutine<Boolean> { continuation ->
                val checkSelfPermission = ActivityCompat.checkSelfPermission(
                    activity,
                    permission
                )
                var isGPSaccessGranted = (checkSelfPermission == PackageManager.PERMISSION_GRANTED)
                Log.d(TAG, " isGPSaccessGranted ? $isGPSaccessGranted")
                if (isGPSaccessGranted) {
                    continuation.resume(true)
                } else {
                    // Check Permissions Now

                    val shouldShowRequestPermissionRationale =
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    Log.d(
                        TAG,
                        " Check Permissions Now shouldShowRequestPermissionRationale $shouldShowRequestPermissionRationale"
                    )
                    if (shouldShowRequestPermissionRationale) {
                        CoroutineScope(Dispatchers.Main + Job()).launch {
                            runCatching {
                                isGPSaccessGranted =
                                    RequestPermissionDialog.requestPermission(activity)
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                    MainActivity.REQUEST_LOCATION_PERMISSION
                                )
                            }.onFailure {
                                when (it) {
                                    is Exception -> Log.d(TAG, "${it.message}")
                                    else -> continuation.resumeWithException(it)
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "shouldShowRequestPermissionRationale =$shouldShowRequestPermissionRationale ")
                    }
                }
            }
    }
}
