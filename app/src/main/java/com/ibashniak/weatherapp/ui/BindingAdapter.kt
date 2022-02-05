package com.ibashniak.weatherapp.ui

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:weatherIcon")
fun loadImage(view: ImageView?, pathToIcon: String?) {
    if (pathToIcon !== null && view != null) {
        view.setImageBitmap(BitmapFactory.decodeFile(pathToIcon))
    }
}

@BindingAdapter("app:isLoading")
fun isLoading(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("app:isNetworkMissed")
fun isNetworkMissed(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("app:rotation")
fun rotation(view: View, windDegree: Float) {
    val CIRCLE = 360
    val DURATION = 1500L
    view.rotation = 0F
    view.animate().rotation(CIRCLE + windDegree).duration = DURATION
}
