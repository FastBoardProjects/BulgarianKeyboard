package com.maya.newbulgariankeyboard.main_classes

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.voiceime.LatestVoiceRecognitionTrigger
import com.maya.newbulgariankeyboard.BuildConfig
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.activities.LatestHomeActivity
import com.maya.newbulgariankeyboard.main_utils.LatestCustomizeHelper
import com.maya.newbulgariankeyboard.main_utils.LatestViewLayoutUtils
import com.maya.newbulgariankeyboard.main_utils.refreshLayoutOf
import com.maya.newbulgariankeyboard.media_inputs.LatestMediaHelper
import com.maya.newbulgariankeyboard.suggestions_utils.keyboard_app_database.LatestDatabaseManager
import com.maya.newbulgariankeyboard.text_inputs.LatestInputHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGesturesAction
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import com.squareup.moshi.Json
import java.io.File
import java.lang.ref.WeakReference

private var latestKeyboardService: LatestKeyboardService? = null

class LatestKeyboardService : InputMethodService(), ClipboardManager.OnPrimaryClipChangedListener {

    private val TAG = "MLogger:"
    lateinit var prefs: LatestPreferencesHelper
        private set
    val context: Context
        get() = latestKeyboardWindowView?.context ?: this
    var latestKeyboardView: LatestKeyboardView? = null
        private set
    private var latestKeyboardWindowView: LatestKeyboardWindowView? = null
    private var eventListeners: MutableList<WeakReference<EventListener?>?> = mutableListOf()
    private var audioManager: AudioManager? = null
    var clipboardManager: ClipboardManager? = null
    private var vibrator: Vibrator? = null
    private val osHandler = Handler()
    var activeEditorInstance: LatestServiceHelper = LatestServiceHelper.default()
    lateinit var latestLocaleHelper: LatestLocaleHelper
    lateinit var activeLanguageModel: LanguageModel
    private var currentThemeIsNight: Boolean = false
    private var currentThemeResId: Int = 0
    private var isNumberRowVisible: Boolean = false
    val latestInputHelper: LatestInputHelper
    val latestMediaHelper: LatestMediaHelper
    var databaseManager: LatestDatabaseManager? = null
    private var mVoiceRecognitionTrigger: LatestVoiceRecognitionTrigger? = null

    init {
        latestKeyboardService = this
        latestInputHelper = LatestInputHelper.getInstance()
        latestMediaHelper = LatestMediaHelper.getInstance()
    }


    /*todo Change to 14sdk*/
    companion object {

        private const val APP_KEYBOARD_PCKG: String =
            "com.maya.newbulgariankeyboard/.main_classes.LatestKeyboardService"

        fun checkForEnablingOfIme(context: Context): Boolean {
            return if (AndroidVersion.ATLEAST_API34_U) {
                context.systemServiceOrNull(InputMethodManager::class)
                    ?.enabledInputMethodList
                    ?.any { it.packageName == BuildConfig.APPLICATION_ID } ?: false
            } else {
                val enabledImeList = Settings.Secure.getString(
                    context.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS
                )
                return enabledImeList.split(":").contains(APP_KEYBOARD_PCKG)
            }
        }


       /* fun checkForEnablingOfIme(context: Context): Boolean {
            val activeImeIds = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_INPUT_METHODS
            )
            Log.d("MLogger:", "List of active IMEs: $activeImeIds")
            return activeImeIds.split(":").contains(APP_KEYBOARD_PCKG)
        }*/

        fun checkOfSelectionOfIme(context: Context): Boolean {
            val selectedImeId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
            return selectedImeId == APP_KEYBOARD_PCKG
        }

        
        @Synchronized
        fun getInstance(): LatestKeyboardService {
            if (latestKeyboardService != null)
                return latestKeyboardService!!
            else
                return LatestKeyboardService()
        }
        
        @Synchronized
        fun getInstanceOrNull(): LatestKeyboardService? {
            return latestKeyboardService
        }

        fun getDayNightBaseThemeId(isNightTheme: Boolean): Int {
            return when (isNightTheme) {
                true -> R.style.AppThemeBase_Night
                else -> R.style.AppThemeBase_Day
            }
        }

    }

    override fun onCreate() {
        mVoiceRecognitionTrigger =
            LatestVoiceRecognitionTrigger(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(this)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        prefs = LatestPreferencesHelper.getDefaultInstance(this)
        prefs.initAppPreferences()
        prefs.sync()
        latestLocaleHelper = LatestLocaleHelper(this, prefs)
        activeLanguageModel = latestLocaleHelper.getActiveSubtype() ?: LanguageModel.DEFAULT
        currentThemeIsNight = prefs.mAppInternal.themeCurrentIsNight
        currentThemeResId = getDayNightBaseThemeId(currentThemeIsNight)
        isNumberRowVisible = prefs.mAppKeyboard.numberRow
        setTheme(currentThemeResId)
        Log.d("ThemingUpdate:", " updateTheme from onCreate()")
        updateTheme()
        super.onCreate()
        eventListeners.toList().forEach { it?.get()?.onCreate() }
    }

    @SuppressLint("InflateParams")
    override fun onCreateInputView(): View? {
        if (BuildConfig.DEBUG) Log.i(TAG, "onCreateInputView()")
        baseContext.setTheme(currentThemeResId)
        try {
            latestKeyboardWindowView =
                layoutInflater.inflate(R.layout.keyboard_main_view_layout, null) as LatestKeyboardWindowView
            eventListeners.toList().forEach { it?.get()?.onCreateInputView() }
        } catch (e: Exception) {
        }
        return latestKeyboardWindowView
    }


    fun registerInputView(latestKeyboardView: LatestKeyboardView) {
        if (BuildConfig.DEBUG) Log.i(TAG, "registerInputView($latestKeyboardView)")
        this.latestKeyboardView = latestKeyboardView
        initializeOneHandedEnvironment()
        Log.d("ThemingUpdate:", " updateTheme from registerInputView() onAttachedToWindow")
        updateTheme()
        checkForSystemTheme()
        updateSoftInputWindowLayoutParameters()
        updateOneHandedPanelVisibility()
        eventListeners.toList().forEach { it?.get()?.onRegisterInputView(latestKeyboardView) }
    }

    override fun onDestroy() {
        if (BuildConfig.DEBUG) Log.i(TAG, "onDestroy()")
        clipboardManager?.removePrimaryClipChangedListener(this)
        osHandler.removeCallbacksAndMessages(null)
        latestKeyboardService = null
        if (mVoiceRecognitionTrigger != null) {
            mVoiceRecognitionTrigger = null
        }
        eventListeners.toList().forEach { it?.get()?.onDestroy() }
        eventListeners.clear()
        super.onDestroy()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onStartInput($attribute, $restarting)")
        super.onStartInput(attribute, restarting)
        setActiveInput(R.id.text_input)
        databaseManager =
            LatestDatabaseManager(
                this
            )/*first time call*/
        currentInputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
    }

    fun addCurrentWordToDb(word: String) {
        try {
            val subtypeLocale =
                latestKeyboardService!!.latestLocaleHelper.getActiveSubtype()!!.locale.toString()
            Log.d("SuggestionTaskLog:", "Current Locale: $subtypeLocale ")
            val cursor: Cursor =
                databaseManager!!.checkWord(word)
            if (cursor.count == 0) {
                Log.d("NewLfd:", " Adding Word")
                databaseManager!!.insertNewRecordWithSubtype(word, subtypeLocale)
            } else {
                Log.d("NewLfd:", " Not Adding Word")
            }
        } catch (e: Exception) {
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onStartInputView($info, $restarting)")

        super.onStartInputView(info, restarting)
        if (mVoiceRecognitionTrigger != null) {
            mVoiceRecognitionTrigger!!.onStartInputView()
        }
        setActiveInput(R.id.text_input)
        activeEditorInstance = LatestServiceHelper.from(info, this)
        eventListeners.toList().forEach {
            it?.get()?.onStartInputView(activeEditorInstance, restarting)
        }
    }

    fun startGoogleVoicePopUp() {
        if (mVoiceRecognitionTrigger!!.isInstalled && mVoiceRecognitionTrigger!!.isEnabled) {
            try {
                Log.d("VoiceInput:", "${activeLanguageModel.locale.toLanguageTag()}")
                mVoiceRecognitionTrigger!!.startVoiceRecognition(activeLanguageModel.locale.toLanguageTag())
            } catch (e: Exception) {
            }
        } else {
            Toast.makeText(this, "Voice input is not supported by your device.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onFinishInputView($finishingInput)")

        if (finishingInput) {
            activeEditorInstance = LatestServiceHelper.default()
        }
        super.onFinishInputView(finishingInput)
        eventListeners.toList().forEach { it?.get()?.onFinishInputView(finishingInput) }
    }

    override fun onFinishInput() {
        if (BuildConfig.DEBUG) Log.i(TAG, "onFinishInput()")
        super.onFinishInput()
        //activeEditorInstance.updateForCandidatesSuggestions()
        if (databaseManager != null) {
            databaseManager!!.close()
        }
        currentInputConnection?.requestCursorUpdates(0)
    }

    override fun onWindowShown() {
        if (BuildConfig.DEBUG) Log.i(TAG, "onWindowShown()")
        prefs.sync()
        val newIsNumberRowVisible = prefs.mAppKeyboard.numberRow
        if (isNumberRowVisible != newIsNumberRowVisible) {
            latestInputHelper.layoutManager.clearLayoutCache(LatestInputMode.CHARACTERS)
            isNumberRowVisible = newIsNumberRowVisible
        }
        Log.d("ThemingUpdate:", " updateTheme from onWindowShown()")
        updateTheme()
        updateOneHandedPanelVisibility()
        activeLanguageModel = latestLocaleHelper.getActiveSubtype() ?: LanguageModel.DEFAULT
        onSubtypeChanged(activeLanguageModel)
        setActiveInput(R.id.text_input)
        super.onWindowShown()
        eventListeners.toList().forEach { it?.get()?.onWindowShown() }
    }

    override fun onWindowHidden() {
        if (BuildConfig.DEBUG) Log.i(TAG, "onWindowHidden()")

        super.onWindowHidden()
        eventListeners.toList().forEach { it?.get()?.onWindowHidden() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onConfigurationChanged($newConfig)")
        if (isInputViewShown) {
            updateOneHandedPanelVisibility()
        }

        super.onConfigurationChanged(newConfig)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        if (BuildConfig.DEBUG) Log.i(
            TAG,
            "onUpdateSelection($oldSelStart, $oldSelEnd, $newSelStart, $newSelEnd, $candidatesStart, $candidatesEnd)"
        )

        super.onUpdateSelection(
            oldSelStart, oldSelEnd,
            newSelStart, newSelEnd,
            candidatesStart, candidatesEnd
        )
        activeEditorInstance.onUpdateSelection(
            oldSelStart, oldSelEnd,
            newSelStart, newSelEnd
        )
        eventListeners.toList().forEach { it?.get()?.onUpdateSelection() }
    }

    /*imp only */ /*onCreate  onRegisterInputView onWindowShown*/

    fun updateTheme() {
        val newThemeIsNightMode = prefs.mAppInternal.themeCurrentIsNight
        if (currentThemeIsNight != newThemeIsNightMode) {
            currentThemeResId = getDayNightBaseThemeId(newThemeIsNightMode)
            currentThemeIsNight = newThemeIsNightMode
            setInputView(onCreateInputView())
            return
        }
        val w = window?.window ?: return
        var flags = w.decorView.systemUiVisibility

        // Update navigation bar theme
        val bgcolor = prefs.mThemingApp.navBarColor //-2039584
        Log.d(TAG, " Set Nav Bg Color: ${bgcolor}")
        w.navigationBarColor = bgcolor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            flags = if (prefs.mThemingApp.navBarIsLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        try {
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        } catch (e: Exception) {
        }
        w.statusBarColor = Color.TRANSPARENT

        w.decorView.systemUiVisibility = flags
        /*My changes*/
        Log.d(TAG, "Keyboard Bg Color: ${prefs.mThemingApp.keyboardBgColor}")
        if (prefs.mThemingApp.isMediaTheme == 1) { /*color*/
            latestKeyboardView?.setBackgroundColor(prefs.mThemingApp.keyboardBgColor)
        } else if (prefs.mThemingApp.isMediaTheme == 2) { /*shape*/
            latestKeyboardView?.setBackgroundResource(prefs.mThemingApp.keyboardBgShape)
        } else if (prefs.mThemingApp.isMediaTheme == 3) { /*path*/
            val file = File(prefs.mThemingApp.keyboardBgPhoto)
            if (file.exists()) {
                val drawable: Drawable? =
                    Drawable.createFromPath(prefs.mThemingApp.keyboardBgPhoto)
                latestKeyboardView?.setBackgroundDrawable(drawable)
            } else {
                /*if no image (file not exists) then set color default image*/
                /*but only bg is applied rest of things are not applied*/
                /*so no del btn*/
                latestKeyboardView?.setBackgroundColor(prefs.mThemingApp.keyboardBgShape)
            }
        }
        // appKeyboardView?.setBackgroundColor(prefs.theme.keyboardBgColor)
        latestKeyboardView?.oneHandedCtrlPanelStart?.setBackgroundColor(prefs.mThemingApp.oneHandedBgColor)
        latestKeyboardView?.oneHandedCtrlPanelEnd?.setBackgroundColor(prefs.mThemingApp.oneHandedBgColor)
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_move_start)
            ?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.oneHandedButtonFgColor)
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_move_end)
            ?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.oneHandedButtonFgColor)
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_close_start)
            ?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.oneHandedButtonFgColor)
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_close_end)
            ?.imageTintList = ColorStateList.valueOf(prefs.mThemingApp.oneHandedButtonFgColor)
        eventListeners.toList().forEach { it?.get()?.onApplyThemeAttributes() }
        //checkForSystemTheme()
    }

    private fun checkForSystemTheme() {
        /*combo i*/
        val sharedPreferences =
            getSharedPreferences("Themes", Context.MODE_PRIVATE)
        val themeSelected = sharedPreferences!!.getInt("Theme", 48)
        if (themeSelected == 48) { /*if system theme selected*/
            Log.d("ThemSysLog:", " theme is system")
            if (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            ) {
                Log.d("ThemSysLog:", " theme is system dark")
                val theme = LatestCustomizeHelper.fromJsonFile(
                    this,
                    "app_assets/theme/app_night_theme.json"
                ) ?: return
                LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
            } else {
                Log.d("ThemSysLog:", " theme is system light")
                val theme = LatestCustomizeHelper.fromJsonFile(
                    this,
                    "app_assets/theme/app_day_theme.json"
                ) ?: return
                LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
            }
        } else {
            Log.d("ThemSysLog:", " theme is not system")
        }
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        val inputView = this.latestKeyboardView ?: return
        val inputWindowView = this.latestKeyboardWindowView ?: return
        if (!isInputViewShown) {
            outInsets?.contentTopInsets = inputWindowView.height
            outInsets?.visibleTopInsets = inputWindowView.height
            return
        }
        val visibleTopY = inputWindowView.height - inputView.measuredHeight
        outInsets?.contentTopInsets = visibleTopY
        outInsets?.visibleTopInsets = visibleTopY
    }

    override fun updateFullscreenMode() {
        super.updateFullscreenMode()
        updateSoftInputWindowLayoutParameters()
    }

    private fun updateSoftInputWindowLayoutParameters() {
        val w = window?.window ?: return
        try {
            LatestViewLayoutUtils.updateLayoutHeightOf(w, WindowManager.LayoutParams.MATCH_PARENT)
            val inputWindowView = this.latestKeyboardWindowView
            if (inputWindowView != null) {
                val layoutHeight = if (isFullscreenMode) {
                    WindowManager.LayoutParams.WRAP_CONTENT
                } else {
                    WindowManager.LayoutParams.MATCH_PARENT
                }
                val inputArea = w.findViewById<View>(android.R.id.inputArea)
                LatestViewLayoutUtils.updateLayoutHeightOf(inputArea, layoutHeight)
                LatestViewLayoutUtils.updateLayoutGravityOf(inputArea, Gravity.BOTTOM)
                LatestViewLayoutUtils.updateLayoutHeightOf(inputWindowView, layoutHeight)
            }
        } catch (e: Exception) {
        }
    }

    fun keyPressVibrate() {
        if (prefs.mAppKeyboard.vibrationEnabled) {
            var vibrationStrength = prefs.mAppKeyboard.vibrationStrength
            if (vibrationStrength == -1 && prefs.mAppKeyboard.vibrationEnabledSystem) {
                vibrationStrength = 36
            }
            if (vibrationStrength > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            vibrationStrength.toLong(), VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(vibrationStrength.toLong())
                }
            }
        }
    }


    fun keyPressSound(latestSingleKeyDating: LatestSingleKeyDating? = null) {
        if (prefs.mAppKeyboard.soundEnabled) {
            val soundVolume = prefs.mAppKeyboard.soundVolume
            val effect = when (latestSingleKeyDating?.code) {
                LatestSingleKeyCoding.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                LatestSingleKeyCoding.DELETE -> AudioManager.FX_KEYPRESS_DELETE
                LatestSingleKeyCoding.ENTER -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            if (soundVolume == -1 && prefs.mAppKeyboard.soundEnabledSystem) {
                audioManager!!.playSoundEffect(effect)
            } else if (soundVolume > 0) {
                audioManager!!.playSoundEffect(effect, soundVolume / 100f)
            }
        }
    }

    fun executeSwipeAction(latestGesturesAction: LatestGesturesAction) {
        when (latestGesturesAction) {
            LatestGesturesAction.HIDE_KEYBOARD -> requestHideSelf(0)
            LatestGesturesAction.SWITCH_TO_PREV_SUBTYPE -> switchToPrevSubtype()
            LatestGesturesAction.SWITCH_TO_NEXT_SUBTYPE -> switchToNextSubtype()
            else -> latestInputHelper.executeSwipeAction(latestGesturesAction)
        }
    }

    fun launchSettings() {
        requestHideSelf(0)   /*2*/
        val i = Intent(this, LatestHomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    fun shouldShowLanguageSwitch(): Boolean {
        return latestLocaleHelper.languageModels.size > 1
    }

    fun switchToPrevSubtype() {
        Log.d(TAG, " setActiveInput: switchToPrevSubtype")
        activeLanguageModel =
            latestLocaleHelper.switchToPrevFavouriteSubtype() ?: LanguageModel.DEFAULT
        onSubtypeChanged(activeLanguageModel)
    }

    fun switchToNextSubtype() {
        Log.d(TAG, " setActiveInput: switchToNextSubtype")
        activeLanguageModel =
            latestLocaleHelper.switchToNextFavouriteSubtype() ?: LanguageModel.DEFAULT
        onSubtypeChanged(activeLanguageModel)
    }

    /*change suggestion db file in this method*/
    private fun onSubtypeChanged(newLanguageModel: LanguageModel) {
        latestInputHelper.onSubtypeChanged(newLanguageModel)
        latestMediaHelper.onSubtypeChanged(newLanguageModel)
    }

    fun setActiveInput(type: Int) {
        when (type) {
            R.id.text_input -> {
                /*here enable/disable suggestions*/
                Log.d(TAG, " setActiveInput: text_input")
                latestKeyboardView?.mainViewFlipper?.displayedChild =
                    latestKeyboardView?.mainViewFlipper?.indexOfChild(latestInputHelper.textViewGroup)
                        ?: 0
            }
            R.id.media_input -> {
                Log.d(TAG, " setActiveInput: media_input")
                latestKeyboardView?.mainViewFlipper?.displayedChild =
                    latestKeyboardView?.mainViewFlipper?.indexOfChild(latestMediaHelper.mediaViewGroup)
                        ?: 0
            }
        }
    }

    private fun initializeOneHandedEnvironment() {
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_move_start)
            ?.setOnClickListener { v -> onOneHandedPanelButtonClick(v) }
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_move_end)
            ?.setOnClickListener { v -> onOneHandedPanelButtonClick(v) }
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_close_start)
            ?.setOnClickListener { v -> onOneHandedPanelButtonClick(v) }
        latestKeyboardView?.findViewById<ImageButton>(R.id.one_handed_ctrl_close_end)
            ?.setOnClickListener { v -> onOneHandedPanelButtonClick(v) }
    }

    private fun onOneHandedPanelButtonClick(v: View) {
        when (v.id) {
            R.id.one_handed_ctrl_move_start -> {
                prefs.mAppKeyboard.oneHandedMode = "start"
            }
            R.id.one_handed_ctrl_move_end -> {
                prefs.mAppKeyboard.oneHandedMode = "end"
            }
            R.id.one_handed_ctrl_close_start,
            R.id.one_handed_ctrl_close_end -> {
                prefs.mAppKeyboard.oneHandedMode = "off"
            }
        }
        updateOneHandedPanelVisibility()
    }

    fun toggleOneHandedMode() {
        when (prefs.mAppKeyboard.oneHandedMode) {
            "off" -> {
                prefs.mAppKeyboard.oneHandedMode = "end"
            }
            else -> {
                prefs.mAppKeyboard.oneHandedMode = "off"
            }
        }
        updateOneHandedPanelVisibility()
    }

    private fun updateOneHandedPanelVisibility() {
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            latestKeyboardView?.oneHandedCtrlPanelStart?.visibility = View.GONE
            latestKeyboardView?.oneHandedCtrlPanelEnd?.visibility = View.GONE
        } else {
            when (prefs.mAppKeyboard.oneHandedMode) {
                "off" -> {
                    latestKeyboardView?.oneHandedCtrlPanelStart?.visibility = View.GONE
                    latestKeyboardView?.oneHandedCtrlPanelEnd?.visibility = View.GONE
                }
                "start" -> {
                    latestKeyboardView?.oneHandedCtrlPanelStart?.visibility = View.GONE
                    latestKeyboardView?.oneHandedCtrlPanelEnd?.visibility = View.VISIBLE
                }
                "end" -> {
                    latestKeyboardView?.oneHandedCtrlPanelStart?.visibility = View.VISIBLE
                    latestKeyboardView?.oneHandedCtrlPanelEnd?.visibility = View.GONE
                }
            }
        }
        osHandler.postDelayed({
            refreshLayoutOf(latestKeyboardView)
        }, 0)
    }

    override fun onPrimaryClipChanged() {
        eventListeners.toList().forEach { it?.get()?.onPrimaryClipChanged() }
    }


    fun addEventListener(listener: EventListener): Boolean {
        return eventListeners.add(WeakReference(listener))
    }

    fun removeEventListener(listener: EventListener): Boolean {
        eventListeners.toList().forEach {
            if (it?.get() == listener) {
                return eventListeners.remove(it)
            }
        }
        return false
    }

    interface EventListener {
        fun onCreate() {}
        fun onCreateInputView() {}
        fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {}
        fun onDestroy() {}

        fun onStartInputView(instance: LatestServiceHelper, restarting: Boolean) {}
        fun onFinishInputView(finishingInput: Boolean) {}

        fun onWindowShown() {}
        fun onWindowHidden() {}

        fun onUpdateSelection() {}

        fun onApplyThemeAttributes() {}
        fun onPrimaryClipChanged() {}
        fun onSubtypeChanged(newLanguageModel: LanguageModel) {}
    }

    data class ImeConfig(
        @Json(name = "package")
        val packageName: String,
        val characterLayouts: Map<String, String> = mapOf(),
        val defaultSubtypes: List<DefaultSubtype> = listOf()
    ) {
        val defaultSubtypesLanguageCodes: List<String>
        val defaultSubtypesLanguageNames: List<String>

        init {
            val tmpCodes = mutableListOf<String>()
            val tmpNames = mutableListOf<String>()
            for (defaultSubtype in defaultSubtypes) {
                tmpCodes.add(defaultSubtype.locale.toString())
                tmpNames.add(defaultSubtype.locale.displayName)
            }
            defaultSubtypesLanguageCodes = tmpCodes.toList()
            defaultSubtypesLanguageNames = tmpNames.toList()
        }
    }
}
