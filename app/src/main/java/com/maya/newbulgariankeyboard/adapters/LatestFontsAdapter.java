package com.maya.newbulgariankeyboard.adapters;

import static com.maya.newbulgariankeyboard.utils.CommonFun.populateNativeAdViewSmallFonts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.maya.newbulgariankeyboard.interfaces.LatestFontsItemCallback;
import com.maya.newbulgariankeyboard.main_utils.LatestUtils;
import com.maya.newbulgariankeyboard.models.LatestFontModel;
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper;

import java.util.ArrayList;

public class LatestFontsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static Boolean isFreeAllowed = false;
    private final Context context;
    private final ArrayList<LatestFontModel> list;
    private final LatestFontsItemCallback callback;

    private static final int ITEM_TYPE = 0;
    private static final int AD_TYPE = 1;
    //    public static ArrayList<Integer> freeIndexs = new ArrayList<Integer>();
    public static ArrayList<Integer> freeIndexs = new ArrayList<>();
    private final LatestBillingHelper latestBillingHelper;

    public LatestFontsAdapter(Context context, ArrayList<LatestFontModel> list, LatestFontsItemCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        latestBillingHelper = new LatestBillingHelper(context);

    }

    @Override
    public int getItemViewType(int position) {
        // Show an ad every 5 items

        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {


            return (position % 8 == 0 && position != 0) ? AD_TYPE : ITEM_TYPE;
        }else{
            return ITEM_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == AD_TYPE) {
            // Create the main FrameLayout container for ad and shimmer
            FrameLayout adContainer = new FrameLayout(context);
            adContainer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // Create the ShimmerFrameLayout programmatically
            ShimmerFrameLayout shimmerLayout = new ShimmerFrameLayout(context);
            shimmerLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300 // Adjust height as needed
            ));
            shimmerLayout.setId(View.generateViewId()); // Set an ID to access later in AdViewHolder

            // Create a placeholder view for shimmer effect
            View shimmerView = new View(context);
            shimmerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    300 // Adjust height as needed
            ));
            shimmerView.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            shimmerLayout.addView(shimmerView);

            // Add shimmer to ad container
            adContainer.addView(shimmerLayout);

            return new AdViewHolder(adContainer, shimmerLayout);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_rv_fonts, viewGroup, false);
            return new FontsViewHolder(view);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int actualPosition;

        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)){
            actualPosition =  position - (position / 8);
        } else {
            actualPosition = position;
        }

        if (getItemViewType(position) == AD_TYPE && holder instanceof AdViewHolder) {
            AdViewHolder adViewHolder = (AdViewHolder) holder;


            loadNativeAdSmallFonts(context, adView -> {
                if (adView != null) {
                    adViewHolder.shimmerLayout.setVisibility(View.GONE);
                    adViewHolder.adContainer.removeAllViews();
                    adViewHolder.adContainer.addView(adView);
                } else {
                    Log.d("AdsLoading", "Ad failed to load or display");
                }
            });
        } else {

            //Changes
            if (holder instanceof FontsViewHolder) {

                FontsViewHolder fontsViewHolder = (FontsViewHolder) holder;
//                int actualPosition = position - (position / 8);
                if (list != null && actualPosition >= 0 && actualPosition < list.size()) {

                    LatestFontModel model = list.get(actualPosition);

                    boolean isPremium = !differentiate(actualPosition) && !freeIndexs.contains(actualPosition) && !isFreeAllowed;

                    fontsViewHolder.tvFont.setText(model.getFontText());
                    Typeface typeface = Typeface.createFromAsset(context.getAssets(), model.getFontName());
                    fontsViewHolder.tvFont.setTypeface(typeface);
                    fontsViewHolder.tvIndex.setText((actualPosition + 1) + "");
                    if (isPremium) {
                        fontsViewHolder.item_get_button.setText("Premium");
                        fontsViewHolder.item_get_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));
                        fontsViewHolder.item_get_button.setTextColor(Color.BLACK);
                    } else {
                        fontsViewHolder.item_get_button.setText("Free");
                        fontsViewHolder.item_get_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF377EF1")));
                        fontsViewHolder.item_get_button.setTextColor(Color.WHITE);
                    }

                    if (fontsViewHolder.item_get_button != null)
                        fontsViewHolder.item_get_button.setOnClickListener(view -> callback.onItemSelected(model, actualPosition));
                    if (fontsViewHolder.tvFont != null)
                        fontsViewHolder.tvFont.setOnClickListener(view -> callback.onItemSelected(model, actualPosition));
                }
            }
        }
    }

    @Override
    public int getItemCount() {


        if(latestBillingHelper.shouldApplyMonetization() && LatestUtils.isConnectionAvailable(context)) {


            return list.size() + (list.size() / 8);
        }else{
            return  list.size();
        }
    }

    static class FontsViewHolder extends RecyclerView.ViewHolder {
        TextView tvFont, tvIndex;
        Button item_get_button;

        public FontsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFont = itemView.findViewById(R.id.tvFont);
            tvIndex = itemView.findViewById(R.id.indexNumber);
            item_get_button = itemView.findViewById(R.id.item_get_button);
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        FrameLayout adContainer;
        ShimmerFrameLayout shimmerLayout;

        public AdViewHolder(@NonNull View itemView, ShimmerFrameLayout shimmer) {
            super(itemView);
            adContainer = (FrameLayout) itemView;
            shimmerLayout = shimmer;
        }
    }

    public static boolean differentiate(int i) {
        switch (i % 6) {
            case 0:
            case 1:
            case 2:
                return true;
            case 3:
            case 4:
            case 5:
                return false;
            default:
                return false;
        }
    }


    static void loadNativeAdSmallFonts(Context context, NativeAdLoadCallback adLoadCallback) {
        AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.native_ads_fonts_id))
                .forNativeAd(nativeAd -> {
                    // Inflate the native ad layout and populate the NativeAdView.
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(
                            R.layout.native_ads_layout_small, null);

                    populateNativeAdViewSmallFonts(nativeAd, adView);

                    // Create and configure the LinearLayout container.
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
                        adLoadCallback.onAdLoaded(null);  // Call the callback with null if loading failed
                    }

                    @Override
                    public void onAdLoaded() {
                        Log.d("AdsLoading", "onAdLoaded");
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Customize your native ad options here
                        .build())
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    interface NativeAdLoadCallback {
        void onAdLoaded(LinearLayout adContainer);
    }
}
