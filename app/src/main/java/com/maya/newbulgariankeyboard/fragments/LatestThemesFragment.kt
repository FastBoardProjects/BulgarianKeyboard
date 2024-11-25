package com.maya.newbulgariankeyboard.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.activities.LatestImageCropActivity
import com.maya.newbulgariankeyboard.activities.LatestPremiumActivity
import com.maya.newbulgariankeyboard.adapters.LatestColoredThemesAdapter
import com.maya.newbulgariankeyboard.adapters.LatestColoredThemesAdapter.freeIndexsColorTheme
import com.maya.newbulgariankeyboard.adapters.LatestDefaultThemesAdapter
import com.maya.newbulgariankeyboard.adapters.LatestFontsAdapter.isFreeAllowed
import com.maya.newbulgariankeyboard.adapters.LatestGalleryThemesAdapter
import com.maya.newbulgariankeyboard.adapters.LatestMediaThemesAdapter
import com.maya.newbulgariankeyboard.adapters.LatestMediaThemesAdapter.freeIndexsMediaTheme
import com.maya.newbulgariankeyboard.database.GalleryThemesDao
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.interfaces.LatestDefaultThemeCallback
import com.maya.newbulgariankeyboard.interfaces.LatestGalleryThemeCallback
import com.maya.newbulgariankeyboard.interfaces.LatestMediaThemeCallback
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestLocaleHelper
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestCustomizeHelper
import com.maya.newbulgariankeyboard.main_utils.LatestThemeFillerHelper
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel
import com.maya.newbulgariankeyboard.models.LatestMediaThemeModel
import com.maya.newbulgariankeyboard.models.LatestThemeModel
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts.LatestViewerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class LatestThemesFragment : Fragment(), LatestGalleryThemeCallback, CoroutineScope by MainScope() {

    private var rewardedAd: RewardedAd? = null
    private val TAG = "AppThemes:"
    lateinit var prefs: LatestPreferencesHelper
    private var currentThemeIsNight: Boolean = false
    private var currentThemeResId: Int = 0
    private lateinit var recyclerViewDefault: RecyclerView
    private lateinit var recyclerViewColors: RecyclerView
    private lateinit var recyclerViewGradients: RecyclerView
    private lateinit var recyclerViewDarkGradients: RecyclerView
    private lateinit var recyclerViewLandscapes: RecyclerView
    private lateinit var recyclerViewGalleryThemes: RecyclerView
    private var listDefaultThemes = ArrayList<LatestThemeModel>()
    private var listColoredThemes = ArrayList<LatestThemeModel>()
    private var listLightGradientThemes = ArrayList<LatestMediaThemeModel>()
    private var listDarkGradientThemes = ArrayList<LatestMediaThemeModel>()
    private var listLandscapesThemes = ArrayList<LatestMediaThemeModel>()
    private var listGalleryThemes = ArrayList<LatestGalleryThemeModel>()
    private var mContext: Context? = null
    private lateinit var latestDefaultThemesAdapter: LatestDefaultThemesAdapter
    private lateinit var latestColoredThemesAdapter: LatestColoredThemesAdapter
    private lateinit var latestLightMediaThemesAdapter: LatestMediaThemesAdapter
    private lateinit var latestDarkMediaThemesAdapter: LatestMediaThemesAdapter
    private lateinit var latestLandscapesThemesAdapter: LatestMediaThemesAdapter
    private lateinit var latestGalleryThemesAdapter: LatestGalleryThemesAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var layoutBottomSheet: CardView
    private lateinit var parentLayout: RelativeLayout
    private lateinit var latestInputView: LatestInputView
    lateinit var activeLanguageModel: LanguageModel
    lateinit var latestLocaleHelper: LatestLocaleHelper
    private lateinit var dao: GalleryThemesDao


    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            try {
                val intent = Intent(mContext!!, LatestImageCropActivity::class.java)
                intent.putExtra("Model", uri.toString())
                startActivityForResult(intent, 2)
                Toast.makeText(context, "Activty Started", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                mContext!!, "Cannot get Your Image.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var isNotPurchased = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_themes, container, false)
        initViews(mView)

        isNotPurchased = mContext?.let { LatestBillingHelper(it).shouldApplyMonetization() } == true
        if (!isNotPurchased) {

        }
        lifecycleScope.launch(Dispatchers.Main) {

            // Set the first adapter and wait until it's done
            async { setGalleryThemesAdapter() }.await()

            // Set the second adapter and await its completion
            async { setDefaultThemesAdapter() }.await()

            async { setColorsThemesAdapter() }.await()

            async { setLightGradientThemesAdapter() }.await()
            async { setDarkGradientThemesAdapter() }.await()

            async { setLandscapesThemesAdapter() }.await()

        }
        return mView
    }

    private fun initViews(view: View) {

        dao = LatestRoomDatabase.getInstance(mContext!!).galleryThemesDao()
        prefs = LatestPreferencesHelper.getDefaultInstance(mContext!!)
        prefs.initAppPreferences()
        prefs.sync()
        parentLayout = view.findViewById(R.id.parentLayout)
        layoutBottomSheet = view.findViewById(R.id.layoutBottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.peekHeight
                }
            }

        })
        currentThemeIsNight = prefs.mAppInternal.themeCurrentIsNight
        currentThemeResId = LatestKeyboardService.getDayNightBaseThemeId(currentThemeIsNight)/*ivBack = view.findViewById(R.id.ivBack)
        ivMenu = view.findViewById(R.id.ivMenu)*/

        view.findViewById<CardView>(R.id.cvDone).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Toast.makeText(
                context, "Theme Successfully Applied", Toast.LENGTH_SHORT
            ).show()
        }
        recyclerViewDefault = view.findViewById(R.id.recyclerViewDefault)
        recyclerViewColors = view.findViewById(R.id.recyclerViewColors)
        recyclerViewGradients = view.findViewById(R.id.recyclerViewGradients)
        recyclerViewDarkGradients = view.findViewById(R.id.recyclerViewDarkGradients)
        recyclerViewLandscapes = view.findViewById(R.id.recyclerViewLandscapes)
        recyclerViewGalleryThemes = view.findViewById(R.id.recyclerViewGalleryThemes)


        //Changes till now
        recyclerViewDefault.setHasFixedSize(true)
        recyclerViewColors.setHasFixedSize(true)
        recyclerViewGradients.setHasFixedSize(true)
        recyclerViewDarkGradients.setHasFixedSize(true)
        recyclerViewLandscapes.setHasFixedSize(true)
        recyclerViewGalleryThemes.setHasFixedSize(true)

        launch(Dispatchers.Default) {
            try {
                val themeContext = ContextThemeWrapper(
                    context,
                    LatestKeyboardService.getDayNightBaseThemeId(prefs.mAppInternal.themeCurrentIsNight)
                )
                val layoutManager = LatestViewerHelper(themeContext)
                latestLocaleHelper = LatestLocaleHelper(mContext!!, prefs)
                activeLanguageModel = latestLocaleHelper.getActiveSubtype() ?: LanguageModel.DEFAULT
                latestInputView = LatestInputView(themeContext)
                try {
                    latestInputView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimension(R.dimen.text_keyboard_actual_height).roundToInt()
                    ).apply {
                        val m = resources.getDimension(R.dimen.preview_margin).toInt()
                        setMargins(m, m, m, m)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                latestInputView.isPreviewMode = true
                try {
                    latestInputView.computedLayout = layoutManager.fetchComputedLayoutAsync(
                        LatestInputMode.CHARACTERS, activeLanguageModel, prefs
                    ).await()
                    latestInputView.updateVisibility()
                } catch (e: Exception) {
                    Log.d("CrashLog:", "E: ${e.printStackTrace()}")
                    Log.d("CrashLog:", "Crash: ${e.localizedMessage}")
                }
                latestInputView.onApplyThemeAttributes()
                withContext(Dispatchers.Main) {
                    parentLayout.addView(latestInputView, 0)
                }
            } catch (e: Exception) {
            }
        }
    }


    private suspend fun setDefaultThemesAdapter(): Int {
        return withContext(Dispatchers.Main) {
            listDefaultThemes = LatestThemeFillerHelper.fillDefaultThemes()

            recyclerViewDefault.layoutManager = GridLayoutManager(mContext!!, 1)

            latestDefaultThemesAdapter = LatestDefaultThemesAdapter(
                mContext!!,
                listDefaultThemes,
                object : LatestDefaultThemeCallback {
                    override fun onThemeSelected(modelop: LatestThemeModel?, position: Int) {
                        if (modelop != null) {
                            onDefaultThemeSelectedProcedure(modelop)
                        }
                    }

                })

            recyclerViewDefault.adapter = latestDefaultThemesAdapter
            return@withContext 0
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataChanged() {
        try {
            latestDefaultThemesAdapter.notifyDataSetChanged()
            latestColoredThemesAdapter.notifyDataSetChanged()
            latestLightMediaThemesAdapter.notifyDataSetChanged()
            latestDarkMediaThemesAdapter.notifyDataSetChanged()
            latestLandscapesThemesAdapter.notifyDataSetChanged()
            latestGalleryThemesAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun onDefaultThemeSelectedProcedure(modelop: LatestThemeModel) {
        when (modelop.itemId) {
            1 -> {
                prefs.mThemingApp.isMediaTheme = 1
                val theme = LatestCustomizeHelper.fromJsonFile(
                    mContext!!, "app_assets/theme/app_day_theme.json"
                ) ?: return
                LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
                notifyDataChanged()
            }

            2 -> {
                prefs.mThemingApp.isMediaTheme = 1
                val theme = LatestCustomizeHelper.fromJsonFile(
                    mContext!!, "app_assets/theme/app_night_theme.json"
                ) ?: return
                LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
                notifyDataChanged()

            }

            48 -> {
                if (mContext!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    prefs.mThemingApp.isMediaTheme = 1
                    val theme = LatestCustomizeHelper.fromJsonFile(
                        mContext!!, "app_assets/theme/app_night_theme.json"
                    ) ?: return
                    LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
                    notifyDataChanged()
                } else {
                    prefs.mThemingApp.isMediaTheme = 1
                    val theme = LatestCustomizeHelper.fromJsonFile(
                        mContext!!, "app_assets/theme/app_day_theme.json"
                    ) ?: return
                    LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
                    notifyDataChanged()
                }
            }
        }
        prefs.mAppInternal.themeCurrentIsModified = true
        latestInputView.onApplyThemeAttributes()
        latestInputView.invalidate()
        latestInputView.invalidateAllKeys()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private suspend fun setColorsThemesAdapter(): Int {
        return withContext(Dispatchers.Main) {

            listColoredThemes = LatestThemeFillerHelper.fillColoredThemes()
            recyclerViewColors.layoutManager = GridLayoutManager(mContext!!, 1)
            latestColoredThemesAdapter = LatestColoredThemesAdapter(
                mContext!!,
                listColoredThemes,
                object : LatestDefaultThemeCallback {
                    override fun onThemeSelected(modelop: LatestThemeModel?, position: Int) {

                        if (modelop != null) {

                            if (!LatestColoredThemesAdapter.differentiateTheme(position) && !LatestColoredThemesAdapter.isFreeAllowedColorTheme && !freeIndexsColorTheme.contains(position)) {
                                AlertDialog.Builder(mContext).setTitle(getString(R.string.reward_ad_show_title))
                                    .setMessage(getString(R.string.themes_reward_dailog_des))
                                    .setPositiveButton(getString(R.string.reward_dailog_show_ad_btn)) { dialog, _ ->
                                        dialog.dismiss()

                                        loadAndShowRewardedAD {
                                            freeIndexsColorTheme.add(position)
                                            onColorThemeSelectedProcedure(modelop)
                                        }

                                    }.setNeutralButton(getString(R.string.reward_dailog_get_premium_btn)) { dialog, _ ->
                                        val intent = Intent(mContext, LatestPremiumActivity::class.java)
                                        startActivity(intent)
                                        dialog.dismiss()

                                    }.setNegativeButton(getString(R.string.reward_dailog_cancel_btn)) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()
                            } else {
                                onColorThemeSelectedProcedure(modelop)

                            }
                        }
                    }
                })
            recyclerViewColors.adapter = latestColoredThemesAdapter

            return@withContext 1
        }
    }

    private fun onColorThemeSelectedProcedure(modelop: LatestThemeModel) {
        prefs.mAppInternal.themeCurrentIsNight = false
        prefs.mThemingApp.isMediaTheme = 1
        prefs.mThemingApp.keyboardBgColor = Color.parseColor(modelop.itemBgColor)
        prefs.mThemingApp.keyBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyBgColorPressed = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.oneHandedBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.oneHandedButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyEnterBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyPopupBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupBgColorActive = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftFgColorCapsLock = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.smartbarButtonBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.smartbarButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColorAlt = Color.parseColor(modelop.itemTextColor)

        notifyDataChanged()
        prefs.mAppInternal.themeCurrentIsModified = true
        latestInputView.onApplyThemeAttributes()
        latestInputView.invalidate()
        latestInputView.invalidateAllKeys()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private suspend fun setLightGradientThemesAdapter() {
        withContext(Dispatchers.Main) {

            listLightGradientThemes = LatestThemeFillerHelper.fillLightGradientThemes()
            recyclerViewGradients.layoutManager = GridLayoutManager(mContext!!, 1)
            latestLightMediaThemesAdapter = LatestMediaThemesAdapter(
                mContext!!,
                listLightGradientThemes,
                object : LatestMediaThemeCallback {
                    override fun onThemeSelected(modelop: LatestMediaThemeModel?, position: Int) {

                        prefs.mThemingApp.isMediaTheme = 2
                        if (modelop != null) {

                            if (!LatestColoredThemesAdapter.differentiateTheme(position) && !LatestMediaThemesAdapter.isFreeAllowedMediaTheme && !freeIndexsMediaTheme.contains(position)) {

                                AlertDialog.Builder(mContext).setTitle(getString(R.string.reward_ad_show_title))
                                    .setMessage(getString(R.string.themes_reward_dailog_des))
                                    .setPositiveButton(getString(R.string.reward_dailog_show_ad_btn)) { dialog, _ ->
                                        dialog.dismiss()

                                        loadAndShowRewardedAD {
                                            onLightGradientThemeSelectedProcedure(modelop)
                                            freeIndexsMediaTheme.add(position)

                                        }

                                    }.setNeutralButton(getString(R.string.reward_dailog_get_premium_btn)) { dialog, _ ->
                                        val intent = Intent(mContext, LatestPremiumActivity::class.java)
                                        startActivity(intent)
                                        dialog.dismiss()

                                    }.setNegativeButton(getString(R.string.reward_dailog_cancel_btn)) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()
                            } else {
                                onLightGradientThemeSelectedProcedure(modelop)

                            }
                        }
                    }
                })
            recyclerViewGradients.adapter = latestLightMediaThemesAdapter
        }
    }

    private fun onLightGradientThemeSelectedProcedure(modelop: LatestMediaThemeModel) {
        prefs.mAppInternal.themeCurrentIsNight = false
        prefs.mThemingApp.keyboardBgShape = modelop.itemBgShape
        prefs.mThemingApp.keyBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyBgColorPressed = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.oneHandedBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.oneHandedButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyEnterBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyPopupBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupBgColorActive = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftFgColorCapsLock = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.smartbarButtonBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.smartbarButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColorAlt = Color.parseColor(modelop.itemTextColor)
        notifyDataChanged()
        prefs.mAppInternal.themeCurrentIsModified = true
        latestInputView.onApplyThemeAttributes()
        latestInputView.invalidate()
        latestInputView.invalidateAllKeys()
        latestInputView.setBackgroundResource(modelop.itemBgShape) /*check*/
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private suspend fun setDarkGradientThemesAdapter() {
        withContext(Dispatchers.Main) {

            listDarkGradientThemes = LatestThemeFillerHelper.fillDarkGradientThemes()
            recyclerViewDarkGradients.layoutManager = GridLayoutManager(mContext!!, 1)
            latestDarkMediaThemesAdapter = LatestMediaThemesAdapter(
                mContext!!,
                listDarkGradientThemes,
                object : LatestMediaThemeCallback {
                    override fun onThemeSelected(modelop: LatestMediaThemeModel?, position: Int) {
                        prefs.mThemingApp.isMediaTheme = 2
                        if (modelop != null) {
                            if (!LatestColoredThemesAdapter.differentiateTheme(position) && !LatestMediaThemesAdapter.isFreeAllowedMediaTheme && !freeIndexsMediaTheme.contains(position)) {
                                AlertDialog.Builder(mContext).setTitle(getString(R.string.reward_ad_show_title))
                                    .setMessage(getString(R.string.themes_reward_dailog_des))
                                    .setPositiveButton(getString(R.string.reward_dailog_show_ad_btn)) { dialog, _ ->
                                        dialog.dismiss()

                                        loadAndShowRewardedAD {
                                            onDarkGradientThemeSelectedProcedure(modelop)
                                            freeIndexsMediaTheme.add(position)

                                        }


                                    }.setNeutralButton(getString(R.string.reward_dailog_get_premium_btn)) { dialog, _ ->
                                        val intent = Intent(mContext, LatestPremiumActivity::class.java)
                                        startActivity(intent)
                                        dialog.dismiss()

                                    }.setNegativeButton(getString(R.string.reward_dailog_cancel_btn)) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()
                            } else {
                                onDarkGradientThemeSelectedProcedure(modelop)

                            }
                        }
                    }

                })
            recyclerViewDarkGradients.adapter = latestDarkMediaThemesAdapter
        }
    }

    private fun onDarkGradientThemeSelectedProcedure(modelop: LatestMediaThemeModel) {
        prefs.mAppInternal.themeCurrentIsNight = false
        prefs.mThemingApp.keyboardBgShape = modelop.itemBgShape
        prefs.mThemingApp.keyBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyBgColorPressed = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.oneHandedBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.oneHandedButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyEnterBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyPopupBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupBgColorActive = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftFgColorCapsLock = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.smartbarButtonBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.smartbarButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColorAlt = Color.parseColor(modelop.itemTextColor)
        notifyDataChanged()
        prefs.mAppInternal.themeCurrentIsModified = true
        latestInputView.onApplyThemeAttributes()
        latestInputView.invalidate()
        latestInputView.invalidateAllKeys()
        latestInputView.setBackgroundResource(modelop.itemBgShape)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private suspend fun setLandscapesThemesAdapter() {
        withContext(Dispatchers.Main) {
            listLandscapesThemes = LatestThemeFillerHelper.fillLandscapesThemes()
            recyclerViewLandscapes.layoutManager = GridLayoutManager(mContext!!, 1)
            latestLandscapesThemesAdapter = LatestMediaThemesAdapter(
                mContext!!,
                listLandscapesThemes,
                object : LatestMediaThemeCallback {
                    override fun onThemeSelected(modelop: LatestMediaThemeModel?, position: Int) {
                        prefs.mThemingApp.isMediaTheme = 2
                        if (modelop != null) {

                            if (!LatestColoredThemesAdapter.differentiateTheme(position) && !LatestMediaThemesAdapter.isFreeAllowedMediaTheme && !freeIndexsMediaTheme.contains(position)) {
                                AlertDialog.Builder(mContext).setTitle(getString(R.string.reward_ad_show_title))
                                    .setMessage(getString(R.string.themes_reward_dailog_des))
                                    .setPositiveButton(getString(R.string.reward_dailog_show_ad_btn)) { dialog, _ ->
                                        dialog.dismiss()


                                        loadAndShowRewardedAD {
                                            freeIndexsMediaTheme.add(position)
                                            onLandscapeGradientThemeSelectedProcedure(
                                                modelop
                                            )
                                        }

                                    }.setNeutralButton(getString(R.string.reward_dailog_get_premium_btn)) { dialog, _ ->
                                        val intent = Intent(mContext, LatestPremiumActivity::class.java)
                                        startActivity(intent)
                                        dialog.dismiss()

                                    }.setNegativeButton(getString(R.string.reward_dailog_cancel_btn)) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()
                            } else {
                                onLandscapeGradientThemeSelectedProcedure(
                                    modelop
                                )
                            }

                        }
                    }

                })
            recyclerViewLandscapes.adapter = latestLandscapesThemesAdapter
        }
    }

    private fun onLandscapeGradientThemeSelectedProcedure(modelop: LatestMediaThemeModel) {
        prefs.mAppInternal.themeCurrentIsNight = false
        prefs.mThemingApp.keyboardBgShape = modelop.itemBgShape
        prefs.mThemingApp.keyBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyBgColorPressed = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.keyFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.oneHandedBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.oneHandedButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyEnterBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyEnterFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyPopupBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupBgColorActive = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyPopupFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftBgColor = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftBgColorPressed = Color.parseColor(modelop.itemEnterShiftColor)
        prefs.mThemingApp.keyShiftFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.keyShiftFgColorCapsLock = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.smartbarButtonBgColor = Color.parseColor(modelop.itemKeyShapeColor)
        prefs.mThemingApp.smartbarButtonFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColor = Color.parseColor(modelop.itemTextColor)
        prefs.mThemingApp.mediaFgColorAlt = Color.parseColor(modelop.itemTextColor)
        notifyDataChanged()
        prefs.mAppInternal.themeCurrentIsModified = true
        latestInputView.onApplyThemeAttributes()
        latestInputView.invalidate()
        latestInputView.invalidateAllKeys()
        latestInputView.setBackgroundResource(modelop.itemBgShape)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private suspend fun setGalleryThemesAdapter() {
        withContext(Dispatchers.Main) {

            recyclerViewGalleryThemes.layoutManager = GridLayoutManager(mContext!!, 1)
            listGalleryThemes = dao.allGalleryThemes as ArrayList<LatestGalleryThemeModel>
            listGalleryThemes.add(
                0, LatestGalleryThemeModel(
                    System.currentTimeMillis(), "bg", "#ffffff", "Default", "#ffffff", "#ffffff"
                )
            )
            latestGalleryThemesAdapter = LatestGalleryThemesAdapter(
                mContext!!, listGalleryThemes, this@LatestThemesFragment
            )
            recyclerViewGalleryThemes.adapter = latestGalleryThemesAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onThemePickingClicked() {
        callGalleryIntent()
    }

    override fun onThemeSelected(modelop: LatestGalleryThemeModel?) {
        if (modelop != null) {
            applyGalleryModelToKeyboard(modelop)
        }
    }

    private fun applyGalleryModelToKeyboard(modelop: LatestGalleryThemeModel?) {
        if (modelop != null) {
            prefs.mAppInternal.themeCurrentIsNight = false
            prefs.mThemingApp.isMediaTheme = 3
            val file = File(modelop.itemBgImage)
            if (file.exists()) {
                prefs.mThemingApp.keyboardBgPhoto = modelop.itemBgImage
                prefs.mThemingApp.keyBgColor = Color.parseColor(modelop.itemKeyShapeColor)
                prefs.mThemingApp.keyBgColorPressed = Color.parseColor(modelop.itemKeyShapeColor)
                prefs.mThemingApp.keyFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.oneHandedBgColor = Color.parseColor(modelop.itemKeyShapeColor)
                prefs.mThemingApp.oneHandedButtonFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.keyEnterBgColor = Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyEnterBgColorPressed =
                    Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyEnterFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.keyPopupBgColor = Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyPopupBgColorActive =
                    Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyPopupFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.keyShiftBgColor = Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyShiftBgColorPressed =
                    Color.parseColor(modelop.itemEnterShiftColor)
                prefs.mThemingApp.keyShiftFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.keyShiftFgColorCapsLock =
                    Color.parseColor(modelop!!.itemTextColor)
                prefs.mThemingApp.smartbarButtonBgColor =
                    Color.parseColor(modelop.itemKeyShapeColor)
                prefs.mThemingApp.smartbarButtonFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.mediaFgColor = Color.parseColor(modelop.itemTextColor)
                prefs.mThemingApp.mediaFgColorAlt = Color.parseColor(modelop.itemTextColor)
                notifyDataChanged()
                prefs.mAppInternal.themeCurrentIsModified = true
                latestInputView.onApplyThemeAttributes()
                latestInputView.invalidate()
                latestInputView.invalidateAllKeys()
                val drawable: Drawable? = Drawable.createFromPath(modelop.itemBgImage)
                latestInputView.setBackgroundDrawable(drawable)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                /*file not exists*/
                dao.deleteSingleGalleryTheme(modelop)
                listGalleryThemes.clear()
                listGalleryThemes = dao.allGalleryThemes as ArrayList<LatestGalleryThemeModel>
                listGalleryThemes.add(
                    0, LatestGalleryThemeModel(
                        System.currentTimeMillis(), "bg", "#ffffff", "Default", "#ffffff", "#ffffff"
                    )
                )
                latestGalleryThemesAdapter = LatestGalleryThemesAdapter(
                    mContext!!, listGalleryThemes, this
                )
                recyclerViewGalleryThemes.adapter = latestGalleryThemesAdapter
            }
        }
    }

    override fun onThemeDeleted(model: LatestGalleryThemeModel?) {
        if (model != null) {
            AlertDialog.Builder(mContext!!).setCancelable(true)
                .setIcon(R.drawable.ic_delete_colored_icon).setTitle("Delete Theme?")
                .setMessage("Are you sure to delete this theme?")
                .setPositiveButton("Delete", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        dao.deleteSingleGalleryTheme(model)
                        val file = File(model.itemBgImage)
                        if (file.exists()) {
                            file.delete()
                        }
                        listGalleryThemes.clear()
                        listGalleryThemes =
                            dao.allGalleryThemes as ArrayList<LatestGalleryThemeModel>
                        listGalleryThemes.add(
                            0, LatestGalleryThemeModel(
                                System.currentTimeMillis(),
                                "bg",
                                "#ffffff",
                                "Default",
                                "#ffffff",
                                "#ffffff"
                            )
                        )
                        latestGalleryThemesAdapter = LatestGalleryThemesAdapter(
                            mContext!!, listGalleryThemes, this@LatestThemesFragment
                        )
                        recyclerViewGalleryThemes.adapter = latestGalleryThemesAdapter
                    }

                }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        try {
                            p0!!.dismiss()
                        } catch (e: Exception) {
                        }
                    }

                }).show()
        }
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.M)
    private fun callGalleryIntent() {
        try {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }catch (e : Exception){
            e.printStackTrace()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true || (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true)) {
                // All necessary permissions have been granted
//                imagePickerResultLauncher.launch("image/*")
//                Toast.makeText(requireContext(), "Permissions granted. Launching image picker.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    requireContext(), "Permissions denied. Cannot open gallery.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadAndShowRewardedAD(onRewardGet: OnUserEarnedRewardListener) {

        if (mContext!= null && LatestBillingHelper(mContext!!).shouldApplyMonetization() && LatestUtils.isConnectionAvailable(mContext!!))
        {
            val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_with_progress, null)
            val dialogBuilder = AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.loading_ads))
                .setView(dialogView).show()


            val adRequest = AdRequest.Builder().build()
            mContext?.let {
                RewardedAd.load(it,
                    getString(R.string.rewarded_ads_id),
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(TAG, adError.toString())
                            rewardedAd = null
                            dialogBuilder.dismiss()
                            Toast.makeText(
                                mContext,
                                "Failed to load reward ad. Please try again later.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            Log.d(TAG, "Ad was loaded.")
                            rewardedAd = ad
                            ad.show(mContext as Activity, onRewardGet)
                            dialogBuilder.dismiss()

                        }
                    })
            }
        }
        else{
            LatestUtils.showInternetDialog(requireActivity())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    Log.d(TAG, " Gallery uri: ${uri}")
                    if (uri != null) {
                        val intent = Intent(mContext!!, LatestImageCropActivity::class.java)
                        intent.putExtra("Model", uri.toString())
                        startActivityForResult(intent, 2)
                    }
                } else {
                    Log.d(TAG, "Error: ${requestCode} : ${data}")
                    Toast.makeText(mContext!!, "Cannot get Image.", Toast.LENGTH_SHORT).show()
                }
            }

            2 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val model: LatestGalleryThemeModel? = data.getParcelableExtra("Model")
                    Log.d(TAG, " Cropped model: ${model}")
                    if (model != null) {
                        listGalleryThemes.clear()
                        listGalleryThemes =
                            dao.allGalleryThemes as ArrayList<LatestGalleryThemeModel>
                        listGalleryThemes.add(
                            0, LatestGalleryThemeModel(
                                System.currentTimeMillis(),
                                "bg",
                                "#ffffff",
                                "Default",
                                "#ffffff",
                                "#ffffff"
                            )
                        )
                        latestGalleryThemesAdapter = LatestGalleryThemesAdapter(
                            mContext!!, listGalleryThemes, this
                        )
                        recyclerViewGalleryThemes.adapter =
                            latestGalleryThemesAdapter/*for updating of position 0 theme*/
                        latestGalleryThemesAdapter.setSpecificTheme(model.itemId.toInt())
                        latestGalleryThemesAdapter.notifyDataSetChanged()
                        applyGalleryModelToKeyboard(model)
                    }
                } else {
                    Log.d(TAG, "Error: ${requestCode} : ${data}")
                }
            }
        }
    }

    private var isPurchased = false
    override fun onAttach(context: Context) {
        super.onAttach(context)

        isPurchased = !LatestBillingHelper(context).shouldApplyMonetization()

        Log.d(TAG, "onAttach: $isPurchased")
        if (isPurchased) {
            LatestMediaThemesAdapter.isFreeAllowedMediaTheme = true
            LatestColoredThemesAdapter.isFreeAllowedColorTheme = true
            Log.d("cdsavcadsfvcearfc", "onAttach: IS FREE $isFreeAllowed")
        }

        mContext = context
    }
}