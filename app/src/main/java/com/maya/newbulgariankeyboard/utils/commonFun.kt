package com.maya.newbulgariankeyboard.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.RemoteException
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.maya.newbulgariankeyboard.R

object CommonFun {

    fun changeTheme(themeVal: Int, context: Context) {

        when (themeVal) {

            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            }

            1 -> {
                val currentNightMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_NO
                if (currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

            2 -> {

                val currentNightMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }

    }

    fun inAppReview(context: Context) {
        try {
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow((context as Activity), task.result)
                    Log.d("InAppReview", "Review dialog is likely to be shown")
                } else {
                    val exception = task.exception
                    Log.d("InAppReview", "inAppReview: $exception")

                    if (exception is ReviewException) {
                        val reviewErrorCode = exception.errorCode
                        Log.d(
                            "InAppReview",
                            "Review dialog will not be shown. Error code: $reviewErrorCode"
                        )
                    } else if (exception is RemoteException) {
                        Log.d("InAppReview", "RemoteException occurred: ${exception.message}")
                    } else {
                        Log.d("InAppReview", "An unexpected error occurred: ${exception?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAdSize(resources: Resources, context: Context): AdSize {

        val outMetrics = resources.displayMetrics
        val widthPixels = outMetrics.widthPixels
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }


    @JvmStatic
    @SuppressLint("InflateParams")
    fun loadNativeAdSmallExit(context: Context, onAdLoaded: (LinearLayout?) -> Unit) {
        val adLoader = AdLoader.Builder(context, context.getString(R.string.native_ads_exit_id))
            .forNativeAd { nativeAd ->
                // Inflate the native ad layout and populate the NativeAdView.
                val adView = (context as Activity).layoutInflater.inflate(
                    R.layout.native_ads_layout_big, null
                ) as NativeAdView
                populateNativeAdViewSmallExit(nativeAd, adView)

                // Create and configure the LinearLayout container.
                val adContainer = LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16) // Add padding if needed
                    addView(adView)
                }

                onAdLoaded(adContainer) // Call the callback with the adContainer
            }.withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(errorCode: LoadAdError) {
                    Log.d("AdsLoading", "onAdFailedToLoad: $errorCode")
                    onAdLoaded(null) // Call the callback with null if loading failed
                }

                override fun onAdLoaded() {
                    Log.d("AdsLoading", "onAdLoaded: ")
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Customize your native ad options here
                    .build()
            ).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }


    @JvmStatic
    fun populateNativeAdViewSmallExit(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.headline)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.bodyView = adView.findViewById(R.id.secondary_text)
        adView.callToActionView = adView.findViewById(R.id.call_to_action)

        // Set the text in the views
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
        }
        adView.bodyView?.let {
            (it as TextView).text = nativeAd.body
        }
        (adView.callToActionView as Button).text = nativeAd.callToAction

        adView.setNativeAd(nativeAd)

    }

    @JvmStatic
    fun populateNativeAdViewSmallFonts(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.headline)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.bodyView = adView.findViewById(R.id.secondary_text)
        adView.callToActionView = adView.findViewById(R.id.call_to_action)

        // Set the text in the views
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
        }
        adView.bodyView?.let {
            (it as TextView).text = nativeAd.body
        }
        (adView.callToActionView as Button).text = nativeAd.callToAction

        adView.setNativeAd(nativeAd)

    }

    @JvmStatic
    fun populateNativeAdViewSmallThemes(nativeAd: NativeAd, adView: NativeAdView) {
        adView.headlineView = adView.findViewById(R.id.headline)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.bodyView = adView.findViewById(R.id.secondary_text)
        adView.callToActionView = adView.findViewById(R.id.call_to_action)

        // Set the text in the views
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(it.drawable)
        }
        adView.bodyView?.let {
            (it as TextView).text = nativeAd.body
        }
        (adView.callToActionView as Button).text = nativeAd.callToAction

        adView.setNativeAd(nativeAd)

    }



    /*private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        adView.headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        adView.bodyView = adView.findViewById<TextView>(R.id.ad_body)
        adView.callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        adView.starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)

        // The headline is guaranteed to be in every NativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        // Other assets aren't guaranteed, so check before setting.
        adView.bodyView?.visibility = if (nativeAd.body == null) View.INVISIBLE else {
            (adView.bodyView as TextView).text = nativeAd.body
            View.VISIBLE
        }

        adView.callToActionView?.visibility =
            if (nativeAd.callToAction == null) View.INVISIBLE else {
                (adView.callToActionView as Button).text = nativeAd.callToAction
                View.VISIBLE
            }

        adView.iconView?.visibility = if (nativeAd.icon == null) View.INVISIBLE else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            View.VISIBLE
        }

        adView.advertiserView?.visibility = if (nativeAd.advertiser == null) View.INVISIBLE else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            View.VISIBLE
        }

        adView.starRatingView?.visibility = if (nativeAd.starRating == null) View.INVISIBLE else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            View.VISIBLE
        }

        // Assign the native ad object to the native view.
        adView.setNativeAd(nativeAd)
    }*/

    /*@SuppressLint("InflateParams")
        fun loadNativeAd(context: Context, onAdLoaded: (LinearLayout?) -> Unit) {
            val adLoader = AdLoader.Builder(context, context.getString(R.string.native_ads_id))
                .forNativeAd { nativeAd ->
                    // Inflate the native ad layout and populate the NativeAdView.
                    val adView = (context as Activity).layoutInflater.inflate(
                        R.layout.native_ads_layout, null
                    ) as NativeAdView
                    populateNativeAdView(nativeAd, adView)

                    // Create and configure the LinearLayout container.
                    val adContainer = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 16, 16, 16) // Add padding if needed
                        addView(adView)
                    }

                    onAdLoaded(adContainer) // Call the callback with the adContainer
                }.withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(errorCode: LoadAdError) {
                        Log.d("AdsLoading", "onAdFailedToLoad: $errorCode")
                        onAdLoaded(null) // Call the callback with null if loading failed
                    }

                    override fun onAdLoaded() {
                        Log.d("AdsLoading", "onAdLoaded: ")
                    }
                }).withNativeAdOptions(
                    NativeAdOptions.Builder()
                        // Customize your native ad options here
                        .build()
                ).build()

            adLoader.loadAd(AdRequest.Builder().build())
        }*/
}