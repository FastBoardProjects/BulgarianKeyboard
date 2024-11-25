package com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.database.SticekrsDao
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardView
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_classes.LatestServiceHelper
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap

class LatestStickerKeyboardView : LinearLayout,
    LatestKeyboardService.EventListener
    ,
    LatestStickerAdapterCallback {

    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val TAG = "StickerKeyboardView:"
    private val mainScope = MainScope()
    private var emojiViewFlipper: ViewFlipper
    private val tabLayout: TabLayout
    private val textView: TextView
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private val buttonRetry: Button
    private var activeCategory: LatestStickerCategories =
        LatestStickerCategories.STICKER_ONE
    private var stickersDao: SticekrsDao? = null
    private val uiLayouts = EnumMap<LatestStickerCategories, RecyclerView>(LatestStickerCategories::class.java)
    private val uiAdapters =
        EnumMap<LatestStickerCategories, LatestStickersAdapter>(LatestStickerCategories::class.java)

    companion object {
        var listStickersRecent = ArrayList<LatestStickerModel>()
        var listStickers1 = ArrayList<LatestStickerModel>()
        var listStickers2 = ArrayList<LatestStickerModel>()
        var listStickers3 = ArrayList<LatestStickerModel>()
        var listStickers4 = ArrayList<LatestStickerModel>()
        var listStickers5 = ArrayList<LatestStickerModel>()
        var listStickers6 = ArrayList<LatestStickerModel>()
        var listStickers7 = ArrayList<LatestStickerModel>()
        var listStickers8 = ArrayList<LatestStickerModel>()
        var listStickers9 = ArrayList<LatestStickerModel>()
        var listStickers10 = ArrayList<LatestStickerModel>()
        var listStickers11 = ArrayList<LatestStickerModel>()
        var listStickers12 = ArrayList<LatestStickerModel>()
        var listStickers13 = ArrayList<LatestStickerModel>()
        var listStickers14 = ArrayList<LatestStickerModel>()
        var listStickers15 = ArrayList<LatestStickerModel>()
        var listStickers16 = ArrayList<LatestStickerModel>()
        var listStickers17 = ArrayList<LatestStickerModel>()
        var listStickers18 = ArrayList<LatestStickerModel>()
        var listStickers19 = ArrayList<LatestStickerModel>()
        var listStickers20 = ArrayList<LatestStickerModel>()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        Log.d("NewSticLog:", "StickerKeyboardView Constructor")
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        orientation = VERTICAL
        gravity = Gravity.CENTER
        emojiViewFlipper = ViewFlipper(context)
        emojiViewFlipper.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1.0f
        }
        tabLayout =
            ViewGroup.inflate(context, R.layout.media_input_stickers_tabs, null) as TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setActiveCategory(
                    when (tab?.position) {
                        0 -> LatestStickerCategories.STICKER_RECENT
                        1 -> LatestStickerCategories.STICKER_ONE
                        2 -> LatestStickerCategories.STICKER_TWO
                        3 -> LatestStickerCategories.STICKER_THIRD
                        4 -> LatestStickerCategories.STICKER_FOUR
                        5 -> LatestStickerCategories.STICKER_FIFTH
                        6 -> LatestStickerCategories.STICKER_SIXTH
                        7 -> LatestStickerCategories.STICKER_SEVEN
                        8 -> LatestStickerCategories.STICKER_EIGHT
                        9 -> LatestStickerCategories.STICKER_NINE
                        10 -> LatestStickerCategories.STICKER_TEN
                        11 -> LatestStickerCategories.STICKER_ELEVEN
                        12 -> LatestStickerCategories.STICKER_TWELVE
                        13 -> LatestStickerCategories.STICKER_THIRTEEN
                        14 -> LatestStickerCategories.STICKER_FOURTEEN
                        15 -> LatestStickerCategories.STICKER_FIFTEEN
                        16 -> LatestStickerCategories.STICKER_SIXTEEN
                        17 -> LatestStickerCategories.STICKER_SEVENTEEN
                        18 -> LatestStickerCategories.STICKER_EIGHTEEN
                        19 -> LatestStickerCategories.STICKER_NINETEEN
                        20 -> LatestStickerCategories.STICKER_TWENTY
                        else -> LatestStickerCategories.STICKER_RECENT
                    }
                )
            }

        })
        stickersDao = LatestRoomDatabase.getInstance(context).stickersDao
        textView = TextView(context)
        textView.text = "Cannot load Stickers.\nCheck your internet connection.\n"
        textView.textSize = 16f
        textView.gravity = Gravity.CENTER or Gravity.TOP
        textView.setTextColor(ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor))
        addView(textView)
        buttonRetry = Button(context)
        buttonRetry.text = "Try Again"
        buttonRetry.textSize = 15f
        buttonRetry.layoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        buttonRetry.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        buttonRetry.setOnClickListener {
            setActiveCategory(LatestStickerCategories.STICKER_ONE)
            tabLayout.getTabAt(1)!!.select()
            if (LatestUtils.isConnectionAvailable(context)) {
                Log.d("NewSticLog:", "StickerKeyboardView Yes internet")
                tabLayout.visibility = View.VISIBLE
                emojiViewFlipper.visibility = View.VISIBLE
                textView.visibility = View.GONE
                buttonRetry.visibility = View.GONE
            } else {
                Toast.makeText(context, "No Internet.", Toast.LENGTH_SHORT).show()
                Log.d("NewSticLog:", "StickerKeyboardView No Internet")
                tabLayout.visibility = View.GONE
                emojiViewFlipper.visibility = View.GONE
                textView.visibility = View.VISIBLE
                buttonRetry.visibility = View.VISIBLE
            }
        }
        addView(buttonRetry)
        emojiViewFlipper.measureAllChildren = false
        addView(emojiViewFlipper)
        addView(tabLayout)
        try {
            latestKeyboardService?.addEventListener(this)
        } catch (e: Exception) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("NewSticLog:", "StickerKeyboardView onAttachedToWindow")
        mainScope.launch {
            fillRecentStickersList()
            fillAllStickersList()
            buildLayout()
            setActiveCategory(LatestStickerCategories.STICKER_ONE)
            tabLayout.getTabAt(1)!!.select()
            if (LatestUtils.isConnectionAvailable(context)) {
                Log.d("NewSticLog:", "StickerKeyboardView Yes internet")
                //(context as (Activity)).runOnUiThread {
                tabLayout.visibility = View.VISIBLE
                emojiViewFlipper.visibility = View.VISIBLE
                textView.visibility = View.GONE
                buttonRetry.visibility = View.GONE
                //}
            } else {
                Log.d("NewSticLog:", "StickerKeyboardView No Internet")
                //(context as (Activity)).runOnUiThread {
                tabLayout.visibility = View.GONE
                emojiViewFlipper.visibility = View.GONE
                textView.visibility = View.VISIBLE
                buttonRetry.visibility = View.VISIBLE
                //}
            }
        }
        onApplyThemeAttributes()
    }

    override fun onCreateInputView() {
        super.onCreateInputView()
        Log.d("NewSticLog:", "StickerKeyboardView onCreateInputView")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NewSticLog:", "StickerKeyboardView onCreateInputView")
    }

    override fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {
        super.onRegisterInputView(latestKeyboardView)
        Log.d("NewSticLog:", "StickerKeyboardView onRegisterInputView")
    }

    /*these methods*/
    override fun onStartInputView(instance: LatestServiceHelper, restarting: Boolean) {
        super.onStartInputView(instance, restarting)
        Log.d("NewSticLog:", "StickerKeyboardView onStartInputView")
        if (LatestUtils.isConnectionAvailable(context)) {
            Log.d("NewSticLog:", "StickerKeyboardView Yes internet")
            tabLayout.visibility = View.VISIBLE
            emojiViewFlipper.visibility = View.VISIBLE
            textView.visibility = View.GONE
            buttonRetry.visibility = View.GONE
            setActiveCategory(LatestStickerCategories.STICKER_ONE)
        } else {
            Log.d("NewSticLog:", "StickerKeyboardView No Internet")
            tabLayout.visibility = View.GONE
            emojiViewFlipper.visibility = View.GONE
            textView.visibility = View.VISIBLE
            buttonRetry.visibility = View.VISIBLE
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d("NewSticLog:", "StickerKeyboardView onFinishInputView")
    }

    private fun fillRecentStickersList() {
        listStickersRecent.clear()
        listStickersRecent = stickersDao!!.allStickers as ArrayList<LatestStickerModel>
        Log.d("StickerLogger:", "Db Size: ${listStickersRecent.size}")
    }

    private fun fillAllStickersList() {
        listStickers1 = LatestStickersFillerHelper.fillStickersListOne()
        listStickers2 = LatestStickersFillerHelper.fillStickersListTwo()
        listStickers3 = LatestStickersFillerHelper.fillStickersListThree()
        listStickers4 = LatestStickersFillerHelper.fillStickersListFour()
        listStickers5 = LatestStickersFillerHelper.fillStickersListFive()
        listStickers6 = LatestStickersFillerHelper.fillStickersListSix()
        listStickers7 = LatestStickersFillerHelper.fillStickersListSeven()
        listStickers8 = LatestStickersFillerHelper.fillStickersListEight()
        listStickers9 = LatestStickersFillerHelper.fillStickersListNine()
        listStickers10 = LatestStickersFillerHelper.fillStickersListTen()
        listStickers11 = LatestStickersFillerHelper.fillStickersListEleven()
        listStickers12 = LatestStickersFillerHelper.fillStickersListTwelve()
        listStickers13 = LatestStickersFillerHelper.fillStickersListThirteen()
        listStickers14 = LatestStickersFillerHelper.fillStickersListFourteen()
        listStickers15 = LatestStickersFillerHelper.fillStickersListFifteen()
        listStickers16 = LatestStickersFillerHelper.fillStickersListSixteen()
        listStickers17 = LatestStickersFillerHelper.fillStickersListSeventeen()
        listStickers18 = LatestStickersFillerHelper.fillStickersListEighteen()
        listStickers19 = LatestStickersFillerHelper.fillStickersListNinteen()
        listStickers20 = LatestStickersFillerHelper.fillStickersListTwenty()
    }

    fun setActiveCategory(newActiveCategory: LatestStickerCategories) {
        Log.d(TAG, "setActiveCategory")
        emojiViewFlipper.displayedChild =
            emojiViewFlipper.indexOfChild(uiLayouts[newActiveCategory])
        activeCategory = newActiveCategory
    }

    private suspend fun buildLayout() = withContext(Dispatchers.Default) {
        for (category in LatestStickerCategories.values()) {
            val recyclerView = buildLayoutForCategory(category)
            uiLayouts[category] = recyclerView
            withContext(Dispatchers.Main) {
                emojiViewFlipper.addView(recyclerView)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun buildLayoutForCategory(
        category: LatestStickerCategories
    ): RecyclerView = withContext(Dispatchers.Default) {
        val recyclerView = RecyclerView(context)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = buildAdapterForCategory(category)
        return@withContext recyclerView
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun buildAdapterForCategory(
        category: LatestStickerCategories
    ): LatestStickersAdapter = withContext(Dispatchers.Default) {
        val adapter =
            LatestStickersAdapter(
                context,
                getListFromCategory(category),
                this@LatestStickerKeyboardView
            )
        uiAdapters[category] = adapter
        return@withContext adapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun getListFromCategory(category: LatestStickerCategories): ArrayList<LatestStickerModel> {
        var list = ArrayList<LatestStickerModel>()
        when (category) {
            LatestStickerCategories.STICKER_RECENT -> {
                list = listStickersRecent
            }
            LatestStickerCategories.STICKER_ONE -> {
                list = listStickers1
            }
            LatestStickerCategories.STICKER_TWO -> {
                list = listStickers2
            }
            LatestStickerCategories.STICKER_THIRD -> {
                list = listStickers3
            }
            LatestStickerCategories.STICKER_FOUR -> {
                list = listStickers4
            }
            LatestStickerCategories.STICKER_FIFTH -> {
                list = listStickers5
            }
            LatestStickerCategories.STICKER_SIXTH -> {
                list = listStickers6
            }
            LatestStickerCategories.STICKER_SEVEN -> {
                list = listStickers7
            }
            LatestStickerCategories.STICKER_EIGHT -> {
                list = listStickers8
            }
            LatestStickerCategories.STICKER_NINE -> {
                list = listStickers9
            }
            LatestStickerCategories.STICKER_TEN -> {
                list = listStickers10
            }

            LatestStickerCategories.STICKER_ELEVEN -> {
                list = listStickers11
            }
            LatestStickerCategories.STICKER_TWELVE -> {
                list = listStickers12
            }
            LatestStickerCategories.STICKER_THIRTEEN -> {
                list = listStickers13
            }
            LatestStickerCategories.STICKER_FOURTEEN -> {
                list = listStickers14
            }
            LatestStickerCategories.STICKER_FIFTEEN -> {
                list = listStickers15
            }
            LatestStickerCategories.STICKER_SIXTEEN -> {
                list = listStickers16
            }
            LatestStickerCategories.STICKER_SEVENTEEN -> {
                list = listStickers17
            }
            LatestStickerCategories.STICKER_EIGHTEEN -> {
                list = listStickers18
            }
            LatestStickerCategories.STICKER_NINETEEN -> {
                list = listStickers19
            }
            LatestStickerCategories.STICKER_TWENTY -> {
                list = listStickers20
            }
        }
        return list
    }

    override fun onStickerItemClicked(modelLatest: LatestStickerModel?, itemUri: Uri?) {
        if (itemUri != null) {
            Log.d(TAG, "onStickerItemClicked: $itemUri")
            stickersDao?.insertSingleSticker(modelLatest)
            refreshRecentAdapter()
            latestKeyboardService?.latestMediaHelper?.sendClickedStickerContentUri(itemUri)
        }
    }

    private fun refreshRecentAdapter() {
        listStickersRecent = stickersDao!!.allStickers as ArrayList<LatestStickerModel>
        val adapter =
            LatestStickersAdapter(
                context,
                listStickersRecent,
                this@LatestStickerKeyboardView
            )
        uiAdapters[LatestStickerCategories.STICKER_RECENT] = adapter
        uiLayouts[LatestStickerCategories.STICKER_RECENT]?.adapter = adapter
    }

    override fun onApplyThemeAttributes() {
        Log.d(TAG, "onApplyThemeAttributes")
        /*      tabLayout.tabIconTint = ColorStateList.valueOf(prefs.theme.mediaFgColor)*/
        tabLayout.setSelectedTabIndicatorColor(prefs.mThemingApp.mediaFgColor)
    }

}