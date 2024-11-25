package com.maya.newbulgariankeyboard.main_classes

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.preference.PreferenceManager
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGestureDistanceValues
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGesturesAction
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestSpeedHelperGesture

class LatestPreferencesHelper(
    private val context: Context,
    val shared: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) {

    val mAppLocalization = AppLocalization(this)
    val mAppSuggestions = AppSuggestions(this)
    val mSingleSuggestion = AppSingleSuggestion(this)
    val mThemingApp = AppTheming(this)
    val mAppVoiceTyping = AppVoiceTyping(this)
    val mAppEditingPanel = AppEditingPanel(this)
    val mAppAdvanced = AppAdvanced(this)
    val mAppCorrection = AppCorrection(this)
    val mAppGestures = AppGestures(this)
    val mAppGlidingTyping = AppGlidingTyping(this)
    val mAppInternal = LatestAppInternal(this)
    val mAppKeyboard = AppKeyboard(this)
    private val cacheBoolean: HashMap<String, Boolean> = hashMapOf()
    private val cacheInt: HashMap<String, Int> = hashMapOf()
    private val cacheString: HashMap<String, String> = hashMapOf()


    companion object {
        @SuppressLint("StaticFieldLeak")
        private var latestPreferencesHelper: LatestPreferencesHelper? = null

        @Synchronized
        fun getDefaultInstance(context: Context): LatestPreferencesHelper {
            if (latestPreferencesHelper == null) {
                latestPreferencesHelper = LatestPreferencesHelper(context)
            }
            return latestPreferencesHelper!!
        }
    }


    fun initAppPreferences() {}

    fun sync() {
        val contentResolver = context.contentResolver
        mAppKeyboard.soundEnabledSystem = Settings.System.getInt(
            contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 0
        ) != 0
        mAppKeyboard.vibrationEnabledSystem = Settings.System.getInt(
            contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 0
        ) != 0

        cacheBoolean.clear()
        cacheInt.clear()
        cacheString.clear()
    }


    class AppAdvanced(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val SETTINGS_THEME = "advanced_settings_theme"
        }

        var settingsTheme: String = ""
            get() = latestPreferencesHelper.getPref(SETTINGS_THEME, "auto")
            private set
    }


    class AppCorrection(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val AUTO_CAPITALIZATION = "correction_auto_capitalization"
            const val DOUBLE_SPACE_PERIOD = "correction_double_space_period"
            const val REMEMBER_CAPS_LOCK_STATE = "correction_remember_caps_lock_state"
        }

        var autoCapitalization: Boolean
            get() = latestPreferencesHelper.getPref(AUTO_CAPITALIZATION, true)
            set(v) = latestPreferencesHelper.setPref(AUTO_CAPITALIZATION, v)
        var doubleSpacePeriod: Boolean
            get() = latestPreferencesHelper.getPref(DOUBLE_SPACE_PERIOD, true)
            set(v) = latestPreferencesHelper.setPref(DOUBLE_SPACE_PERIOD, v)
        var rememberCapsLockState: Boolean
            get() = latestPreferencesHelper.getPref(REMEMBER_CAPS_LOCK_STATE, false)
            set(v) = latestPreferencesHelper.setPref(REMEMBER_CAPS_LOCK_STATE, v)
    }


    class AppVoiceTyping(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val VOICE_ENABLED = "voice_feature_enable"
        }

        var isVoiceEnabled: Boolean
            get() = latestPreferencesHelper.getPref(VOICE_ENABLED, true)
            set(v) = latestPreferencesHelper.setPref(VOICE_ENABLED, v)
    }

    class AppEditingPanel(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val EDITING_PANEL_ENABLED = "editing_feature_enable"
        }

        var isEditingPanelEnabled: Boolean
            get() = latestPreferencesHelper.getPref(EDITING_PANEL_ENABLED, true)
            set(v) = latestPreferencesHelper.setPref(EDITING_PANEL_ENABLED, v)
    }

    class AppGestures(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val SWIPE_UP = "gestures_swipe_up"
            const val SWIPE_DOWN = "gestures_swipe_down"
            const val SWIPE_LEFT = "gestures_swipe_left"
            const val SWIPE_RIGHT = "gestures_swipe_right"
            const val SPACE_BAR_SWIPE_LEFT = "gestures_space_bar_swipe_left"
            const val SPACE_BAR_SWIPE_RIGHT = "gestures_space_bar_swipe_right"
            const val DELETE_KEY_SWIPE_LEFT = "gestures_delete_key_swipe_left"
            const val SWIPE_VELOCITY_THRESHOLD = "gestures_swipe_velocity_threshold"
            const val SWIPE_DISTANCE_THRESHOLD = "gestures_swipe_distance_threshold"
        }

        var latestGesturesUp: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_UP,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_UP, v)
        var latestGesturesDown: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_DOWN,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_DOWN, v)
        var latestGesturesLeft: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_LEFT,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_LEFT, v)
        var latestGesturesRight: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_RIGHT,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_RIGHT, v)
        var spaceBarLatestGesturesLeft: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SPACE_BAR_SWIPE_LEFT,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SPACE_BAR_SWIPE_LEFT, v)
        var spaceBarLatestGesturesRight: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    SPACE_BAR_SWIPE_RIGHT,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SPACE_BAR_SWIPE_RIGHT, v)
        var deleteKeyLatestGesturesLeft: LatestGesturesAction
            get() = LatestGesturesAction.fromString(
                latestPreferencesHelper.getPref(
                    DELETE_KEY_SWIPE_LEFT,
                    "no_action"
                )
            )
            set(v) = latestPreferencesHelper.setPref(DELETE_KEY_SWIPE_LEFT, v)
        var swipeLatestSpeedHelperGesture: LatestSpeedHelperGesture
            get() = LatestSpeedHelperGesture.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_VELOCITY_THRESHOLD,
                    "normal"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_VELOCITY_THRESHOLD, v)
        var swipeLatestGestureDistanceValues: LatestGestureDistanceValues
            get() = LatestGestureDistanceValues.fromString(
                latestPreferencesHelper.getPref(
                    SWIPE_DISTANCE_THRESHOLD,
                    "normal"
                )
            )
            set(v) = latestPreferencesHelper.setPref(SWIPE_DISTANCE_THRESHOLD, v)
    }


    class AppGlidingTyping(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val ENABLED = "glide_enabled"
            const val SHOW_TRAIL = "glide_show_trail"
        }

        var enabled: Boolean
            get() = latestPreferencesHelper.getPref(ENABLED, false)
            set(v) = latestPreferencesHelper.setPref(ENABLED, v)
        var showTrail: Boolean
            get() = latestPreferencesHelper.getPref(SHOW_TRAIL, false)
            set(v) = latestPreferencesHelper.setPref(SHOW_TRAIL, v)
    }


    private inline fun <reified T> getPref(key: String, default: T): T {
        return when {
            false is T -> {
                (cacheBoolean[key] ?: getPrefInternal(key, default)) as T
            }
            0 is T -> {
                (cacheInt[key] ?: getPrefInternal(key, default)) as T
            }
            "" is T -> {
                (cacheString[key] ?: getPrefInternal(key, default)) as T
            }
            else -> null as T
        }
    }


    private inline fun <reified T> getPrefInternal(key: String, default: T): T {
        return when {
            false is T -> {
                val value = shared.getBoolean(key, default as Boolean)
                cacheBoolean[key] = value
                value as T
            }
            0 is T -> {
                val value = shared.getInt(key, default as Int)
                cacheInt[key] = value
                value as T
            }
            "" is T -> {
                val value = (shared.getString(key, default as String) ?: (default as String))
                cacheString[key] = value
                value as T
            }
            else -> null as T
        }
    }


    private inline fun <reified T> setPref(key: String, value: T) {
        when {
            false is T -> {
                shared.edit().putBoolean(key, value as Boolean).apply()
                cacheBoolean[key] = value as Boolean
            }
            0 is T -> {
                shared.edit().putInt(key, value as Int).apply()
                cacheInt[key] = value as Int
            }
            "" is T -> {
                shared.edit().putString(key, value as String).apply()
                cacheString[key] = value as String
            }
        }
    }

    class LatestAppInternal(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val IS_IME_SET_UP = "internal_is_ime_set_up"
            const val THEME_CURRENT_BASED_ON = "internal_theme_current_based_on"
            const val THEME_CURRENT_IS_MODIFIED = "internal_theme_current_is_modified"
            const val THEME_CURRENT_IS_NIGHT = "internal_theme_current_is_night"
        }

        var isImeSetUp: Boolean
            get() = latestPreferencesHelper.getPref(IS_IME_SET_UP, false)
            set(v) = latestPreferencesHelper.setPref(IS_IME_SET_UP, v)
        var themeCurrentBasedOn: String
            get() = latestPreferencesHelper.getPref(THEME_CURRENT_BASED_ON, "undefined")
            set(v) = latestPreferencesHelper.setPref(THEME_CURRENT_BASED_ON, v)
        var themeCurrentIsModified: Boolean
            get() = latestPreferencesHelper.getPref(THEME_CURRENT_IS_MODIFIED, false)
            set(v) = latestPreferencesHelper.setPref(THEME_CURRENT_IS_MODIFIED, v)
        var themeCurrentIsNight: Boolean
            get() = latestPreferencesHelper.getPref(THEME_CURRENT_IS_NIGHT, false)
            set(v) = latestPreferencesHelper.setPref(THEME_CURRENT_IS_NIGHT, v)
    }

    class AppKeyboard(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val BOTTOM_OFFSET = "keyboard_bottom_offset"
            const val FONT_SIZE_MULTIPLIER_PORTRAIT = "keyboard_font_size_multiplier_portrait"
            const val FONT_SIZE_MULTIPLIER_LANDSCAPE = "keyboard_font_size_multiplier_landscape"
            const val HEIGHT_FACTOR = "keyboard_height_factor"
            const val HEIGHT_FACTOR_CUSTOM = "keyboard_height_factor_custom"
            const val HINTED_NUMBER_ROW = "keyboard_hinted_number_row"
            const val HINTED_SYMBOLS = "keyboard_hinted_symbols"
            const val LONG_PRESS_DELAY = "keyboard_long_press_delay"
            const val NUMBER_ROW = "keyboard_number_row"
            const val ONE_HANDED_MODE = "keyboard_one_handed_mode"
            const val POPUP_ENABLED = "keyboard_popup_enabled"
            const val SOUND_ENABLED = "keyboard_sound_enabled"
            const val SOUND_VOLUME = "keyboard_sound_volume"
            const val VIBRATION_ENABLED = "keyboard_vibration_enabled"
            const val VIBRATION_STRENGTH = "keyboard_vibration_strength"
        }

        var bottomOffset: Int = 0
            get() = latestPreferencesHelper.getPref(BOTTOM_OFFSET, 0)
            private set
        var fontSizeMultiplierPortrait: Int
            get() = latestPreferencesHelper.getPref(FONT_SIZE_MULTIPLIER_PORTRAIT, 110)
            set(v) = latestPreferencesHelper.setPref(FONT_SIZE_MULTIPLIER_PORTRAIT, v)
        var fontSizeMultiplierLandscape: Int
            get() = latestPreferencesHelper.getPref(FONT_SIZE_MULTIPLIER_LANDSCAPE, 110)
            set(v) = latestPreferencesHelper.setPref(FONT_SIZE_MULTIPLIER_LANDSCAPE, v)
        var heightFactor: String = ""
            get() = latestPreferencesHelper.getPref(HEIGHT_FACTOR, "normal")
            private set
        var heightFactorCustom: Int
            get() = latestPreferencesHelper.getPref(HEIGHT_FACTOR_CUSTOM, 100)
            set(v) = latestPreferencesHelper.setPref(HEIGHT_FACTOR_CUSTOM, v)
        var hintedNumberRow: Boolean
            get() = latestPreferencesHelper.getPref(HINTED_NUMBER_ROW, false)
            set(v) = latestPreferencesHelper.setPref(HINTED_NUMBER_ROW, v)
        var hintedSymbols: Boolean
            get() = latestPreferencesHelper.getPref(HINTED_SYMBOLS, false)
            set(v) = latestPreferencesHelper.setPref(HINTED_SYMBOLS, v)
        var longPressDelay: Int = 0
            get() = latestPreferencesHelper.getPref(LONG_PRESS_DELAY, 300)
            private set
        var numberRow: Boolean
            get() = latestPreferencesHelper.getPref(NUMBER_ROW, false)
            set(v) = latestPreferencesHelper.setPref(NUMBER_ROW, v)
        var oneHandedMode: String
            get() = latestPreferencesHelper.getPref(ONE_HANDED_MODE, "off")
            set(value) = latestPreferencesHelper.setPref(ONE_HANDED_MODE, value)
        var popupEnabled: Boolean = false
            get() = latestPreferencesHelper.getPref(POPUP_ENABLED, true)
            private set
        var soundEnabled: Boolean = false
            get() = latestPreferencesHelper.getPref(SOUND_ENABLED, true)
            private set
        var soundEnabledSystem: Boolean = false
        var soundVolume: Int = 0
            get() = latestPreferencesHelper.getPref(SOUND_VOLUME, -1)
            private set
        var vibrationEnabled: Boolean = false
            get() = latestPreferencesHelper.getPref(VIBRATION_ENABLED, true)
            private set
        var vibrationEnabledSystem: Boolean = false
        var vibrationStrength: Int = 0
            get() = latestPreferencesHelper.getPref(VIBRATION_STRENGTH, -1)
            private set
    }

    class AppLocalization(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val ACTIVE_SUBTYPE_ID = "localization_active_subtype_id"
            const val SUBTYPES = "localization_subtypes"
        }

        var activeSubtypeId: Int
            get() = latestPreferencesHelper.getPref(ACTIVE_SUBTYPE_ID, LanguageModel.DEFAULT.id)
            set(v) = latestPreferencesHelper.setPref(ACTIVE_SUBTYPE_ID, v)
        var subtypes: String
            get() = latestPreferencesHelper.getPref(SUBTYPES, "")
            set(v) = latestPreferencesHelper.setPref(SUBTYPES, v)
    }


    class AppSuggestions(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val ENABLED = "app_suggestion_enable"
        }

        var enabled: Boolean
            get() = latestPreferencesHelper.getPref(ENABLED, true)
            set(v) = latestPreferencesHelper.setPref(ENABLED, v)
    }


    class AppSingleSuggestion(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val ENABLED = "suggestion_enabled"
            const val SUGGEST_CLIPBOARD_CONTENT = "suggestion_suggest_clipboard_content"
            const val USE_PREV_WORDS = "suggestion_use_prev_words"
        }

        var enabled: Boolean
            get() = latestPreferencesHelper.getPref(ENABLED, true)
            set(v) = latestPreferencesHelper.setPref(ENABLED, v)
        var suggestClipboardContent: Boolean
            get() = latestPreferencesHelper.getPref(SUGGEST_CLIPBOARD_CONTENT, false)
            set(v) = latestPreferencesHelper.setPref(SUGGEST_CLIPBOARD_CONTENT, v)
    }


    class AppTheming(private val latestPreferencesHelper: LatestPreferencesHelper) {
        companion object {
            const val COLOR_PRIMARY = "theme_colorPrimary"
            const val COLOR_PRIMARY_DARK = "theme_colorPrimaryDark"
            const val COLOR_ACCENT = "theme_colorAccent"
            const val NAV_BAR_COLOR = "theme_navBarColor"
            const val NAV_BAR_IS_LIGHT = "theme_navBarIsLight"
            const val KEYBOARD_BG_COLOR = "theme_keyboard_bgColor"
            const val KEY_BG_COLOR = "theme_key_bgColor"
            const val KEY_BG_COLOR_PRESSED = "theme_key_bgColorPressed"
            const val KEY_FG_COLOR = "theme_key_fgColor"
            const val KEY_ENTER_BG_COLOR = "theme_keyEnter_bgColor"
            const val KEY_ENTER_BG_COLOR_PRESSED = "theme_keyEnter_bgColorPressed"
            const val KEY_ENTER_FG_COLOR = "theme_keyEnter_fgColor"
            const val KEY_SHIFT_BG_COLOR = "theme_keyShift_bgColor"
            const val KEY_SHIFT_BG_COLOR_PRESSED = "theme_keyShift_bgColorPressed"
            const val KEY_SHIFT_FG_COLOR = "theme_keyShift_fgColor"
            const val KEY_SHIFT_FG_COLOR_CAPSLOCK = "theme_keyShift_fgColorCapsLock"
            const val KEY_POPUP_BG_COLOR = "theme_keyPopup_bgColor"
            const val KEY_POPUP_BG_COLOR_ACTIVE = "theme_keyPopup_bgColorActive"
            const val KEY_POPUP_FG_COLOR = "theme_keyPopup_fgColor"
            const val MEDIA_FG_COLOR = "theme_media_fgColor"
            const val MEDIA_FG_COLOR_ALT = "theme_media_fgColorAlt"
            const val ONE_HANDED_BG_COLOR = "theme_oneHanded_bgColor"
            const val ONE_HANDED_BUTTON_FG_COLOR = "theme_oneHandedButton_fgColor"
            const val SMARTBAR_BG_COLOR = "theme_smartbar_bgColor"
            const val SMARTBAR_FG_COLOR = "theme_smartbar_fgColor"
            const val SMARTBAR_FG_COLOR_ALT = "theme_smartbar_fgColorAlt"
            const val SMARTBAR_BUTTON_BG_COLOR = "theme_smartbarButton_bgColor"
            const val SMARTBAR_BUTTON_FG_COLOR = "theme_smartbarButton_fgColor"
            const val MEDIA_THEME_KEY = "media_theme_key"
            const val IS_DB_INSTALLED = "is_db_installed"
            const val KEY_BG_SHAPE = "shape_key_bgColor"
            const val KEY_BG_PHOTO = "photo_key_bgColor"
        }

        var isDbInstalled: Boolean
            get() = latestPreferencesHelper.getPref(IS_DB_INSTALLED, false)
            set(v) = latestPreferencesHelper.setPref(IS_DB_INSTALLED, v)
        var colorPrimary: Int
            get() = latestPreferencesHelper.getPref(COLOR_PRIMARY, 0)
            set(v) = latestPreferencesHelper.setPref(COLOR_PRIMARY, v)
        var colorPrimaryDark: Int
            get() = latestPreferencesHelper.getPref(COLOR_PRIMARY_DARK, 0)
            set(v) = latestPreferencesHelper.setPref(COLOR_PRIMARY_DARK, v)
        var colorAccent: Int
            get() = latestPreferencesHelper.getPref(COLOR_ACCENT, 0)
            set(v) = latestPreferencesHelper.setPref(COLOR_ACCENT, v)
        var navBarColor: Int
            get() = latestPreferencesHelper.getPref(NAV_BAR_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(NAV_BAR_COLOR, v)
        var navBarIsLight: Boolean
            get() = latestPreferencesHelper.getPref(NAV_BAR_IS_LIGHT, false)
            set(v) = latestPreferencesHelper.setPref(NAV_BAR_IS_LIGHT, v)
        var keyboardBgColor: Int
            get() = latestPreferencesHelper.getPref(KEYBOARD_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEYBOARD_BG_COLOR, v)
        var keyboardBgShape: Int
            get() = latestPreferencesHelper.getPref(KEY_BG_SHAPE, R.drawable.ic_photos_theme_1)
            set(v) = latestPreferencesHelper.setPref(KEY_BG_SHAPE, v)
        var keyboardBgPhoto: String
            get() = latestPreferencesHelper.getPref(KEY_BG_PHOTO, "Path")
            set(v) = latestPreferencesHelper.setPref(KEY_BG_PHOTO, v)
        var keyBgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_BG_COLOR, v)
        var keyBgColorPressed: Int
            get() = latestPreferencesHelper.getPref(KEY_BG_COLOR_PRESSED, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_BG_COLOR_PRESSED, v)
        var keyFgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_FG_COLOR, v)
        var keyEnterBgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_ENTER_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_ENTER_BG_COLOR, v)
        var keyEnterBgColorPressed: Int
            get() = latestPreferencesHelper.getPref(KEY_ENTER_BG_COLOR_PRESSED, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_ENTER_BG_COLOR_PRESSED, v)
        var keyEnterFgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_ENTER_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_ENTER_FG_COLOR, v)
        var keyShiftBgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_SHIFT_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_SHIFT_BG_COLOR, v)
        var keyShiftBgColorPressed: Int
            get() = latestPreferencesHelper.getPref(KEY_SHIFT_BG_COLOR_PRESSED, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_SHIFT_BG_COLOR_PRESSED, v)
        var keyShiftFgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_SHIFT_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_SHIFT_FG_COLOR, v)
        var keyShiftFgColorCapsLock: Int
            get() = latestPreferencesHelper.getPref(KEY_SHIFT_FG_COLOR_CAPSLOCK, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_SHIFT_FG_COLOR_CAPSLOCK, v)
        var keyPopupBgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_POPUP_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_POPUP_BG_COLOR, v)
        var keyPopupBgColorActive: Int
            get() = latestPreferencesHelper.getPref(KEY_POPUP_BG_COLOR_ACTIVE, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_POPUP_BG_COLOR_ACTIVE, v)
        var keyPopupFgColor: Int
            get() = latestPreferencesHelper.getPref(KEY_POPUP_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(KEY_POPUP_FG_COLOR, v)
        var mediaFgColor: Int
            get() = latestPreferencesHelper.getPref(MEDIA_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(MEDIA_FG_COLOR, v)
        var mediaFgColorAlt: Int
            get() = latestPreferencesHelper.getPref(MEDIA_FG_COLOR_ALT, 0)
            set(v) = latestPreferencesHelper.setPref(MEDIA_FG_COLOR_ALT, v)
        var oneHandedBgColor: Int
            get() = latestPreferencesHelper.getPref(ONE_HANDED_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(ONE_HANDED_BG_COLOR, v)
        var oneHandedButtonFgColor: Int
            get() = latestPreferencesHelper.getPref(ONE_HANDED_BUTTON_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(ONE_HANDED_BUTTON_FG_COLOR, v)
        var smartbarBgColor: Int
            get() = latestPreferencesHelper.getPref(SMARTBAR_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(SMARTBAR_BG_COLOR, v)
        var smartbarFgColor: Int
            get() = latestPreferencesHelper.getPref(SMARTBAR_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(SMARTBAR_FG_COLOR, v)
        var smartbarFgColorAlt: Int
            get() = latestPreferencesHelper.getPref(SMARTBAR_FG_COLOR_ALT, 0)
            set(v) = latestPreferencesHelper.setPref(SMARTBAR_FG_COLOR_ALT, v)
        var smartbarButtonBgColor: Int
            get() = latestPreferencesHelper.getPref(SMARTBAR_BUTTON_BG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(SMARTBAR_BUTTON_BG_COLOR, v)
        var smartbarButtonFgColor: Int
            get() = latestPreferencesHelper.getPref(SMARTBAR_BUTTON_FG_COLOR, 0)
            set(v) = latestPreferencesHelper.setPref(SMARTBAR_BUTTON_FG_COLOR, v)
        var isMediaTheme: Int
            get() = latestPreferencesHelper.getPref(MEDIA_THEME_KEY, 1)
            set(v) = latestPreferencesHelper.setPref(MEDIA_THEME_KEY, v)
    }
}
