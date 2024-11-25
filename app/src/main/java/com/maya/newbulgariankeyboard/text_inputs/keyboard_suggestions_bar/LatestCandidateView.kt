package com.maya.newbulgariankeyboard.text_inputs.keyboard_suggestions_bar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.maya.newbulgariankeyboard.BuildConfig
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.databinding.SuggestionsBarViewBinding
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestKeyboardService
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.setBackgroundTintColor2
import com.maya.newbulgariankeyboard.main_utils.setDrawableTintColor2
import com.maya.newbulgariankeyboard.suggestions_utils.LatestSuggestionsItemAdapter
import com.maya.newbulgariankeyboard.suggestions_utils.LatestSuggestionsItemCallback
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyVariation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class LatestCandidateView : ConstraintLayout,
    LatestSuggestionsItemCallback {

    private val TAGME = "AppCandidateView:"
    private lateinit var adapterLatestSuggestions: LatestSuggestionsItemAdapter
    private val latestKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()
    private val latestPreferencesHelper: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    private var eventListener: WeakReference<EventListener?>? = null
    private val mainScope = MainScope()
    private var cachedActionStartAreaVisible: Boolean = false
    @IdRes
    private var cachedActionStartAreaId: Int? = null
    @IdRes
    private var cachedMainAreaId: Int? = null
    private var cachedActionEndAreaVisible: Boolean = false
    @IdRes
    private var cachedActionEndAreaId: Int? = null

    var candidateSupportingBtns: Boolean = false
        set(v) {
            if (v) {
                binding.quickActionToggle.setImageResource(R.drawable.ic_keyboard_arrow_left)
            } else {
                binding.quickActionToggle.setImageResource(R.drawable.ic_keyboard_arrow_right)
            }
            field = v
        }
    private var shouldSuggestClipboardContents: Boolean = false

    private lateinit var binding: SuggestionsBarViewBinding
    private var indexedActionStartArea: MutableList<Int> = mutableListOf()
    private var indexedMainArea: MutableList<Int> = mutableListOf()
    private var indexedActionEndArea: MutableList<Int> = mutableListOf()

    var listSuggestions = ArrayList<String>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("CheckLogger:", " onDetachedFromWindow")
        listSuggestions.clear()
    }

    override fun onAttachedToWindow() {
        Log.d("CheckLogger:", " onAttachedToWindow")
        if (BuildConfig.DEBUG) Log.i(TAGME, "onAttachedToWindow()")

        super.onAttachedToWindow()

        binding = SuggestionsBarViewBinding.bind(this)

        for (view in binding.actionStartArea.children) {
            indexedActionStartArea.add(view.id)
        }
        for (view in binding.mainArea.children) {
            indexedMainArea.add(view.id)
        }
        for (view in binding.actionEndArea.children) {
            indexedActionEndArea.add(view.id)
        }
        binding.recyclerViewSuggestions.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapterLatestSuggestions =
            LatestSuggestionsItemAdapter(
                context,
                listSuggestions,
                this
            )
        binding.recyclerViewSuggestions.adapter = adapterLatestSuggestions
        binding.backButton.setOnClickListener {
            eventListener?.get()?.onSuggestionBarBackButtonPressed()
        }
        binding.clipboardCursorRow.isSmartbarKeyboardView = true
        mainScope.launch(Dispatchers.Default) {
            latestKeyboardService?.let {
                val layout =
                    latestKeyboardService.latestInputHelper.layoutManager.fetchComputedLayoutAsync(
                        LatestInputMode.SMARTBAR_CLIPBOARD_CURSOR_ROW,
                        LanguageModel.DEFAULT,
                        latestPreferencesHelper
                    ).await()
                launch(Dispatchers.Main) {
                    binding.clipboardCursorRow.computedLayout = layout
                    binding.clipboardCursorRow.updateVisibility()
                }
            }
        }

        binding.clipboardSuggestion.setOnClickListener {
            Log.d(TAGME, " clipboardSuggestion maya")
            latestKeyboardService?.activeEditorInstance?.performClipboardPaste()
            shouldSuggestClipboardContents = false
            updateSuggestionsBarState()
        }

        binding.numberRow.isSmartbarKeyboardView = true
        mainScope.launch(Dispatchers.Default) {
            latestKeyboardService?.let {
                val layout = it.latestInputHelper.layoutManager.fetchComputedLayoutAsync(
                    LatestInputMode.SMARTBAR_NUMBER_ROW,
                    LanguageModel.DEFAULT,
                    latestPreferencesHelper
                ).await()
                launch(Dispatchers.Main) {
                    binding.numberRow.computedLayout = layout
                    binding.numberRow.updateVisibility()
                }
            }
        }

        for (quickAction in binding.quickActions.children) {
            if (quickAction is LatestCandidateItemView) {
                quickAction.setOnClickListener {
                    eventListener?.get()?.onSuggestionsBarQuickActionPressed(quickAction.id)
                }
            }
        }

        binding.quickActionToggle.setOnClickListener {
            candidateSupportingBtns = !candidateSupportingBtns
            updateSuggestionsBarState()
        }

        configureFeatureVisibility(
            actionStartAreaVisible = false,
            actionStartAreaId = null,
            mainAreaId = null,
            actionEndAreaVisible = false,
            actionEndAreaId = null
        )

        latestKeyboardService?.latestInputHelper?.registerSuggestionsBar(this)
        latestKeyboardService?.activeEditorInstance?.registerSuggestionsBar(this, context)
    }

    fun setSmartBarSuggestionsList(listSuggestions: ArrayList<String>) {
        Log.d(TAGME, " Setting List: $listSuggestions")
        if (listSuggestions.size > 0) {
            this.listSuggestions = listSuggestions
            adapterLatestSuggestions =
                LatestSuggestionsItemAdapter(
                    context,
                    listSuggestions,
                    this
                )
            binding.recyclerViewSuggestions.adapter = adapterLatestSuggestions
        }
    }

    companion object {
        var isSuggestedItemClick = false /*must because when item maya it also shows suggestion*/
    }

    override fun onSuggestionItemClicked(selectedTxt: String?, position: Int) {
        if (selectedTxt != null) {
            latestKeyboardService?.addCurrentWordToDb(selectedTxt)
            isSuggestedItemClick = true
            listSuggestions.clear()
            adapterLatestSuggestions =
                LatestSuggestionsItemAdapter(
                    context,
                    listSuggestions,
                    this
                )
            binding.recyclerViewSuggestions.adapter = adapterLatestSuggestions
            latestKeyboardService?.activeEditorInstance?.commitSuggestionsRvText(selectedTxt)
            updateSuggestionsBarState()
            isSuggestedItemClick = false
        }
    }

    private fun configureFeatureVisibility(
        actionStartAreaVisible: Boolean = cachedActionStartAreaVisible,
        @IdRes actionStartAreaId: Int? = cachedActionStartAreaId,
        @IdRes mainAreaId: Int? = cachedMainAreaId,
        actionEndAreaVisible: Boolean = cachedActionEndAreaVisible,
        @IdRes actionEndAreaId: Int? = cachedActionEndAreaId
    ) {
        binding.actionStartArea.visibility = when {
            actionStartAreaVisible && actionStartAreaId != null -> View.VISIBLE
            actionStartAreaVisible && actionStartAreaId == null -> View.INVISIBLE
            else -> View.GONE
        }
        if (actionStartAreaId != null) {
            binding.actionStartArea.displayedChild =
                indexedActionStartArea.indexOf(actionStartAreaId).coerceAtLeast(0)
        }
        binding.mainArea.visibility = when (mainAreaId) {
            null -> View.INVISIBLE
            else -> View.VISIBLE
        }
        if (mainAreaId != null) {
            binding.mainArea.displayedChild =
                indexedMainArea.indexOf(mainAreaId).coerceAtLeast(0)
        }
        binding.actionEndArea.visibility = when {
            actionEndAreaVisible && actionEndAreaId != null -> View.VISIBLE
            actionEndAreaVisible && actionEndAreaId == null -> View.INVISIBLE
            else -> View.GONE
        }
        if (actionEndAreaId != null) {
            binding.actionEndArea.displayedChild =
                indexedActionEndArea.indexOf(actionEndAreaId).coerceAtLeast(0)
        }
    }


    fun updateSuggestionsBarState() {
        Log.d(TAGME, " updateSuggestionsBarState")
        val isVoiceEnabled = latestPreferencesHelper.mAppVoiceTyping.isVoiceEnabled
        if (isVoiceEnabled) {
            binding.quickActionVoiceInput.visibility = View.VISIBLE
        } else {
            binding.quickActionVoiceInput.visibility = View.GONE
        }
        val isEditingPanelEnabled = latestPreferencesHelper.mAppEditingPanel.isEditingPanelEnabled
        if (isEditingPanelEnabled) {
            binding.quickActionSwitchToEditingContext.visibility = View.VISIBLE
        } else {
            binding.quickActionSwitchToEditingContext.visibility = View.GONE
        }
        binding.clipboardCursorRow.updateVisibility()
        when (latestKeyboardService) {
            null -> configureFeatureVisibility(
                actionStartAreaVisible = false,
                actionStartAreaId = null,
                mainAreaId = null,
                actionEndAreaVisible = false,
                actionEndAreaId = null
            )
            else -> configureFeatureVisibility(
                actionStartAreaVisible = when (latestKeyboardService.latestInputHelper.keyVariation) {
                    KeyVariation.PASSWORD -> false
                    else -> true
                },
                actionStartAreaId = when (latestKeyboardService.latestInputHelper.getActiveKeyboardMode()) {
                    LatestInputMode.EDITING -> R.id.back_button
                    else -> R.id.quick_action_toggle
                },
                mainAreaId = when (latestKeyboardService.latestInputHelper.keyVariation) {
                    KeyVariation.PASSWORD -> R.id.number_row
                    else -> when (candidateSupportingBtns) {
                        true -> R.id.quick_actions
                        else -> when (latestKeyboardService.latestInputHelper.getActiveKeyboardMode()) {
                            LatestInputMode.EDITING,
                            LatestInputMode.NUMERIC,
                            LatestInputMode.PHONE,
                            LatestInputMode.PHONE2 -> null
                            else -> when {
                                latestKeyboardService.activeEditorInstance.isComposingEnabled &&
                                        shouldSuggestClipboardContents
                                -> R.id.clipboard_suggestion_row
                                latestKeyboardService.activeEditorInstance.isComposingEnabled &&
                                        latestKeyboardService.activeEditorInstance.selection.isCursorMode
                                -> R.id.candidates
                                else -> R.id.clipboard_cursor_row
                            }
                        }
                    }
                },
                actionEndAreaVisible = when (latestKeyboardService.latestInputHelper.keyVariation) {
                    KeyVariation.PASSWORD -> false
                    else -> true
                },
                actionEndAreaId = null
            )
        }
    }

    fun onPrimaryClipChanged() {
        Log.d(TAGME, " onPrimaryClipChanged")
        if (latestPreferencesHelper.mSingleSuggestion.enabled && latestPreferencesHelper.mSingleSuggestion.suggestClipboardContent) {
            shouldSuggestClipboardContents = true
            val item = latestKeyboardService?.clipboardManager?.primaryClip?.getItemAt(0)
            when {
                item?.text != null -> {
                    binding.clipboardSuggestion.text = item.text
                    Log.d(TAGME, " onPrimaryClipChanged : Text ${item.text}")
                }
                item?.uri != null -> {
                    binding.clipboardSuggestion.text = "(Image) " + item.uri.toString()
                    Log.d(TAGME, " onPrimaryClipChanged : Image ${item.uri}")
                }
                else -> {
                    Log.d(TAGME, " onPrimaryClipChanged : Error: ")
                    binding.clipboardSuggestion.text =
                        item?.text ?: "(Error while retrieving clipboard data)"
                }
            }
            updateSuggestionsBarState()
        }
    }

    fun resetClipboardSuggestion() {
        if (latestPreferencesHelper.mSingleSuggestion.enabled && latestPreferencesHelper.mSingleSuggestion.suggestClipboardContent) {
            shouldSuggestClipboardContents = false
            updateSuggestionsBarState()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                // Must be this size
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                (latestKeyboardService?.latestKeyboardView?.desiredSmartbarHeight
                    ?: resources.getDimension(R.dimen.keyboard_suggestions_height)).coerceAtMost(heightSize)
            }
            else -> {
                latestKeyboardService?.latestKeyboardView?.desiredSmartbarHeight
                    ?: resources.getDimension(R.dimen.keyboard_suggestions_height)
            }
        }

        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height.roundToInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setBackgroundColor(latestPreferencesHelper.mThemingApp.smartbarBgColor)
        setBackgroundTintColor2(
            binding.clipboardSuggestion,
            latestPreferencesHelper.mThemingApp.smartbarButtonBgColor
        )
        setDrawableTintColor2(
            binding.clipboardSuggestion,
            latestPreferencesHelper.mThemingApp.smartbarButtonFgColor
        )
        binding.clipboardSuggestion.setTextColor(latestPreferencesHelper.mThemingApp.smartbarButtonFgColor)
    }

    fun setEventListener(listener: EventListener) {
        eventListener = WeakReference(listener)
    }

    interface EventListener {
        fun onSuggestionBarBackButtonPressed() {}
        fun onSuggestionsBarQuickActionPressed(@IdRes quickActionId: Int) {}
    }

}
