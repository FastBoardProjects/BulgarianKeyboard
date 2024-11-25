
package com.maya.newbulgariankeyboard.text_inputs.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.maya.newbulgariankeyboard.R


class LatestInputHorizontalView(context: Context) : FlexboxLayout(context) {
    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            setMargins(
                resources.getDimension(R.dimen.row_key_margin_horizontal).toInt(), 0,
                resources.getDimension(R.dimen.row_key_margin_horizontal).toInt(), 0
            )
        }
        flexDirection = FlexDirection.ROW
        justifyContent = JustifyContent.CENTER
        setPadding(0, 0, 0, 0)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}
