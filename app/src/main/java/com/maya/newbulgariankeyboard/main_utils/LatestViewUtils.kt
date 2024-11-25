package com.maya.newbulgariankeyboard.main_utils

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.children


fun setBackgroundTintColor2(view: View, colorInt: Int) {
    view.backgroundTintList = ColorStateList.valueOf(colorInt)
}


fun setDrawableTintColor2(view: Button, colorInt: Int) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        view.compoundDrawableTintList = ColorStateList.valueOf(colorInt)
    }
}

fun setImageTintColor2(view: ImageView, colorInt: Int) {
    view.imageTintList = ColorStateList.valueOf(colorInt)
}


fun refreshLayoutOf(view: View?) {
    if (view is ViewGroup) {
        view.invalidate()
        view.requestLayout()
        for (childView in view.children) {
            refreshLayoutOf(childView)
        }
    } else {
        view?.invalidate()
        view?.requestLayout()
    }
}
