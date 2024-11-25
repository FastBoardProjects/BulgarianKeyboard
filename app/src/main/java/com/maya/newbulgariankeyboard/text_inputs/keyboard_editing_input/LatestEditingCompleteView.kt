
package com.maya.newbulgariankeyboard.text_inputs.keyboard_editing_input

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2
import kotlin.math.roundToInt


class LatestEditingCompleteView : ConstraintLayout, LatestKeyboardService.EventListener {

    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private var arrowUpItemLatest: LatestEditingItemView? = null
    private var arrowDownItemLatest: LatestEditingItemView? = null
    private var selectItemLatest: LatestEditingItemView? = null
    private var selectAllItemLatest: LatestEditingItemView? = null
    private var cutItemLatest: LatestEditingItemView? = null
    private var copyItemLatest: LatestEditingItemView? = null
    private var pasteItemLatest: LatestEditingItemView? = null

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

        arrowUpItemLatest = findViewById(R.id.arrow_up)
        arrowDownItemLatest = findViewById(R.id.arrow_down)
        selectItemLatest = findViewById(R.id.select)
        selectAllItemLatest = findViewById(R.id.select_all)
        cutItemLatest = findViewById(R.id.clipboard_cut)
        copyItemLatest = findViewById(R.id.clipboard_copy)
        pasteItemLatest = findViewById(R.id.clipboard_paste)
    }

    override fun onUpdateSelection() {
        val isSelectionActive =
            latestKeyboardService?.activeEditorInstance?.selection?.isSelectionMode ?: false
        val isSelectionMode = latestKeyboardService?.latestInputHelper?.isManualSelectionMode ?: false
        arrowUpItemLatest?.isEnabled = !(isSelectionActive || isSelectionMode)
        arrowDownItemLatest?.isEnabled = !(isSelectionActive || isSelectionMode)
        selectItemLatest?.isHighlighted = isSelectionActive || isSelectionMode
        copyItemLatest?.isEnabled = isSelectionActive
        pasteItemLatest?.isEnabled = latestKeyboardService?.clipboardManager?.hasPrimaryClip() ?: false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                (latestKeyboardService?.latestKeyboardView?.desiredTextKeyboardViewHeight ?: 0.0f).coerceAtMost(
                    heightSize
                )
            }
            else -> {
                latestKeyboardService?.latestKeyboardView?.desiredTextKeyboardViewHeight ?: 0.0f
            }
        }

        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height.roundToInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setBackgroundTintColor2(
            this,
            prefs.mThemingApp.smartbarBgColor
        )
    }
}
