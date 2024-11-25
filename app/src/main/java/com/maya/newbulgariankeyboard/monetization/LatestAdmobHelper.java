package com.maya.newbulgariankeyboard.monetization;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.main_utils.LatestUtils;

public class LatestAdmobHelper {

    public static InterstitialAd staticInterstitialAd = null;
    public static boolean adIsLoading;

    public static void loadAppAdMobBanner(final Context context, final LinearLayout adContainer) {

        AdView mAdView = new AdView(context);
        mAdView.setAdSize(LatestBannerAdSizeUtils.Companion.getAdSizeForSmartBanner((Activity) context, adContainer));
        mAdView.setAdUnitId(context.getString(R.string.banner_ads_id));

        LatestBillingHelper billingHelper = new LatestBillingHelper(context);

        if (billingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {
            try {
                Log.e("InterstitialLogger", "onAdLoaded: Requesting BannerAd ");
                mAdView.loadAd(new AdRequest.Builder()
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                try {
                    Log.e("InterstitialLogger", "onAdLoaded: BannerAd Loaded");
                    adContainer.removeAllViews();
                    adContainer.addView(mAdView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("InterstitialLogger", "onAdLoaded: BannerAd Failed");
                super.onAdFailedToLoad(loadAdError);
            }
        });
    }

    public static void loadAppAdFullScreenAd(Context context) {

        LatestBillingHelper billingHelper = new LatestBillingHelper(context);

        if (billingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {
            if (adIsLoading || staticInterstitialAd != null){
                Log.e("InterstitialLogger", "onAdLoaded: Already available InterstitialAd");
                return;
            }
        }
        adIsLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e("InterstitialLogger", "onAdLoaded: Requesting InterstitialAd");
        InterstitialAd.load(
                context,
                context.getString(R.string.interstitial_ads_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        staticInterstitialAd = interstitialAd;
                        adIsLoading = false;
                        Log.e("InterstitialLogger", "onAdLoaded: InterstitialAd Loaded");

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e("InterstitialLogger", "onAdLoaded: InterstitialAd Failed");
                        staticInterstitialAd = null;
                        adIsLoading = false;
                    }
                });
    }

}