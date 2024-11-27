package com.maya.newbulgariankeyboard.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.monetization.LatestConsentManager
import com.maya.newbulgariankeyboard.monetization.LatestKeyboardClass
import com.maya.newbulgariankeyboard.monetization.LatestKeyboardClass.Companion.isShowingAd
import com.maya.newbulgariankeyboard.monetization.LatestKeyboardClass.Companion.isWorkNotDone
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val LOG_TAG = "LatestSplashActivity"
private const val COUNTER_TIME_MILLISECONDS = 4000L

@SuppressLint("CustomSplashScreen")
class LatestSplashActivity : AppCompatActivity() {


    private lateinit var latestConsentManager: LatestConsentManager
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var secondsRemaining: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        sharedPref.edit().putInt("theme", 0).apply()


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal, this@LatestSplashActivity)
        setContentView(R.layout.activity_splash)

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


        createTimer(COUNTER_TIME_MILLISECONDS)
        latestConsentManager = LatestConsentManager.getInstance(this@LatestSplashActivity)
        latestConsentManager.gatherConsent(this@LatestSplashActivity) { consentError ->

            if (consentError != null) {
                Log.w(
                    LOG_TAG,
                    String.format("%s: %s", consentError.errorCode, consentError.message)
                )
            }

            if (latestConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }

            if (secondsRemaining <= 0) {
                Handler().postDelayed({
                    Log.e("InterstitialLogger", "onAdLoaded: FirstConcent")
                    moveToNextActivity()
                }, 6000)
            }
        }

        if (latestConsentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }

        isShowingAd = false

    }

    private fun createTimer(time: Long) {

        val countDownTimer: CountDownTimer =
            object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                }

                override fun onFinish() {
                    secondsRemaining = 0


                    if (latestConsentManager.canRequestAds) {
                        Handler().postDelayed({
                            Log.e("InterstitialLogger", "onAdLoaded: 2ndConcent")
                            moveToNextActivity()
                        }, 5000)
                    }else{
                        val intent =
                            Intent(this@LatestSplashActivity, LatestHomeActivity::class.java)
                        intent.putExtra("ishome", true)
                        startActivity(intent)
                        finish()

                    }
                }
            }
        countDownTimer.start()
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        MobileAds.initialize(this) {}

        initAppOpenAd()

    }

    private fun initAppOpenAd() {
        val billingHelper = LatestBillingHelper(this)
        if (billingHelper.shouldApplyMonetization()) {
            try {
                LatestKeyboardClass.latestOpenManager?.loadAppOpenStartAd(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveToNextActivity() {

        if (LatestKeyboardClass.latestOpenManager != null && LatestUtils.isConnectionAvailable(this)) {
            LatestKeyboardClass.latestOpenManager!!.showAppAppOpenAd(this,
                object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)

                        if (!LatestUtils.isConnectionAvailable(this@LatestSplashActivity)) {
                            val intent =
                                Intent(this@LatestSplashActivity, LatestHomeActivity::class.java)
                            intent.putExtra("ishome", true)
                            startActivity(intent)
                            finish()
                        } else {

                            val intent =
                                Intent(this@LatestSplashActivity, LatestPremiumActivity::class.java)
                            intent.putExtra("ishome", true)
                            startActivity(intent)
                            finish()
                        }

                        Log.d("SplashManagerLogs", "Splash onAdFailedToShowFullScreenContent")
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                        super.onAdShowedFullScreenContent()
                        Log.d("SplashManagerLogs", "Splash onAdShowedFullScreenContent")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isShowingAd = false

                        super.onAdDismissedFullScreenContent()

                        if (!LatestUtils.isConnectionAvailable(this@LatestSplashActivity)) {
                            val intent =
                                Intent(this@LatestSplashActivity, LatestHomeActivity::class.java)
                            intent.putExtra("ishome", true)
                            startActivity(intent)
                            finish()
                        } else {

                            val intent =
                                Intent(this@LatestSplashActivity, LatestPremiumActivity::class.java)
                            intent.putExtra("ishome", true)
                            startActivity(intent)
                            finish()
                        }
                        Log.d("SplashManagerLogs", "Splash onAdDismissedFullScreenContent")

                    }
                })
        } else {

            if (!LatestUtils.isConnectionAvailable(this)) {
                val intent =
                    Intent(this@LatestSplashActivity, LatestHomeActivity::class.java)
                intent.putExtra("ishome", true)
                startActivity(intent)
                finish()
            } else {

                val intent =
                    Intent(this@LatestSplashActivity, LatestPremiumActivity::class.java)
                intent.putExtra("ishome", true)
                startActivity(intent)
                finish()
            }
        }
    }


    override fun onStart() {
        isShowingAd = true
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        isWorkNotDone = true
    }

}
