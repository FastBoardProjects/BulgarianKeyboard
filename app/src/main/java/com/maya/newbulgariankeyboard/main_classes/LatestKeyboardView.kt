package com.maya.newbulgariankeyboard.main_classes

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.widget.LinearLayout
import android.widget.ViewFlipper
import com.maya.newbulgariankeyboard.BuildConfig
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestViewLayoutUtils
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import kotlin.math.roundToInt

class LatestKeyboardView : LinearLayout {
    private var latestKeyboardService: LatestKeyboardService = LatestKeyboardService.getInstance()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)

    var desiredInputViewHeight: Float = resources.getDimension(R.dimen.keyboard_actual_height)
        private set
    var desiredSmartbarHeight: Float = resources.getDimension(R.dimen.keyboard_suggestions_height)
        private set
    var desiredTextKeyboardViewHeight: Float =
        resources.getDimension(R.dimen.text_keyboard_actual_height)
        private set
    var desiredMediaKeyboardViewHeight: Float =
        resources.getDimension(R.dimen.media_keyboard_actual_height)
        private set

    var mainViewFlipper: ViewFlipper? = null
        private set
    var oneHandedCtrlPanelStart: LinearLayout? = null
        private set
    var oneHandedCtrlPanelEnd: LinearLayout? = null
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        if (BuildConfig.DEBUG) Log.i(this::class.simpleName, "onAttachedToWindow()")
        super.onAttachedToWindow()
        mainViewFlipper = findViewById(R.id.main_view_flipper)
        oneHandedCtrlPanelStart = findViewById(R.id.one_handed_ctrl_panel_start)
        oneHandedCtrlPanelEnd = findViewById(R.id.one_handed_ctrl_panel_end)
        latestKeyboardService.registerInputView(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightFactor = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> 1.0f
            else -> if (prefs.mAppKeyboard.oneHandedMode != "off") {
                1.0f
            } else {
                1.0f
            }
        } * when (prefs.mAppKeyboard.heightFactor) {
            "extra_short" -> 0.90f
            "short" -> 1.00f
            "mid_short" -> 1.05f
            "normal" -> 1.10f
            "mid_tall" -> 1.15f
            "tall" -> 1.20f
            "extra_tall" -> 1.25f
            "custom" -> prefs.mAppKeyboard.heightFactorCustom.toFloat() / 100.0f
            else -> 1.00f
        }
        var baseHeight = calcInputViewHeight() * heightFactor
        var baseSmartbarHeight = 0.16129f * baseHeight
        var baseTextInputHeight = baseHeight - baseSmartbarHeight
        val tim = latestKeyboardService.latestInputHelper
        val shouldGiveAdditionalSpace = prefs.mAppKeyboard.numberRow &&
                !(tim.getActiveKeyboardMode() == LatestInputMode.NUMERIC ||
                        tim.getActiveKeyboardMode() == LatestInputMode.PHONE ||
                        tim.getActiveKeyboardMode() == LatestInputMode.PHONE2)
        if (shouldGiveAdditionalSpace) {
            val additionalHeight = desiredTextKeyboardViewHeight * 0.18f
            baseHeight += additionalHeight
            baseTextInputHeight += additionalHeight
        }
        val smartbarDisabled = !prefs.mAppSuggestions.enabled ||
                tim.getActiveKeyboardMode() == LatestInputMode.NUMERIC ||
                tim.getActiveKeyboardMode() == LatestInputMode.PHONE ||
                tim.getActiveKeyboardMode() == LatestInputMode.PHONE2
        if (smartbarDisabled) {
            baseHeight = baseTextInputHeight
            baseSmartbarHeight = 0.0f
        }
        desiredInputViewHeight = baseHeight

        /*todo Change*/
        /*desiredSmartbarHeight = baseSmartbarHeight*/
        desiredTextKeyboardViewHeight = baseTextInputHeight
        desiredMediaKeyboardViewHeight = baseHeight
        baseHeight += LatestViewLayoutUtils.convertDpToPixel(
            latestKeyboardService.prefs.mAppKeyboard.bottomOffset.toFloat(),
            context
        )

        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(baseHeight.roundToInt(), MeasureSpec.EXACTLY)
        )
    }

    private fun calcInputViewHeight(): Float {
        val dm: DisplayMetrics = resources.displayMetrics
        val minBaseSize: Float = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> resources.getFraction(
                R.fraction.input_view_height_percentage_min, dm.heightPixels, dm.heightPixels
            )
            else -> resources.getFraction(
                R.fraction.input_view_height_percentage_min, dm.widthPixels, dm.widthPixels
            )
        }
        val maxBaseSize: Float = resources.getFraction(
            R.fraction.input_view_height_percentage_max, dm.heightPixels, dm.heightPixels
        )
        return ((minBaseSize + maxBaseSize) / 2.0f).coerceAtLeast(
            resources.getDimension(R.dimen.keyboard_actual_height)
        )
    }

}
