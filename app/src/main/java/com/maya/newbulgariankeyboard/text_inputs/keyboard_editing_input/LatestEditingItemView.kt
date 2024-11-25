package com.maya.newbulgariankeyboard.text_inputs.keyboard_editing_input

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.widget.AppCompatImageButton
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate


class LatestEditingItemView : AppCompatImageButton {

    private val TAG = "EditingKeyView:"
    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private val datingLatestSingle: LatestSingleKeyDating
    private var isKeyPressed: Boolean = false
    private val mainScope = MainScope()
    private var osTimer: Timer? = null

    private var label: String? = null
    private var labelPaint: Paint = Paint().apply {
        alpha = 255
        color = 0
        isAntiAlias = true
        isFakeBoldText = false
        textAlign = Paint.Align.CENTER
        textSize = Button(context).textSize
        typeface = Typeface.DEFAULT
    }

    var isHighlighted: Boolean = false
        set(value) {
            field = value; invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.style.EditingViewItemStyle
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        Log.d(TAG, "constructor: $id")
        val code = when (id) {
            R.id.arrow_down -> LatestSingleKeyCoding.ARROW_DOWN
            R.id.arrow_left -> LatestSingleKeyCoding.ARROW_LEFT
            R.id.arrow_right -> LatestSingleKeyCoding.ARROW_RIGHT
            R.id.arrow_up -> LatestSingleKeyCoding.ARROW_UP
            R.id.backspace -> LatestSingleKeyCoding.DELETE
            R.id.clipboard_copy -> LatestSingleKeyCoding.CLIPBOARD_COPY
            R.id.clipboard_cut -> LatestSingleKeyCoding.CLIPBOARD_CUT
            R.id.clipboard_paste -> LatestSingleKeyCoding.CLIPBOARD_PASTE
            R.id.move_home -> LatestSingleKeyCoding.MOVE_HOME
            R.id.move_end -> LatestSingleKeyCoding.MOVE_END
            R.id.select -> LatestSingleKeyCoding.CLIPBOARD_SELECT
            R.id.select_all -> LatestSingleKeyCoding.CLIPBOARD_SELECT_ALL
            else -> 0
        }
        datingLatestSingle = LatestSingleKeyDating(code)
        context.obtainStyledAttributes(attrs, R.styleable.AttrEditingKeyView).apply {
            label = getString(R.styleable.AttrEditingKeyView_android_text)
            recycle()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled || event == null) {
            return false
        }
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isKeyPressed = true
                latestKeyboardService?.keyPressVibrate()
                latestKeyboardService?.keyPressSound(datingLatestSingle)
                when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.ARROW_DOWN,
                    LatestSingleKeyCoding.ARROW_LEFT,
                    LatestSingleKeyCoding.ARROW_RIGHT,
                    LatestSingleKeyCoding.ARROW_UP,
                    LatestSingleKeyCoding.DELETE -> {
                        osTimer = Timer()
                        osTimer?.scheduleAtFixedRate(500, 50) {
                            mainScope.launch(Dispatchers.Main) {
                                Log.d(TAG, "onTouchEvent1: $datingLatestSingle")
                                latestKeyboardService?.latestInputHelper?.sendKeyPress(datingLatestSingle)
                            }
                            if (!isKeyPressed) {
                                osTimer?.cancel()
                                osTimer = null
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isKeyPressed = false
                osTimer?.cancel()
                osTimer = null
                if (event.actionMasked != MotionEvent.ACTION_CANCEL) {
                    Log.d(TAG, "onTouchEvent2: $datingLatestSingle")
                    latestKeyboardService?.latestInputHelper?.sendKeyPress(datingLatestSingle)
                }
            }
            else -> return false
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas

        imageTintList = ColorStateList.valueOf(
            when {
                isEnabled -> prefs.mThemingApp.smartbarFgColor
                else -> prefs.mThemingApp.smartbarFgColor
            }
        )

        val label = label
        if (label != null) {
            labelPaint.color = if (isHighlighted && isEnabled) {
                prefs.mThemingApp.colorPrimary
            } else if (!isEnabled) {
                prefs.mThemingApp.smartbarFgColor
            } else {
                prefs.mThemingApp.smartbarFgColor
            }
            val isPortrait =
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            if (!isPortrait) {
                labelPaint.textSize *= 0.9f
            }
            val centerX = measuredWidth / 2.0f
            val centerY = measuredHeight / 2.0f + (labelPaint.textSize - labelPaint.descent()) / 2
            if (label.contains("\n")) {
                val labelLines = label.split("\n")
                canvas.drawText(labelLines[0], centerX, centerY * 0.70f, labelPaint)
                canvas.drawText(labelLines[1], centerX, centerY * 1.30f, labelPaint)
            } else {
                canvas.drawText(label, centerX, centerY, labelPaint)
            }
        }
    }
}
