package com.maya.newbulgariankeyboard.monetization

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestFontsOverride
import java.util.Date

class LatestKeyboardClass : Application(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {


    var LOG_TAG = "Hello_OPEN_Ad"

    private var currentActivity: Activity? = null


    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private lateinit var appOpenAdManager: AppOpenAdManager

    private inner class AppOpenAdManager {

        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false

        private var loadTime: Long = 0

        /** Request an ad. */
        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) {
                return
            }
            isLoadingAd = true
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                getString(R.string.app_open_ads_id), // Replace with your ad unit ID
                adRequest,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Log.d(LOG_TAG, "Ad loaded successfully.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(LOG_TAG, loadAdError.message)
                        isLoadingAd = false
                    }
                }
            )
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numHours * numMilliSecondsPerHour
        }

        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.")
                return
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Called when full screen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    if (LatestBillingHelper(this@LatestKeyboardClass).shouldApplyMonetization())
                        loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, adError.message)
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    if (LatestBillingHelper(this@LatestKeyboardClass).shouldApplyMonetization())
                        loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    Log.d(LOG_TAG, "Ad showed fullscreen content.")
                }
            }
            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        @SuppressLint("StaticFieldLeak")
        var latestOpenManager: LatestAppOpenAdHelper? = null
        var isShowingAd = false
        var isWorkNotDone = true

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.

        Log.d("egfadresfvaerf", "onMoveToForeground: ${LatestBillingHelper(this@LatestKeyboardClass).shouldApplyMonetization()}")
        if (LatestBillingHelper(this@LatestKeyboardClass).shouldApplyMonetization()) {

//            Toast.makeText(this@LatestKeyboardClass, "IT was true", Toast.LENGTH_SHORT).show()
            currentActivity?.let { showAdIfAvailable(it) }
        }
    }




override fun onCreate() {
    super.onCreate()


    /*todo change*/


    appOpenAdManager = AppOpenAdManager()

    ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    FirebaseApp.initializeApp(this)

    firebaseAnalytics = Firebase.analytics

    latestOpenManager = LatestAppOpenAdHelper(this)


    LatestFontsOverride.setDefaultFont(this, "MONOSPACE", "app_fonts/roboto_regular.ttf")
    LatestFontsOverride.setDefaultFont(this, "DEFAULT", "app_fonts/roboto_regular.ttf")
    LatestFontsOverride.setDefaultFont(this, "SERIF", "app_fonts/roboto_regular.ttf")
    LatestFontsOverride.setDefaultFont(this, "SANS-SERIF", "app_fonts/roboto_regular.ttf")

    registerActivityLifecycleCallbacks(this)

}

override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    MultiDex.install(this)
}


/** Method to be called in each activity's onResume() method. */
private fun showAdIfAvailable(activity: Activity) {
    appOpenAdManager.showAdIfAvailable(
        activity,
        object : OnShowAdCompleteListener {
            override fun onShowAdComplete() {
                // Handle any logic after the ad is shown
                Log.d(LOG_TAG, "onShowAdComplete() called.")
            }
        }
    )
}

override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        // Not Implemented
}

override fun onActivityStarted(activity: Activity) {
    currentActivity = activity


}

override fun onActivityResumed(activity: Activity) {
    Log.d("MyApplication", "Current activity: ${activity::class.java.simpleName}")
}

override fun onActivityPaused(activity: Activity) {
    // Not Implemented
}

override fun onActivityStopped(activity: Activity) {
    // Not Implemented
}

override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    // Not Implemented
}

override fun onActivityDestroyed(activity: Activity) {
    // Not Implemented
}
}
