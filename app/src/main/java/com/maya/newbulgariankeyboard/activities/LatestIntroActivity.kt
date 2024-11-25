package com.maya.newbulgariankeyboard.activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme


class LatestIntroActivity : IntroActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFullscreen = true
        isButtonNextVisible = true
        initViews()


        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal,this@LatestIntroActivity)
    }

    private fun initViews() {
        addSlide(
            SimpleSlide.Builder()
                .title("Languages")
                .description("Afghan Keyboard offers your multiple languages with suggestions.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Themes")
                .description("You can customize your keyboard with amazing themes and colors.")
                .image(R.drawable.keyboard)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )

        addSlide(
            SimpleSlide.Builder()
                .title("Emojis")
                .description("Afghan Keyboard offers you 1500+ Emojis.")
                .image(R.drawable.emjois)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Stickers")
                .description("You can enjoy chatting with your friends with different stickers packs.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("GIFs")
                .description("Afghan Keyboard contains GIFs of multiple categories.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Gestures")
                .description("You can customize gestures for your keyboard.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("One-Hand Typing")
                .description("You can enable/disable One-Hand typing mode for your keyboard.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Voice Typing")
                .description("You can use voice input with multiple languages.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(true)
                .build()
        )

        addSlide(
            SimpleSlide.Builder()
                .title("Enable Afghan Keyboard")
                .description("Starting using Afghan Keyboard By Enabling it from settings and selecting it as your Default Keyboard.")
                .image(R.drawable.icon_splash)
                .background(R.color.colorWhite)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .canGoForward(false)
                .buttonCtaLabel("Finish")
                .buttonCtaClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        Log.d("Intro:", " Btn clicked")
                        val intent = Intent(this@LatestIntroActivity, LatestSetupActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                })
                .build()
        )

    }
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        finishAffinity()
    }
}