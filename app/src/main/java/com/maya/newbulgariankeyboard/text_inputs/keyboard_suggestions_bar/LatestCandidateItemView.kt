package com.maya.newbulgariankeyboard.text_inputs.keyboard_suggestions_bar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2

class LatestCandidateItemView : androidx.appcompat.widget.AppCompatImageButton {
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        updateTheme()
        elevation = 0.0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        updateTheme()
        super.onDraw(canvas)
    }

    private fun updateTheme() {
        setBackgroundTintColor2(
            this,
            prefs.mThemingApp.smartbarButtonBgColor
        )
        setColorFilter(prefs.mThemingApp.smartbarButtonFgColor)
        elevation = 0.0f /*it worked with 1*/
    }
}
