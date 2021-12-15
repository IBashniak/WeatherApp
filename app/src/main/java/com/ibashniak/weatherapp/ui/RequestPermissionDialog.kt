package com.ibashniak.weatherapp.ui

import android.app.Activity
import android.app.AlertDialog
import com.ibashniak.weatherapp.R
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RequestPermissionDialog {

    companion object {
        suspend fun requestPermission(activity: Activity) =
            suspendCoroutine<Boolean> { continuation ->

                val alertDialog: AlertDialog = activity.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Yes") { _, _ ->
                            Timber.d("User clicked OK button")
                            continuation.resume(true)
                        }
                        setNegativeButton("NO") { _, _ ->
                            Timber.d("User cancelled the dialog")
                            continuation.resumeWithException(Exception("Permission denied"))
                        }
                    }

                    builder.create()
                }

                alertDialog.let {
                    it.setMessage(activity.resources.getString(R.string.request_permission))
                    it.setCancelable(false)
                    it.show()
                }
            }
    }
}
