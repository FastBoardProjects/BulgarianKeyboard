package com.maya.newbulgariankeyboard.popups

import android.content.res.Configuration
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.get
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.databinding.KeyPopupExtendedViewBinding
import com.maya.newbulgariankeyboard.databinding.KeyPopupViewLayoutBinding
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmoticonsKeyDetails
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmoticonsView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmoticonsViewer
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyLayout


class LatestClickedKeyManager<T_KBD : View, T_KV : View>(private val keyboardView: T_KBD) {

    private val TAG = "KeyPopupManager"
    private var anchorLeft: Boolean = false
    private var anchorRight: Boolean = false
    private var anchorOffset: Int = 0
    private var activeExtIndex: Int? = null
    private val exceptionsForKeyCodes = listOf(
        LatestSingleKeyCoding.ENTER,
        LatestSingleKeyCoding.LANGUAGE_SWITCH,
        LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT,
        LatestSingleKeyCoding.SWITCH_TO_MEDIA_CONTEXT
    )
    private var keyPopupWidth: Int
    private var keyPopupHeight: Int
    var keyPopupTextSize: Float =
        keyboardView.resources.getDimension(R.dimen.single_key_text_size_popup)
    private var keyPopupDiffX: Int = 0
    private val popupView: KeyPopupViewLayoutBinding
    private val popupViewExt: KeyPopupExtendedViewBinding
    private var row0count: Int = 0
    private var row1count: Int = 0
    private var window: PopupWindow
    private var windowExt: PopupWindow

    val isShowingPopup: Boolean
        get() = popupView.root.visibility == View.VISIBLE

    val isShowingExtendedPopup: Boolean
        get() = windowExt.isShowing

    init {
        val inflater = LayoutInflater.from(keyboardView.context)
        keyPopupWidth = keyboardView.resources.getDimension(R.dimen.single_key_width).toInt()
        keyPopupHeight = keyboardView.resources.getDimension(R.dimen.single_key_height).toInt()
        popupView = KeyPopupViewLayoutBinding.inflate(inflater, null, false)
        popupView.root.visibility = View.INVISIBLE
        popupViewExt = KeyPopupExtendedViewBinding.inflate(inflater, null, false)
        window = createPopupWindow(popupView.root)
        windowExt = createPopupWindow(popupViewExt.root)
    }

    private fun createTextView(
        keyView: T_KV,
        k: Int,
        isInitActive: Boolean = false,
        isWrapBefore: Boolean = false
    ): LatestClickedExtendedOneView {
        Log.d(TAG, " createTextView")
        val textView = LatestClickedExtendedOneView(keyView.context, k, isInitActive)
        val lp = FlexboxLayout.LayoutParams(keyPopupWidth, (keyPopupHeight * 0.4f).toInt())
        lp.isWrapBefore = isWrapBefore
        textView.layoutParams = lp
        textView.gravity = Gravity.CENTER
        val textSize = keyPopupTextSize
        if (keyView is LatestSingleKeyLayout) {
            when (keyView.datingPopupWithHintLatestSingle[k].code) {
                LatestSingleKeyCoding.SETTINGS -> {
                    textView.iconDrawable = getDrawable(
                        keyView.context, R.drawable.ic_settings
                    )
                }
                LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT -> {
                    textView.text = "Abc"
                }
                LatestSingleKeyCoding.SWITCH_TO_MEDIA_CONTEXT -> {
                    textView.iconDrawable = getDrawable(
                        keyView.context, R.drawable.ic_smile_emoji
                    )
                }
                LatestSingleKeyCoding.TOGGLE_ONE_HANDED_MODE -> {
                    textView.iconDrawable = getDrawable(
                        keyView.context, R.drawable.ic_smartphone
                    )
                }
                else -> {
                    textView.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        when (keyView.datingPopupWithHintLatestSingle[k].code) {
                            LatestSingleKeyCoding.URI_COMPONENT_TLD,
                            LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT -> textSize * 0.6f
                            else -> textSize
                        }
                    )
                    textView.text =
                        keyView.getComputedLetter(keyView.datingPopupWithHintLatestSingle[k])
                }
            }
        } else if (keyView is LatestEmoticonsViewer) {
            textView.text = keyView.detailsLatest.popup[k].getCodePointsAsString()
        }
        return textView
    }

    private fun createPopupWindow(view: View): PopupWindow {
        return PopupWindow(keyboardView.context).apply {
            animationStyle = 0
            contentView = view
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                enterTransition = null
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                exitTransition = null
            }
            isClippingEnabled = false
            isFocusable = false
            isTouchable = false
            setBackgroundDrawable(null)
        }
    }

    private fun calc(keyView: T_KV) {
        if (keyboardView is LatestInputView) {
            when (keyboardView.resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    if (keyboardView.isSmartbarKeyboardView) {
                        keyPopupWidth = (keyView.measuredWidth * 0.6f).toInt()
                        keyPopupHeight = (keyboardView.desiredKeyHeight * 2.0f * 1.2f).toInt()
                    } else {
                        keyPopupWidth = (keyboardView.desiredKeyWidth * 0.6f).toInt()
                        keyPopupHeight = (keyboardView.desiredKeyHeight * 2.0f).toInt()
                    }
                }
                else -> {
                    if (keyboardView.isSmartbarKeyboardView) {
                        keyPopupWidth = (keyView.measuredWidth * 1.1f).toInt()
                        keyPopupHeight = (keyboardView.desiredKeyHeight * 1.5f * 1.2f).toInt()
                    } else {
                        keyPopupWidth = (keyboardView.desiredKeyWidth * 1.1f).toInt()
                        keyPopupHeight = (keyboardView.desiredKeyHeight * 1.5f).toInt()
                    }
                }
            }
        } else if (keyboardView is LatestEmoticonsView) {
            keyPopupWidth = keyView.measuredWidth
            keyPopupHeight = (keyView.measuredHeight * 2.5f).toInt()
        }
        keyPopupDiffX = (keyView.measuredWidth - keyPopupWidth) / 2
    }

    fun show(keyView: T_KV) {
        if (keyView is LatestSingleKeyLayout && keyView.datingLatestSingle.code <= LatestSingleKeyCoding.SPACE) {
            return
        }

        calc(keyView)

        val keyPopupX = keyPopupDiffX
        val keyPopupY = -keyPopupHeight
        if (window.isShowing) {
            window.update(keyView, keyPopupX, keyPopupY, keyPopupWidth, keyPopupHeight)
        } else {
            window.width = keyPopupWidth
            window.height = keyPopupHeight
            window.showAsDropDown(keyView, keyPopupX, keyPopupY, Gravity.NO_GRAVITY)
        }
        if (keyView is LatestSingleKeyLayout) {
            popupView.symbol.layoutParams.height = (keyPopupHeight * 0.4f).toInt()
            popupView.symbol.setTextSize(TypedValue.COMPLEX_UNIT_PX, keyPopupTextSize)
            popupView.symbol.text = keyView.getComputedLetter()
            popupView.threedots.visibility = when {
                keyView.datingPopupWithHintLatestSingle.isEmpty() -> View.INVISIBLE
                else -> View.VISIBLE
            }
        } else if (keyView is LatestEmoticonsViewer) {
            popupView.symbol.text = keyView.detailsLatest.getCodePointsAsString()
            popupView.threedots.visibility = when {
                keyView.detailsLatest.popup.isEmpty() -> View.INVISIBLE
                else -> View.VISIBLE
            }
        }
        popupView.root.visibility = View.VISIBLE
    }

    fun extend(keyView: T_KV) {
        if (keyView is LatestSingleKeyLayout && keyView.datingLatestSingle.code <= LatestSingleKeyCoding.SPACE
            && !exceptionsForKeyCodes.contains(keyView.datingLatestSingle.code)
        ) {
            return
        }

        if (!isShowingPopup) {
            calc(keyView)
        }

        anchorLeft = keyView.x < keyboardView.measuredWidth / 2
        anchorRight = !anchorLeft

        val n = when (keyView) {
            is LatestSingleKeyLayout -> keyView.datingPopupWithHintLatestSingle.size
            is LatestEmoticonsViewer -> keyView.detailsLatest.popup.size
            else -> 0
        }
        when {
            n <= 5 -> {
                row1count = 0
                row0count = n
            }
            n > 5 && n % 2 == 1 -> {
                row1count = (n - 1) / 2
                row0count = (n + 1) / 2
            }
            else -> {
                row1count = n / 2
                row0count = n / 2
            }
        }

        anchorOffset = when {
            row0count <= 1 -> 0
            else -> {
                var offset = when {
                    row0count % 2 == 1 -> (row0count - 1) / 2
                    row0count % 2 == 0 -> (row0count / 2) - 1
                    else -> 0
                }
                val availableSpace = when {
                    anchorLeft -> keyView.x.toInt() + keyPopupDiffX
                    anchorRight -> keyboardView.measuredWidth -
                            (keyView.x.toInt() + keyPopupDiffX + keyPopupWidth)
                    else -> 0
                }
                while (offset > 0) {
                    if (availableSpace >= offset * keyPopupWidth) {
                        break
                    } else {
                        offset -= 1
                    }
                }
                offset
            }
        }

        popupViewExt.root.removeAllViews()
        val indices = when (keyView) {
            is LatestSingleKeyLayout -> keyView.datingPopupWithHintLatestSingle.indices
            is LatestEmoticonsViewer -> keyView.detailsLatest.popup.indices
            else -> IntRange(0, 0)
        }
        var hasShownFirst = false
        for (k in indices) {
            val isInitActive =
                anchorLeft && (k - row1count == anchorOffset) ||
                        anchorRight && (k - row1count == row0count - 1 - anchorOffset)
            val kk = when (keyView) {
                is LatestSingleKeyLayout -> when {
                    isInitActive -> {
                        hasShownFirst = true
                        0
                    }
                    hasShownFirst -> k
                    else -> k + 1
                }
                else -> k
            }
            popupViewExt.root.addView(
                createTextView(
                    keyView, kk, isInitActive, (row1count > 0) && (k - row1count == 0)
                )
            )
            if (isInitActive) {
                activeExtIndex = k
            }
        }
        popupView.threedots.visibility = View.INVISIBLE

        val extWidth = row0count * keyPopupWidth
        val extHeight = when {
            row1count > 0 -> keyPopupHeight * 0.4f * 2.0f
            else -> keyPopupHeight * 0.4f
        }.toInt()
        popupViewExt.root.justifyContent = if (anchorLeft) {
            JustifyContent.FLEX_START
        } else {
            JustifyContent.FLEX_END
        }
        if (popupViewExt.root.layoutParams == null) {
            popupViewExt.root.layoutParams = ViewGroup.LayoutParams(extWidth, extHeight)
        } else {
            popupViewExt.root.layoutParams.apply {
                width = extWidth
                height = extHeight
            }
        }
        val x = ((keyView.measuredWidth - keyPopupWidth) / 2) + when {
            anchorLeft -> -anchorOffset * keyPopupWidth
            anchorRight -> -extWidth + keyPopupWidth + anchorOffset * keyPopupWidth
            else -> 0
        }
        val y = -keyPopupHeight - when {
            row1count > 0 -> (keyPopupHeight * 0.4f).toInt()
            else -> 0
        }

        // Position and show popup window
        if (windowExt.isShowing) {
            try {
                windowExt.update(keyView, x, y, extWidth, extHeight)
            } catch (e: Exception) {
            }
        } else {
            windowExt.width = extWidth
            windowExt.height = extHeight
            try {
                windowExt.showAsDropDown(keyView, x, y, Gravity.NO_GRAVITY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun propagateMotionEvent(keyView: T_KV, event: MotionEvent): Boolean {
        if (!isShowingExtendedPopup) {
            return false
        }

        val kX: Float = event.x / keyPopupWidth.toFloat()

        if (event.y < -keyPopupHeight || event.y > 0.9f * keyPopupHeight) {
            return false
        }

        activeExtIndex = when {
            anchorLeft -> when {
                event.x < keyPopupDiffX - (anchorOffset + 1) * keyPopupWidth ||
                        event.x > (keyPopupDiffX + (row0count + 1 - anchorOffset) * keyPopupWidth) -> {
                    return false
                }
                event.y < 0 && row1count > 0 -> when {
                    kX >= row1count - anchorOffset -> row1count - 1
                    kX < -anchorOffset -> 0
                    kX < 0 -> kX.toInt() - 1 + anchorOffset
                    else -> kX.toInt() + anchorOffset
                }
                else -> when {
                    kX >= row0count - anchorOffset -> row1count + row0count - 1
                    kX < -anchorOffset -> row1count
                    kX < 0 -> row1count + kX.toInt() - 1 + anchorOffset
                    else -> row1count + kX.toInt() + anchorOffset
                }
            }
            anchorRight -> when {
                event.x > keyView.measuredWidth - keyPopupDiffX + (anchorOffset + 1) * keyPopupWidth ||
                        event.x < (keyView.measuredWidth -
                        keyPopupDiffX - (row0count + 1 - anchorOffset) * keyPopupWidth) -> {
                    return false
                }
                event.y < 0 && row1count > 0 -> when {
                    kX >= anchorOffset -> row1count - 1
                    kX < -(row1count - 1 - anchorOffset) -> 0
                    kX < 0 -> row1count - 2 + kX.toInt() - anchorOffset
                    else -> row1count - 1 + kX.toInt() - anchorOffset
                }
                // row 0
                else -> when {
                    kX >= anchorOffset -> row1count + row0count - 1
                    kX < -(row0count - 1 - anchorOffset) -> row1count
                    kX < 0 -> row1count + row0count - 2 + kX.toInt() - anchorOffset
                    else -> row1count + row0count - 1 + kX.toInt() - anchorOffset
                }
            }
            else -> -1
        }

        if (keyView is LatestSingleKeyLayout) {
            for (k in keyView.datingPopupWithHintLatestSingle.indices) {
                val view = popupViewExt.root.getChildAt(k)
                if (view != null) {
                    val textView = view as LatestClickedExtendedOneView
                    textView.isActive = k == activeExtIndex
                }
            }
        } else if (keyView is LatestEmoticonsViewer) {
            for (k in keyView.detailsLatest.popup.indices) {
                val view = popupViewExt.root.getChildAt(k)
                if (view != null) {
                    val textView = view as LatestClickedExtendedOneView
                    textView.isActive = k == activeExtIndex
                }
            }
        }

        return true
    }

    fun getActiveKeyData(keyView: T_KV): LatestSingleKeyDating? {
        return if (keyView is LatestSingleKeyLayout) {
            val activeExtIndex = activeExtIndex
            if (activeExtIndex != null) {
                val singleView = popupViewExt.root[activeExtIndex]
                if (singleView is LatestClickedExtendedOneView) {
                    keyView.datingPopupWithHintLatestSingle.getOrNull(singleView.adjustedIndex)
                        ?: keyView.datingLatestSingle
                } else {
                    keyView.datingLatestSingle
                }
            } else {
                keyView.datingLatestSingle
            }
        } else {
            null
        }
    }

    fun getActiveEmojiKeyData(keyView: T_KV): LatestEmoticonsKeyDetails? {
        return if (keyView is LatestEmoticonsViewer) {
            keyView.detailsLatest.popup.getOrNull(activeExtIndex ?: -1) ?: keyView.detailsLatest
        } else {
            null
        }
    }

    fun hide() {
        popupView.root.visibility = View.INVISIBLE
        if (windowExt.isShowing) {
            windowExt.dismiss()
        }

        activeExtIndex = null
    }


    fun dismissAllPopups() {
        if (window.isShowing) {
            window.dismiss()
        }
        if (windowExt.isShowing) {
            windowExt.dismiss()
        }

        activeExtIndex = null
    }
}
