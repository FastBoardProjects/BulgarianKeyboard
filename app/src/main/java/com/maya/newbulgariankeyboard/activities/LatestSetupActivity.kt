package com.maya.newbulgariankeyboard.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestSliderAdapter
import com.maya.newbulgariankeyboard.monetization.LatestKeyboardClass.Companion.isShowingAd
import com.maya.newbulgariankeyboard.utils.CommonFun.changeTheme
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.activity_setup.cvChoose
import kotlinx.android.synthetic.main.activity_setup.cvFinish
import kotlinx.android.synthetic.main.activity_setup.cvSwitch
import kotlinx.android.synthetic.main.activity_setup.headerTvDes
import kotlinx.android.synthetic.main.activity_setup.tvChoose
import kotlinx.android.synthetic.main.activity_setup.tvChoose1
import kotlinx.android.synthetic.main.activity_setup.tvFinish
import kotlinx.android.synthetic.main.activity_setup.tvFinish1
import kotlinx.android.synthetic.main.activity_setup.tvOne
import kotlinx.android.synthetic.main.activity_setup.tvSwitch
import kotlinx.android.synthetic.main.activity_setup.tvSwitch1
import kotlinx.android.synthetic.main.activity_setup.tvThree
import kotlinx.android.synthetic.main.activity_setup.tvTwo


class LatestSetupActivity : AppCompatActivity() {

    private val TAG = "AppSetupActivity:"
    lateinit var imm: InputMethodManager
    lateinit var prefs: LatestPreferencesHelper
    private var shouldFinish: Boolean = false
    private var handlerThread: HandlerThread? = null
    private var taskingHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isShowingAd = true

        prefs = LatestPreferencesHelper(this)
        prefs.initAppPreferences()
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

//        val mode = when (prefs.mAppAdvanced.settingsTheme) {
//            "light" -> AppCompatDelegate.MODE_NIGHT_NO
//            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
//            "auto" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
//            else -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
//        }
//        AppCompatDelegate.setDefaultNightMode(mode)
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        sharedPref.edit().putInt("theme", 0).apply()


        val themeVal = sharedPref.getInt("theme", 0)


        changeTheme(themeVal,this@LatestSetupActivity)

        setContentView(R.layout.activity_setup)
        initViews()
    }




    private fun initViews() {


        val sliderView = findViewById<SliderView>(R.id.imageSlider)

        val adapter =
            LatestSliderAdapter(
                this
            )
        adapter.count = 5

        sliderView.setSliderAdapter(adapter)

        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)

        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
        sliderView.indicatorSelectedColor = Color.BLUE
        sliderView.indicatorUnselectedColor = Color.WHITE
        sliderView.scrollTimeInSec = 4

        sliderView.startAutoCycle()

        sliderView.setOnIndicatorClickListener { position ->
            sliderView.currentPagePosition = position
        }



        handlerThread = HandlerThread("HandlerThread")
        if (handlerThread != null) {
            handlerThread!!.start() /*may be in onResume*/
            taskingHandler = Handler(handlerThread!!.looper)
        }
///  prefs.internal.isImeSetUp = true
        cvChoose.setOnClickListener {
            enableFragment()
        }
        cvSwitch.setOnClickListener {
            makeDefaultAction()
        }
        cvFinish.setOnClickListener {
            makeDefaultAction()
        }

        /*for first run so shift it*/
        /* Theme.writeThemeToPrefs(
             prefs,
             Theme.fromJsonFile(this, "app_assets/theme/app_day_theme.json")!!
         )*/

        /*Try*/
        /*   val mKeyboardService: LatestKeyboardService = LatestKeyboardService()
        Log.d("ThemingUpdate:", " updateTheme from AppSetupActivity")
        mKeyboardService.updateTheme()*/
    }

    override fun onResume() {
        super.onResume()
        if (shouldFinish) {
            if (!isFinishing) {
                finish()
            }
            return
        }
        /*use logic in intro show activity*/
        /*  if (prefs.internal.isImeSetUp) {
              launchSettingsAndSetFinishFlag()
          }*/
        updateImeIssueCardsVisibilities()
    }

    private fun launchSettingsAndSetFinishFlag() { /*1 Settings*/
        Intent(this, LatestHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
        }
        finish()
        prefs.mAppInternal.isImeSetUp = true
        shouldFinish = true

    }

    private fun enableFragment() {
        val intent = Intent()
        intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivity(intent)
    }

    private fun makeDefaultAction() {
        //just for safe
       /* Theme.writeThemeToPrefs(
            prefs,
            Theme.fromJsonFile(this, "app_assets/theme/app_day_theme.json")!!
        )*/
        imm.showInputMethodPicker()
    }

    private fun updateImeIssueCardsVisibilities() {
        val isImeEnabled = LatestKeyboardService.checkForEnablingOfIme(this)
        val isImeSelected = LatestKeyboardService.checkOfSelectionOfIme(this)
        cvChoose.visibility =
            if (isImeEnabled) {
                View.GONE
            } else {
                headerTvDes.text = getString(R.string.enable_des)
                tvChoose.text = getString(R.string.choose_keyboard)
                tvSwitch.text = ""
                tvFinish.text = ""
                View.VISIBLE

            }
        cvSwitch.visibility =
            if (!isImeEnabled || isImeSelected) {
                View.GONE
            } else {
                headerTvDes.text = getString(R.string.select_des)
                tvSwitch.text = getString(R.string.switch_keyboard)
                tvChoose.text = ""
                tvFinish.text = ""
                View.VISIBLE
            }
        cvFinish.visibility =
            if (!isImeEnabled || !isImeSelected) {
                View.GONE
            } else {
                headerTvDes.text = getString(R.string.finish_des)
                tvFinish.text = getString(R.string.finish_keyboard)
                tvChoose.text = ""
                tvSwitch.text = ""
                View.VISIBLE
            }
        tvOne.alpha =
            if (isImeEnabled) {
                0.2f
            } else {
                1f
            }
        tvTwo.alpha =
            if (!isImeEnabled || isImeSelected) {
                0.2f
            } else {
                1f
            }
        tvThree.alpha =
            if (!isImeEnabled || !isImeSelected) {
                0.2f
            } else {
                1f
            }
        /*inner text alphas*/
        tvChoose1.alpha =
            if (isImeEnabled) {
                0.2f
            } else {
                1f
            }
        tvSwitch1.alpha =
            if (!isImeEnabled || isImeSelected) {
                0.2f
            } else {
                1f
            }
        tvFinish1.alpha =
            if (!isImeEnabled || !isImeSelected) {
                0.2f
            } else {
                1f
            }
        tvChoose.alpha =
            if (isImeEnabled) {
                0.2f
            } else {
                1f
            }
        tvSwitch.alpha =
            if (!isImeEnabled || isImeSelected) {
                0.2f
            } else {
                1f
            }
        tvFinish.alpha =
            if (!isImeEnabled || !isImeSelected) {
                0.2f
            } else {
                1f
            }
        if (isImeEnabled && isImeSelected) {
            launchSettingsAndSetFinishFlag()
        }
        checkForServiceTask()
    }

    private fun checkForServiceTask() {
        if (taskingHandler != null) {
            try {
                taskingHandler!!.postDelayed(object : Runnable {
                    override fun run() {
                        val isImeEnabled =
                            LatestKeyboardService.checkForEnablingOfIme(this@LatestSetupActivity)
                        val isImeSelected =
                            LatestKeyboardService.checkOfSelectionOfIme(this@LatestSetupActivity)
                        Log.d(TAG, "Runnable working")
                        if (isImeEnabled && isImeSelected) {
                            launchSettingsAndSetFinishFlag()
                        }
                        taskingHandler!!.postDelayed(this, 1000)
                    }

                }, 1000)
            } catch (e: Exception) {
            }
        }
    }

    override fun onPause() {
        try {
            taskingHandler!!.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (handlerThread != null) {
            handlerThread!!.quitSafely()
        }
        isShowingAd = false

        super.onDestroy()
    }
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        android.app.AlertDialog.Builder(this@LatestSetupActivity)
            .setCancelable(true)
            .setIcon(R.drawable.ic_exit_icon)
            .setTitle("Exit?")
            .setMessage("Are you sure to Exit?")
            .setPositiveButton("Exit", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    finish()
                    finishAffinity()
                }

            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    try {
                        p0!!.dismiss()
                    } catch (e: Exception) {
                    }
                }

            }).show()
    }
}
