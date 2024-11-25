package com.maya.newbulgariankeyboard.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.activities.LatestPremiumActivity
import com.maya.newbulgariankeyboard.adapters.LatestFontsAdapter
import com.maya.newbulgariankeyboard.adapters.LatestFontsAdapter.isFreeAllowed
import com.maya.newbulgariankeyboard.interfaces.LatestFontsItemCallback
import com.maya.newbulgariankeyboard.main_utils.LatestFillerHelper
import com.maya.newbulgariankeyboard.main_utils.LatestFontsOverride
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.models.LatestFontModel
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.text_inputs.LatestInputHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode

class LatestFontsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val TAG = "AppFontsFragment:"
    private lateinit var mContext: Context
    private var rewardedAd: RewardedAd? = null


    lateinit var sharedPref: SharedPreferences


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val mView = inflater.inflate(R.layout.fragment_fonts, container, false)
        initViews(mView)
        return mView
    }

    private lateinit var adapter: LatestFontsAdapter

    @SuppressLint("NotifyDataSetChanged")
    private fun initViews(view: View) {

        recyclerView = view.findViewById(R.id.recyclerViewFonts);
        recyclerView.layoutManager = GridLayoutManager(mContext, 1)
        adapter = LatestFontsAdapter(mContext,
            LatestFillerHelper.fillFontsList(),
            object : LatestFontsItemCallback {
                override fun onItemSelected(model: LatestFontModel?, i: Int) {
                    if (model != null) {

                        if (!LatestFontsAdapter.differentiate(i) && !LatestFontsAdapter.freeIndexs.contains(i) && !isFreeAllowed) {

                            AlertDialog.Builder(mContext).setTitle(getString(R.string.reward_ad_show_title))
                                .setMessage(getString(R.string.fonts_reward_dailog_des))
                                .setPositiveButton(getString(R.string.reward_dailog_show_ad_btn)) { dialog, _ ->
                                    dialog.dismiss()

                                    loadAndShowRewardedAD(){
                                        LatestFontsAdapter.freeIndexs.add(i)
                                        onFontSelectedProcedure(model, i)
                                        notifyme()
                                    }

                                }.setNeutralButton(getString(R.string.reward_dailog_get_premium_btn)) { dialog, _ ->
                                    val intent = Intent(mContext, LatestPremiumActivity::class.java)
                                    startActivity(intent)
                                    dialog.dismiss()

                                }.setNegativeButton(getString(R.string.reward_dailog_cancel_btn)) { dialog, _ ->
                                    dialog.dismiss()
                                }.show()
                        } else {
                            onFontSelectedProcedure(model, i)
                        }
                    }
                }

            })
        recyclerView.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyme(){
        adapter.notifyDataSetChanged()
    }
    private fun onFontSelectedProcedure(model: LatestFontModel, i: Int) {


        with(sharedPref.edit()) {
            putInt(getString(R.string.fonts), i)
            apply()
        }

        val latestInputHelper = LatestInputHelper.getInstance()
        latestInputHelper.layoutManager.clearLayoutCache(LatestInputMode.CHARACTERS)


        LatestFontsOverride.setDefaultFont(
            mContext, "MONOSPACE", model.fontName
        )
        LatestFontsOverride.setDefaultFont(
            mContext, "DEFAULT", model.fontName
        )
        LatestFontsOverride.setDefaultFont(
            mContext, "SERIF", model.fontName
        )
        LatestFontsOverride.setDefaultFont(
            mContext, "SANS-SERIF", model.fontName
        )
        Toast.makeText(
            mContext, "Font Updated Successfully.", Toast.LENGTH_SHORT
        ).show()
    }



    private fun loadAndShowRewardedAD(onRewardGet: OnUserEarnedRewardListener) {

        if (LatestBillingHelper(mContext).shouldApplyMonetization() && LatestUtils.isConnectionAvailable(mContext = mContext)) {
            val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_with_progress, null)
            val dialogBuilder = AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.loading_ads))
                .setView(dialogView).show()

            val adRequest = AdRequest.Builder().build()
            mContext.let {
                RewardedAd.load(it,
                    getString(R.string.rewarded_ads_id),
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(TAG, adError.toString())
                            rewardedAd = null
                            dialogBuilder.dismiss()
                            Toast.makeText(
                                mContext,
                                "Failed to load reward ad. Please try again later.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            Log.d(TAG, "Ad was loaded.")
                            rewardedAd = ad
                            ad.show(mContext as Activity, onRewardGet)
                            dialogBuilder.dismiss()

                        }
                    })
            }
        } else {
            LatestUtils.showInternetDialog(requireContext())
        }
    }
    private var isPurchased = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        isPurchased = !LatestBillingHelper(context).shouldApplyMonetization()

        Log.d(TAG, "onAttach: $isPurchased")
        if (isPurchased) {
            isFreeAllowed = true
            Log.d("cdsavcadsfvcearfc", "onAttach: IS FREE $isFreeAllowed")
        }
        mContext = context
    }


}
