package com.maya.newbulgariankeyboard.text_inputs.keyboard_keys

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.ImeOptions
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGesturesAction
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestSwippingGesturring
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

@SuppressLint("ViewConstructor")
class LatestSingleKeyLayout(

    private val latestInputView: LatestInputView,
    val datingLatestSingle: LatestSingleKeyDating
) : View(latestInputView.context), LatestSwippingGesturring.Listener {
    val datingPopupWithHintLatestSingle: MutableList<LatestSingleKeyDating>
    private var isKeyPressed: Boolean = false
        set(value) {
            field = value
            updateKeyPressedBackground()
        }
    private var hasTriggeredGestureMove: Boolean = false
    private val mainScope = MainScope()
    private var osHandler: Handler? = null
    private var osTimer: Timer? = null
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private var shouldBlockNextKeyCode: Boolean = false

    private var desiredWidth: Int = 0
    private var desiredHeight: Int = 0
    private var drawable: Drawable? = null
    private var drawableColor: Int = 0
    private var drawablePaddingH: Int = 0
    private var drawablePaddingV: Int = 0
    private var label: String? = null
    private var labelPaint: Paint = Paint().apply {
        alpha = 255
        color = 0
        isAntiAlias = true
        isFakeBoldText = false
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.single_key_text_size)
        typeface = Typeface.DEFAULT
    }
    private var hintedLabel: String? = null
    private var hintedLabelPaint: Paint = Paint().apply {
        alpha = 120
        color = 0
        isAntiAlias = true
        isFakeBoldText = false
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.single_key_text_size_hint)
        typeface = Typeface.DEFAULT
    }
    private val tempRect: Rect = Rect()
    var latestKeyboardService: LatestKeyboardService? = null
    private val swipeGestureDetector = LatestSwippingGesturring.AppGestureDetector(context, this)
    var touchHitBox: Rect = Rect(-1, -1, -1, -1)

    init {
        layoutParams = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                resources.getDimension((R.dimen.single_key_margin_flat)).toInt(),
                resources.getDimension(R.dimen.single_key_margin_stand).toInt(),
                resources.getDimension((R.dimen.single_key_margin_flat)).toInt(),
                resources.getDimension(R.dimen.single_key_margin_stand).toInt()
            )
            flexShrink = when (latestInputView.computedLayout?.mode) {
                LatestInputMode.NUMERIC,
                LatestInputMode.NUMERIC_ADVANCED,
                LatestInputMode.PHONE,
                LatestInputMode.PHONE2 -> 1.0f
                else -> when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.SHIFT,
                    LatestSingleKeyCoding.VIEW_CHARACTERS,
                    LatestSingleKeyCoding.VIEW_SYMBOLS,
                    LatestSingleKeyCoding.VIEW_SYMBOLS2,
                    LatestSingleKeyCoding.DELETE,
                    LatestSingleKeyCoding.SPACE,
                    LatestSingleKeyCoding.ENTER -> 0.0f
                    else -> 1.0f
                }
            }
            flexGrow = when (latestInputView.computedLayout?.mode) {
                LatestInputMode.NUMERIC,
                LatestInputMode.PHONE,
                LatestInputMode.PHONE2 -> 0.0f
                LatestInputMode.NUMERIC_ADVANCED -> when (datingLatestSingle.type) {
                    KeyType.NUMERIC -> 1.0f
                    else -> 0.0f
                }
                else -> when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.SPACE -> 1.0f
                    else -> 0.0f
                }
            }
        }
        setPadding(0, 0, 0, 0)

        background = getDrawable(context, R.drawable.bg_shape_keyboard_key)
        elevation = 0.0f

        var hintLatestSingleKeyDating: LatestSingleKeyDating? = null
        val hintedNumber = datingLatestSingle.hintedNumber
        if (prefs.mAppKeyboard.hintedNumberRow && hintedNumber != null) {
            hintLatestSingleKeyDating = hintedNumber
        }
        val hintedSymbol = datingLatestSingle.hintedSymbol
        if (prefs.mAppKeyboard.hintedSymbols && hintedSymbol != null) {
            hintLatestSingleKeyDating = hintedSymbol
        }
        datingPopupWithHintLatestSingle = if (hintLatestSingleKeyDating == null) {
            datingLatestSingle.popup.toMutableList()
        } else {
            val popupList = datingLatestSingle.popup.toMutableList()
            popupList.add(hintLatestSingleKeyDating)
            popupList
        }
        updateKeyPressedBackground()
    }

    fun getComputedLetter(latestSingleKeyDating: LatestSingleKeyDating = datingLatestSingle): String {
        if (latestSingleKeyDating.code == LatestSingleKeyCoding.URI_COMPONENT_TLD) {
            return when (latestKeyboardService?.latestInputHelper?.caps) {
                true -> latestSingleKeyDating.label.uppercase(Locale.getDefault())
                else -> latestSingleKeyDating.label.lowercase(Locale.getDefault())
            }
        }


        val label = String(Character.toChars(latestSingleKeyDating.code))

        return when {
            latestKeyboardService?.latestInputHelper?.caps
                ?: false -> label.uppercase(Locale.getDefault())
            else -> label
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    fun onSpecificTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !isEnabled) return false
        if (swipeGestureDetector.onTouchEvent(event)) {
            isKeyPressed = false
            osHandler?.removeCallbacksAndMessages(null)
            osTimer?.cancel()
            osTimer = null
            latestInputView.popupManager.hide()
            return true
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                hasTriggeredGestureMove = false
                shouldBlockNextKeyCode = false
                latestKeyboardService?.prefs?.mAppKeyboard?.let {
                    if (it.popupEnabled) {
                        try {
                            latestInputView.popupManager.show(this)
                        } catch (e: Exception) {
                        }
                    }
                }
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
                                latestKeyboardService?.latestInputHelper?.sendKeyPress(
                                    datingLatestSingle
                                )
                            }
                            if (!isKeyPressed) {
                                osTimer?.cancel()
                                osTimer = null
                            }
                        }
                    }
                }
                val delayMillis = prefs.mAppKeyboard.longPressDelay
                if (osHandler == null) {
                    osHandler = Handler()
                }
                osHandler?.postDelayed({
                    if (datingPopupWithHintLatestSingle.isNotEmpty()) {
                        try {
                            latestInputView.popupManager.extend(this)
                        } catch (e: Exception) {
                        }
                    }
                    if (datingLatestSingle.code == LatestSingleKeyCoding.SPACE) {
                        latestKeyboardService?.latestInputHelper?.sendKeyPress(
                            LatestSingleKeyDating(
                                LatestSingleKeyCoding.SHOW_INPUT_METHOD_PICKER,
                                type = KeyType.FUNCTION
                            )
                        )
                        shouldBlockNextKeyCode = true
                    }
                }, delayMillis.toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (latestInputView.popupManager.isShowingExtendedPopup) {
                    val isPointerWithinBounds =
                        latestInputView.popupManager.propagateMotionEvent(this, event)
                    if (!isPointerWithinBounds && !shouldBlockNextKeyCode) {
                        latestInputView.dismissActiveKeyViewReference()
                    }
                } else {
                    val parent = parent as ViewGroup
                    if ((event.x < -0.1f * measuredWidth && parent.children.first() != this)
                        || (event.x > 1.1f * measuredWidth && parent.children.last() != this)
                        || event.y < -0.35f * measuredHeight
                        || event.y > 1.35f * measuredHeight
                    ) {
                        if (!shouldBlockNextKeyCode) {
                            latestInputView.dismissActiveKeyViewReference()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isKeyPressed = false
                osHandler?.removeCallbacksAndMessages(null)
                osTimer?.cancel()
                osTimer = null
                if (hasTriggeredGestureMove && datingLatestSingle.code == LatestSingleKeyCoding.DELETE) {
                    hasTriggeredGestureMove = false
                    latestKeyboardService?.activeEditorInstance?.apply {
                        if (selection.isSelectionMode) {
                            deleteBackwards()
                        }
                    }
                } else {
                    val retData = latestInputView.popupManager.getActiveKeyData(this)
                    latestInputView.popupManager.hide()
                    if (event.actionMasked != MotionEvent.ACTION_CANCEL && !shouldBlockNextKeyCode && retData != null) {
                        latestKeyboardService?.latestInputHelper?.sendKeyPress(retData)
                        performClick()
                    } else {
                        shouldBlockNextKeyCode = false
                    }
                }
            }
            else -> return false
        }
        return true
    }

    override fun onSwipe(
        direction: LatestSwippingGesturring.Direction,
        type: LatestSwippingGesturring.Type
    ): Boolean {
        return when (datingLatestSingle.code) {
            LatestSingleKeyCoding.DELETE -> when (type) {
                LatestSwippingGesturring.Type.TOUCH_MOVE -> when (direction) {
                    LatestSwippingGesturring.Direction.LEFT -> when (prefs.mAppGestures.deleteKeyLatestGesturesLeft) {
                        LatestGesturesAction.DELETE_CHARACTERS -> {
                            latestKeyboardService?.activeEditorInstance?.apply {
                                setSelection(
                                    if (selection.start > 0) {
                                        selection.start - 1
                                    } else {
                                        selection.start
                                    },
                                    selection.end
                                )
                            }
                            hasTriggeredGestureMove = true
                            shouldBlockNextKeyCode = true
                            true
                        }
                        LatestGesturesAction.DELETE_WORDS_PRECISELY -> {
                            latestKeyboardService?.activeEditorInstance?.apply {
                                leftAppendWordToSelection()
                            }

                            hasTriggeredGestureMove = true
                            shouldBlockNextKeyCode = true
                            true
                        }
                        else -> false
                    }
                    LatestSwippingGesturring.Direction.RIGHT -> when (prefs.mAppGestures.deleteKeyLatestGesturesLeft) {
                        LatestGesturesAction.DELETE_CHARACTERS -> {
                            latestKeyboardService?.activeEditorInstance?.apply {
                                setSelection(
                                    if (selection.start < selection.end) {
                                        selection.start + 1
                                    } else {
                                        selection.start
                                    },
                                    selection.end
                                )
                            }
                            shouldBlockNextKeyCode = true
                            true
                        }

                        LatestGesturesAction.DELETE_WORDS_PRECISELY -> {
                            latestKeyboardService?.activeEditorInstance?.apply {
                                leftPopWordFromSelection()
                            }
                            shouldBlockNextKeyCode = true
                            true
                        }
                        else -> false
                    }
                    else -> false
                }
                else -> false
            }
            LatestSingleKeyCoding.SPACE -> when (type) {
                LatestSwippingGesturring.Type.TOUCH_MOVE -> when (direction) {
                    LatestSwippingGesturring.Direction.LEFT -> {
                        latestKeyboardService?.executeSwipeAction(prefs.mAppGestures.spaceBarLatestGesturesLeft)
                        shouldBlockNextKeyCode = true
                        true
                    }
                    LatestSwippingGesturring.Direction.RIGHT -> {
                        latestKeyboardService?.executeSwipeAction(prefs.mAppGestures.spaceBarLatestGesturesRight)
                        shouldBlockNextKeyCode = true
                        true
                    }
                    else -> false
                }
                else -> false
            }
            else -> false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        desiredWidth = when (latestInputView.computedLayout?.mode) {
            LatestInputMode.NUMERIC,
            LatestInputMode.PHONE,
            LatestInputMode.PHONE2 -> (latestInputView.desiredKeyWidth * 2.68f).toInt()
            LatestInputMode.NUMERIC_ADVANCED -> when (datingLatestSingle.code) {
                44, 46 -> latestInputView.desiredKeyWidth
                LatestSingleKeyCoding.VIEW_SYMBOLS, 61 -> (latestInputView.desiredKeyWidth * 1.34f).toInt()
                else -> (latestInputView.desiredKeyWidth * 1.56f).toInt()
            }
            else -> when (datingLatestSingle.code) {
                LatestSingleKeyCoding.SHIFT,
                LatestSingleKeyCoding.VIEW_CHARACTERS,
                LatestSingleKeyCoding.VIEW_SYMBOLS,
                LatestSingleKeyCoding.VIEW_SYMBOLS2,
                LatestSingleKeyCoding.DELETE,
                LatestSingleKeyCoding.ENTER -> (latestInputView.desiredKeyWidth * 1.56f).toInt()
                else -> latestInputView.desiredKeyWidth
            }
        }
        desiredHeight = latestInputView.desiredKeyHeight

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                desiredWidth.coerceAtMost(widthSize)
            }
            else -> {
                desiredWidth
            }
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                desiredHeight.coerceAtMost(heightSize)
            }
            else -> {
                desiredHeight
            }
        }

        drawablePaddingH = (0.2f * width).toInt()
        drawablePaddingV = (0.2f * height).toInt()

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updateTouchHitBox()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        outlineProvider = KeyViewOutline(w, h)
    }


    private fun updateEnabledState() {
        isEnabled = when (datingLatestSingle.code) {
            LatestSingleKeyCoding.CLIPBOARD_COPY,
            LatestSingleKeyCoding.CLIPBOARD_CUT -> {
                latestKeyboardService?.activeEditorInstance?.selection?.isSelectionMode == true &&
                        latestKeyboardService?.activeEditorInstance?.isRawInputEditor == false
            }
            LatestSingleKeyCoding.CLIPBOARD_PASTE -> latestKeyboardService?.clipboardManager?.hasPrimaryClip() == true
            LatestSingleKeyCoding.CLIPBOARD_SELECT_ALL -> {
                latestKeyboardService?.activeEditorInstance?.isRawInputEditor == false
            }
            else -> true
        }
        if (!isEnabled) {
            isKeyPressed = false
        }
    }
        // todo Change Enter, Shift, Cancel, Number, Space Colors Here
    private fun updateKeyPressedBackground() {
        Log.d("KeyView", " updateKeyPressedBackground")
        when {
            latestInputView.isSmartbarKeyboardView -> {
                elevation = 0.0f
                setBackgroundTintColor2(
                    this, when {
                        isKeyPressed && isEnabled -> prefs.mThemingApp.smartbarButtonBgColor
                        else -> prefs.mThemingApp.smartbarBgColor
                    }
                )
            }
            else -> {
                elevation = 0.0f
                when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.ENTER -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyEnterBgColorPressed
                                else -> prefs.mThemingApp.keyEnterBgColor
                            }
                        )
                    }
                    LatestSingleKeyCoding.SHIFT -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyShiftBgColorPressed
                                else -> prefs.mThemingApp.keyShiftBgColor
                            }
                        )
                    }

                    LatestSingleKeyCoding.DELETE -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyShiftBgColorPressed
                                else -> prefs.mThemingApp.keyShiftBgColor
                            }
                        )
                    }

                    LatestSingleKeyCoding.SPACE -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyShiftBgColor
                                else -> prefs.mThemingApp.keyShiftBgColor
                            }
                        )
                    }

                    LatestSingleKeyCoding.VIEW_SYMBOLS -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyShiftBgColorPressed
                                else -> prefs.mThemingApp.keyShiftBgColor
                            }
                        )
                    }

                    else -> {
                        setBackgroundTintColor2(
                            this, when {
                                isKeyPressed && isEnabled -> prefs.mThemingApp.keyBgColorPressed
                                else -> prefs.mThemingApp.keyBgColor
                            }
                        )
                    }
                }
            }
        }
    }

    private fun updateTouchHitBox() {
        if (visibility == GONE) {
            touchHitBox.set(-1, -1, -1, -1)
        } else {
            val parent = parent as ViewGroup
            val keyMarginH = resources.getDimension((R.dimen.single_key_margin_flat)).toInt()
            val keyMarginV = resources.getDimension((R.dimen.single_key_margin_stand)).toInt()

            touchHitBox.apply {
                left = when (this@LatestSingleKeyLayout) {
                    parent.children.first() -> 0
                    else -> (parent.x + x - keyMarginH).toInt()
                }
                right = when (this@LatestSingleKeyLayout) {
                    parent.children.last() -> latestInputView.measuredWidth
                    else -> (parent.x + x + measuredWidth + keyMarginH).toInt()
                }
                top = (parent.y + y - keyMarginV).toInt()
                bottom = (parent.y + y + measuredHeight + keyMarginV).toInt()
            }
        }
    }

    fun updateVisibility() {
        updateEnabledState()
        when (datingLatestSingle.code) {
            LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT,
            LatestSingleKeyCoding.SWITCH_TO_MEDIA_CONTEXT -> {
                visibility = if (latestKeyboardService?.shouldShowLanguageSwitch() == true) {
                    VISIBLE
                } else {
                    GONE
                }
            }
            LatestSingleKeyCoding.LANGUAGE_SWITCH -> {
                visibility = if (latestKeyboardService?.shouldShowLanguageSwitch() == true) {
                    VISIBLE
                } else {
                    GONE
                }
            }
            else -> if (datingLatestSingle.variation != KeyVariation.ALL) {
                val keyVariation =
                    latestKeyboardService?.latestInputHelper?.keyVariation ?: KeyVariation.NORMAL
                visibility =
                    if (datingLatestSingle.variation == KeyVariation.NORMAL && (keyVariation == KeyVariation.NORMAL
                                || keyVariation == KeyVariation.PASSWORD)
                    ) {
                        VISIBLE
                    } else if (datingLatestSingle.variation == keyVariation) {
                        VISIBLE
                    } else {
                        GONE
                    }
                updateTouchHitBox()
            }
        }
    }

    private fun setTextSizeFor(
        boxPaint: Paint,
        boxWidth: Float,
        boxHeight: Float,
        text: String,
        multiplier: Float = 1.0f
    ): Float {
        var stage = 1
        var textSize = 0.0f
        while (stage < 3) {
            if (stage == 1) {
                textSize += 10.0f
            } else if (stage == 2) {
                textSize -= 1.0f
            }
            boxPaint.textSize = textSize
            boxPaint.getTextBounds(text, 0, text.length, tempRect)
            val fits = tempRect.width() < boxWidth && tempRect.height() < boxHeight
            if (stage == 1 && !fits || stage == 2 && fits) {
                stage++
            }
        }
        textSize *= multiplier
        boxPaint.textSize = textSize
        return textSize
    }

     override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

         canvas

        try {
            updateKeyPressedBackground()
        } catch (e: Exception) {
        }

        try {
            if (datingLatestSingle.type == KeyType.CHARACTER && datingLatestSingle.code != LatestSingleKeyCoding.SPACE
                && datingLatestSingle.code != LatestSingleKeyCoding.HALF_SPACE && datingLatestSingle.code != LatestSingleKeyCoding.KESHIDA || datingLatestSingle.type == KeyType.NUMERIC
            ) {
                label = getComputedLetter()
                val hintedNumber = datingLatestSingle.hintedNumber
                if (prefs.mAppKeyboard.hintedNumberRow && hintedNumber != null) {
                    hintedLabel = getComputedLetter(hintedNumber)
                }
                val hintedSymbol = datingLatestSingle.hintedSymbol
                if (prefs.mAppKeyboard.hintedSymbols && hintedSymbol != null) {
                    hintedLabel = getComputedLetter(hintedSymbol)
                }

            } else {
                when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.ARROW_LEFT -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_keyboard_arrow_left)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.ARROW_RIGHT -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_keyboard_arrow_right)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.CLIPBOARD_COPY -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_content_copy)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.CLIPBOARD_CUT -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_content_cut)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.CLIPBOARD_PASTE -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_content_paste)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.CLIPBOARD_SELECT_ALL -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_select_all)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.DELETE -> {
                        try {
                            drawable = getDrawable(context, R.drawable.new_backspace)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.ENTER -> {
                        try {
                            val imeOptions =
                                latestKeyboardService?.activeEditorInstance?.imeOptions
                                    ?: ImeOptions.default()
                            drawable = getDrawable(
                                context, when (imeOptions.action) {
                                    ImeOptions.Action.DONE -> R.drawable.ic_done
                                    ImeOptions.Action.GO -> R.drawable.ic_arrow_right_alt
                                    ImeOptions.Action.NEXT -> R.drawable.ic_arrow_right_alt
                                    ImeOptions.Action.NONE -> R.drawable.new_return
                                    ImeOptions.Action.PREVIOUS -> R.drawable.ic_arrow_right_alt
                                    ImeOptions.Action.SEARCH -> R.drawable.ic_search
                                    ImeOptions.Action.SEND -> R.drawable.ic_send
                                    ImeOptions.Action.UNSPECIFIED -> R.drawable.new_return
                                }
                            )
                            drawableColor = prefs.mThemingApp.keyEnterFgColor
                            if (imeOptions.flagNoEnterAction) {
                                drawable = getDrawable(context, R.drawable.new_return)
                            }
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.LANGUAGE_SWITCH -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_lang_toggle)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.PHONE_PAUSE -> label ="Pause"
                    LatestSingleKeyCoding.PHONE_WAIT -> label ="Wait"
                    LatestSingleKeyCoding.SHIFT -> {
                        drawable = getDrawable(
                            context, when {
                                latestKeyboardService?.latestInputHelper?.caps ?: false && latestKeyboardService?.latestInputHelper?.capsLock ?: false -> {
                                    drawableColor = prefs.mThemingApp.keyShiftFgColor
                                    R.drawable.ic_caps_locked
                                }
                                latestKeyboardService?.latestInputHelper?.caps ?: false && !(latestKeyboardService?.latestInputHelper?.capsLock
                                    ?: false) -> {
                                    drawableColor = prefs.mThemingApp.keyShiftFgColor
                                    R.drawable.ic_caps_lock_on
                                }
                                else -> {
                                    drawableColor = prefs.mThemingApp.keyShiftFgColor
                                    R.drawable.ic_caps_lock_off
                                }
                            }
                        )
                    }
                    LatestSingleKeyCoding.SPACE -> {
                        try {
                            when (latestInputView.computedLayout?.mode) {
                                LatestInputMode.NUMERIC,
                                LatestInputMode.NUMERIC_ADVANCED,
                                LatestInputMode.PHONE,
                                LatestInputMode.PHONE2 -> {
                                    drawable = getDrawable(context, R.drawable.ic_space_bar)
                                    drawableColor = prefs.mThemingApp.keyFgColor
                                }
                                LatestInputMode.CHARACTERS -> {
                                    label = latestKeyboardService?.activeLanguageModel?.locale?.displayName
                                    labelPaint.textSize = resources.getDimension(R.dimen.text_size_language_name_on_space)
                                }
                                else -> {
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.SWITCH_TO_MEDIA_CONTEXT -> {
                        try {
                            drawable = getDrawable(context, R.drawable.ic_smile_emoji)
                            drawableColor = prefs.mThemingApp.keyFgColor
                        } catch (e: Exception) {
                        }
                    }
                    LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT,
                    LatestSingleKeyCoding.VIEW_CHARACTERS -> {
                        label = "Abc"
                    }
                    LatestSingleKeyCoding.VIEW_NUMERIC,
                    LatestSingleKeyCoding.VIEW_NUMERIC_ADVANCED -> {
                        label = "1 2\n3 4"
                    }
                    LatestSingleKeyCoding.VIEW_PHONE -> {
                        label = "123"
                    }
                    LatestSingleKeyCoding.VIEW_PHONE2 -> {
                        label = "* #"
                    }
                    LatestSingleKeyCoding.VIEW_SYMBOLS -> {
                        label = "123"
                    }
                    LatestSingleKeyCoding.VIEW_SYMBOLS2 -> {
                        label = resources.getString(R.string.symbols_key)

                    }
                }
            }

            val drawable = drawable
            if (drawable != null) {
                if (latestInputView.isSmartbarKeyboardView && !isEnabled) {
                    drawableColor = prefs.mThemingApp.smartbarFgColorAlt
                }
                var marginV = 0
                var marginH = 0
                if (measuredWidth > measuredHeight) {
                    marginH = (measuredWidth - measuredHeight) / 2
                } else {
                    marginV = (measuredHeight - measuredWidth) / 2
                }
                drawable.setBounds(
                    marginH + drawablePaddingV,
                    marginV + drawablePaddingV,
                    measuredWidth - marginH - drawablePaddingV,
                    measuredHeight - marginV - drawablePaddingV
                )
                drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    drawableColor,
                    BlendModeCompat.SRC_ATOP
                )
                drawable.draw(canvas)
            }

            val label = label
            if (label != null) {
                when (datingLatestSingle.code) {
                    LatestSingleKeyCoding.VIEW_NUMERIC, LatestSingleKeyCoding.VIEW_NUMERIC_ADVANCED, LatestSingleKeyCoding.VIEW_PHONE, LatestSingleKeyCoding.VIEW_PHONE2, LatestSingleKeyCoding.VIEW_SYMBOLS, LatestSingleKeyCoding.VIEW_SYMBOLS2, LatestSingleKeyCoding.VIEW_CHARACTERS -> {
                       labelPaint.textSize = resources.getDimension(R.dimen.single_key_text_size_numeric)
                    }
                    else -> when {
                        (datingLatestSingle.type == KeyType.CHARACTER) &&
                                datingLatestSingle.code != LatestSingleKeyCoding.SPACE -> {
                            val cachedTextSize = setTextSizeFor(
                                labelPaint,
                                desiredWidth - (3.4f * drawablePaddingH),
                                desiredHeight - (3.4f * drawablePaddingV),

                                "X",
                                when (resources.configuration.orientation) {
                                    Configuration.ORIENTATION_PORTRAIT -> {
                                        prefs.mAppKeyboard.fontSizeMultiplierPortrait.toFloat() / 100.0f
                                    }
                                    Configuration.ORIENTATION_LANDSCAPE -> {
                                        prefs.mAppKeyboard.fontSizeMultiplierLandscape.toFloat() / 100.0f
                                    }
                                    else -> 1.0f
                                }
                            )
                            latestInputView.popupManager.keyPopupTextSize = cachedTextSize
                        }
                        else -> {

                            /*todo change*/

                            setTextSizeFor(
                                labelPaint,
                                measuredWidth - (1.0f * drawablePaddingH),
                                measuredHeight - (3.6f * drawablePaddingV),
                                when (datingLatestSingle.code) {
                                    LatestSingleKeyCoding.VIEW_CHARACTERS, LatestSingleKeyCoding.VIEW_SYMBOLS, LatestSingleKeyCoding.VIEW_SYMBOLS2 -> {
                                        resources.getString(R.string.symbols_key)
                                    }
                                    else -> label
                                }
                            )
                        }
                    }
                }
                labelPaint.color = prefs.mThemingApp.keyFgColor
                labelPaint.alpha = if (latestInputView.computedLayout?.mode == LatestInputMode.CHARACTERS &&
                    datingLatestSingle.code == LatestSingleKeyCoding.SPACE
                ) {
                    120
                } else {
                    255
                }
                val centerX = measuredWidth / 2.0f
                val centerY = measuredHeight / 2.0f + (labelPaint.textSize - labelPaint.descent()) / 2
                if (label.contains("\n")) {
                    // Even if more lines may be existing only the first 2 are shown
                    val labelLines = label.split("\n")
                    canvas.drawText(labelLines[0], centerX, centerY * 0.70f, labelPaint)
                    canvas.drawText(labelLines[1], centerX, centerY * 1.30f, labelPaint)
                } else {
                    canvas.drawText(label, centerX, centerY, labelPaint)
                }
            }

            val hintedLabel = hintedLabel
            if (hintedLabel != null) {
                setTextSizeFor(
                    hintedLabelPaint,
                    desiredWidth * 1.0f / 5.0f,
                    desiredHeight * 1.0f / 5.0f,
                    "X"
                )
                hintedLabelPaint.color = prefs.mThemingApp.keyFgColor
                hintedLabelPaint.alpha = 120
                val centerX = (measuredWidth * 5.0f / 6.0f) / 4
                val centerY =
                    measuredHeight * 1.0f / 6.0f + (hintedLabelPaint.textSize - hintedLabelPaint.descent()) / 2
                canvas.drawText(hintedLabel, centerX, centerY, hintedLabelPaint)
            }
        } catch (e: Exception) {
        }
    }

    private class KeyViewOutline(
        private val width: Int,
        private val height: Int
    ) : ViewOutlineProvider() {

        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline ?: return
            outline.setRoundRect(
                0,
                0,
                width,
                height,
                view.resources.getDimension(R.dimen.key_corner_radius)
            )
        }
    }
}
