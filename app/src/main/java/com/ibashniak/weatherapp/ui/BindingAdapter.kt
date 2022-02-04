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
