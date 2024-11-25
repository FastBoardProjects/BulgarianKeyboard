package com.maya.newbulgariankeyboard.main_classes

import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputBinding
import android.view.inputmethod.InputContentInfo
import androidx.annotation.RequiresApi
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.maya.newbulgariankeyboard.text_inputs.LatestInputHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyCoding
import com.maya.newbulgariankeyboard.text_inputs.keyboard_suggestions_bar.LatestCandidateView

private const val LIGHT_SKIN_TONE = 0x1F3FB
private const val MEDIUM_LIGHT_SKIN_TONE = 0x1F3FC
private const val MEDIUM_SKIN_TONE = 0x1F3FD
private const val MEDIUM_DARK_SKIN_TONE = 0x1F3FE
private const val DARK_SKIN_TONE = 0x1F3FF
private const val RED_HAIR = 0x1F9B0
private const val CURLY_HAIR = 0x1F9B1
private const val WHITE_HAIR = 0x1F9B2
private const val BALD = 0x1F9B3
private const val ZERO_WIDTH_JOINER = 0x200D
private const val VARIATION_SELECTOR = 0xFE0F

private val emojiVariationArray: Array<Int> = arrayOf(
    LIGHT_SKIN_TONE,
    MEDIUM_LIGHT_SKIN_TONE,
    MEDIUM_SKIN_TONE,
    MEDIUM_DARK_SKIN_TONE,
    DARK_SKIN_TONE,
    RED_HAIR,
    CURLY_HAIR,
    WHITE_HAIR,
    BALD
)

class LatestServiceHelper private constructor(private val ims: InputMethodService?) {

    private val TAGME = "LatestServiceHelper:"

    private val mKeyboardService: LatestKeyboardService? = LatestKeyboardService.getInstanceOrNull()

    var mMimeTypes: Array<out String?>? = null

    val cursorCapsMode: InputAttributes.CapsMode
        get() {
            val ic = ims?.currentInputConnection ?: return InputAttributes.CapsMode.NONE
            return InputAttributes.CapsMode.fromFlags(
                ic.getCursorCapsMode(inputAttributes.capsMode.toFlags())
            )
        }
    var currentWord: Region = Region(this)
        private set
    var imeOptions: ImeOptions = ImeOptions.fromImeOptionsInt(EditorInfo.IME_NULL)
        private set
    var inputAttributes: InputAttributes = InputAttributes.fromInputTypeInt(InputType.TYPE_NULL)
        private set
    var isComposingEnabled: Boolean = false
        set(v) {
            field = v
            reevaluateCurrentWord()
            if (v && !isRawInputEditor) {
                markComposingRegion(currentWord)
            } else {
                markComposingRegion(null)
            }
        }
    var isNewSelectionInBoundsOfOld: Boolean = false
        private set
    var isRawInputEditor: Boolean = true
        private set
    var packageName: String = "undefined"
        private set
    var selection: Selection = Selection(this)
        private set
    var cachedText: String = ""

    private var clipboardManager: ClipboardManager? = null

    init { /*check this if null*/
        val tmpClipboardManager = ims?.getSystemService(Context.CLIPBOARD_SERVICE)
        if (tmpClipboardManager != null && tmpClipboardManager is ClipboardManager) {
            clipboardManager = tmpClipboardManager
        }
    }

    fun registerSuggestionsBar(view: LatestCandidateView, context: Context) {
        Log.d(TAGME, " registerSmartbarView")

    }

    companion object {
        fun default(): LatestServiceHelper {
            return LatestServiceHelper(null)
        }

        fun from(editorInfo: EditorInfo?, ims: InputMethodService?): LatestServiceHelper {
            return if (editorInfo == null) {
                default()
            } else {
                LatestServiceHelper(ims).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        mMimeTypes = editorInfo.contentMimeTypes
                    }
                    imeOptions = ImeOptions.fromImeOptionsInt(editorInfo.imeOptions)
                    inputAttributes = InputAttributes.fromInputTypeInt(editorInfo.inputType)
                    packageName = editorInfo.packageName
                }
            }
        }
    }

    init {
        updateEditorState()
        reevaluateCurrentWord()
    }


    fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int
    ) {
        //Log.d(TAGME, " onUpdateSelection")
        updateEditorState()
        isNewSelectionInBoundsOfOld =
            newSelStart >= (oldSelStart - 1) &&
                    newSelStart <= (oldSelStart + 1) &&
                    newSelEnd >= (oldSelEnd - 1) &&
                    newSelEnd <= (oldSelEnd + 1)
        selection.apply {
            start = newSelStart
            end = newSelEnd
        }
        reevaluateCurrentWord()
        if (selection.isCursorMode && isComposingEnabled && !isRawInputEditor) {
            markComposingRegion(currentWord)
        } else {
            markComposingRegion(null)
        }
    }

    fun commitCompletion(text: String): Boolean {
        Log.d(TAGME, " commitCompletion")
        val ic = ims?.currentInputConnection ?: return false
        return if (isRawInputEditor) {
            false
        } else {
            ic.beginBatchEdit()
            ic.setComposingText(text, 1)
            markComposingRegion(null)
            updateEditorState()
            reevaluateCurrentWord()
            ic.endBatchEdit()
            true
        }
    }

    fun commitContent(content: Uri, description: ClipDescription): Boolean {
        Log.d(TAGME, " commitContent")
        val ic = ims?.currentInputConnection ?: return false
        val contentMimeTypes = mMimeTypes
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 || contentMimeTypes == null || contentMimeTypes.isEmpty()) {
            commitText(content.toString())
        } else {
            var mimeTypesDoMatch = false
            for (contentMimeType in contentMimeTypes) {
                if (description.hasMimeType(contentMimeType)) {
                    mimeTypesDoMatch = true
                    break
                }
            }
            if (mimeTypesDoMatch) {
                ic.beginBatchEdit()
                markComposingRegion(null)
                val ret = ic.commitContent(InputContentInfo(content, description), 0, null)
                ic.endBatchEdit()
                ret
            } else {
                commitText(content.toString())
            }
        }
    }

    fun commitGifImage(contentUri: Uri) {
        Log.d(TAGME, " commitGifImage ${contentUri}")
        val inputContentInfo = InputContentInfoCompat(
            contentUri,
            ClipDescription("", arrayOf("image/gif")),
            null
        )
        val ic = ims?.currentInputConnection
        if (ic != null) {
            val editorInfo: EditorInfo = ims!!.currentInputEditorInfo
            var flags = 0
            if (Build.VERSION.SDK_INT >= 25) {
                flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
            }
            InputConnectionCompat.commitContent(
                ic,
                editorInfo,
                inputContentInfo,
                flags,
                null
            )
        }
    }

    fun commitStickerImage(contentUri: Uri) {
        Log.d(TAGME, " commitStickerImage ${contentUri}")
        val inputContentInfo = InputContentInfoCompat(
            contentUri,
            ClipDescription("", arrayOf("image/png")),
            null
        )
        val ic = ims?.currentInputConnection
        if (ic != null) {
            val editorInfo: EditorInfo = ims!!.currentInputEditorInfo
            var flags = 0
            if (Build.VERSION.SDK_INT >= 25) {
                flags = flags or InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
            }
            InputConnectionCompat.commitContent(
                ic,
                editorInfo,
                inputContentInfo,
                flags,
                null
            )
        }
    }

    private fun isCommitContentSupported(editorInfo: EditorInfo?, mimeType: String): Boolean {
        if (editorInfo == null) {
            return false
        }
        val ic = ims?.currentInputConnection ?: return false
        if (!validatePackageName(editorInfo)) {
            return false
        }
        val supportedMimeTypes: Array<String> =
            EditorInfoCompat.getContentMimeTypes(editorInfo)
        for (supportedMimeType in supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true
            }
        }
        return false
    }

    private fun validatePackageName(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) {
            return false
        }
        val packageName = editorInfo.packageName ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true
        }
        val inputBinding: InputBinding = mKeyboardService!!.currentInputBinding
        if (inputBinding == null) {
            return false
        }
        val packageUid = inputBinding.uid
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val appOpsManager =
                mKeyboardService.context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                appOpsManager.checkPackage(packageUid, packageName)
            } catch (e: Exception) {
                return false
            }
            return true
        }
        val packageManager: PackageManager = mKeyboardService.context.packageManager
        val possiblePackageNames =
            packageManager.getPackagesForUid(packageUid)
        for (possiblePackageName in possiblePackageNames!!) {
            if (packageName == possiblePackageName) {
                return true
            }
        }
        return false
    }

    fun commitText(text: String): Boolean {
        Log.d(TAGME, " commitText ${text}")
        val ic = ims?.currentInputConnection ?: return false
        return if (isRawInputEditor) {
            ic.commitText(text, 1)
        } else {
            ic.beginBatchEdit()
            /*    if (!SmartbarView.isSuggestedItemClick) {*/
            markComposingRegion(null)
            /*} else {
                ic.finishComposingText()
            }*/
            ic.commitText(text, 1)
            updateEditorState()
            reevaluateCurrentWord()
            if (isComposingEnabled && text != LatestSingleKeyCoding.SPACE.toChar().toString()) {
                markComposingRegion(currentWord)
                Log.d(TAGME, "Yes commitText isComposingEnabled ${currentWord.text}")
                /* if (!isSuggestedText) {
                     updateForCandidatesSuggestions()
                 }*/
            } else {
                Log.d(TAGME, "No commitText isComposingEnable")
            }
            ic.endBatchEdit()
            true
        }
    }

    fun updateForCandidatesSuggestions() {
        try {
            if (currentWord.text.isNotEmpty()) {
                Log.d(
                    TAGME,
                    " updateForCandidatesSuggestions: ${LatestCandidateView.isSuggestedItemClick}"
                )
                val suggestionsTask = SuggestionWordFethcingTask()
                val subtypeLocale =
                    mKeyboardService!!.latestLocaleHelper.getActiveSubtype()!!.locale.toString()
                Log.d("SuggestionTaskLog:", "Current Locale: $subtypeLocale ")
                suggestionsTask.setSuggestionsSubtype(subtypeLocale)
                suggestionsTask.execute(currentWord.text)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    inner class SuggestionWordFethcingTask : AsyncTask<String, Void, ArrayList<String>>() {

        private val TASKTAG = "SuggestionTaskLog:"
        private var list = ArrayList<String>()
        private var subtype = "en_US"
        private var typedText = ""

        override fun onPreExecute() {
            super.onPreExecute()
            Log.d(TASKTAG, " onPreExecute :Subtype $subtype ")
        }

        fun setSuggestionsSubtype(subtype: String) {
            this.subtype = subtype
        }

        override fun doInBackground(vararg params: String?): ArrayList<String> {
            typedText = params[0]!!
            Log.d(TASKTAG, " doInBackground ${typedText} ")
            try { /*Crash if subtype table not present*/
                // mKeyboardService?.databaseManager!!.insertNewRecord("aalo","en_US")
                list = mKeyboardService?.databaseManager!!.getAllRow(typedText, subtype)
            } catch (e: Exception) {
            }
            return list
        }

        override fun onPostExecute(result: ArrayList<String>?) {
            super.onPostExecute(result)
            Log.d(TASKTAG, " onPostExecute $result ")
            if (result != null) {
                /*Add new word to list if this item is not in list*/
                if (LatestInputHelper.latestCandidateView != null) {
                    if (result.size > 0) {
                        Log.d(TASKTAG, " smartbarView not null onPostExecute: $result ")
                        if (!result.contains(typedText)) {
                            result.add(0, typedText)
                            Log.d("NewLfd:", "Typed List filled 1: ${typedText}")
                        } else {
                            Log.d("NewLfd:", "Typed List filled 2: ${typedText}")
                        }
                        LatestInputHelper.latestCandidateView!!.setSmartBarSuggestionsList(result)
                    } else {
                        val innerResult = ArrayList<String>()
                        if (!innerResult.contains(typedText)) {
                            innerResult.add(0, typedText)
                            Log.d("NewLfd:", "Typed List Unfilled 1: ${typedText}")
                        } else {
                            Log.d("NewLfd:", "Typed List Unfilled 2: ${typedText}")
                        }
                        LatestInputHelper.latestCandidateView!!.setSmartBarSuggestionsList(
                            innerResult
                        )
                    }
                } else {
                    Log.d("NewLfd:", "Typed Smartbar null: ${typedText}")
                    Log.d(TASKTAG, " smartbarView is null onPostExecute $result ")
                }
            } else {
                Log.d("NewLfd:", "Typed Null: ${typedText}")
            }
        }
    }


    fun deleteBackwards(): Boolean {
        Log.d(TAGME, " deleteBackwards")
        val ic = ims?.currentInputConnection ?: return false
        return if (isRawInputEditor) {
            sendSystemKeyEvent(KeyEvent.KEYCODE_DEL)
        } else {
            ic.beginBatchEdit()
            markComposingRegion(null)
            sendSystemKeyEvent(KeyEvent.KEYCODE_DEL)
            updateEditorState()
            reevaluateCurrentWord()
            if (isComposingEnabled) {
                markComposingRegion(currentWord)
            }
            ic.endBatchEdit()
            true
        }
    }

    fun deleteWordsBeforeCursor(n: Int): Boolean {
        Log.d(TAGME, " deleteWordsBeforeCursor")
        val ic = ims?.currentInputConnection ?: return false
        return if (n < 1 || isRawInputEditor || !selection.isValid || !selection.isCursorMode) {
            false
        } else {
            ic.beginBatchEdit()
            markComposingRegion(null)

            getWordsInString(cachedText.substring(0, selection.start)).run {
                get(size - n.coerceAtLeast(0)).range
            }.run {
                ic.setSelection(first, selection.start)
            }

            ic.commitText("", 1)

            updateEditorState()
            reevaluateCurrentWord()
            ic.endBatchEdit()
            true
        }
    }


    private fun getWordsInString(string: String): List<MatchResult> {
        Log.d(TAGME, " getWordsInString")
        val wordRegexPattern = "[\\p{L}]+".toRegex()
        return wordRegexPattern.findAll(
            string
        ).toList()
    }


    fun getTextAfterCursor(n: Int): String {
        Log.d(TAGME, " getTextAfterCursor")
        if (!selection.isValid || n < 1 || isRawInputEditor) {
            return ""
        }
        val from = selection.end.coerceIn(0, cachedText.length)
        val to = (selection.end + n).coerceIn(0, cachedText.length)
        return cachedText.substring(from, to)
    }

    fun getTextBeforeCursor(n: Int): String {
        Log.d(TAGME, " getTextBeforeCursor")
        if (!selection.isValid || n < 1 || isRawInputEditor) {
            return ""
        }
        val from = (selection.start - n).coerceIn(0, cachedText.length)
        val to = selection.start.coerceIn(0, cachedText.length)
        return cachedText.substring(from, to)
    }

    fun performClipboardCut(): Boolean {
        Log.d(TAGME, " performClipboardCut")
        return if (selection.isSelectionMode) {
            val clipData: ClipData = ClipData.newPlainText(selection.text, selection.text)
            clipboardManager?.setPrimaryClip(clipData)
            deleteBackwards()
            true
        } else {
            false
        }
    }

    fun performClipboardCopy(): Boolean {
        Log.d(TAGME, " performClipboardCopy")
        return if (selection.isSelectionMode) {
            val clipData: ClipData = ClipData.newPlainText(selection.text, selection.text)
            clipboardManager?.setPrimaryClip(clipData)
            setSelection(selection.end, selection.end)
            true
        } else {
            false
        }
    }

    fun performClipboardPaste(): Boolean {
        Log.d(TAGME, " performClipboardPaste")
        val clipData: ClipData? = clipboardManager?.primaryClip
        val item: ClipData.Item? = clipData?.getItemAt(0)
        return when {
            item?.text != null -> {
                commitText(item.text.toString())
            }
            item?.uri != null -> {
                commitContent(item.uri, clipData.description)
            }
            else -> {
                false
            }
        }
    }

    var isSuggestedText = false
    fun commitSuggestionsRvText(text: String) {
        isSuggestedText = true
        /*on maya of suggestion clear already made word and place our word*/
        val inputConnection = ims?.currentInputConnection
        inputConnection?.commitText("", 1)
        Log.d(TAGME, " commitSuggestionsRvText: $text")
        commitText(text)
        commitText(LatestSingleKeyCoding.SPACE.toChar().toString())
        isSuggestedText = false
    }

    fun performEnter(): Boolean {
        Log.d(TAGME, " performEnter")
        return if (isRawInputEditor) {
            sendSystemKeyEvent(KeyEvent.KEYCODE_ENTER)
        } else {
            commitText("\n")
        }
    }

    fun performEnterAction(action: ImeOptions.Action): Boolean {
        Log.d(TAGME, " performEnterAction")
        val ic = ims?.currentInputConnection ?: return false
        return ic.performEditorAction(action.toInt())
    }


    fun sendSystemKeyEvent(keyCode: Int): Boolean {
        Log.d(TAGME, " sendSystemKeyEvent")
        val ic = ims?.currentInputConnection ?: return false
        return ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    }


    fun sendSystemKeyEventAlt(keyCode: Int): Boolean {
        Log.d(TAGME, " sendSystemKeyEventAlt")
        val ic = ims?.currentInputConnection ?: return false
        return ic.sendKeyEvent(
            KeyEvent(
                0,
                1,
                KeyEvent.ACTION_DOWN, keyCode,
                0,
                KeyEvent.META_ALT_LEFT_ON
            )
        )
    }


    fun setSelection(from: Int, to: Int): Boolean {
        Log.d(TAGME, " setSelection")
        val ic = ims?.currentInputConnection ?: return false
        return if (isRawInputEditor) {
            selection.apply {
                start = -1
                end = -1
            }
            false
        } else {
            selection.apply {
                start = from
                end = to
            }
            ic.setSelection(from, to)
        }
    }

    fun leftAppendWordToSelection(): Boolean {
        Log.d(TAGME, " leftAppendWordToSelection")
        if (selection.start <= 0)
            return false
        val stringBeforeSelection = cachedText.substring(
            0,
            selection.start
        )
        getWordsInString(stringBeforeSelection).last().range.apply {
            setSelection(first, selection.end)
        }
        return true
    }

    fun leftPopWordFromSelection(): Boolean {
        Log.d(TAGME, " leftPopWordFromSelection")
        if (selection.start >= selection.end)
            return false
        val stringInsideSelection = cachedText.substring(
            selection.start,
            selection.end
        )
        getWordsInString(stringInsideSelection).first().range.apply {
            setSelection(selection.start + last + 1, selection.end)
        }
        return true
    }


    private fun detectLastUnicodeCharacterLengthBeforeCursor(): Int {
        if (!selection.isValid) {
            return 0
        }
        var charIndex = 0
        var charLength = 0
        var charShouldGlue = false
        val textToSearch = cachedText.substring(0, selection.start.coerceAtMost(cachedText.length))
        var i = 0
        while (i < textToSearch.length) {
            val cp = textToSearch.codePointAt(i)
            val cpLength = Character.charCount(cp)
            when {
                charShouldGlue || cp == VARIATION_SELECTOR || emojiVariationArray.contains(cp) -> {
                    charLength += cpLength
                    charShouldGlue = false
                }
                cp == ZERO_WIDTH_JOINER -> {
                    charLength += cpLength
                    charShouldGlue = true
                }
                else -> {
                    charIndex = i
                    charLength = 0
                    charShouldGlue = false
                }
            }
            i += cpLength
        }
        return textToSearch.length - charIndex
    }

    private fun markComposingRegion(region: Region?): Boolean {
        try{
            val ic = ims?.currentInputConnection ?: return false
            return when (region) {
                null -> {
                    ic.finishComposingText()
                }

                else -> if (region.isValid) {
                    Log.d(TAGME, " markComposingRegion NonSug: ${region.text}")
                    if (LatestInputHelper.latestCandidateView != null) {
                        Log.d("ShuttingQuick", " Candidate not null")
                        try {
                            LatestInputHelper.latestCandidateView!!.candidateSupportingBtns = false
                            LatestInputHelper.latestCandidateView!!.updateSuggestionsBarState()
                        } catch (e: Exception) {
                        }
                    } else {
                        Log.d("ShuttingQuick", " Candidate is null")
                    }
                    if (mKeyboardService!!.prefs.mThemingApp.isDbInstalled) {
                        updateForCandidatesSuggestions()
                    }
                    ic.setComposingRegion(region.start, region.end)
                } else {
                    Log.d(TAGME, " markComposingRegion nonValid ")
                    ic.finishComposingText()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            return false
        }
    }

    private fun reevaluateCurrentWord(regex: Regex): Boolean {
        //Log.d(TAGME, " reevaluateCurrentWord")
        var foundValidWord = false
        if (selection.isValid && selection.isCursorMode) {
            val words = cachedText.split("((?<=$regex)|(?=$regex))".toRegex())
            var pos = 0
            for (word in words) {
                if (selection.start >= pos && selection.start <= pos + word.length &&
                    word.isNotEmpty() && !word.matches(regex)
                ) {
                    currentWord.apply {
                        start = pos
                        end = pos + word.length
                    }
                    foundValidWord = true
                    break
                } else {
                    pos += word.length
                }
            }
        }
        if (!foundValidWord) {
            currentWord.apply {
                start = -1
                end = -1
            }
        }
        return foundValidWord
    }


    private fun reevaluateCurrentWord() {
        val regex = "[^\\p{L}]".toRegex()
        reevaluateCurrentWord(regex)
    }

    private fun updateEditorState() {
        try {
            Log.d(TAGME, " updateEditorState")
            val ic = ims?.currentInputConnection
            val et = ic?.getExtractedText(
                ExtractedTextRequest(), 0
            )
            val text = et?.text
            if (ic == null || et == null || text == null) {
                isRawInputEditor = true
                cachedText = ""
                selection.apply {
                    start = -1
                    end = -1
                }
            } else {
                isRawInputEditor = false
                cachedText = text.toString()
                selection.apply {
                    start = et.selectionStart.coerceAtMost(cachedText.length)
                    end = et.selectionEnd.coerceAtMost(cachedText.length)
                }
            }
            reevaluateCurrentWord()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
}

class ImeOptions private constructor(imeOptions: Int) {
    val action: Action = Action.fromInt(imeOptions)
    val flagNoEnterAction: Boolean = imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION > 0

    @RequiresApi(Build.VERSION_CODES.O)
    val flagNoPersonalizedLearning: Boolean =
        imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING > 0

    companion object {
        fun default(): ImeOptions {
            return fromImeOptionsInt(EditorInfo.IME_NULL)
        }

        fun fromImeOptionsInt(imeOptions: Int): ImeOptions {
            return ImeOptions(imeOptions)
        }
    }

    enum class Action {
        DONE,
        GO,
        NEXT,
        NONE,
        PREVIOUS,
        SEARCH,
        SEND,
        UNSPECIFIED;

        companion object {
            fun fromInt(raw: Int): Action {
                return when (raw and EditorInfo.IME_MASK_ACTION) {
                    EditorInfo.IME_ACTION_DONE -> DONE
                    EditorInfo.IME_ACTION_GO -> GO
                    EditorInfo.IME_ACTION_NEXT -> NEXT
                    EditorInfo.IME_ACTION_NONE -> NONE
                    EditorInfo.IME_ACTION_PREVIOUS -> PREVIOUS
                    EditorInfo.IME_ACTION_SEARCH -> SEARCH
                    EditorInfo.IME_ACTION_SEND -> SEND
                    EditorInfo.IME_ACTION_UNSPECIFIED -> UNSPECIFIED
                    else -> NONE
                }
            }
        }

        fun toInt(): Int {
            return when (this) {
                DONE -> EditorInfo.IME_ACTION_DONE
                GO -> EditorInfo.IME_ACTION_GO
                NEXT -> EditorInfo.IME_ACTION_NEXT
                NONE -> EditorInfo.IME_ACTION_NONE
                PREVIOUS -> EditorInfo.IME_ACTION_PREVIOUS
                SEARCH -> EditorInfo.IME_ACTION_SEARCH
                SEND -> EditorInfo.IME_ACTION_SEND
                UNSPECIFIED -> EditorInfo.IME_ACTION_UNSPECIFIED
            }
        }
    }
}
class InputAttributes private constructor(inputType: Int) {
    val type: Type
    val variation: Variation
    val capsMode: CapsMode
    var flagNumberDecimal: Boolean = false
        private set
    var flagNumberSigned: Boolean = false
        private set
    var flagTextAutoComplete: Boolean = false
        private set
    var flagTextAutoCorrect: Boolean = false
        private set
    var flagTextImeMultiLine: Boolean = false
        private set
    var flagTextMultiLine: Boolean = false
        private set
    var flagTextNoSuggestions: Boolean = false
        private set

    init {
        when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_DATETIME -> {
                type = Type.DATETIME
                variation = when (inputType and InputType.TYPE_MASK_VARIATION) {
                    InputType.TYPE_DATETIME_VARIATION_DATE -> Variation.DATE
                    InputType.TYPE_DATETIME_VARIATION_NORMAL -> Variation.NORMAL
                    InputType.TYPE_DATETIME_VARIATION_TIME -> Variation.TIME
                    else -> Variation.NORMAL
                }
                capsMode = CapsMode.NONE
            }
            InputType.TYPE_CLASS_NUMBER -> {
                type = Type.NUMBER
                variation = when (inputType and InputType.TYPE_MASK_VARIATION) {
                    InputType.TYPE_NUMBER_VARIATION_NORMAL -> Variation.NORMAL
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD -> Variation.PASSWORD
                    else -> Variation.NORMAL
                }
                capsMode = CapsMode.NONE
                flagNumberDecimal = inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL > 0
                flagNumberSigned = inputType and InputType.TYPE_NUMBER_FLAG_SIGNED > 0
            }
            InputType.TYPE_CLASS_PHONE -> {
                type = Type.PHONE
                variation = Variation.NORMAL
                capsMode = CapsMode.NONE
            }
            InputType.TYPE_CLASS_TEXT -> {
                type = Type.TEXT
                variation = when (inputType and InputType.TYPE_MASK_VARIATION) {
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> Variation.EMAIL_ADDRESS
                    InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT -> Variation.EMAIL_SUBJECT
                    InputType.TYPE_TEXT_VARIATION_FILTER -> Variation.FILTER
                    InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE -> Variation.LONG_MESSAGE
                    InputType.TYPE_TEXT_VARIATION_NORMAL -> Variation.NORMAL
                    InputType.TYPE_TEXT_VARIATION_PASSWORD -> Variation.PASSWORD
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME -> Variation.PERSON_NAME
                    InputType.TYPE_TEXT_VARIATION_PHONETIC -> Variation.PHONETIC
                    InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS -> Variation.POSTAL_ADDRESS
                    InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE -> Variation.SHORT_MESSAGE
                    InputType.TYPE_TEXT_VARIATION_URI -> Variation.URI
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> Variation.VISIBLE_PASSWORD
                    InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT -> Variation.WEB_EDIT_TEXT
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> Variation.WEB_EMAIL_ADDRESS
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> Variation.WEB_PASSWORD
                    else -> Variation.NORMAL
                }
                capsMode = CapsMode.fromFlags(inputType)
                flagTextAutoComplete = inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE > 0
                flagTextAutoCorrect = inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT > 0
                flagTextImeMultiLine = inputType and InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE > 0
                flagTextMultiLine = inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE > 0
                flagTextNoSuggestions = inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS > 0
            }
            else -> {
                type = Type.TEXT
                variation = Variation.NORMAL
                capsMode = CapsMode.NONE
            }
        }
    }

    companion object {
        fun fromInputTypeInt(inputType: Int): InputAttributes {
            return InputAttributes(inputType)
        }
    }

    enum class Type {
        DATETIME,
        NUMBER,
        PHONE,
        TEXT;
    }

    enum class Variation {
        DATE,
        EMAIL_ADDRESS,
        EMAIL_SUBJECT,
        FILTER,
        LONG_MESSAGE,
        NORMAL,
        PASSWORD,
        PERSON_NAME,
        PHONETIC,
        POSTAL_ADDRESS,
        SHORT_MESSAGE,
        TIME,
        URI,
        VISIBLE_PASSWORD,
        WEB_EDIT_TEXT,
        WEB_EMAIL_ADDRESS,
        WEB_PASSWORD;
    }

    enum class CapsMode {
        ALL,
        NONE,
        SENTENCES,
        WORDS;

        companion object {
            fun fromFlags(flags: Int): CapsMode {
                return when {
                    flags and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS > 0 -> ALL
                    flags and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES > 0 -> SENTENCES
                    flags and InputType.TYPE_TEXT_FLAG_CAP_WORDS > 0 -> WORDS
                    else -> NONE
                }
            }
        }

        fun toFlags(): Int {
            return when (this) {
                ALL -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                SENTENCES -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                WORDS -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
                else -> 0
            }
        }
    }
}


open class Region(private val editorInstance: LatestServiceHelper) {
    var start: Int = -1
    var end: Int = -1
    val isValid: Boolean
        get() = start >= 0 && end >= 0 && length >= 0
    val length: Int
        get() = end - start
    val text: String
        get() {
            val eiText = editorInstance.cachedText
            return if (!isValid || start >= eiText.length) {
                ""
            } else {
                val end = if (end >= eiText.length) {
                    eiText.length
                } else {
                    end
                }
                editorInstance.cachedText.substring(start, end)
            }
        }

    override operator fun equals(other: Any?): Boolean {
        return if (other is Region) {
            start == other.start && end == other.end
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        return result
    }
}

class Selection(private val editorInstance: LatestServiceHelper) : Region(editorInstance) {
    val isCursorMode: Boolean
        get() = length == 0 && isValid
    val isSelectionMode: Boolean
        get() = length != 0 && isValid
}
