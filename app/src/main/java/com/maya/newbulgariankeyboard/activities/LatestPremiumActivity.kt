package com.maya.newbulgariankeyboard.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme
import kotlinx.android.synthetic.main.activity_premium.CacelSub
import kotlinx.android.synthetic.main.activity_premium.crossButton
import kotlinx.android.synthetic.main.activity_premium.linearLayout
import kotlinx.android.synthetic.main.activity_premium.linearlayout2
import kotlinx.android.synthetic.main.activity_premium.linearlayoutyear
import kotlinx.android.synthetic.main.activity_premium.priceLifeTime
import kotlinx.android.synthetic.main.activity_premium.priceOneMonth
import kotlinx.android.synthetic.main.activity_premium.tvTermsLink
import kotlinx.android.synthetic.main.activity_premium.yearPrice

class LatestPremiumActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var appBillingHelper: LatestBillingHelper

    companion object {
        var priceOneMonthString = "1400"
        var priceOneYearString = "2000"
        var priceLifeTimeString = "2400"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val isHome = intent.getBooleanExtra("ishome", false)

        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        sharedPref.edit().putInt("theme", 0).apply()


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal, this@LatestPremiumActivity)
        setContentView(R.layout.activity_premium)
        initViews()


        crossButton.setOnClickListener {
            if (isHome) {
                startActivity(Intent(this@LatestPremiumActivity, LatestHomeActivity::class.java))
                finish()
            } else {
                finish()
            }
        }
    }

    private fun initViews() {

        appBillingHelper = LatestBillingHelper(this)

        priceOneMonth.text = priceOneMonthString
        priceLifeTime.text = priceLifeTimeString
        yearPrice.text = priceOneYearString

        tvTermsLink.setOnClickListener(this)
        linearLayout.setOnClickListener(this)
        linearlayoutyear.setOnClickListener(this)
        linearlayout2.setOnClickListener(this)
        CacelSub.setOnClickListener {

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data =
                    Uri.parse("https://play.google.com/store/account/subscriptions?sku=<SUBSCRIPTION_ID>&package=$packageName")
                setPackage("com.android.vending")  // Ensure it opens in Google Play Store
            }

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // If Google Play Store is not found on the device
                Toast.makeText(
                    this,
                    "Google Play Store not found on your device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.linearLayout -> {
                appBillingHelper.purchasePremiumOneMonthSubscribed()
            }

            R.id.linearlayoutyear -> {
                appBillingHelper.purchasePremiumOneYearSubscribed()
            }
            R.id.linearlayout2 -> {
                appBillingHelper.purchaseAdsPlan()
            }
            R.id.tvTermsLink -> {
                LatestUtils.goToSubTermsConditions(this)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this@LatestPremiumActivity, LatestHomeActivity::class.java))
    }
}