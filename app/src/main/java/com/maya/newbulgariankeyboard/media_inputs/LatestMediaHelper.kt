package com.maya.newbulgariankeyboard.media_inputs

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import com.google.android.material.tabs.TabLayout
import com.maya.newbulgariankeyboard.BuildConfig
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardView
import com.maya.newbulgariankeyboard.main_classes.LatestServiceHelper
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmojiDbModel
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmoticonsKeyDetails
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmoticonsView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs.LatestGifKeyboardView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers.LatestGifStickerKeyboardView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers.LatestStickerKeyboardView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyType
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class LatestMediaHelper private constructor() : CoroutineScope by MainScope(),
    LatestKeyboardService.EventListener {

    private val mKeyboardService = LatestKeyboardService.getInstance()
    private val mServiceHelper: LatestServiceHelper
        get() = mKeyboardService.activeEditorInstance

    private var activeTab: Tab? = null
    private var mediaViewFlipper: ViewFlipper? = null
    private var osTimer: Timer? = null
    private var tabLayout: TabLayout? = null
    private val tabViews = EnumMap<Tab, LinearLayout>(Tab::class.java)

    var mediaViewGroup: LinearLayout? = null
    private val TAGME = "MediaInputManager:"

    companion object {
        private var instance: LatestMediaHelper? = null

        @Synchronized
        fun getInstance(): LatestMediaHelper {
            if (instance == null) {
                instance = LatestMediaHelper()
            }
            return instance!!
        }
    }

    init {
        try {
            mKeyboardService.addEventListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {
        Log.d(TAGME, " onRegisterInputView")
        if (BuildConfig.DEBUG) Log.i(this::class.simpleName, "onRegisterInputView(inputView)")

        launch(Dispatchers.Default) {
            mediaViewGroup = latestKeyboardView.findViewById(R.id.media_input)
            mediaViewFlipper = latestKeyboardView.findViewById(R.id.media_input_view_flipper)

            // Init bottom buttons
            latestKeyboardView.findViewById<ImageButton>(R.id.media_input_switch_to_text_input_button)
                .setOnTouchListener { view, event -> onBottomButtonEvent(view, event) }
            latestKeyboardView.findViewById<ImageButton>(R.id.media_input_backspace_button)
                .setOnTouchListener { view, event -> onBottomButtonEvent(view, event) }

            tabLayout = latestKeyboardView.findViewById(R.id.media_input_tabs)
            tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position) {
                        0 -> setActiveTab(Tab.EMOJI)
                        1 -> setActiveTab(Tab.EMOTICON)
                        2 -> setActiveTab(Tab.GIF)
                        3 -> setActiveTab(Tab.STICKERS)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            withContext(Dispatchers.Main) {
                for (tab in Tab.values()) {
                    val tabView = createTabViewFor(tab)
                    tabViews[tab] = tabView
                    mediaViewFlipper?.addView(tabView)
                }
                tabLayout?.selectTab(tabLayout?.getTabAt(0))
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAGME, " onDestroy")
        if (BuildConfig.DEBUG) Log.i(this::class.simpleName, "onDestroy()")
        cancel()
        instance = null
    }


    private fun onBottomButtonEvent(view: View, event: MotionEvent?): Boolean {
        Log.d(TAGME, " onBottomButtonEvent")
        event ?: return false
        val data = when (view.id) {
            R.id.media_input_switch_to_text_input_button -> {
                LatestSingleKeyDating(LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT)
            }
            R.id.media_input_backspace_button -> {
                LatestSingleKeyDating(LatestSingleKeyCoding.DELETE, type = KeyType.ENTER_EDITING)
            }
            else -> null
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mKeyboardService.keyPressVibrate()
                mKeyboardService.keyPressSound(data)
                if (data?.code == LatestSingleKeyCoding.DELETE && data.type == KeyType.ENTER_EDITING) {
                    osTimer = Timer()
                    osTimer?.scheduleAtFixedRate(500, 50) {
                        launch(Dispatchers.Main) {
                            mKeyboardService.latestInputHelper.sendKeyPress(data)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                osTimer?.cancel()
                osTimer = null
                if (event.actionMasked != MotionEvent.ACTION_CANCEL && data != null) {
                    mKeyboardService.latestInputHelper.sendKeyPress(data)
                }
            }
        }
        return false
    }


    private fun createTabViewFor(tab: Tab): LinearLayout {
        Log.d(TAGME, " createTabViewFor")
        return when (tab) {
            Tab.EMOJI -> LatestEmoticonsView(mKeyboardService)
            Tab.EMOTICON -> LatestStickerKeyboardView(mKeyboardService.context)
            Tab.GIF -> LatestGifKeyboardView(mKeyboardService.context)
            Tab.STICKERS -> LatestGifStickerKeyboardView(mKeyboardService.context)
            else -> LinearLayout(mKeyboardService.context).apply {
                addView(TextView(context).apply {
                    text = "Gifs will appear here"
                })
            }
        }
    }

    /**
     * Sets the actively shown tab.
     */
    fun setActiveTab(newActiveTab: Tab) {
        Log.d(TAGME, " setActiveTab")
        mediaViewFlipper?.displayedChild =
            mediaViewFlipper?.indexOfChild(tabViews[newActiveTab]) ?: 0
        activeTab = newActiveTab
    }

    fun sendEmojiKeyPress(latestEmoticonsKeyDetails: LatestEmoticonsKeyDetails) {
        Log.d(TAGME, " sendEmojiKeyPress")
        mServiceHelper.commitText(latestEmoticonsKeyDetails.getCodePointsAsString())
    }

    fun sendEmojiKeyPressRecent(latestEmojiDbModel: LatestEmojiDbModel) {
        Log.d(TAGME, " sendEmojiKeyPress")
        mServiceHelper.commitText(latestEmojiDbModel.itemEmoji)
    }

    fun sendClickedGifContentUri(gifUri: Uri) {
        Log.d(TAGME, " sendClickedGifContentUri")
        mServiceHelper.commitGifImage(gifUri)
    }

    fun sendClickedStickerContentUri(gifUri: Uri) {
        Log.d(TAGME, " sendClickedStickerContentUri")
        mServiceHelper.commitStickerImage(gifUri)
    }


    enum class Tab {
        EMOJI,
        EMOTICON,
        GIF,
        STICKERS
    }
}
