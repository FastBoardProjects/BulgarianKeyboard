package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs

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
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers.LatestGifStickerAdapterCallback
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers.LatestGifStickerCategory
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers.LatestGifStickerKeyboardView
import com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers.LatestGifStickersAdapter
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

class LatestGifKeyboardView : LinearLayout,
    LatestKeyboardService.EventListener
    ,
    LatestGifAdapterCallback {

    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val TAG = "GifKeyboardView:"
    private val mainScope = MainScope()
    private var emojiViewFlipper: ViewFlipper
    private val tabLayout: TabLayout
    private val textView: TextView
    private val buttonRetry: Button
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private var gifsDao: GifsDao? = null
    private var activeCategoryLatest: LatestGifCategory = LatestGifCategory.GIF_TRENDING
    //private val uiLayouts = EnumMap<GifCategory, RecyclerView>(GifCategory::class.java)

    companion object {
        val uiLayouts = EnumMap<LatestGifCategory, RecyclerView>(LatestGifCategory::class.java)
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
        Log.d("NewGifLog:", "GifKeyboardView Constructor")
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        gravity = Gravity.CENTER
        orientation = VERTICAL
        emojiViewFlipper = ViewFlipper(context)
        emojiViewFlipper.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
            weight = 1.0f
        }
        tabLayout =
            ViewGroup.inflate(context, R.layout.media_input_gifs_tabs, null) as TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                setActiveCategory(
                    when (tab?.position) {
                        0 -> LatestGifCategory.GIF_RECENT
                        1 -> LatestGifCategory.GIF_TRENDING
                        2 -> LatestGifCategory.GIF_HAPPY
                        3 -> LatestGifCategory.GIF_SAD
                        4 -> LatestGifCategory.GIF_FOOD
                        5 -> LatestGifCategory.GIF_ACTION
                        6 -> LatestGifCategory.GIF_ANIMAL
                        7 -> LatestGifCategory.GIF_CARTOON
                        8 -> LatestGifCategory.GIF_EMOTICON
                        9 -> LatestGifCategory.GIF_NATURE
                        10 -> LatestGifCategory.GIF_MUSIC
                        else -> LatestGifCategory.GIF_RECENT
                    }
                )
            }

        })
        //tabLayout.getTabAt(0)!!.icon!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
        gifsDao = LatestRoomDatabase.getInstance(context).gifsDao()
        textView = TextView(context)
        textView.text = "Cannot load GIFs.\nCheck your internet connection.\n"
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
                Log.d("NewGifLog:", "GifKeyboardView Yes internet")
                tabLayout.visibility = View.VISIBLE
                emojiViewFlipper.visibility = View.VISIBLE
                textView.visibility = View.GONE
                buttonRetry.visibility = View.GONE
            } else {
                Toast.makeText(context, "No Internet.", Toast.LENGTH_SHORT).show()
                Log.d("NewGifLog:", "GifKeyboardView No Internet")
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
        Log.d("NewGifLog:", "GifKeyboardView onAttachedToWindow")
        mainScope.launch {
            buildLayout()
            fillRecentGifsList()
            getTrendingRvList()
            tabLayout.getTabAt(1)!!.select()
            if (LatestUtils.isConnectionAvailable(context)) {
                Log.d("NewGifLog:", "GifKeyboardView Yes internet")
                //(context as (Activity)).runOnUiThread {
                    tabLayout.visibility = View.VISIBLE
                    emojiViewFlipper.visibility = View.VISIBLE
                    textView.visibility = View.GONE
                    buttonRetry.visibility = View.GONE
                //}
            } else {
                Log.d("NewGifLog:", "GifKeyboardView No Internet")
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
        Log.d("NewGifLog:", "GifKeyboardView onCreateInputView")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NewGifLog:", "GifKeyboardView onCreateInputView")
    }

    override fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {
        super.onRegisterInputView(latestKeyboardView)
        Log.d("NewGifLog:", "GifKeyboardView onRegisterInputView")
    }

    /*these methods*/
    override fun onStartInputView(instance: LatestServiceHelper, restarting: Boolean) {
        super.onStartInputView(instance, restarting)
        Log.d("NewGifLog:", "GifKeyboardView onStartInputView")
        if (LatestUtils.isConnectionAvailable(context)) {
            Log.d("NewGifLog:", "GifKeyboardView Yes internet")
            tabLayout.visibility = View.VISIBLE
            emojiViewFlipper.visibility = View.VISIBLE
            textView.visibility = View.GONE
            buttonRetry.visibility = View.GONE
            getTrendingRvList() /*check*/
        } else {
            Log.d("NewGifLog:", "GifKeyboardView No Internet")
            tabLayout.visibility = View.GONE
            emojiViewFlipper.visibility = View.GONE
            textView.visibility = View.VISIBLE
            buttonRetry.visibility = View.VISIBLE
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d("NewGifLog:", "GifKeyboardView onFinishInputView")
    }

    private fun fillRecentGifsList() {
        listGifsRecent.clear()
        listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        Log.d("RecentGifFiller:", "Db Size: ${LatestStickerKeyboardView.listStickersRecent.size}")
    }

    fun setActiveCategory(newActiveCategoryLatest: LatestGifCategory) {
        Log.d(TAG, "setActiveCategory")
        when (newActiveCategoryLatest) {
            LatestGifCategory.GIF_RECENT -> {
                val adapter =
                    LatestAppGifAdapter(
                        context,
                        listGifsRecent,
                        this@LatestGifKeyboardView
                    )
                uiLayouts[newActiveCategoryLatest]!!.adapter = adapter
            }
            LatestGifCategory.GIF_TRENDING -> {
                /*   val adapter = AppGifAdapter(context, listTrending, this@GifKeyboardView)
                   uiLayouts[newActiveCategory]!!.adapter = adapter*/
                if (listTrending.size > 0) {  /*24/4/21*/
                    Log.d(TAG, "List Trending already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listTrending,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_TRENDING]!!.adapter = adapter
                } else {
                    getTrendingRvList()
                }
            }
            LatestGifCategory.GIF_HAPPY -> {
                if (listHappy.size > 0) {
                    Log.d(TAG, "List Happy already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listHappy,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_HAPPY]!!.adapter = adapter
                } else {
                    getHappyRvList()
                }
            }
            LatestGifCategory.GIF_SAD -> {
                if (listSad.size > 0) {
                    Log.d(TAG, "List Sad already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listSad,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_SAD]!!.adapter = adapter
                } else {
                    getSadRvList()
                }
            }

            LatestGifCategory.GIF_FOOD -> {
                if (listFood.size > 0) {
                    Log.d(TAG, "List Food already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listFood,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_FOOD]!!.adapter = adapter
                } else {
                    getFoodRvList()
                }
            }
            LatestGifCategory.GIF_ACTION -> {
                if (listAction.size > 0) {
                    Log.d(TAG, "List Action already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listAction,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_ACTION]!!.adapter = adapter
                } else {
                    getActionRvList()
                }
            }
            LatestGifCategory.GIF_ANIMAL -> {
                if (listAnimal.size > 0) {
                    Log.d(TAG, "List Animal already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listAnimal,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_ANIMAL]!!.adapter = adapter
                } else {
                    getAnimalRvList()
                }
            }
            LatestGifCategory.GIF_CARTOON -> {
                if (listCartoon.size > 0) {
                    Log.d(TAG, "List Cartoon already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listCartoon,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_CARTOON]!!.adapter = adapter
                } else {
                    getCartoonRvList()
                }
            }
            LatestGifCategory.GIF_EMOTICON -> {
                if (listEmoticon.size > 0) {
                    Log.d(TAG, "List Emoticon already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listEmoticon,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_EMOTICON]!!.adapter = adapter
                } else {
                    getEmoticonRvList()
                }
            }
            LatestGifCategory.GIF_NATURE -> {
                if (listNature.size > 0) {
                    Log.d(TAG, "List Nature already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listNature,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_NATURE]!!.adapter = adapter
                } else {
                    getNatureRvList()
                }
            }
            LatestGifCategory.GIF_MUSIC -> {
                if (listMusic.size > 0) {
                    Log.d(TAG, "List Music already fetched")
                    val adapter =
                        LatestAppGifAdapter(
                            context,
                            listMusic,
                            this@LatestGifKeyboardView
                        )
                    uiLayouts[LatestGifCategory.GIF_MUSIC]!!.adapter = adapter
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
        for (category in LatestGifCategory.values()) {
            val recyclerView = buildLayoutForCategory(category)
            uiLayouts[category] = recyclerView
            withContext(Dispatchers.Main) {
                emojiViewFlipper.addView(recyclerView)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private suspend fun buildLayoutForCategory(
        categoryLatest: LatestGifCategory
    ): RecyclerView = withContext(Dispatchers.Default) {
        val recyclerView = RecyclerView(context)
        recyclerView.hasFixedSize()
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        return@withContext recyclerView
    }

    override fun onGifItemClicked(itemUri: Uri?, model: Datum?) {
        if (model != null && itemUri != null) {
            Log.d(TAG, "onGifItemClicked: $itemUri")
            gifsDao?.insertSingleGif(model)
            refreshRecentGifsAdapter()
            latestKeyboardService?.latestMediaHelper?.sendClickedGifContentUri(itemUri)
        }
    }

    /*also updating sticker gif recent adapter because we are using same db*/
    private fun refreshRecentGifsAdapter() {
        listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        val adapter =
            LatestAppGifAdapter(
                context,
                listGifsRecent,
                this@LatestGifKeyboardView
            )
        uiLayouts[LatestGifCategory.GIF_RECENT]?.adapter = adapter
        /*to refresh other sticker recent list and its adapter update*/
        LatestGifStickerKeyboardView.listGifsRecent = gifsDao!!.allGifs as ArrayList<Datum>
        val adapterOther =
            LatestGifStickersAdapter(
                context,
                LatestGifStickerKeyboardView.listGifsRecent, object :
                    LatestGifStickerAdapterCallback {
                    override fun onGifItemClicked(itemUri: Uri?, model: Datum?) {
                        if (itemUri != null && model != null) {
                            Log.d(TAG, "onGifItemClicked: $itemUri")
                            gifsDao?.insertSingleGif(model)
                            refreshRecentGifsAdapter()
                            latestKeyboardService?.latestMediaHelper?.sendClickedGifContentUri(itemUri)
                        }
                    }
                }
            )
        LatestGifStickerKeyboardView.uiLayouts[LatestGifStickerCategory.STICKER_RECENT]?.adapter = adapterOther
    }

    fun getTrendingRvList() {
        var list = ArrayList<Datum>()
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getTrendingGifsFromGiphy(
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        listTrending = model.data as ArrayList<Datum>
                        val adapter =
                            LatestAppGifAdapter(
                                context,
                                listTrending,
                                this@LatestGifKeyboardView
                            )
                        Log.d(TAG, "again adapter setting: GIF_TRENDING")
                        uiLayouts[LatestGifCategory.GIF_TRENDING]!!.adapter = adapter
                        //setActiveCategory(GifCategory.GIF_TRENDING)
                        Log.d(TAG, " list:${list.size}")
                    }
                }
            }

        })
    }

    fun getHappyRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Happy",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        listHappy = model.data as ArrayList<Datum>
                        val adapter =
                            LatestAppGifAdapter(
                                context,
                                listHappy,
                                this@LatestGifKeyboardView
                            )
                        Log.d(TAG, "Adapter setting: GIF_Happy")
                        uiLayouts[LatestGifCategory.GIF_HAPPY]!!.adapter = adapter
                        Log.d(TAG, " list:${listHappy.size}")
                    }
                }
            }

        })
    }

    fun getSadRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Sad",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        listSad = model.data as ArrayList<Datum>
                        val adapter =
                            LatestAppGifAdapter(
                                context,
                                listSad,
                                this@LatestGifKeyboardView
                            )
                        Log.d(TAG, "Adapter setting: GIF_Happy")
                        uiLayouts[LatestGifCategory.GIF_SAD]!!.adapter = adapter
                        Log.d(TAG, " list:${listSad.size}")
                    }
                }
            }

        })
    }

    fun getFoodRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Food",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        listFood = model.data as ArrayList<Datum>
                        val adapter =
                            LatestAppGifAdapter(
                                context,
                                listFood,
                                this@LatestGifKeyboardView
                            )
                        Log.d(TAG, "Adapter setting: GIF_FOOD")
                        uiLayouts[LatestGifCategory.GIF_FOOD]!!.adapter = adapter
                        Log.d(TAG, " list:${listFood.size}")
                    }
                }
            }

        })
    }

    fun getActionRvList() {
        val apiInterface =
            LatestRetrofitInstance.getRetrofitAppInstance(context).create(
                LatestGifEndpoints::class.java)
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Action",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listAction = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listAction,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_ACTION")
                            uiLayouts[LatestGifCategory.GIF_ACTION]!!.adapter = adapter
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
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Animal",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listAnimal = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listAnimal,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_ANIMAL")
                            uiLayouts[LatestGifCategory.GIF_ANIMAL]!!.adapter = adapter
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
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Cartoon",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listCartoon = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listCartoon,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_CARTOON")
                            uiLayouts[LatestGifCategory.GIF_CARTOON]!!.adapter = adapter
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
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Emoticon",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listEmoticon = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listEmoticon,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_EMOTICON")
                            uiLayouts[LatestGifCategory.GIF_EMOTICON]!!.adapter = adapter
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
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Nature",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listNature = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listNature,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_NATURE")
                            uiLayouts[LatestGifCategory.GIF_NATURE]!!.adapter = adapter
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
        val calltoApi = apiInterface.getSearchedGifsFromGiphy(
            "Music",
            context.getString(R.string.giphy_api_key)
            , 24
        )
        calltoApi.enqueue(object : Callback<AppGifModel> {
            override fun onFailure(call: Call<AppGifModel>, t: Throwable) {
                Log.d(TAG, " onFailure Retrofit: ${t.localizedMessage}")
            }

            override fun onResponse(call: Call<AppGifModel>, response: Response<AppGifModel>) {
                Log.d(TAG, " onResponse Retrofit:")
                if (response.isSuccessful) {
                    val model: AppGifModel? = response.body()
                    if (model != null) {
                        try {
                            listMusic = model.data as ArrayList<Datum>
                            val adapter =
                                LatestAppGifAdapter(
                                    context,
                                    listMusic,
                                    this@LatestGifKeyboardView
                                )
                            Log.d(TAG, "Adapter setting: GIF_SAD")
                            uiLayouts[LatestGifCategory.GIF_MUSIC]!!.adapter = adapter
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