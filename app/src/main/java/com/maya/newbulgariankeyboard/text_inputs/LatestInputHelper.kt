package com.maya.newbulgariankeyboard.text_inputs

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ViewFlipper
import com.maya.newbulgariankeyboard.BuildConfig
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.ImeOptions
import com.maya.newbulgariankeyboard.main_classes.InputAttributes
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardView
import com.maya.newbulgariankeyboard.main_classes.LatestServiceHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_editing_input.LatestEditingCompleteView
import com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input.LatestGesturesAction
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyType
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyVariation
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts.LatestViewerHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard_suggestions_bar.LatestCandidateView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap
import java.util.Locale


class LatestInputHelper private constructor() : CoroutineScope by MainScope(),
    LatestKeyboardService.EventListener, LatestCandidateView.EventListener {

    private val mKeyboardService = LatestKeyboardService.getInstance()
    private val activeEditorInstance: LatestServiceHelper
        get() = mKeyboardService.activeEditorInstance

    private var activeLatestInputMode: LatestInputMode? = null
    private val keyboardViews = EnumMap<LatestInputMode, LatestInputView>(LatestInputMode::class.java)
    private var latestEditingCompleteView: LatestEditingCompleteView? = null
    private val osHandler = Handler()
    private var textViewFlipper: ViewFlipper? = null
    var textViewGroup: LinearLayout? = null

    var keyVariation: KeyVariation = KeyVariation.NORMAL
    val layoutManager = LatestViewerHelper(mKeyboardService)

    var caps: Boolean = false
        private set
    var capsLock: Boolean = false
        private set
    private var hasCapsRecentlyChanged: Boolean = false
    private var hasSpaceRecentlyPressed: Boolean = false

    var isManualSelectionMode: Boolean = false
    private var isManualSelectionModeLeft: Boolean = false
    private var isManualSelectionModeRight: Boolean = false
    private val TAGME = "AppInputingHelper:"

    companion object {
        private val TAG: String? = LatestInputHelper::class.simpleName
        private var instance: LatestInputHelper? = null
        var latestCandidateView: LatestCandidateView? = null

        @Synchronized
        fun getInstance(): LatestInputHelper {
            if (instance == null) {
                try {
                    instance = LatestInputHelper()
                } catch (e: Exception) {
                }
            }
            return instance!!
        }
    }

    init {
        mKeyboardService.addEventListener(this)
    }


    override fun onCreate() {
        Log.d(TAGME, " onCreate")
        if (BuildConfig.DEBUG) Log.i(TAG, "onCreate()")
        var subtypes = mKeyboardService.latestLocaleHelper.languageModels
        if (subtypes.isEmpty()) {
            Log.d(TAGME, " Subtypes List is Empty")
            mKeyboardService.latestLocaleHelper.addAllSubtypesToIME()
            for (subtype in subtypes) {
                for (mode in LatestInputMode.values()) {
                    layoutManager.preloadComputedLayout(mode, subtype, mKeyboardService.prefs)
                }
            }
        } else {
            Log.d(TAGME, " Subtypes List is :${subtypes.size}")
            for (subtype in subtypes) {
                for (mode in LatestInputMode.values()) {
                    layoutManager.preloadComputedLayout(mode, subtype, mKeyboardService.prefs)
                }
            }
        }

    }

    private suspend fun addKeyboardView(mode: LatestInputMode) {
        Log.d(TAGME, " addKeyboardView Mode: ${mode}")
        val keyboardView = LatestInputView(mKeyboardService.context)
        keyboardView.computedLayout = layoutManager.fetchComputedLayoutAsync(
            mode,
            mKeyboardService.activeLanguageModel,
            mKeyboardService.prefs
        ).await()
        keyboardViews[mode] = keyboardView
        withContext(Dispatchers.Main) {
            textViewFlipper?.addView(keyboardView)
        }
    }


    override fun onRegisterInputView(latestKeyboardView: LatestKeyboardView) {
        Log.d(TAGME, " onRegisterInputView")
        if (BuildConfig.DEBUG) Log.i(TAG, "onRegisterInputView(inputView)")

        launch(Dispatchers.Default) {
            textViewGroup = latestKeyboardView.findViewById(R.id.text_input)
            textViewFlipper = latestKeyboardView.findViewById(R.id.text_input_view_flipper)
            latestEditingCompleteView = latestKeyboardView.findViewById(R.id.editing)

            val activeKeyboardMode = getActiveKeyboardMode()
            addKeyboardView(activeKeyboardMode)
            withContext(Dispatchers.Main) {
                setActiveKeyboardMode(activeKeyboardMode)
            }
            for (mode in LatestInputMode.values()) {
                if (mode != activeKeyboardMode && mode != LatestInputMode.SMARTBAR_NUMBER_ROW) {
                    addKeyboardView(mode)
                }
            }
        }
    }

    fun registerSuggestionsBar(view: LatestCandidateView) {
        Log.d(TAGME, " registerSmartbarView")
        latestCandidateView = view
        latestCandidateView?.setEventListener(this)
    }


    override fun onDestroy() {
        Log.d(TAGME, " onDestroy")
        if (BuildConfig.DEBUG) Log.i(TAG, "onDestroy()")

        cancel()
        osHandler.removeCallbacksAndMessages(null)
        layoutManager.onDestroy()
        instance = null
    }

    override fun onStartInputView(instance: LatestServiceHelper, restarting: Boolean) {
        Log.d(TAGME, " onStartInputView")
        val keyboardMode = when (instance.inputAttributes.type) {
            InputAttributes.Type.NUMBER -> {
                Log.d(TAGME, " onStartInputView NUMBER")
                keyVariation = KeyVariation.NORMAL
                LatestInputMode.NUMERIC
            }
            InputAttributes.Type.PHONE -> {
                Log.d(TAGME, " onStartInputView PHONE")
                keyVariation = KeyVariation.NORMAL
                LatestInputMode.PHONE
            }
            InputAttributes.Type.TEXT -> {
                Log.d(TAGME, " onStartInputView TEXT")
                keyVariation = when (instance.inputAttributes.variation) {
                    InputAttributes.Variation.EMAIL_ADDRESS,
                    InputAttributes.Variation.WEB_EMAIL_ADDRESS -> {
                        KeyVariation.EMAIL_ADDRESS
                    }
                    InputAttributes.Variation.PASSWORD,
                    InputAttributes.Variation.VISIBLE_PASSWORD,
                    InputAttributes.Variation.WEB_PASSWORD -> {
                        KeyVariation.PASSWORD
                    }
                    InputAttributes.Variation.URI -> {
                        KeyVariation.URI
                    }
                    else -> {
                        KeyVariation.NORMAL
                    }
                }
                LatestInputMode.CHARACTERS
            }
            else -> {
                keyVariation = KeyVariation.NORMAL
                LatestInputMode.CHARACTERS
            }
        }
        instance.isComposingEnabled = when (keyboardMode) {
            LatestInputMode.NUMERIC,
            LatestInputMode.PHONE,
            LatestInputMode.PHONE2 -> false
            else -> keyVariation != KeyVariation.PASSWORD &&
                    mKeyboardService.prefs.mSingleSuggestion.enabled
        }
        if (!mKeyboardService.prefs.mAppCorrection.rememberCapsLockState) {
            capsLock = false
        }
        updateCapsState()
        setActiveKeyboardMode(keyboardMode)
        latestCandidateView?.updateSuggestionsBarState()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        Log.d(TAGME, " onFinishInputView")
        latestCandidateView?.updateSuggestionsBarState()
    }

    override fun onWindowShown() {
        Log.d(TAGME, " onWindowShown")
        keyboardViews[LatestInputMode.CHARACTERS]?.updateVisibility()
        latestCandidateView?.updateSuggestionsBarState()
    }


    fun getActiveKeyboardMode(): LatestInputMode {
        //Log.d(TAGME, " getActiveKeyboardMode")
        return activeLatestInputMode ?: LatestInputMode.CHARACTERS
    }

    fun setActiveKeyboardMode(mode: LatestInputMode) {
        Log.d(TAGME, " setActiveKeyboardMode")
        textViewFlipper?.displayedChild = textViewFlipper?.indexOfChild(
            when (mode) {
                LatestInputMode.EDITING -> latestEditingCompleteView
                else -> keyboardViews[mode]
            }
        ) ?: 0
        keyboardViews[mode]?.updateVisibility()
        keyboardViews[mode]?.requestLayout()
        keyboardViews[mode]?.requestLayoutAllKeys()
        activeLatestInputMode = mode
        isManualSelectionMode = false
        isManualSelectionModeLeft = false
        isManualSelectionModeRight = false
        // TODO: show panel always
        latestCandidateView?.candidateSupportingBtns = true
        latestCandidateView?.updateSuggestionsBarState()
    }

    override fun onSubtypeChanged(newLanguageModel: LanguageModel) {
        Log.d(TAGME, " onSubtypeChanged")
        launch {
            val keyboardView = keyboardViews[LatestInputMode.CHARACTERS]
            keyboardView?.computedLayout = layoutManager.fetchComputedLayoutAsync(
                LatestInputMode.CHARACTERS,
                newLanguageModel,
                mKeyboardService.prefs
            ).await()
            keyboardView?.updateVisibility()
        }
    }


    override fun onUpdateSelection() {
        Log.d(TAGME, " onUpdateSelection")
        if (!activeEditorInstance.isNewSelectionInBoundsOfOld) {
            isManualSelectionMode = false
            isManualSelectionModeLeft = false
            isManualSelectionModeRight = false
        }
        updateCapsState()
        latestCandidateView?.updateSuggestionsBarState()
    }

    override fun onPrimaryClipChanged() {
        Log.d(TAGME, " onPrimaryClipChanged")
        latestCandidateView?.onPrimaryClipChanged()
    }

    private fun updateCapsState() {
        Log.d(TAGME, " updateCapsState")
        if (!capsLock) {
            caps = mKeyboardService.prefs.mAppCorrection.autoCapitalization &&
                    activeEditorInstance.cursorCapsMode != InputAttributes.CapsMode.NONE
            launch(Dispatchers.Main) {
                keyboardViews[activeLatestInputMode]?.invalidateAllKeys()
            }
        }
    }

    fun executeSwipeAction(latestGesturesAction: LatestGesturesAction) {
        Log.d(TAGME, " executeSwipeAction")
        when (latestGesturesAction) {
            LatestGesturesAction.DELETE_WORD -> handleDeleteWord()
            LatestGesturesAction.MOVE_CURSOR_DOWN -> handleArrow(LatestSingleKeyCoding.ARROW_DOWN)
            LatestGesturesAction.MOVE_CURSOR_UP -> handleArrow(LatestSingleKeyCoding.ARROW_UP)
            LatestGesturesAction.MOVE_CURSOR_LEFT -> handleArrow(LatestSingleKeyCoding.ARROW_LEFT)
            LatestGesturesAction.MOVE_CURSOR_RIGHT -> handleArrow(LatestSingleKeyCoding.ARROW_RIGHT)
            LatestGesturesAction.SHIFT -> handleShift()
            else -> {
            }
        }
    }

    override fun onSuggestionBarBackButtonPressed() {
        Log.d(TAGME, " onSmartbarBackButtonPressed")
        setActiveKeyboardMode(LatestInputMode.CHARACTERS)
    }

    override fun onSuggestionsBarQuickActionPressed(quickActionId: Int) {
        Log.d(TAGME, " onSmartbarQuickActionPressed")
        when (quickActionId) {
            R.id.quick_action_switch_to_editing_context -> {
                if (activeLatestInputMode == LatestInputMode.EDITING) {
                    setActiveKeyboardMode(LatestInputMode.CHARACTERS)
                } else {
                    setActiveKeyboardMode(LatestInputMode.EDITING)
                }
            }
            R.id.quick_action_switch_to_media_context -> {
                mKeyboardService.switchToNextSubtype()
            } /*mKeyboardService.setActiveInput(R.id.media_input)*/
            R.id.quick_action_open_settings -> mKeyboardService.launchSettings()
            R.id.quick_action_one_handed_toggle -> mKeyboardService.toggleOneHandedMode()
            R.id.quick_action_voice_input -> {
                mKeyboardService.startGoogleVoicePopUp()
            }
        }
        latestCandidateView?.candidateSupportingBtns = true
        latestCandidateView?.updateSuggestionsBarState()
    }


    private fun handleDelete() {
        Log.d(TAGME, " handleDelete")
        isManualSelectionMode = false
        isManualSelectionModeLeft = false
        isManualSelectionModeRight = false
        activeEditorInstance.deleteBackwards()
    }

    private fun handleDeleteWord() {
        Log.d(TAGME, " handleDeleteWord")
        isManualSelectionMode = false
        isManualSelectionModeLeft = false
        isManualSelectionModeRight = false
        activeEditorInstance.deleteWordsBeforeCursor(1)
    }

    private fun handleEnter() {
        Log.d(TAGME, " handleEnter")
        if (activeEditorInstance.imeOptions.flagNoEnterAction) {
            activeEditorInstance.performEnter()
        } else {
            when (activeEditorInstance.imeOptions.action) {
                ImeOptions.Action.DONE,
                ImeOptions.Action.GO,
                ImeOptions.Action.NEXT,
                ImeOptions.Action.PREVIOUS,
                ImeOptions.Action.SEARCH,
                ImeOptions.Action.SEND -> {
                    activeEditorInstance.performEnterAction(activeEditorInstance.imeOptions.action)
                }
                else -> activeEditorInstance.performEnter()
            }
        }
    }


    private fun handleShift() {
        Log.d(TAGME, " handleShift")
        if (hasCapsRecentlyChanged) {
            osHandler.removeCallbacksAndMessages(null)
            caps = true
            capsLock = true
            hasCapsRecentlyChanged = false
        } else {
            caps = !caps
            capsLock = false
            hasCapsRecentlyChanged = true
            osHandler.postDelayed({
                hasCapsRecentlyChanged = false
            }, 300)
        }
        keyboardViews[activeLatestInputMode]?.invalidateAllKeys()
    }

    private fun handleSpace() {
        Log.d(TAGME, " handleSpace")
        if (mKeyboardService.prefs.mAppCorrection.doubleSpacePeriod) {
            if (hasSpaceRecentlyPressed) {
                osHandler.removeCallbacksAndMessages(null)
                val text = activeEditorInstance.getTextBeforeCursor(2)
                if (text.length == 2 && !text.matches("""[.!?‽\s][\s]""".toRegex())) {
                    activeEditorInstance.deleteBackwards()
                    activeEditorInstance.commitText(".")
                }
                hasSpaceRecentlyPressed = false
            } else {
                hasSpaceRecentlyPressed = true
                osHandler.postDelayed({
                    hasSpaceRecentlyPressed = false
                }, 300)
            }
        }


        activeEditorInstance.commitText(  String(Character.toChars(LatestSingleKeyCoding.SPACE)))
    }


    private fun handleArrow(code: Int) = activeEditorInstance.apply {
        Log.d(TAGME, " handleArrow")
        val selectionStartMin = 0
        val selectionEndMax = cachedText.length
        if (selection.isSelectionMode && isManualSelectionMode) {
            when (code) {
                LatestSingleKeyCoding.ARROW_DOWN -> {
                }
                LatestSingleKeyCoding.ARROW_LEFT -> {
                    if (isManualSelectionModeLeft) {
                        setSelection(
                            (selection.start - 1).coerceAtLeast(selectionStartMin),
                            selection.end
                        )
                    } else {
                        setSelection(selection.start, selection.end - 1)
                    }
                }
                LatestSingleKeyCoding.ARROW_RIGHT -> {
                    if (isManualSelectionModeRight) {
                        setSelection(
                            selection.start,
                            (selection.end + 1).coerceAtMost(selectionEndMax)
                        )
                    } else {
                        setSelection(selection.start + 1, selection.end)
                    }
                }
                LatestSingleKeyCoding.ARROW_UP -> {
                }
                LatestSingleKeyCoding.MOVE_HOME -> {
                    if (isManualSelectionModeLeft) {
                        setSelection(selectionStartMin, selection.end)
                    } else {
                        setSelection(selectionStartMin, selection.start)
                    }
                }
                LatestSingleKeyCoding.MOVE_END -> {
                    if (isManualSelectionModeRight) {
                        setSelection(selection.start, selectionEndMax)
                    } else {
                        setSelection(selection.end, selectionEndMax)
                    }
                }
            }
        } else if (selection.isSelectionMode && !isManualSelectionMode) {
            when (code) {
                LatestSingleKeyCoding.ARROW_DOWN -> {
                }
                LatestSingleKeyCoding.ARROW_LEFT -> {
                    setSelection(selection.start, selection.end - 1)
                }
                LatestSingleKeyCoding.ARROW_RIGHT -> {
                    setSelection(
                        selection.start,
                        (selection.end + 1).coerceAtMost(selectionEndMax)
                    )
                }
                LatestSingleKeyCoding.ARROW_UP -> {
                }
                LatestSingleKeyCoding.MOVE_HOME -> {
                    setSelection(selectionStartMin, selection.start)
                }
                LatestSingleKeyCoding.MOVE_END -> {
                    setSelection(selection.start, selectionEndMax)
                }
            }
        } else if (!selection.isSelectionMode && isManualSelectionMode) {
            when (code) {
                LatestSingleKeyCoding.ARROW_DOWN -> {
                }
                LatestSingleKeyCoding.ARROW_LEFT -> {
                    setSelection(
                        (selection.start - 1).coerceAtLeast(selectionStartMin),
                        selection.start
                    )
                    isManualSelectionModeLeft = true
                    isManualSelectionModeRight = false
                }
                LatestSingleKeyCoding.ARROW_RIGHT -> {
                    setSelection(
                        selection.end,
                        (selection.end + 1).coerceAtMost(selectionEndMax)
                    )
                    isManualSelectionModeLeft = false
                    isManualSelectionModeRight = true
                }
                LatestSingleKeyCoding.ARROW_UP -> {
                }
                LatestSingleKeyCoding.MOVE_HOME -> {
                    setSelection(selectionStartMin, selection.start)
                    isManualSelectionModeLeft = true
                    isManualSelectionModeRight = false
                }
                LatestSingleKeyCoding.MOVE_END -> {
                    setSelection(selection.end, selectionEndMax)
                    isManualSelectionModeLeft = false
                    isManualSelectionModeRight = true
                }
            }
        } else {
            when (code) {
                LatestSingleKeyCoding.ARROW_DOWN -> activeEditorInstance.sendSystemKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN)
                LatestSingleKeyCoding.ARROW_LEFT -> activeEditorInstance.sendSystemKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT)
                LatestSingleKeyCoding.ARROW_RIGHT -> activeEditorInstance.sendSystemKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT)
                LatestSingleKeyCoding.ARROW_UP -> activeEditorInstance.sendSystemKeyEvent(KeyEvent.KEYCODE_DPAD_UP)
                LatestSingleKeyCoding.MOVE_HOME -> activeEditorInstance.sendSystemKeyEventAlt(KeyEvent.KEYCODE_DPAD_UP)
                LatestSingleKeyCoding.MOVE_END -> activeEditorInstance.sendSystemKeyEventAlt(KeyEvent.KEYCODE_DPAD_DOWN)
            }
        }
    }


    private fun handleClipboardSelect() = activeEditorInstance.apply {
        Log.d(TAGME, " handleClipboardSelect")
        if (selection.isSelectionMode) {
            if (isManualSelectionMode && isManualSelectionModeLeft) {
                setSelection(selection.start, selection.start)
            } else {
                setSelection(selection.end, selection.end)
            }
            isManualSelectionMode = false
        } else {
            isManualSelectionMode = !isManualSelectionMode
            latestEditingCompleteView?.onUpdateSelection()
        }
    }


    private fun handleClipboardSelectAll() {
        activeEditorInstance.setSelection(0, activeEditorInstance.cachedText.length)
    }

    fun sendKeyPress(latestSingleKeyDating: LatestSingleKeyDating) {
        Log.d(TAGME, " sendKeyPress: ${latestSingleKeyDating.code}")
        when (latestSingleKeyDating.code) {
            LatestSingleKeyCoding.ARROW_DOWN,
            LatestSingleKeyCoding.ARROW_LEFT,
            LatestSingleKeyCoding.ARROW_RIGHT,
            LatestSingleKeyCoding.ARROW_UP,
            LatestSingleKeyCoding.MOVE_HOME,
            LatestSingleKeyCoding.MOVE_END -> handleArrow(latestSingleKeyDating.code)
            LatestSingleKeyCoding.CLIPBOARD_CUT -> activeEditorInstance.performClipboardCut()
            LatestSingleKeyCoding.CLIPBOARD_COPY -> activeEditorInstance.performClipboardCopy()
            LatestSingleKeyCoding.CLIPBOARD_PASTE -> {
                activeEditorInstance.performClipboardPaste()
                latestCandidateView?.resetClipboardSuggestion()
            }
            LatestSingleKeyCoding.CLIPBOARD_SELECT -> handleClipboardSelect()
            LatestSingleKeyCoding.CLIPBOARD_SELECT_ALL -> handleClipboardSelectAll()
            LatestSingleKeyCoding.DELETE -> {
                handleDelete()
                latestCandidateView?.resetClipboardSuggestion()
            }
            LatestSingleKeyCoding.ENTER -> {
                handleEnter()
                latestCandidateView?.resetClipboardSuggestion()
            }
            LatestSingleKeyCoding.LANGUAGE_SWITCH ->  mKeyboardService.switchToNextSubtype()

            LatestSingleKeyCoding.SETTINGS -> mKeyboardService.launchSettings()
            LatestSingleKeyCoding.SHIFT -> handleShift()
            LatestSingleKeyCoding.SHOW_INPUT_METHOD_PICKER -> {
                val im =
                    mKeyboardService.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.showInputMethodPicker()
            }
            LatestSingleKeyCoding.SWITCH_TO_MEDIA_CONTEXT -> mKeyboardService.setActiveInput(R.id.media_input)
            LatestSingleKeyCoding.SWITCH_TO_TEXT_CONTEXT -> mKeyboardService.setActiveInput(R.id.text_input)
            LatestSingleKeyCoding.TOGGLE_ONE_HANDED_MODE -> mKeyboardService.toggleOneHandedMode()
            LatestSingleKeyCoding.VIEW_CHARACTERS -> setActiveKeyboardMode(LatestInputMode.CHARACTERS)
            LatestSingleKeyCoding.VIEW_NUMERIC -> setActiveKeyboardMode(LatestInputMode.NUMERIC)
            LatestSingleKeyCoding.VIEW_NUMERIC_ADVANCED -> setActiveKeyboardMode(LatestInputMode.NUMERIC_ADVANCED)
            LatestSingleKeyCoding.VIEW_PHONE -> setActiveKeyboardMode(LatestInputMode.PHONE)
            LatestSingleKeyCoding.VIEW_PHONE2 -> setActiveKeyboardMode(LatestInputMode.PHONE2)
            LatestSingleKeyCoding.VIEW_SYMBOLS -> setActiveKeyboardMode(LatestInputMode.SYMBOLS)
            LatestSingleKeyCoding.VIEW_SYMBOLS2 -> setActiveKeyboardMode(LatestInputMode.SYMBOLS2)
            else -> {
                when (activeLatestInputMode) {
                    LatestInputMode.NUMERIC,
                    LatestInputMode.NUMERIC_ADVANCED,
                    LatestInputMode.PHONE,
                    LatestInputMode.PHONE2 -> when (latestSingleKeyDating.type) {
                        KeyType.CHARACTER,
                        KeyType.NUMERIC -> {
                            val text = String(Character.toChars(latestSingleKeyDating.code))
                            Log.d(TAGME, " Committing NUMERIC: $text")
                            activeEditorInstance.commitText(text)
                        }
                        else -> when (latestSingleKeyDating.code) {
                            LatestSingleKeyCoding.PHONE_PAUSE,
                            LatestSingleKeyCoding.PHONE_WAIT -> {
                                val text = String(Character.toChars(latestSingleKeyDating.code))
                                activeEditorInstance.commitText(text)
                            }
                        }
                    }
                    else -> when (latestSingleKeyDating.type) {
                        KeyType.CHARACTER -> when (latestSingleKeyDating.code) {
                            LatestSingleKeyCoding.SPACE -> handleSpace()
                            LatestSingleKeyCoding.URI_COMPONENT_TLD -> {
                                val tld = when (caps) {
                                    true -> latestSingleKeyDating.label.uppercase(Locale.getDefault())
                                    false -> latestSingleKeyDating.label.lowercase(Locale.getDefault())
                                }
                                activeEditorInstance.commitText(tld)
                            }
                            else -> {
                                /*Here check for suggestions and update smart bar*/
                                var text = String(Character.toChars(latestSingleKeyDating.code))
                                Log.d(TAGME, " Committing Text: $text")
                                text = when (caps) {
                                    true -> text.uppercase(Locale.getDefault())
                                    false -> text.lowercase(Locale.getDefault())
                                }
                                activeEditorInstance.commitText(text)
                            }
                        }
                        else -> {
                            Log.e(
                                TAGME,
                                "sendKeyPress(keyData): Received unknown key: $latestSingleKeyDating"
                            )
                        }
                    }
                }
                latestCandidateView?.resetClipboardSuggestion()
            }
        }
        if (latestSingleKeyDating.code != LatestSingleKeyCoding.SHIFT && !capsLock) {
            updateCapsState()
        }
        latestCandidateView?.updateSuggestionsBarState()
    }
}
