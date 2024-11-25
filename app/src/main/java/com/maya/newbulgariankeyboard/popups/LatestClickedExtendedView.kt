
package com.maya.newbulgariankeyboard.popups

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.google.android.flexbox.FlexboxLayout
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2

class LatestClickedExtendedView : FlexboxLayout {
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        setBackgroundTintColor2(
            this,
            prefs.mThemingApp.keyPopupBgColor
        )
        super.onDraw(canvas)
    }
}
