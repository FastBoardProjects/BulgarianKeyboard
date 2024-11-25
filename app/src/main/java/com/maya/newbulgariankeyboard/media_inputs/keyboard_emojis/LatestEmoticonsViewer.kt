package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.database.EmojiDao
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper


@SuppressLint("ViewConstructor")
class LatestEmoticonsViewer(
    private val latestEmoticonsView: LatestEmoticonsView,
    val detailsLatest: LatestEmoticonsKeyDetails
) : androidx.appcompat.widget.AppCompatTextView(latestEmoticonsView.context),
    LatestKeyboardService.EventListener {
    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)

    private var isCancelled: Boolean = false
    private var osHandler: Handler? = null
    private var triangleDrawable: Drawable? = null
    private var emojisDao: EmojiDao? = null

    init {
        background = null
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, 0)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.single_key_text_size_emoji))
        emojisDao = LatestRoomDatabase.getInstance(context).emojiDao()
        triangleDrawable = ContextCompat.getDrawable(context, R.drawable.triangle_bottom_right)

        text = detailsLatest.getCodePointsAsString()
        //Log.d("EmoChec:", " Txt: ${details.getCodePointsAsString()}")
        try {
            latestKeyboardService?.addEventListener(this)
        } catch (e: Exception) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onApplyThemeAttributes()
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isCancelled = false
                val delayMillis = prefs.mAppKeyboard.longPressDelay
                if (osHandler == null) {
                    osHandler = Handler()
                }
                osHandler?.postDelayed({
                    (parent.parent as ScrollView)
                        .requestDisallowInterceptTouchEvent(true)
                    latestEmoticonsView.isScrollBlocked = true
                    latestEmoticonsView.popupManager.show(this)
                    latestEmoticonsView.popupManager.extend(this)
                    latestKeyboardService?.keyPressVibrate()
                    latestKeyboardService?.keyPressSound()
                }, delayMillis.toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (latestEmoticonsView.popupManager.isShowingExtendedPopup) {
                    val isPointerWithinBounds =
                        latestEmoticonsView.popupManager.propagateMotionEvent(this, event)
                    if (!isPointerWithinBounds) {
                        latestEmoticonsView.dismissKeyView(this)
                    }
                } else {
                    if (event.x < -0.1f * measuredWidth || event.x > 1.1f * measuredWidth
                        || event.y < -0.1f * measuredHeight || event.y > 1.1f * measuredHeight
                    ) {
                        latestEmoticonsView.dismissKeyView(this)
                    }
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                osHandler?.removeCallbacksAndMessages(null)
                val retData =
                    latestEmoticonsView.popupManager.getActiveEmojiKeyData(this)
                latestEmoticonsView.popupManager.hide()
                if (event.actionMasked != MotionEvent.ACTION_CANCEL &&
                    retData != null && !isCancelled
                ) {
                    if (!latestEmoticonsView.isScrollBlocked) {
                        latestKeyboardService?.keyPressVibrate()
                        latestKeyboardService?.keyPressSound()
                    }
                    /*item maya*/
                    latestKeyboardService?.latestMediaHelper?.sendEmojiKeyPress(retData)
                    /*add to db*/
                    if (emojisDao!!.getGifByEmojiTxt(retData.getCodePointsAsString()) == null) {
                        Log.d("ClickDet:", "Inserting")
                        try {
                            emojisDao!!.insertSingleGif(
                                LatestEmojiDbModel(
                                    retData.getCodePointsAsString()
                                )
                            )
                            latestEmoticonsView.refreshRecentAdapter()
                        } catch (e: Exception) {
                        }
                    } else {
                        Log.d("ClickDet:", "Not inserting")
                    }
                    performClick()
                }
                if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    isCancelled = true
                }
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        triangleDrawable?.setBounds(
            (measuredWidth * 0.85f).toInt(),
            (measuredHeight * 0.85f).toInt(),
            (measuredWidth * 0.85f).toInt(),
            (measuredHeight * 0.85f).toInt()
        )
    }

    override fun onApplyThemeAttributes() {
        triangleDrawable?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                prefs.mThemingApp.mediaFgColorAlt, BlendModeCompat.SRC_ATOP
            )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas

        if (detailsLatest.popup.isNotEmpty()) {
            triangleDrawable?.draw(canvas)
        }
    }
}
