package com.maya.newbulgariankeyboard.popups

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.LinearLayout
import com.maya.newbulgariankeyboard.databinding.KeyPopupViewLayoutBinding
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2
import com.maya.newbulgariankeyboard.main_utils.setImageTintColor2

class LatestClickedKeyView : LinearLayout {
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private lateinit var binding: KeyPopupViewLayoutBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding = KeyPopupViewLayoutBinding.bind(this)
    }

    override fun onDraw(canvas: Canvas) {
        setBackgroundTintColor2(
            this,
            prefs.mThemingApp.keyPopupBgColor
        )
        binding.symbol.setTextColor(prefs.mThemingApp.keyPopupFgColor)
        setImageTintColor2(
            binding.threedots,
            prefs.mThemingApp.keyPopupFgColor
        )
        super.onDraw(canvas)
    }
}
