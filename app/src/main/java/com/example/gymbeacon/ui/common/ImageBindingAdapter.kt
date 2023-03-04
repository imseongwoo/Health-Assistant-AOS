package com.example.gymbeacon.ui.common

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.gymbeacon.GlideApp

class ImageBindingAdapter {

    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, imageUrl: String?) {
        GlideApp.with(view)
            .load(imageUrl)
            .into(view)

    }
}