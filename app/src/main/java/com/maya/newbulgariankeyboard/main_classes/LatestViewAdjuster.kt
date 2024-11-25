
package com.maya.newbulgariankeyboard.main_classes

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewFlipper

class LatestViewAdjuster : ViewFlipper {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow()
        } catch (e: IllegalArgumentException) {
            stopFlipping()
        }
    }
}
