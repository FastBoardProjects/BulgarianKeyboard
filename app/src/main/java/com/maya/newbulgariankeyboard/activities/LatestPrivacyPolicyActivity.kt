package com.maya.newbulgariankeyboard.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestCustomizeHelper
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.monetization.LatestConsentManager
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme
import kotlinx.android.synthetic.main.activity_privacy_policy.cvNext
import kotlinx.android.synthetic.main.activity_privacy_policy.tvPrivacyPolicy
import java.util.concurrent.atomic.AtomicBoolean

private const val LOG_TAG = "PrivacyPolicyActivity"

class LatestPrivacyPolicyActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var prefsPrivacy: SharedPreferences
    private var mFirstRun = false
    lateinit var prefs: LatestPreferencesHelper


    private lateinit var latestConsentManager: LatestConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        sharedPref.edit().putInt("theme", 0).apply()


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal,this@LatestPrivacyPolicyActivity)

        setContentView(R.layout.activity_privacy_policy)


        initPoliciesViews()

        latestConsentManager = LatestConsentManager.getInstance(applicationContext)
        latestConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Log.w(LOG_TAG, String.format("%s: %s", consentError.errorCode, consentError.message))
            }
            if (latestConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }

        if (latestConsentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }

        initPoliciesViews()
    }


    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        MobileAds.initialize(this) {}
    }

    private fun initPoliciesViews() {
        prefs = LatestPreferencesHelper(this)
        prefs.initAppPreferences()
        prefsPrivacy = getSharedPreferences("MainPrefs", Context.MODE_PRIVATE)
        mFirstRun = prefsPrivacy.getBoolean("mFirstRun", true)
        if (!mFirstRun) {
            moveToMain()
        }
        cvNext.setOnClickListener(this)
        tvPrivacyPolicy.setOnClickListener(this)

        /*checkboxPrivacy.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    cvNext.alpha = 1F
                    cvNext.isEnabled = true
                } else {
                    cvNext.alpha = 0.5F
                    cvNext.isEnabled = false
                }
            }

        })*/
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cvNext -> {
                /* if (checkboxPrivacy.isChecked) {
                   *//*imp for first time otherwise blank keyboard*//*
                    LatestCustomizeHelper.saveThemingToPreferences(
                        prefs,
                        LatestCustomizeHelper.fromJsonFile(this, "app_assets/theme/app_day_theme.json")!!
                    )
                    prefsPrivacy.edit().putBoolean("mFirstRun", false).apply()
                    moveToMain()
                } else {
                    Toast.makeText(this, "Please accept app's privacy policy.", Toast.LENGTH_SHORT)
                        .show()
                }*/


                LatestCustomizeHelper.saveThemingToPreferences(
                    prefs,
                    LatestCustomizeHelper.fromJsonFile(this, "app_assets/theme/app_day_theme.json")!!
                )
                prefsPrivacy.edit().putBoolean("mFirstRun", false).apply()
                moveToMain()

            }
            R.id.tvPrivacyPolicy -> {
                LatestUtils.goToPrivacyPolicy(this)
            }
        }
    }

    private fun moveToMain() {
        val intent = Intent(this, LatestSplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToIntro() {
        val intent = Intent(this, LatestIntroActivity::class.java)
        startActivity(intent)
        finish()
    }
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        finishAffinity()
    }
}