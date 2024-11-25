package com.maya.newbulgariankeyboard.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.activities.LatestHomeActivity
import com.maya.newbulgariankeyboard.adapters.LatestHomeSettingsAdapter
import com.maya.newbulgariankeyboard.interfaces.LatestHomeItemCallback
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_utils.LatestFillerHelper
import com.maya.newbulgariankeyboard.monetization.LatestAdmobHelper
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper

class LatestHomeFragment : Fragment(),
    LatestHomeItemCallback {

    private lateinit var recyclerView: RecyclerView
    private val TAG = "AppHouseFragment:"
    private lateinit var mContext: Context

    private var adFragmentProgressDialog: ProgressDialog? = null
    private var adFragmentHandler: Handler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(mView)
        return mView
    }

    private fun initViews(view: View) {

        recyclerView = view.findViewById(R.id.recyclerViewHome)
        recyclerView.layoutManager =
            GridLayoutManager(mContext, 1)
        val list = LatestFillerHelper.fillSettingsList(mContext)
        val adapter =
            LatestHomeSettingsAdapter(
                mContext,
                list,
                this
            )
        recyclerView.adapter = adapter
    }


    override fun onItemSelected(position: Int) {

        val isImeEnabled = LatestKeyboardService.checkForEnablingOfIme(mContext)
        val isImeSelected = LatestKeyboardService.checkOfSelectionOfIme(mContext)
        val extraCondition =
            if (position == 0 || position == 1 || position == 3 || position ==  6) isImeEnabled && isImeSelected
            else true
        if (extraCondition &&
            LatestAdmobHelper.staticInterstitialAd != null && System.currentTimeMillis() % 2L == 0L && LatestBillingHelper(
                mContext
            ).shouldApplyMonetization()
        ) {
            showLoading(mContext as Activity)
            adFragmentHandler = Handler()
            adFragmentHandler!!.postDelayed(Runnable {
                hideLoading()

                if (LatestAdmobHelper.staticInterstitialAd != null) {
                    LatestAdmobHelper.staticInterstitialAd.show(mContext as Activity)
                    LatestAdmobHelper.staticInterstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                LatestAdmobHelper.staticInterstitialAd = null
                                initFullScreenAds()
                                val activityMine = activity as LatestHomeActivity
                                activityMine.setFragmentWithName(position + 1)
                                Log.e("InterstitialLogger", "onAdLoaded: InterstitialAd Dismissed")
                            }
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e("InterstitialLogger", "onAdLoaded: InterstitialAd Failed")
                                LatestAdmobHelper.staticInterstitialAd = null
                            }
                            override fun onAdShowedFullScreenContent() {
                                Log.e("InterstitialLogger", "onAdLoaded: InterstitialAd Showed")
                            }
                        }
                } else {
                    try {
                        val activityMine = activity as LatestHomeActivity
                        activityMine.setFragmentWithName(position + 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }, 1000)
        } else {
            val activityMine = activity as LatestHomeActivity
            activityMine.setFragmentWithName(position + 1)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun initFullScreenAds() {
        LatestAdmobHelper.loadAppAdFullScreenAd(mContext)
    }

    private fun showLoading(activity: Activity) {
        if (adFragmentProgressDialog != null) {
            adFragmentProgressDialog!!.dismiss()
            adFragmentProgressDialog = null
        }
        adFragmentProgressDialog = ProgressDialog(activity)
        adFragmentProgressDialog!!.setTitle(getString(R.string.loading_ads))
        adFragmentProgressDialog!!.setMessage(getString(R.string.please_wait))
        adFragmentProgressDialog!!.setCancelable(false)
        if (!adFragmentProgressDialog!!.isShowing && !activity.isFinishing) {
            adFragmentProgressDialog!!.show()
        }
    }

    private fun hideLoading() {
        if (adFragmentProgressDialog != null && adFragmentProgressDialog!!.isShowing) {
            adFragmentProgressDialog!!.dismiss()
        }
    }

}