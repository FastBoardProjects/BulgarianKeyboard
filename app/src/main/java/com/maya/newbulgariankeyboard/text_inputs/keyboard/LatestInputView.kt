package com.maya.newbulgariankeyboard.text_inputs.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.popups.LatestClickedKeyManager
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGesturesAction
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestSwippingGesturring
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyLayout
import com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts.ComputedLayoutData
import kotlin.math.roundToInt


class LatestInputView : LinearLayout, LatestKeyboardService.EventListener,
    LatestSwippingGesturring.Listener {

    private var activeLatestSingleKeyLayout: LatestSingleKeyLayout? = null
    private var activePointerId: Int? = null
    private var activeX: Float = 0.0f
    private var activeY: Float = 0.0f
    private val TAG = "AppInputView:"

    var computedLayout: ComputedLayoutData? = null
        set(v) {
            field = v
            buildLayout()
        }
    var desiredKeyWidth: Int = resources.getDimension(R.dimen.single_key_width).toInt()
    var desiredKeyHeight: Int = resources.getDimension(R.dimen.single_key_height).toInt()
    var latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private var initialKeyCode: Int = 0
    var isPreviewMode: Boolean = false
    var isSmartbarKeyboardView: Boolean = false
    var popupManager = LatestClickedKeyManager<LatestInputView, LatestSingleKeyLayout>(this)
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private val swipeGestureDetector = LatestSwippingGesturring.AppGestureDetector(context, this)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        Log.d(TAG, " constructor")
        orientation = VERTICAL
        layoutParams = layoutParams ?: FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        try {
            latestKeyboardService?.addEventListener(this)
        } catch (e: Exception) {
        }
        onWindowShown()
    }

    private fun buildLayout() {
        Log.d(TAG, " buildLayout")
        destroyLayout()
        val computedLayout = computedLayout ?: return
        for (row in computedLayout.arrangement) {
            val rowView = LatestInputHorizontalView(context)
            for (key in row) {
                val keyView = LatestSingleKeyLayout(this, key)
                keyView.latestKeyboardService = latestKeyboardService
                rowView.addView(keyView)
                //Log.d(TAG, " key: ${key.code} , Row: ${row} ,Row View: ${rowView}")
                Log.d(TAG, " keyView: ${keyView.datingLatestSingle}")
            }
            addView(rowView)
        }
    }

    private fun destroyLayout() {
        removeAllViews()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        popupManager.dismissAllPopups()
    }

    override fun onWindowShown() {
        Log.d(TAG, " onWindowShown")
        swipeGestureDetector.apply {
            latestGestureDistanceValues = prefs.mAppGestures.swipeLatestGestureDistanceValues
            latestSpeedHelperGesture = prefs.mAppGestures.swipeLatestSpeedHelperGesture
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (isPreviewMode) {
            return false
        }
        val MotionEventKeyboard = MotionEvent.obtainNoHistory(event)
        if (!isSmartbarKeyboardView && swipeGestureDetector.onTouchEvent(event)) {
            sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_CANCEL)
            activeLatestSingleKeyLayout = null
            activePointerId = null
            return true
        }
        val pointerIndex = event.actionIndex
        var pointerId = event.getPointerId(pointerIndex)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (activePointerId == null) {
                    activePointerId = pointerId
                    activeX = event.getX(pointerIndex)
                    activeY = event.getY(pointerIndex)
                    searchForActiveKeyView()
                    initialKeyCode = activeLatestSingleKeyLayout?.datingLatestSingle?.code ?: 0
                    sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_DOWN)
                } else if (activePointerId != pointerId) {
                    // New pointer arrived. Send ACTION_UP to current active view and move on
                    sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_UP)
                    activePointerId = pointerId
                    activeX = event.getX(pointerIndex)
                    activeY = event.getY(pointerIndex)
                    searchForActiveKeyView()
                    initialKeyCode = activeLatestSingleKeyLayout?.datingLatestSingle?.code ?: 0
                    sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_DOWN)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (index in 0 until event.pointerCount) {
                    pointerId = event.getPointerId(index)
                    if (activePointerId == pointerId) {
                        activeX = event.getX(index)
                        activeY = event.getY(index)
                        if (activeLatestSingleKeyLayout == null) {
                            searchForActiveKeyView()
                            sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_DOWN)
                        } else {
                            sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_MOVE)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_POINTER_UP -> {
                if (activePointerId == pointerId) {
                    sendSpecificTouchEvent(MotionEventKeyboard, MotionEvent.ACTION_UP)
                    activeLatestSingleKeyLayout = null
                    activePointerId = null
                }
            }
            else -> return false
        }
        MotionEventKeyboard.recycle()
        return true
    }

    private fun sendSpecificTouchEvent(event: MotionEvent, actionParam: Int) {
        val keyView = activeLatestSingleKeyLayout ?: return
        val keyViewParent = keyView.parent as ViewGroup
        keyView.onSpecificTouchEvent(event.apply {
            action = when (actionParam) {
                MotionEvent.ACTION_POINTER_DOWN -> MotionEvent.ACTION_DOWN
                MotionEvent.ACTION_POINTER_UP -> MotionEvent.ACTION_UP
                else -> actionParam
            }
            setLocation(
                activeX - keyViewParent.x - keyView.x,
                activeY - keyViewParent.y - keyView.y
            )
        })
    }


    override fun onSwipe(
        direction: LatestSwippingGesturring.Direction,
        type: LatestSwippingGesturring.Type
    ): Boolean {
        return when {
            initialKeyCode == LatestSingleKeyCoding.DELETE -> {
                if (type == LatestSwippingGesturring.Type.TOUCH_UP && direction == LatestSwippingGesturring.Direction.LEFT &&
                    prefs.mAppGestures.deleteKeyLatestGesturesLeft == LatestGesturesAction.DELETE_WORD
                ) {
                    latestKeyboardService?.executeSwipeAction(prefs.mAppGestures.deleteKeyLatestGesturesLeft)
                    true
                } else {
                    false
                }
            }
            initialKeyCode > LatestSingleKeyCoding.SPACE && !popupManager.isShowingExtendedPopup -> when {
                !prefs.mAppGlidingTyping.enabled -> when (type) {
                    LatestSwippingGesturring.Type.TOUCH_UP -> {
                        val swipeAction = when (direction) {
                            LatestSwippingGesturring.Direction.UP -> prefs.mAppGestures.latestGesturesUp
                            LatestSwippingGesturring.Direction.DOWN -> prefs.mAppGestures.latestGesturesDown
                            LatestSwippingGesturring.Direction.LEFT -> prefs.mAppGestures.latestGesturesLeft
                            LatestSwippingGesturring.Direction.RIGHT -> prefs.mAppGestures.latestGesturesRight
                            else -> LatestGesturesAction.NO_ACTION
                        }
                        if (swipeAction != LatestGesturesAction.NO_ACTION) {
                            latestKeyboardService?.executeSwipeAction(swipeAction)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
                else -> false
            }
            else -> false
        }
    }


    private fun searchForActiveKeyView() {
        loop@ for (row in children) {
            if (row is FlexboxLayout) {
                for (keyView in row.children) {
                    if (keyView is LatestSingleKeyLayout) {
                        if (keyView.touchHitBox.contains(activeX.toInt(), activeY.toInt())) {
                            activeLatestSingleKeyLayout = keyView
                            break@loop
                        }
                    }
                }
            }
        }
    }


    fun dismissActiveKeyViewReference() {
        activeLatestSingleKeyLayout?.onSpecificTouchEvent(
            MotionEvent.obtain(
                0, 0, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0
            )
        )
        activeLatestSingleKeyLayout = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val keyMarginH = resources.getDimension((R.dimen.single_key_margin_flat)).toInt()
        val keyMarginV = resources.getDimension((R.dimen.single_key_margin_stand)).toInt()

        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        desiredKeyWidth = if (isSmartbarKeyboardView) {
            (desiredWidth / 6.0f - 2.0f * keyMarginH).roundToInt()
        } else {
            (desiredWidth / 10.0f - 2.0f * keyMarginH).roundToInt()
        }
        val desiredHeight = if (isSmartbarKeyboardView || isPreviewMode) {
            MeasureSpec.getSize(heightMeasureSpec).toFloat()
        } else {
            (latestKeyboardService?.latestKeyboardView?.desiredTextKeyboardViewHeight
                ?: MeasureSpec.getSize(
                    heightMeasureSpec
                ).toFloat())
        } * if (isPreviewMode) {
            0.90f
        } else {
            1.00f
        }
        desiredKeyHeight = when {
            isSmartbarKeyboardView -> desiredHeight - 1.5f * keyMarginV
            else -> desiredHeight / (computedLayout?.arrangement?.size?.toFloat()
                ?: 4.0f) - 2.0f * keyMarginV
        }.roundToInt()

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(desiredWidth.roundToInt(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(desiredHeight.roundToInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onApplyThemeAttributes() {
        if (isPreviewMode) {
            setBackgroundColor(prefs.mThemingApp.keyboardBgColor)
            /*doubt for my theme*/
        }
    }

    fun requestLayoutAllKeys() {
        for (row in children) {
            if (row is FlexboxLayout) {
                for (keyView in row.children) {
                    if (keyView is LatestSingleKeyLayout) {
                        keyView.requestLayout()
                    }
                }
            }
        }
    }


    fun invalidateAllKeys() {
        for (row in children) {
            if (row is FlexboxLayout) {
                for (keyView in row.children) {
                    if (keyView is LatestSingleKeyLayout) {
                        keyView.invalidate()
                    }
                }
            }
        }
    }


    fun updateVisibility() {
        for (row in children) {
            if (row is FlexboxLayout) {
                for (keyView in row.children) {
                    if (keyView is LatestSingleKeyLayout) {
                        keyView.updateVisibility()
                    }
                }
            }
        }
    }
}