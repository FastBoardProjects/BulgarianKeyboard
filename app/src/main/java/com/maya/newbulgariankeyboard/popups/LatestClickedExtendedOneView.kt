
package com.maya.newbulgariankeyboard.popups

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2

@SuppressLint("ViewConstructor")
class LatestClickedExtendedOneView(
    context: Context, val adjustedIndex: Int, var isActive: Boolean = false
) : androidx.appcompat.widget.AppCompatTextView(
    context, null, 0
) {
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    var iconDrawable: Drawable? = null

    init {
        background = getDrawable(context, R.drawable.bg_shape_keyboard_key)
    }

    override fun onDraw(canvas: Canvas) {
        setBackgroundTintColor2(
            this, when {
                isActive -> prefs.mThemingApp.keyPopupBgColorActive
                else -> Color.TRANSPARENT
            }
        )
        setTextColor(prefs.mThemingApp.keyPopupFgColor)

        super.onDraw(canvas)

        canvas

        val drawable = iconDrawable
        val drawablePadding = (0.2f * measuredHeight).toInt()
        if (drawable != null) {
            var marginV = 0
            var marginH = 0
            if (measuredWidth > measuredHeight) {
                marginH = (measuredWidth - measuredHeight) / 2
            } else {
                marginV = (measuredHeight - measuredWidth) / 2
            }
            drawable.setBounds(
                marginH + drawablePadding,
                marginV + drawablePadding,
                measuredWidth - marginH - drawablePadding,
                measuredHeight - marginV - drawablePadding
            )
            drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                prefs.mThemingApp.keyPopupFgColor,
                BlendModeCompat.SRC_ATOP
            )
            drawable.draw(canvas)
        }
    }
}
