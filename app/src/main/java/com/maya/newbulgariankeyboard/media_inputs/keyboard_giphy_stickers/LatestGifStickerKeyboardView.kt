package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers

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
import com.maya.newbulgariankeyboard.database.GifsDao
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.gif_model.AppGifModel
import com.maya.newbulgariankeyboard.gif_model.Datum
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardView
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_classes.LatestServiceHelper
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs.LatestAppGifAdapter
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs.LatestGifAdapterCallback
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs.LatestGifCategory
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs.LatestGifKeyboardView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers.LatestStickerKeyboardView
import com.maya.newbulgariankeyboard.webclient.LatestGifEndpoints
import com.maya.newbulgariankeyboard.webclient.LatestRetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.EnumMap

class LatestGifStickerKeyboardView : LinearLayout,
    LatestKeyboardService.EventListener
    ,
    LatestGifStickerAdapterCallback {

    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val TAG = "GifKeyboardView:"
    private val mainScope = MainScope()
    private var emojiViewFlipper: ViewFlipper
    private val tabLayout: TabLayout
    private val textView: TextView
    private val buttonRetry: Button
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private var gifsDao: GifsDao? = null
    private var activeCategoryLatest: LatestGifStickerCategory = LatestGifStickerCategory.STICKER_TRENDING
    // private val uiLayouts =
    // EnumMap<GifStickerCategory, RecyclerView>(GifStickerCategory::class.java)

    companion object {
        val uiLayouts =
            EnumMap<LatestGifStickerCategory, RecyclerView>(LatestGifStickerCategory::class.java)
        var listGifsRecent = ArrayList<Datum>()
        var listTrending = ArrayList<Datum>()
        var listHappy = ArrayList<Datum>()
        var listSad = ArrayList<Datum>()
        var listFood = ArrayList<Datum>()
        var listAction = ArrayList<Datum>()
        var listAnimal = ArrayList<Datum>()
        var listCartoon = ArrayList<Datum>()
        var listEmoticon = ArrayList<Datum>()
        var listNature = ArrayList<Datum>()
        var listMusic = ArrayList<Datum>()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        Log.d("NewStickerLog:", "GifStickerKeyboardView Constructor")
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
            ViewGroup.inflate(context, R.layout.media_input_gifs_sticker_tabs, null) as TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setActiveCategory(
                    when (tab?.position) {
                        0 -> LatestGifStickerCategory.STICKER_RECENT
                        1 -> LatestGifStickerCategory.STICKER_TRENDING
                        2 -> LatestGifStickerCategory.STICKER_HAPPY
                        3 -> LatestGifStickerCategory.STICKER_SAD
                        4 -> LatestGifStickerCategory.STICKER_FOOD
                        5 -> LatestGifStickerCategory.STICKER_ACTION
                        6 -> LatestGifStickerCategory.STICKER_ANIMAL
                        7 -> LatestGifStickerCategory.STICKER_CARTOON
                        8 -> LatestGifStickerCategory.STICKER_EMOTICON
                        9 -> LatestGifStickerCategory.STICKER_NATURE
                        10 -> LatestGifStickerCategory.STICKER_MUSIC
                        else -> LatestGifStickerCategory.STICKER_RECENT
                    }
                )
            }

        })
        gifsDao = LatestRoomDatabase.getInstance(context).gifsDao()
        textView = TextView(context)
        textView.text = "Cannot load GIFs Stickers.\nCheck your internet connection.\n"
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
            getTrendingRvList()
            tabLayout.getTabAt(1)!!.select()
            if (LatestUtils.isConnectionAvailable(context)) {
                Log.d("NewGifLog:", "GifStickerKeyboardView Yes internet")
                tabLayout.visibility = View.VISIBLE
                emojiViewFlipper.visibility = View.VISIBLE
                textView.visibility = View.GONE
                buttonRetry.visibility = View.GONE
            } else {
                Toast.makeText(context, "No Internet.", Toast.LENGTH_SHORT).show()
                Log.d("NewGifLog:", "GifStickerKeyboardView No Internet")
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
        Log.d("NewStickerLog:", "GifStickerKeyboardView onAttachedToWindow")
        mainScope.launch {
            buildLayout()
            fillRecentGifsList()
            getTrendingRvList()
            tabLayout.getTabAt(1)!!.select()
            if (LatestUtils.isConnectionAvailable(context)) {
                Log.d("NewStickerLog:", "GifStickerKeyboardView Yes internet")
                // (context as (Activity)).runOnUiThread {
                tabLayout.visibility = View.VISIBLE
                emojiViewFlipper.visibility = View.VISIBLE
                textView.visibility = View.GONE
                buttonRetry.visibility = View.GONE
                //}
            } else {
                Log.d("NewStickerLog:", "GifStickerKeyboardView No Internet")
                //  (context as (Activity)).runOnUiThread {
                tabLayout.visibility = View.GONE
                emojiViewFlipper.visibility = View.GONE
                textView.visibility = View.VISIBLE
                buttonRetry.visibility = View.VISIBLE
                // }
            }
        }
        onApplyThemeAttributes()
    }

    override fun onCreateInputView() {
        super.onCreateInputView()
        Log.d("NewStickerLog:", "GifStickerKeyboardView onCreateInputView")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NewStickerLog:", "GifStickerKeyboardView onCreateInputView")
    }

    override fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {
        super.onRegisterInputView(latestKeyboardView)
        Log.d("NewStickerLog:", "GifStickerKeyboardView onRegisterInputView")
    }

    /*these methods*/
    override fun onStartInputView(instance: LatestServiceHelper, restarting: Boolean) {
        super.onStartInputView(instance, restarting)
        Log.d("NewStickerLog:", "GifStickerKeyboardView onStartInputView")
        if (LatestUtils.isConnectionAvailable(context)) {
            Log.d("NewStickerLog:", "GifStickerKeyboardView Yes internet")
            tabLayout.visibility = View.VISIBLE
            emojiViewFlipper.visibility = View.VISIBLE
            textView.visibility = View.GONE
            buttonRetry.visibility = View.GONE
            getTrendingRvList() /*check*/
        } else {
            Log.d("NewStickerLog:", "GifStickerKeyboardView No Internet")
            tabLayout.visibility = View.GONE
            emojiViewFlipper.visibility = View.GONE
            textView.visibility = View.VISIBLE
            buttonRetry.visibility = View.VISIBLE
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d("NewStickerLog:", "GifKeyboardView onFinishInputView")
    }

    private fun fillRecentGifsList() {
        listGifsRecent.clear()
        listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        Log.d("RecentGifFiller:", "Db Size: ${LatestStickerKeyboardView.listStickersRecent.size}")
    }

    fun setActiveCategory(newActiveCategoryLatest: LatestGifStickerCategory) {
        Log.d(TAG, "setActiveCategory")
        when (newActiveCategoryLatest) {
            LatestGifStickerCategory.STICKER_RECENT -> {
                val adapter =
                    LatestGifStickersAdapter(
                        context,
                        listGifsRecent,
                        this@LatestGifStickerKeyboardView
                    )
                uiLayouts[newActiveCategoryLatest]!!.adapter = adapter
            }
            LatestGifStickerCategory.STICKER_TRENDING -> {
                /*this list is already made to show user*/
                val adapter =
                    LatestGifStickersAdapter(
                        context,
                        listTrending,
                        this@LatestGifStickerKeyboardView
                    )
                uiLayouts[newActiveCategoryLatest]!!.adapter = adapter
            }
            LatestGifStickerCategory.STICKER_HAPPY -> {
                if (listHappy.size > 0) {
                    Log.d(TAG, "List Happy already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listHappy,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_HAPPY]!!.adapter = adapter
                } else {
                    getHappyRvList()
                }
            }
            LatestGifStickerCategory.STICKER_SAD -> {
                if (listSad.size > 0) {
                    Log.d(TAG, "List Sad already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listSad,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_SAD]!!.adapter = adapter
                } else {
                    getSadRvList()
                }
            }

            LatestGifStickerCategory.STICKER_FOOD -> {
                if (listFood.size > 0) {
                    Log.d(TAG, "List Food already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listFood,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_FOOD]!!.adapter = adapter
                } else {
                    getFoodRvList()
                }
            }
            LatestGifStickerCategory.STICKER_ACTION -> {
                if (listAction.size > 0) {
                    Log.d(TAG, "List Action already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listAction,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_ACTION]!!.adapter = adapter
                } else {
                    getActionRvList()
                }
            }
            LatestGifStickerCategory.STICKER_ANIMAL -> {
                if (listAnimal.size > 0) {
                    Log.d(TAG, "List Animal already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listAnimal,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_ANIMAL]!!.adapter = adapter
                } else {
                    getAnimalRvList()
                }
            }
            LatestGifStickerCategory.STICKER_CARTOON -> {
                if (listCartoon.size > 0) {
                    Log.d(TAG, "List Cartoon already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listCartoon,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_CARTOON]!!.adapter = adapter
                } else {
                    getCartoonRvList()
                }
            }
            LatestGifStickerCategory.STICKER_EMOTICON -> {
                if (listEmoticon.size > 0) {
                    Log.d(TAG, "List Emoticon already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listEmoticon,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_EMOTICON]!!.adapter = adapter
                } else {
                    getEmoticonRvList()
                }
            }
            LatestGifStickerCategory.STICKER_NATURE -> {
                if (listNature.size > 0) {
                    Log.d(TAG, "List Nature already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listNature,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_NATURE]!!.adapter = adapter
                } else {
                    getNatureRvList()
                }
            }
            LatestGifStickerCategory.STICKER_MUSIC -> {
                if (listMusic.size > 0) {
                    Log.d(TAG, "List Music already fetched")
                    val adapter =
                        LatestGifStickersAdapter(
                            context,
                            listMusic,
                            this@LatestGifStickerKeyboardView
                        )
                    uiLayouts[LatestGifStickerCategory.STICKER_MUSIC]!!.adapter = adapter
                } else {
                    getMusicRvList()
                }
            }
        }
        emojiViewFlipper.displayedChild =
            emojiViewFlipper.indexOfChild(uiLayouts[newActiveCategoryLatest])
        activeCategoryLatest = newActiveCategoryLatest
    }

    private suspend fun buildLayout() = withContext(Dispatchers.Default) {
        for (category in LatestGifStickerCategory.values()) {
            val recyclerView = buildLayoutForCategory(category)
            uiLayouts[category] = recyclerView
            withContext(Dispatchers.Main) {
                emojiViewFlipper.addView(recyclerView)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun buildLayoutForCategory(
        categoryLatest: LatestGifStickerCategory
    ): RecyclerView = withContext(Dispatchers.Default) {
        val recyclerView = RecyclerView(context)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        return@withContext recyclerView
    }

    override fun onGifItemClicked(itemUri: Uri?, model: Datum?) {
        if (itemUri != null && model != null) {
            Log.d(TAG, "onGifItemClicked: $itemUri")
            gifsDao?.insertSingleGif(model)
            refreshRecentGifsAdapter()
            latestKeyboardService?.latestMediaHelper?.sendClickedGifContentUri(itemUri)
        }
    }

    private fun refreshRecentGifsAdapter() {
        listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        val adapter =
            LatestGifStickersAdapter(
                context,
                listGifsRecent,
                this@LatestGifStickerKeyboardView
            )
        uiLayouts[LatestGifStickerCategory.STICKER_RECENT]?.adapter = adapter

        LatestGifKeyboardView.listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        val adapterOther =
            LatestAppGifAdapter(
                context,
                LatestGifKeyboardView.listGifsRecent, object :
                    LatestGifAdapterCallback {
                    override fun onGifItemClicked(itemUri: Uri?, model: Datum?) {
                        if (model != null && itemUri != null) {
                            Log.d(TAG, "onGifItemClicked: $itemUri")
                            gifsDao?.insertSingleGif(model)
                            refreshRecentGifsAdapter()
                            latestKeyboardService?.latestMediaHelper?.sendClickedGifContentUri(itemUri)
                        }
                    }

                }
            )
        LatestGifKeyboardView.uiLayouts[LatestGifCategory.GIF_RECENT]?.adapter = adapterOther
    }

    fun getTrendingRvList() {
        var list = ArrayList<Datum>()
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getTrendingStickersFromGiphy(
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listTrending = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listTrending,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "again adapter setting: STICKER_TRENDING")
                            uiLayouts[LatestGifStickerCategory.STICKER_TRENDING]!!.adapter = adapter
                            Log.d(TAG, " list:${list.size}")
                        } catch (e: Exception) {
                        }
                        //setActiveCategory(StickerCategory.STICKER_TRENDING)
                    }
                }
            }

        })
    }

    fun getHappyRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Happy",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listHappy = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listHappy,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_Happy")
                            uiLayouts[LatestGifStickerCategory.STICKER_HAPPY]!!.adapter = adapter
                            Log.d(TAG, " list:${listHappy.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getSadRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Sad",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listSad = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listSad,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_Happy")
                            uiLayouts[LatestGifStickerCategory.STICKER_SAD]!!.adapter = adapter
                            Log.d(TAG, " list:${listSad.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getFoodRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Food",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listFood = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listFood,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_FOOD")
                            uiLayouts[LatestGifStickerCategory.STICKER_FOOD]!!.adapter = adapter
                            Log.d(TAG, " list:${listFood.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getActionRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Action",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listAction = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listAction,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_ACTION")
                            uiLayouts[LatestGifStickerCategory.STICKER_ACTION]!!.adapter = adapter
                            Log.d(TAG, " list:${listAction.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getAnimalRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Animal",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listAnimal = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listAnimal,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_ANIMAL")
                            uiLayouts[LatestGifStickerCategory.STICKER_ANIMAL]!!.adapter = adapter
                            Log.d(TAG, " list:${listAnimal.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getCartoonRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Cartoon",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listCartoon = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listCartoon,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_CARTOON")
                            uiLayouts[LatestGifStickerCategory.STICKER_CARTOON]!!.adapter = adapter
                            Log.d(TAG, " list:${listCartoon.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getEmoticonRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Emoticon",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listEmoticon = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listEmoticon,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_EMOTICON")
                            uiLayouts[LatestGifStickerCategory.STICKER_EMOTICON]!!.adapter = adapter
                            Log.d(TAG, " list:${listEmoticon.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getNatureRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Nature",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listNature = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listNature,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_NATURE")
                            uiLayouts[LatestGifStickerCategory.STICKER_NATURE]!!.adapter = adapter
                            Log.d(TAG, " list:${listNature.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    fun getMusicRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedStickersFromGiphy(
            "Music",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(
                call: Call<AppGifModel>,
                response: Response<AppGifModel>
            ) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listMusic = model.data as ArrayList<Datum>
                            val adapter =
                                LatestGifStickersAdapter(
                                    context,
                                    listMusic,
                                    this@LatestGifStickerKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: STICKER_SAD")
                            uiLayouts[LatestGifStickerCategory.STICKER_MUSIC]!!.adapter = adapter
                            Log.d(TAG, " list:${listMusic.size}")
                        } catch (e: Exception) {
                        }
                    }
                }
            }

        })
    }

    override fun onApplyThemeAttributes() {
        super.onApplyThemeAttributes()
        tabLayout.tabIconTint = ColorStateList.valueOf(prefs.mThemingApp.mediaFgColor)
        tabLayout.setSelectedTabIndicatorColor(prefs.mThemingApp.mediaFgColor)
    }


}