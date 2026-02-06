package com.utfpr.appapis.ui

import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.utfpr.appapis.R

fun ImageView.loadUrl(url: String) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        .transform(CircleTransform())
        .into(this)
}