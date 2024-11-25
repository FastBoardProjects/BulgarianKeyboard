package com.maya.newbulgariankeyboard.monetization

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.maya.newbulgariankeyboard.R
import java.util.Date

class LatestAppOpenAdHelper(private val myAppClass: LatestKeyboardClass) : LifecycleObserver,

    ActivityLifecycleCallbacks {
    private val TAG = "AppOpenAdHelper:"
    private var mAppOpenAd: AppOpenAd? = null
    private var mLoadCallback: AppOpenAdLoadCallback? = null
    private var mRunningActivity: Activity? = null
    private var mTimeHelper: Long = 0


    fun loadAppOpenStartAd(givenContext: Context) {


        if ((isShowingAd || isOpenAdAvailable) && LatestBillingHelper(givenContext).shouldApplyMonetization()) {
            Log.e("InterstitialLogger", "onAdLoaded: Already available AppOpenAd")
        } else {
            Log.e("InterstitialLogger", "onAdLoaded: Requesting AppOpenAd")
            mLoadCallback = object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(p0: AppOpenAd) {
                    super.onAdLoaded(p0)
                    Log.e("InterstitialLogger", "onAdLoaded: AppOpenAd Loaded")
                    this@LatestAppOpenAdHelper.mAppOpenAd = p0
                    mTimeHelper = Date().time
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e("InterstitialLogger", "onAdLoaded: AppOpenAd Failed")
                    Log.d(TAG, "onAppOpenAdFailedToLoad : Error: $p0")
                }
            }
            AppOpenAd.load(
                myAppClass,
                givenContext.getString(R.string.app_open_ads_id),
                com.google.android.gms.ads.AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                mLoadCallback as AppOpenAdLoadCallback
            )
        }
    }

    fun showAppAppOpenAd(activity: AppCompatActivity?, listener: FullScreenContentCallback?) {
        if (!isShowingAd && isOpenAdAvailable && activity?.baseContext?.let { LatestBillingHelper(it).shouldApplyMonetization() } == true) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        listener?.onAdDismissedFullScreenContent()
                        mAppOpenAd = null
                        isShowingAd = false
                        Log.e("InterstitialLogger", "onAdLoaded: AppOpenAd Dismissed")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        listener?.onAdFailedToShowFullScreenContent(adError)
                        Log.e("InterstitialLogger", "onAdLoaded: AppOpenAd FailedToShow")
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                        listener!!.onAdShowedFullScreenContent()
                        Log.e("InterstitialLogger", "onAdLoaded: AppOpenAd Showed")
                    }
                }
            if (activity != null) {
                mAppOpenAd!!.show(activity)
            }
            mAppOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
        } else {
            Log.d(TAG, "Can not show ad. Fetching Ad")
            listener?.onAdDismissedFullScreenContent()
        }
    }


    private fun checkForLoadingTime(hourGiven: Long): Boolean {
        val dateDifference = Date().time - mTimeHelper
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * hourGiven
    }

    private val isOpenAdAvailable: Boolean
        get() = mAppOpenAd != null && checkForLoadingTime(4)

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        mRunningActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        mRunningActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        mRunningActivity = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        //showAdIfAvailable();
        //Log.d(TAG, "OnLifecycleEvent onStart");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.d(TAG, "OnLifecycleEvent onStop")
    }

    companion object {
        private var isShowingAd = false
    }

    init {
        myAppClass.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}