package com.maya.newbulgariankeyboard.activities

import com.maya.newbulgariankeyboard.fragments.LatestHomeBottomSheet
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.app_update_review.AppUpdateHelper
import com.maya.newbulgariankeyboard.dialogs.LatestDownloadDbDialog
import com.maya.newbulgariankeyboard.fragments.LatestCorrectionFragment
import com.maya.newbulgariankeyboard.fragments.LatestEditingPanelFragment
import com.maya.newbulgariankeyboard.fragments.LatestFavouritesLanguagesFragment
import com.maya.newbulgariankeyboard.fragments.LatestFontsFragment
import com.maya.newbulgariankeyboard.fragments.LatestGesturesFragment
import com.maya.newbulgariankeyboard.fragments.LatestHomeFragment
import com.maya.newbulgariankeyboard.fragments.LatestOneHandedFragment
import com.maya.newbulgariankeyboard.fragments.LatestPreferenceFragment
import com.maya.newbulgariankeyboard.fragments.LatestSuggestionsFragment
import com.maya.newbulgariankeyboard.fragments.LatestThemesFragment
import com.maya.newbulgariankeyboard.fragments.LatestVoiceTypingFragment
import com.maya.newbulgariankeyboard.interfaces.LatestDownloadingDbCallback
import com.maya.newbulgariankeyboard.interfaces.LatestHomeItemCallback
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestLocaleHelper
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.main_utils.LatestUtils.Companion.getDbFilePathAndroid11
import com.maya.newbulgariankeyboard.monetization.LatestAdmobHelper
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.monetization.LatestConsentManager
import com.maya.newbulgariankeyboard.monetization.LatestKeyboardClass.Companion.isShowingAd
import com.maya.newbulgariankeyboard.pick_language.LanguagePickerActivity
import com.maya.newbulgariankeyboard.utils.CommonFun
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme
import com.maya.newbulgariankeyboard.utils.CommonFun.inAppReview
import kotlinx.android.synthetic.main.activity_home_drawer.drawerLayout
import kotlinx.android.synthetic.main.activity_home_drawer.navigationView
import kotlinx.android.synthetic.main.activity_home_layout.enableTV_Click
import kotlinx.android.synthetic.main.activity_home_layout.giftBoxAnimHome
import kotlinx.android.synthetic.main.activity_home_layout.ivBack
import kotlinx.android.synthetic.main.activity_home_layout.ivFamilyAppsHome
import kotlinx.android.synthetic.main.activity_home_layout.selectTV_Click
import kotlinx.android.synthetic.main.activity_home_layout.tvHeader
import kotlinx.android.synthetic.main.layout_update_snackbar.progressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.util.Locale

class LatestHomeActivity : AppCompatActivity(), View.OnClickListener, LatestHomeItemCallback,
    NavigationView.OnNavigationItemSelectedListener, InstallStateUpdatedListener {

    private lateinit var prefs: LatestPreferencesHelper
    private lateinit var prefsShowPrivacy: SharedPreferences
    private var mShowPrivacy = false
    lateinit var imm: InputMethodManager

    private lateinit var dialogLatest: LatestDownloadDbDialog
    private lateinit var latestLocaleHelper: LatestLocaleHelper
    var DB_PATH: String? = null
    var DB_NAME: String? = null
    private lateinit var latestBillingHelper: LatestBillingHelper

    private lateinit var appUpdateManager: AppUpdateManager
    private var appUpdateInfoTask: com.google.android.gms.tasks.Task<AppUpdateInfo>? = null
    private lateinit var latestFavouritesLanguagesFragment: LatestFavouritesLanguagesFragment
    private lateinit var latestPreferenceFragment: LatestPreferenceFragment
    private lateinit var latestThemesFragment: LatestThemesFragment
    private lateinit var latestCorrectionFragment: LatestCorrectionFragment
    private lateinit var latestGesturesFragment: LatestGesturesFragment
    private lateinit var latestVoiceTypingFragment: LatestVoiceTypingFragment
    private lateinit var latestSuggestionsFragment: LatestSuggestionsFragment
    private lateinit var latestOneHandedFragment: LatestOneHandedFragment
    private lateinit var latestEditingPanelFragment: LatestEditingPanelFragment
    private lateinit var latestFontsFragment: LatestFontsFragment

    private lateinit var latestHomeFragment: LatestHomeFragment
    private var positionReceived = 0

    private lateinit var latestConsentManager: LatestConsentManager
    private fun appUpdater() {
        appUpdateHelper.checkForUpdates(activityResultLauncher)
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "Error in app updating", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var appUpdateHelper: AppUpdateHelper


    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    123
                )
            }
        }


        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        appUpdateHelper = AppUpdateHelper(this)
        appUpdateHelper.initialize()

        appUpdater()
        inAppReview(this)

        prefs = LatestPreferencesHelper.getDefaultInstance(this)
        prefs.initAppPreferences()
        prefs.sync()


        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal, this@LatestHomeActivity)
        setContentView(R.layout.activity_home_drawer)
        initViews()

        val fab: FloatingActionButton = findViewById(R.id.ivKeyboardShowHide)

        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        val clickThreshold = 10

        fab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (Math.abs(event.rawX - startX) < clickThreshold && Math.abs(event.rawY - startY) < clickThreshold) {
                        v.performClick()
                    }
                    true
                }

                else -> false
            }
        }

        fab.setOnClickListener {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        checkInternetConnection()

        latestConsentManager = LatestConsentManager.getInstance(this@LatestHomeActivity)

        val consentItem = navigationView.menu
        prefsShowPrivacy = getSharedPreferences("consentItemPrefs", Context.MODE_PRIVATE)

        if (latestConsentManager.isPrivacyOptionsRequired) {
            mShowPrivacy = prefsShowPrivacy.getBoolean("mShowPrivacy", true)
            consentItem.findItem(R.id.mobileAdsConsentItem).isVisible = true
        } else {
            prefsShowPrivacy.edit().putBoolean("mShowPrivacy", false).apply()
            consentItem.findItem(R.id.mobileAdsConsentItem).isVisible = false
        }

        appUpdater()

        if (LatestBillingHelper(this@LatestHomeActivity).shouldApplyMonetization() && LatestUtils.isConnectionAvailable(
                this@LatestHomeActivity
            )
        ) {
            initBannerAds()


            initFullScreenAds()
        }
    }


    private fun initBannerAds() {
        val shimmerContainer = findViewById<ShimmerFrameLayout>(R.id.shimmer_ad_container)
        shimmerContainer.visibility = View.VISIBLE
        shimmerContainer.startShimmer()


        val adSMContainer = findViewById<LinearLayout>(R.id.ad_small_sontainer)
        val adView = AdView(this@LatestHomeActivity)
        adView.adUnitId = getString(R.string.banner_ads_id)
        val adSize = CommonFun.getAdSize(resources, this@LatestHomeActivity)
        adView.setAdSize(adSize)
        val adRequest = AdRequest.Builder().build()
        if (LatestBillingHelper(this@LatestHomeActivity).shouldApplyMonetization() && LatestUtils.isConnectionAvailable(
                mContext = this@LatestHomeActivity
            )
        ) {
            adView.loadAd(adRequest)
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Hide shimmer effect and show ad when ad is loaded
                    shimmerContainer.stopShimmer()
                    shimmerContainer.visibility = View.GONE
                    adSMContainer.visibility = View.VISIBLE
//                    adView.visibility = View.VISIBLE
//                    Toast.makeText(this@LatestHomeActivity, "AD Loaded", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Optionally, stop shimmer and hide ad if loading fails
                    shimmerContainer.stopShimmer()
//                    shimmerContainer.visibility = View.GONE
//                    adView.visibility = View.VISIBLE
                }

                override fun onAdOpened() {
                    // Handle when ad is opened, if needed
                }

                override fun onAdClicked() {
                    // Handle when ad is clicked, if needed
                }

                override fun onAdClosed() {
                    // Reload ad if necessary after it's closed
                    adView.loadAd(adRequest)
                }
            }
            adSMContainer.addView(adView)
        }
    }

    private fun initFullScreenAds() {
        LatestAdmobHelper.loadAppAdFullScreenAd(this)
    }

    private fun initViews() {


        latestHomeFragment = LatestHomeFragment()
        latestFontsFragment = LatestFontsFragment()
        latestFavouritesLanguagesFragment = LatestFavouritesLanguagesFragment()
        latestPreferenceFragment = LatestPreferenceFragment()
        latestThemesFragment = LatestThemesFragment()
        latestCorrectionFragment = LatestCorrectionFragment()
        latestGesturesFragment = LatestGesturesFragment()
        latestVoiceTypingFragment = LatestVoiceTypingFragment()
        latestSuggestionsFragment = LatestSuggestionsFragment()
        latestOneHandedFragment = LatestOneHandedFragment()
        latestEditingPanelFragment = LatestEditingPanelFragment()

        navigationView.setNavigationItemSelectedListener(this)

        dialogLatest = LatestDownloadDbDialog(this, object : LatestDownloadingDbCallback {
            override fun onCancelled() {
            }

        })

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        latestBillingHelper = LatestBillingHelper(this)
        DB_PATH = getDbFilePathAndroid11(this)
        DB_NAME = getDatabaseName()
        ivBack.setOnClickListener(this)

        giftBoxAnimHome.setOnClickListener(this)
        ivFamilyAppsHome.setOnClickListener(this)


        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->

            val isImeEnabled = LatestKeyboardService.checkForEnablingOfIme(this)
            val isImeSelected = LatestKeyboardService.checkOfSelectionOfIme(this)

            when (item.itemId) {
                R.id.settingsIcon -> {
                    addFragmentToContainer(latestHomeFragment)
                    true
                }

                R.id.languages -> {

                    if (!isImeEnabled) {
                        lifecycleScope.launch {
                            showAlertDialogEnable()
                        }
                        false
                    } else if (!isImeSelected) {
                        lifecycleScope.launch {
                            showAlertDialogSelect()
                        }
                        false
                    } else {
                        addFragmentToContainer(latestFavouritesLanguagesFragment)
                        true
                    }
                }

                R.id.themes -> {
                    LatestUtils.FRAGMENT_NAME = "Themes"
                    if (!isImeEnabled) {
                        lifecycleScope.launch {
                            showAlertDialogEnable()
                        }
                        false
                    } else if (!isImeSelected) {
                        lifecycleScope.launch {
                            showAlertDialogSelect()
                        }
                        false
                    } else {
                        addFragmentToContainer(latestThemesFragment)
                        true
                    }
                }

                R.id.preferences -> {
                    addFragmentToContainer(latestPreferenceFragment)
                    true
                }

                else -> false
            }
        }

        latestLocaleHelper = LatestLocaleHelper(this, prefs)


        setFragmentWithName(0)
    }

    fun setFragmentWithName(position: Int) {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val isImeEnabled = LatestKeyboardService.checkForEnablingOfIme(this)
        val isImeSelected = LatestKeyboardService.checkOfSelectionOfIme(this)
        when (position) {
            0 -> {
                LatestUtils.FRAGMENT_NAME = "Settings"
                addFragmentToContainer(latestHomeFragment)
                bottomNavigation.selectedItemId = R.id.settingsIcon
            }

            1 -> {
                LatestUtils.FRAGMENT_NAME = "Languages"
                bottomNavigation.selectedItemId = R.id.languages
                if (!isImeEnabled) {
                    lifecycleScope.launch {
                        showAlertDialogEnable()
                    }
                } else if (!isImeSelected) {
                    lifecycleScope.launch {
                        showAlertDialogSelect()
                    }
                } else addFragmentToContainer(latestFavouritesLanguagesFragment)
            }

            2 -> {
                LatestUtils.FRAGMENT_NAME = "Themes"
                if (!isImeEnabled) {
                    lifecycleScope.launch {
                        showAlertDialogEnable()
                    }
                } else if (!isImeSelected) {
                    lifecycleScope.launch {
                        showAlertDialogSelect()
                    }
                } else {
                    bottomNavigation.selectedItemId = R.id.themes
                    addFragmentToContainer(latestThemesFragment)
                }
            }

            3 -> {
                LatestUtils.FRAGMENT_NAME = "Preferences"
                bottomNavigation.selectedItemId = R.id.preferences
                addFragmentToContainer(latestPreferenceFragment)
            }

            4 -> {
                LatestUtils.FRAGMENT_NAME = "Fonts"
                if (!isImeEnabled) {
                    lifecycleScope.launch {
                        showAlertDialogEnable()
                    }
                } else if (!isImeSelected) {
                    lifecycleScope.launch {
                        showAlertDialogSelect()
                    }
                } else addFragmentToContainer(latestFontsFragment)
            }

            5 -> {
                LatestUtils.FRAGMENT_NAME = "Text Correction"
                addFragmentToContainer(latestCorrectionFragment)
            }

            6 -> {
                LatestUtils.FRAGMENT_NAME = "Gestures"
                addFragmentToContainer(latestGesturesFragment)
            }

            7 -> {
                LatestUtils.FRAGMENT_NAME = "Suggestions"
                if (!isImeEnabled) {
                    lifecycleScope.launch {
                        showAlertDialogEnable()
                    }
                } else if (!isImeSelected) {
                    lifecycleScope.launch {
                        showAlertDialogSelect()
                    }
                } else {
                    val path = DB_PATH + DB_NAME
                    if (prefs.mThemingApp.isDbInstalled && File(path).exists()) {
                        LatestUtils.FRAGMENT_NAME = "Suggestions"
                        addFragmentToContainer(latestSuggestionsFragment)
                    } else {

                        AlertDialog.Builder(this).setTitle(getString(R.string.download))
                            .setMessage(getString(R.string.suggestions_dailog)).setCancelable(false)
                            .setNegativeButton(getString(R.string.later),
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        p0!!.dismiss()
                                    }
                                }).setPositiveButton(getString(R.string.download),
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        if (ActivityCompat.checkSelfPermission(
                                                this@LatestHomeActivity,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                                this@LatestHomeActivity,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            DownloadFileFromURL().execute(LatestUtils.DB_FILE_PATH2)
                                        } else {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                                requestPermissions(
                                                    arrayOf(
                                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                    ), 1
                                                )
                                            }
                                        }
                                        p0!!.dismiss()
                                    }

                                }).setIcon(R.mipmap.ic_launcher).show()
                    }
                }
            }

            8 -> {
                LatestUtils.FRAGMENT_NAME = "One Hand Typing"
                addFragmentToContainer(latestOneHandedFragment)
            }

            9 -> {
                LatestUtils.FRAGMENT_NAME = "Editing Panel"
                addFragmentToContainer(latestEditingPanelFragment)
            }

            10 -> {
                LatestUtils.FRAGMENT_NAME = "Voice Typing"
                addFragmentToContainer(latestVoiceTypingFragment)
            }/*11 -> {
                val intent = Intent(this, LatestPremiumActivity::class.java)
                startActivity(intent)
            }
            12 -> {
                LatestUtils.shareToFriend(this)
            }
            13 -> {
                LatestUtils.intentToPlayStore(this)
            }
            14 -> {
                try {
                    LatestUtils.feedbackAppIntent(
                        getString(R.string.txt_feedback),
                        this@LatestHomeActivity
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }*/
        }
        tvHeader.text = LatestUtils.FRAGMENT_NAME
    }

    private fun addFragmentToContainer(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentNewContainer, fragment)
            .commitAllowingStateLoss()
    }

    private fun checkInternetConnection() {
        if (!LatestUtils.isConnectionAvailable(this)) {
            LatestUtils.showInternetDialog(this)
        }
    }

    override fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            if (progressBar.visibility == View.GONE) {
                progressBar.visibility = View.VISIBLE
            }
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            Log.d("DataLogger:", "${bytesDownloaded} : ${totalBytesToDownload}")
        } else if (state.installStatus() == InstallStatus.CANCELED) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "App update cancelled.", Toast.LENGTH_SHORT).show()
        } else if (state.installStatus() == InstallStatus.FAILED) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "App update failed.", Toast.LENGTH_SHORT).show()
        } else if (state.installStatus() == InstallStatus.INSTALLING) {
            Toast.makeText(this, "App update installing.", Toast.LENGTH_SHORT).show()
        } else if (state.installStatus() == InstallStatus.INSTALLED) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "App update installed", Toast.LENGTH_SHORT).show()
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            progressBar.visibility = View.GONE/*      Toast.makeText(this, "App update is Downloaded.", Toast.LENGTH_SHORT)
                      .show()*/
            try {
                showSnackbarForAppRestart()
            } catch (e: Exception) {
            }
        }
    }

    private fun showSnackbarForAppRestart() {
        try {
            val snackbar = Snackbar.make(
                findViewById(R.id.snackLayout),
                "An update has just been downloaded.",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("RESTART") { appUpdateManager.completeUpdate() }
            snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            snackbar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                drawerLayout.openDrawer(navigationView)
            }

            R.id.ivFamilyAppsHome -> {
                val intent = Intent(this, LatestFamilyAppsActivity::class.java)
                startActivity(intent)
            }

            R.id.giftBoxAnimHome -> {
                val intent = Intent(this, LatestPremiumActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.default_theme),
            getString(R.string.light_theme),
            getString(R.string.dark_theme)
        )
        var selectedThemeIndex = 0

        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.change_app_theme))
            .setSingleChoiceItems(themes, selectedThemeIndex) { dialog, which ->
                selectedThemeIndex = which

            }.setPositiveButton(getString(R.string.app_theme_apply)) { dialog, _ ->
                sharedPref.edit().putInt("theme", selectedThemeIndex).apply()
                changeTheme(selectedThemeIndex, this@LatestHomeActivity)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.app_theme_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeItem -> {
            }

            R.id.languageItem -> {
                startActivity(Intent(this, LanguagePickerActivity::class.java))
            }

            R.id.changeTheme -> {
                showThemeDialog()
            }

            R.id.premiumItem -> {
                val intent = Intent(this, LatestPremiumActivity::class.java)
                startActivity(intent)
            }

            R.id.updateItem -> {
                appUpdater()
            }

            R.id.rateItem -> {
                LatestUtils.intentToPlayStore(this@LatestHomeActivity)
            }

            R.id.shareItem -> {
                LatestUtils.shareToFriend(this@LatestHomeActivity)
            }

            R.id.moreItem -> {
                LatestUtils.furtherAppsOnPlayStore(this@LatestHomeActivity)
            }

            R.id.feedbackItem -> {
                try {
                    LatestUtils.feedbackAppIntent(
                        getString(R.string.txt_feedback), this@LatestHomeActivity
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            R.id.privacyItem -> {
                LatestUtils.goToPrivacyPolicy(this@LatestHomeActivity)
            }

            R.id.mobileAdsConsentItem -> {
                latestConsentManager.showPrivacyOptionsForm(this) { formError ->
                    if (formError != null) {
                        Toast.makeText(this, formError.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.familyAppsItem -> {
                val intent = Intent(this, LatestFamilyAppsActivity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawers()
        return false
    }


    override fun onItemSelected(position: Int) {
        setFragmentWithName(position)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "App is updated successfully.", Toast.LENGTH_SHORT).show()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "App Update failed\nTry again later.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            1 -> { /*instead of 3*/
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "App is updated successfully.", Toast.LENGTH_SHORT).show()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "App Update failed\nTry again later.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                DownloadFileFromURL().execute(LatestUtils.DB_FILE_PATH2)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class DownloadFileFromURL : AsyncTask<String?, String?, String?>() {

        private val TAG = "DownloadTaskInner:"

        override fun onPreExecute() {
            super.onPreExecute()
            dialogLatest.show()
            Log.d(TAG, " Task onPreExecute: ")
        }

        override fun doInBackground(vararg f_url: String?): String {
            val path = DB_PATH + DB_NAME
            var count: Int
            Log.d(TAG, " Task doInBackground: " + f_url[0])
            try {
                val url = URL(f_url[0])
                val connection = url.openConnection()
                connection.connect()
                val lenghtOfFile = connection.contentLength
                val input: InputStream = BufferedInputStream(
                    url.openStream(), 8192
                )
                val output: OutputStream = FileOutputStream(path)
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()
            } catch (e: Exception) {
                prefs.mThemingApp.isDbInstalled = false
                dialogLatest.dismissIt()
                Log.e(TAG, "Task Error: " + e.localizedMessage)
            }
            return path
        }

        override fun onProgressUpdate(vararg progress: String?) {
            // setting progress percentage
            Log.d(TAG, " Task onProgressUpdate: " + progress[0]!!.toInt())
            dialogLatest.setProgressToViews(progress[0]!!.toInt())
        }

        override fun onPostExecute(file_url: String?) {
            // dismiss the dialogApp after the file was downloaded
            Log.d(TAG, " Task onPostExecute: $file_url")
            if (file_url != null) {
                prefs.mThemingApp.isDbInstalled = true
                prefs.mSingleSuggestion.enabled = true
                prefs.mSingleSuggestion.suggestClipboardContent = true
                dialogLatest.dismissIt()
            }
        }
    }


    private fun getDatabaseName(): String {
        return resources.getString(R.string.app_db_name)
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers()
        } else {
            if (latestHomeFragment.isVisible) {
                val latestHomeBottomSheet = LatestHomeBottomSheet()
                latestHomeBottomSheet.show(supportFragmentManager, "Bottom Sheet")
            } else {
                try {
                    setFragmentWithName(0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private var alertDialog: AlertDialog? = null
    private suspend fun showAlertDialogEnable() {
        withContext(Dispatchers.Main) {
            try {
                if (isFinishing || isDestroyed) return@withContext

                // Dismiss the previous dialog safely
                alertDialog?.dismiss()

                // Show a new dialog only if none is showing
                if (alertDialog == null || !alertDialog!!.isShowing) {
                    alertDialog = AlertDialog.Builder(this@LatestHomeActivity)
                        .setTitle(getString(R.string.choose_keyboard))
                        .setMessage(getString(R.string.enable_des))
                        .setPositiveButton(getString(R.string.choose_keyboard)) { dialog, which ->
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            startActivity(intent)
                            dialog.dismiss() // Avoid using `cancel()` here unless necessary
                        }
                        .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .create()

                    // Safely show the dialog
                    alertDialog?.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun showAlertDialogSelect() {
        withContext(Dispatchers.Main) {
            // Check if the dialog is already showing
            if (alertDialog == null || !alertDialog!!.isShowing) {
                alertDialog = AlertDialog.Builder(this@LatestHomeActivity)
                    .setTitle(getString(R.string.switch_keyboard))
                    .setMessage(getString(R.string.select_des))
                    .setPositiveButton(getString(R.string.switch_keyboard)) { dialog, which ->
                        imm.showInputMethodPicker()
                        dialog.cancel()
                    }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                        dialog.cancel()
                    }.create()

                alertDialog?.show()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        try {
            if (isShowingAd) isShowingAd = false
            try {
                val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                val lang = sharedPref.getString("APP_LANGUAGE", "en")
                val currentLang = resources.configuration.locales.get(0).language
                if (lang != currentLang) {
                    updateLocale(this, lang!!)
                    recreate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.IMMEDIATE, this, 1
                    )
                }
            }

//        updateImeIssueCardsVisibilities()
            // prefs.shared.registerOnSharedPreferenceChangeListener(this)


            ///temp code
            var isImeEnabled = LatestKeyboardService.checkForEnablingOfIme(this)
            var isImeSelected = LatestKeyboardService.checkOfSelectionOfIme(this)


            lifecycleScope.launch {
                while (!isImeEnabled) {

                    showAlertDialogEnable()

                    delay(40000)

                    isImeEnabled =
                        LatestKeyboardService.checkForEnablingOfIme(this@LatestHomeActivity)
                }

                while (!isImeSelected) {
                    if (isImeEnabled && !isImeSelected) {
                        showAlertDialogSelect()
                        delay(40000)
                    }
                    isImeSelected =
                        LatestKeyboardService.checkOfSelectionOfIme(this@LatestHomeActivity)

                    if (isImeSelected) {
                        withContext(Dispatchers.Main) {
                            selectTV_Click.visibility = View.GONE
                        }
                    }
                }
            }


            if (!isImeEnabled) {
                enableTV_Click.visibility = View.VISIBLE
            } else if (!isImeSelected) {
                selectTV_Click.visibility = View.VISIBLE

            }


            if (isImeEnabled) {
                enableTV_Click.visibility = View.GONE
            } else if (isImeSelected) {
                selectTV_Click.visibility = View.GONE

            }

            enableTV_Click.setOnClickListener {
                val intent = Intent()
                intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                isShowingAd = true
                startActivity(intent)

            }

            selectTV_Click.setOnClickListener {
                imm.showInputMethodPicker()

                lifecycleScope.launch {

                    while (!isImeSelected) {
                        isImeSelected =
                            LatestKeyboardService.checkOfSelectionOfIme(this@LatestHomeActivity)
                        if (isImeSelected) {
                            withContext(Dispatchers.Main) {
                                selectTV_Click.visibility = View.GONE
                            }
                        }
                        delay(1000)
                    }
                }
            }
            //temp code

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        appUpdateManager.unregisterListener(this)
        super.onPause()
    }
}