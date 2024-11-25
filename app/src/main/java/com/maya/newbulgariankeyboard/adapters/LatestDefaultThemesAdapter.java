package com.maya.newbulgariankeyboard.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.interfaces.LatestDefaultThemeCallback;
import com.maya.newbulgariankeyboard.main_utils.LatestUtils;
import com.maya.newbulgariankeyboard.models.LatestThemeModel;
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper;
import com.maya.newbulgariankeyboard.utils.CommonFun;

import java.util.ArrayList;

public class LatestDefaultThemesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE = 0;
    private static final int AD_TYPE = 1;

    private final Context context;
    private final ArrayList<LatestThemeModel> list;
    private final LatestDefaultThemeCallback callback;
    private final SharedPreferences sharedPreferences;
    private int themeSelected = 0;
    private final LatestBillingHelper latestBillingHelper;

    public LatestDefaultThemesAdapter(Context context, ArrayList<LatestThemeModel> list, LatestDefaultThemeCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        sharedPreferences = context.getSharedPreferences("Themes", Context.MODE_PRIVATE);
        themeSelected = sharedPreferences.getInt("Theme", 48);
        latestBillingHelper = new LatestBillingHelper(context);
    }



    @Override
    public int getItemViewType(int position) {

        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {


            return position == 1 ? AD_TYPE : ITEM_TYPE;
        }else{
            return ITEM_TYPE;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AD_TYPE) {
            FrameLayout adContainer = new FrameLayout(context);
            adContainer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            ShimmerFrameLayout shimmerLayout = new ShimmerFrameLayout(context);
            shimmerLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300 // Adjust height as needed
            ));
            shimmerLayout.setId(View.generateViewId());

            View shimmerView = new View(context);
            shimmerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300
            ));
            shimmerView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            shimmerLayout.addView(shimmerView);
            adContainer.addView(shimmerLayout);

            return new AdViewHolder(adContainer, shimmerLayout);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item_theme_default, parent, false);
            return new ThemesViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        int actualPosition;

        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)){
            actualPosition = position > 1 ? position - 1 : position; // Adjust position to exclude ad
        } else {
            actualPosition = position;
        }

        if (getItemViewType(position) == AD_TYPE && holder instanceof AdViewHolder && latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            loadNativeAd(adView -> {
                if (adView != null) {
                    adViewHolder.shimmerLayout.setVisibility(View.GONE);
                    adViewHolder.adContainer.removeAllViews();
                    adViewHolder.adContainer.addView(adView);
                } else {
                    Log.d("AdsLoading", "Ad failed to load or display.");
                }
            });
        } else if (holder instanceof ThemesViewHolder) {
            ThemesViewHolder themesViewHolder = (ThemesViewHolder) holder;
//            int actualPosition = position > 1 ? position - 1 : position; // Adjust position to exclude ad
            LatestThemeModel model = list.get(actualPosition);

            themeSelected = sharedPreferences.getInt("Theme", 48);
            if (themeSelected == model.getItemId()) {
                themesViewHolder.ivSelected.setVisibility(View.VISIBLE);
            } else {
                themesViewHolder.ivSelected.setVisibility(View.GONE);
            }
            themesViewHolder.tvViewBottom.setText(model.getItemDisplayText());
            themesViewHolder.viewMain.setBackgroundColor(Color.parseColor(model.getItemBgColor()));
            themesViewHolder.itemView.setOnClickListener(view -> {
                sharedPreferences.edit().putInt("Theme", model.getItemId()).apply();
                themeSelected = model.getItemId();
                callback.onThemeSelected(model, actualPosition);
            });
        }
    }

    @Override
    public int getItemCount() {
        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {


            return list.size() + 1; // +1 for the ad
        }else{
            return  list.size();
        }
    }

    static class ThemesViewHolder extends RecyclerView.ViewHolder {
        View viewMain;
        TextView tvViewBottom;
        ImageView ivSelected;

        public ThemesViewHolder(@NonNull View itemView) {
            super(itemView);
            viewMain = itemView.findViewById(R.id.viewMain);
            tvViewBottom = itemView.findViewById(R.id.tvViewBottom);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        FrameLayout adContainer;
        ShimmerFrameLayout shimmerLayout;

        public AdViewHolder(@NonNull View itemView, ShimmerFrameLayout shimmer) {
            super(itemView);
            adContainer = (FrameLayout) itemView;
            shimmerLayout = shimmer;
        }
    }

    private void loadNativeAd(AdLoadCallback adLoadCallback) {
        AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.native_ads_themes_id))
                .forNativeAd(nativeAd -> {
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(
                            R.layout.native_ads_layout_small, null);
                    CommonFun.populateNativeAdViewSmallThemes(nativeAd, adView);

                    LinearLayout adContainer = new LinearLayout(context);
                    adContainer.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    adContainer.setOrientation(LinearLayout.VERTICAL);
                    adContainer.addView(adView);

                    adLoadCallback.onAdLoaded(adContainer);
                })
                .withAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.d("AdsLoading", "onAdFailedToLoad: " + error.getMessage());
                        adLoadCallback.onAdLoaded(null); // Pass null if ad loading fails
                    }

                    @Override
                    public void onAdLoaded() {
                        Log.d("AdsLoading", "onAdLoaded");
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    interface AdLoadCallback {
        void onAdLoaded(LinearLayout adContainer);
    }
}
