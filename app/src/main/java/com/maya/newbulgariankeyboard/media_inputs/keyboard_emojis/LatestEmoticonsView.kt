package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ViewFlipper
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.tabs.TabLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.database.EmojiDao
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.popups.LatestClickedKeyManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap


class LatestEmoticonsView : LinearLayout,
    LatestKeyboardService.EventListener
    ,
    LatestRecentAdapterCallback {

    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private val TAG = "EmojiKeyboardView:"
    private var activeCategory: LatestCategories = LatestCategories.SMILEYS_EMOTION
    private var emojiViewFlipper: ViewFlipper
    private val emojiKeyWidth = resources.getDimension(R.dimen.single_emoticon_key_width).toInt()
    private val emojiKeyHeight = resources.getDimension(R.dimen.single_emoticon_key_height).toInt()
    private var listEmojisRecent = ArrayList<LatestEmojiDbModel>()
    private var layouts: Deferred<EmojiLayoutDataMap>
    private val mainScope = MainScope()
    private val tabLayout: TabLayout
    private var emojisDao: EmojiDao? = null
    private val uiLayouts =
        EnumMap<LatestCategories, LatestScrollView>(LatestCategories::class.java)

    var isScrollBlocked: Boolean = false
    var popupManager = LatestClickedKeyManager<LatestEmoticonsView, LatestEmoticonsViewer>(this)
    lateinit var recyclerView: RecyclerView /*recent recycler view*/

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        Log.d(TAG, "Constructor")
        layouts = mainScope.async(Dispatchers.IO) {
            parseRawEmojiSpecsFile(context, "app_assets/media/emoji/all_emojis.txt")
        }
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        emojisDao = LatestRoomDatabase.getInstance(context).emojiDao()
        orientation = VERTICAL
        emojiViewFlipper = ViewFlipper(context)
        emojiViewFlipper.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1.0f
        }
        emojiViewFlipper.measureAllChildren = false
        addView(emojiViewFlipper)
        tabLayout =
            ViewGroup.inflate(context, R.layout.media_input_emoji_tabs, null) as TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                setActiveCategory(
                    when (tab?.position) {
                        0 -> LatestCategories.SMILEYS_RECENT
                        1 -> LatestCategories.SMILEYS_EMOTION
                        2 -> LatestCategories.PEOPLE_BODY
                        3 -> LatestCategories.ANIMALS_NATURE
                        4 -> LatestCategories.FOOD_DRINK
                        5 -> LatestCategories.TRAVEL_PLACES
                        6 -> LatestCategories.ACTIVITIES
                        7 -> LatestCategories.OBJECTS
                        8 -> LatestCategories.SYMBOLS
                        9 -> LatestCategories.FLAGS
                        else -> LatestCategories.SMILEYS_RECENT
                    }
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
        addView(tabLayout)
        try {
            latestKeyboardService?.addEventListener(this)
        } catch (e: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        mainScope.launch {
            layouts.await()
            fillRecentEmojisList()
            buildLayout()
            setActiveCategory(LatestCategories.SMILEYS_EMOTION)
            tabLayout.getTabAt(1)!!.select()
        }
        onApplyThemeAttributes()
    }

    private fun fillRecentEmojisList() {
        listEmojisRecent.clear()
        listEmojisRecent = emojisDao!!.allGifs as ArrayList<LatestEmojiDbModel>
        Log.d("StickerLogger:", "Db Size: ${listEmojisRecent.size}")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun buildLayout() = withContext(Dispatchers.Default) {
        for (category in LatestCategories.values()) {
            val scrollView = buildLayoutForCategory(category)/*make layout for each category*/
            uiLayouts[category] = scrollView
            withContext(Dispatchers.Main) {
                emojiViewFlipper.addView(scrollView)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private suspend fun buildLayoutForCategory(
        category: LatestCategories
    ): LatestScrollView = withContext(Dispatchers.Default) {
        val scrollView =
            LatestScrollView(
                context
            )
        try {
            scrollView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val flexboxLayout = FlexboxLayout(context)
            flexboxLayout.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            flexboxLayout.flexDirection = FlexDirection.ROW
            flexboxLayout.justifyContent = JustifyContent.SPACE_BETWEEN
            flexboxLayout.flexWrap = FlexWrap.WRAP
            /*termiante flexible layout*/
            if (category == LatestCategories.SMILEYS_RECENT) {
                recyclerView = RecyclerView(context)
                recyclerView.hasFixedSize()
                recyclerView.layoutManager = GridLayoutManager(context, 8)
                try {
                    val adapter =
                        LatestRecentEmojiAdapter(
                            context,
                            listEmojisRecent,
                            this@LatestEmoticonsView
                        )
                    recyclerView.adapter = adapter
                    flexboxLayout.addView(recyclerView)
                } catch (e: Exception) {
                }
            } else {
                for (emojiKeyData in layouts.await()[category].orEmpty()) {
                    val emojiKeyView =
                        LatestEmoticonsViewer(this@LatestEmoticonsView, emojiKeyData)
                    emojiKeyView.layoutParams = FlexboxLayout.LayoutParams(
                        emojiKeyWidth, emojiKeyHeight
                    )
                    flexboxLayout.addView(emojiKeyView)
                }
                for (n in 0 until 24) {
                    val gridPlaceholderView = View(context).apply {
                        layoutParams = LayoutParams(emojiKeyWidth, 0)
                    }
                    flexboxLayout.addView(gridPlaceholderView)
                }
            }

            scrollView.setOnTouchListener { _, _ ->
                return@setOnTouchListener isScrollBlocked
            }
            scrollView.addView(flexboxLayout)
        } catch (e: Exception) {
        }
        return@withContext scrollView
    }

    override fun onEmojiItemClicked(modelLatest: LatestEmojiDbModel?) {
        if (modelLatest != null) {
            latestKeyboardService?.latestMediaHelper?.sendEmojiKeyPressRecent(modelLatest)
        }
    }

    fun setActiveCategory(newActiveCategory: LatestCategories) {
        Log.d(TAG, "setActiveCategory")
        emojiViewFlipper.displayedChild =
            emojiViewFlipper.indexOfChild(uiLayouts[newActiveCategory])
        activeCategory = newActiveCategory
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.d(TAG, "onInterceptTouchEvent")
        if (ev?.actionMasked == MotionEvent.ACTION_DOWN) {
            isScrollBlocked = false
        }
        return false
    }

    fun refreshRecentAdapter() {
        try {
            Log.d("ClickDet:", " Refreshed")
            listEmojisRecent = emojisDao!!.allGifs as ArrayList<LatestEmojiDbModel>
            val adapter =
                LatestRecentEmojiAdapter(
                    context,
                    listEmojisRecent,
                    this@LatestEmoticonsView
                )
            recyclerView.adapter = adapter
        } catch (e: Exception) {
        }
    }

    fun dismissKeyView(keyView: LatestEmoticonsViewer) {
        Log.d(TAG, "dismissKeyView")
        keyView.onTouchEvent(
            MotionEvent.obtain(
                0, 0, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0
            )
        )
        isScrollBlocked = true
    }

    override fun onApplyThemeAttributes() {
        Log.d(TAG, "onApplyThemeAttributes")
        tabLayout.tabIconTint = ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor)
        tabLayout.setSelectedTabIndicatorColor(prefs.mThemingApp.mediaFgColor)
    }


}
