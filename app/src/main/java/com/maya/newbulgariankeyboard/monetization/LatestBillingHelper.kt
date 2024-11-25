package com.maya.newbulgariankeyboard.monetization

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.maya.newbulgariankeyboard.activities.LatestPremiumActivity

class LatestBillingHelper(private val activityContext: Context) : PurchasesUpdatedListener {


    private lateinit var mBillingClient: BillingClient
    private val TAG = "mBillingPreferences:"
    private val mListOfPurchases = ArrayList<SkuDetails>()
    private val listOfSubscriptions = ArrayList<SkuDetails>()
    private lateinit var mBillingPreferences: SharedPreferences

    init {
        initBillingHelper()
    }

    private fun initBillingHelper() {
        mBillingPreferences =
            activityContext.getSharedPreferences("BillingPrefs", Context.MODE_PRIVATE)
        mBillingClient = BillingClient
            .newBuilder(activityContext)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        mBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing is successfully Connected")
                    callForAllInAppProducts()
                    callAvailableSubscriptions()
                    callForAllPurchasedProducts()
                    callForAllSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing is  Disconnected")
            }
        })
    }

    //todo change
    private fun callForAllInAppProducts() {
        val listToQuery = ArrayList<String>()
        listToQuery.add("bulgarian_premium")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(listToQuery).setType(BillingClient.SkuType.INAPP)
        mBillingClient.querySkuDetailsAsync(
            params.build(),
            object : SkuDetailsResponseListener {
                override fun onSkuDetailsResponse(
                    result: BillingResult,
                    skuDetails: MutableList<SkuDetails>?
                ) {
                    Log.i(TAG, "onSkuDetailsResponse ${result.responseCode}")
                    if (skuDetails != null) {
                        for (skuDetail in skuDetails) {
                            mListOfPurchases.add(skuDetail)
                            Log.i(TAG, skuDetail.toString())
                        }
                        try {
                            LatestPremiumActivity.priceLifeTimeString =
                                "(${mListOfPurchases[0].price}/Lifetime)"
                        } catch (e: Exception) {
                        }
                    } else {
                        Log.i(TAG, "No skus found from query")
                    }
                }

            })
    }

    fun callForAllPurchasedProducts() {

        mBillingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP,
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    listAllInApps: MutableList<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                        Log.d("adrgbvadrgvbadr", "onQueryPurchasesResponse: ${listAllInApps.size}")
                        if (listAllInApps.size > 0) {
                            for (singlePurchase in listAllInApps) {
                                Log.d("adrgbvadrgvbadr", "onQueryPurchasesResponse: $singlePurchase")
                                if (singlePurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    mBillingPreferences.edit()
                                        .putBoolean(singlePurchase.skus[0], true).apply()
                                } else {
                                    mBillingPreferences.edit()
                                        .putBoolean(singlePurchase.skus[0], false).apply()
                                    Log.d(TAG, "Not Purchased: ${singlePurchase.skus[0]}")
                                }
                            }
                        } else {
                            Log.d(TAG, "List Purchase Null 1 ${listAllInApps}")
                            mBillingPreferences.edit()
                                .putBoolean("bulgarian_premium", false).apply()
                            mBillingPreferences.edit()
                                .putBoolean("bulgarian_monthly", false).apply()
                            mBillingPreferences.edit()
                                .putBoolean("bulgarian_yearly", false).apply()
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Billing Checker Failed 1: ${billingResult.responseCode}"
                        )
                    }
                }

            })


    }

    private fun callAvailableSubscriptions() {
        val skuListToQuery = ArrayList<String>()
        skuListToQuery.add("bulgarian_monthly")
        skuListToQuery.add("bulgarian_yearly")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuListToQuery).setType(BillingClient.SkuType.SUBS)
        mBillingClient.querySkuDetailsAsync(
            params.build(),
            object : SkuDetailsResponseListener {
                override fun onSkuDetailsResponse(
                    result: BillingResult,
                    skuDetails: MutableList<SkuDetails>?
                ) {
                    Log.i(TAG, "onSkuDetailsResponse ${result.responseCode}")
                    if (skuDetails != null) {
                        for (skuDetail in skuDetails) {
                            listOfSubscriptions.add(skuDetail)
                            Log.i(TAG, skuDetail.toString())
                        }
                        try {
                            LatestPremiumActivity.priceOneMonthString =
                                "(${listOfSubscriptions[0].price}/Monthly)"

                            LatestPremiumActivity.priceOneYearString =
                                "(${listOfSubscriptions[1].price}/Yearly)"

                        } catch (_: Exception) {
                        }
                    } else {
                        Log.i(TAG, "No skus found from query")
                    }
                }
            })
    }

    fun callForAllSubscriptions() {

        mBillingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    listPurchases: MutableList<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (listPurchases.size > 0) {
                            for (singlePurchase in listPurchases) {
                                if (singlePurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                    Log.d(TAG, "Subscribed: ${singlePurchase.skus[0]}")
                                    mBillingPreferences.edit()
                                        .putBoolean(singlePurchase.skus[0], true)
                                        .apply()
                                } else {
                                    mBillingPreferences.edit()
                                        .putBoolean(singlePurchase.skus[0], false)
                                        .apply()
                                    Log.d(TAG, "Not Subscribed: ${singlePurchase.skus[0]}")
                                }
                            }
                        } else {
                            Log.d(TAG, "List Purchase Null 2 ${listPurchases}")
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Billing Checker Failed 2: ${billingResult.responseCode}"
                        )
                    }
                }

            })

    }

    fun shouldApplyMonetization(): Boolean {

        var temp = !((mBillingPreferences.getBoolean("bulgarian_premium", false)
                || mBillingPreferences.getBoolean("bulgarian_monthly", false)
                || mBillingPreferences.getBoolean("bulgarian_yearly", false)
                ))
        return temp
    }

    fun purchasePremiumOneMonthSubscribed() {
        Log.d(TAG, "Purchasing Subscription 1")
        if (listOfSubscriptions.size > 0) {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(listOfSubscriptions[0])
                .build()
            val responseCode =
                mBillingClient.launchBillingFlow(
                    activityContext as Activity,
                    flowParams
                ).responseCode
            Log.d(TAG, "Billing Response Code: " + responseCode)
        } else {
            Log.d(TAG, "Nothing to subscribe 1")
        }
    }


    fun purchasePremiumOneYearSubscribed() {
        Log.d(TAG, "Purchasing Subscription 1")
        if (listOfSubscriptions.size > 0) {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(listOfSubscriptions[1])
                .build()
            val responseCode =
                mBillingClient.launchBillingFlow(
                    activityContext as Activity,
                    flowParams
                ).responseCode
            Log.d(TAG, "Billing Response Code: " + responseCode)
        } else {
            Log.d(TAG, "Nothing to subscribe 1")
        }
    }

    fun purchaseAdsPlan() {
        Log.d(TAG, "Purchasing Ads")
        if (mListOfPurchases.size > 0) {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mListOfPurchases[0])
                .build()
            val responseCode =
                mBillingClient.launchBillingFlow(
                    activityContext as Activity,
                    flowParams
                ).responseCode
            Log.d(TAG, "Billing Response Code: " + responseCode)
        } else {
            Log.d(TAG, "Nothing to purchase")
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d(TAG, " Purchased : " + purchase.skus[0])
                afterInAppPurchaseCall(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "Billing Cancelled")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Log.d(TAG, "Billing Purchased Already")
        } else {
            Log.d(TAG, "Billing other error: " + billingResult.responseCode)
        }
    }

    private val finalAcknowledgeListener: AcknowledgePurchaseResponseListener =
        AcknowledgePurchaseResponseListener { p0 ->
            Log.d(
                TAG,
                "finalAcknowledgeListener Purchase : ${p0.responseCode}"
            )
        }

    private fun afterInAppPurchaseCall(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                Log.d(TAG, "Acknowledging: ${purchase.skus[0]}")
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                mBillingClient.acknowledgePurchase(
                    acknowledgePurchaseParams,
                    finalAcknowledgeListener
                )
            }
        }
    }
}



