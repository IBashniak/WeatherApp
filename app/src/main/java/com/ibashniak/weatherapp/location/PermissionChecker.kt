package com.ibashniak.weatherapp.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.ibashniak.weatherapp.MainActivity
import com.ibashniak.weatherapp.ui.RequestPermissionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PermissionChecker {

    companion object {
        suspend fun checkPermission(activity: Activity, permission: String) =
            suspendCoroutine<Boolean> { continuation ->
                val checkSelfPermission = ActivityCompat.checkSelfPermission(
                    activity,
                    permission
                )
                var isGPSaccessGranted = (checkSelfPermission == PackageManager.PERMISSION_GRANTED)
                Timber.d("isGPSaccessGranted ? $isGPSaccessGranted")
                if (isGPSaccessGranted) {
                    continuation.resume(true)
                } else {
                    // Check Permissions Now

                    val shouldShowRequestPermissionRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    Timber.d(
                        "shouldShowRequestPermissionRationale $shouldShowRequestPermissionRationale"
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
                                    is Exception -> {
                                        Timber.d("${it.message}")
                                        activity.finishAndRemoveTask()
                                    }
                                    else -> continuation.resumeWithException(it)
                                }
                            }
                        }
                    } else {
                        Timber.d("shouldShowRequestPermissionRationale =$shouldShowRequestPermissionRationale ")
                    }
                }
            }
    }
}
