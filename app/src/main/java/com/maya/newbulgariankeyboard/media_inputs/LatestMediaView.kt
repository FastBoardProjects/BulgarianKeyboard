
package com.maya.newbulgariankeyboard.media_inputs

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import kotlin.math.roundToInt

class LatestMediaView : LinearLayout, LatestKeyboardService.EventListener {
    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)

    var tabLayout: TabLayout? = null
        private set
    var switchToTextInputButton: ImageButton? = null
        private set
    var backspaceButton: ImageButton? = null
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        try {
            latestKeyboardService?.addEventListener(this)
        } catch (e: Exception) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        tabLayout = findViewById(R.id.media_input_tabs)
        switchToTextInputButton = findViewById(R.id.media_input_switch_to_text_input_button)
        backspaceButton = findViewById(R.id.media_input_backspace_button)
        onApplyThemeAttributes()
    }

    override fun onApplyThemeAttributes() {
        tabLayout?.setTabTextColors(prefs.mThemingApp.mediaFgColor, prefs.mThemingApp.mediaFgColor)
        tabLayout?.tabIconTint = ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor)
        tabLayout?.setSelectedTabIndicatorColor(prefs.mThemingApp.mediaFgColor)
        switchToTextInputButton?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor)
        backspaceButton?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = latestKeyboardService?.latestKeyboardView?.desiredMediaKeyboardViewHeight ?: 0.0f
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height.roundToInt(), MeasureSpec.EXACTLY)
        )
    }
}
