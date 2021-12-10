package com.ibashniak.weatherapp.ui

import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:weatherIcon")
fun loadImage(view: ImageView?, pathToIcon: String?) {
    if (pathToIcon !== null && view != null) {
        view.setImageBitmap(BitmapFactory.decodeFile(pathToIcon))
    }
}
