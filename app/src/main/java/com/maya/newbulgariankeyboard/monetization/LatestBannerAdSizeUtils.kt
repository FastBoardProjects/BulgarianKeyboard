package com.maya.newbulgariankeyboard.monetization

import android.app.Activity
import android.util.DisplayMetrics
import android.widget.LinearLayout
import com.google.android.gms.ads.AdSize

class LatestBannerAdSizeUtils {
    companion object {

        fun getAdSizeForSmartBanner(mContext: Activity, adContainer: LinearLayout): AdSize {
            val display = mContext.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val density = outMetrics.density
            var adWidthPixels = adContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mContext, adWidth)
        }
    }
}