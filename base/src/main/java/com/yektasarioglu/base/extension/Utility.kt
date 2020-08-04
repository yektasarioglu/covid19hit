package com.yektasarioglu.base.extension

import android.widget.*

import androidx.core.content.ContextCompat

val Any.TAG: String
    get() = javaClass.simpleName

var ImageView.tint: Int?
    get() { return 0 }
    set(value) {
        if (value != null) setColorFilter(ContextCompat.getColor(context, value))
        else colorFilter = null
    }

//fun Button.onClickListener(listener: (Button) -> Unit) = setOnClickListener { listener(it as Button) }
fun ImageView.onClickListener(listener: (ImageView) -> Unit) = setOnClickListener { listener(it as ImageView) }
fun TextView.onClickListener(listener: (TextView) -> Unit) = setOnClickListener { listener(it as TextView) }
fun RelativeLayout.onClickListener(listener: (RelativeLayout) -> Unit) = setOnClickListener { listener(it as RelativeLayout) }
fun LinearLayout.onClickListener(listener: (LinearLayout) -> Unit) = setOnClickListener { listener(it as LinearLayout) }