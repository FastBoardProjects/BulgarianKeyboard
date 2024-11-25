
package com.maya.newbulgariankeyboard.components

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceManager
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.databinding.ThemeSelectorDialogBinding
import com.maya.newbulgariankeyboard.databinding.ThemeSelectorListItemBinding
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestCustomizeHelper
import com.maya.newbulgariankeyboard.main_utils.ThemeMetaOnly


class LatestThemeSelectorPreference : Preference,
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var dialog: AlertDialog? = null
    private val metaDataCache: MutableMap<String, ThemeMetaOnly> = mutableMapOf()
    private val prefs: LatestPreferencesHelper = LatestPreferencesHelper.getDefaultInstance(context)
    val TAG = "ThemePresetPreference:"

    @Suppress("unused")
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        layoutResource = R.layout.home_list_item_layout
        onPreferenceClickListener = OnPreferenceClickListener {
            showThemeSelectorDialog()
            true
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)
        summary = generateSummaryText()
        prefs.shared.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetached() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
        prefs.shared.unregisterOnSharedPreferenceChangeListener(this)
        super.onDetached()
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
        if (key == LatestPreferencesHelper.LatestAppInternal.THEME_CURRENT_IS_MODIFIED) {
            summary = generateSummaryText()
        }
    }


    @SuppressLint("StringFormatInvalid")
    private fun generateSummaryText(): String {
        val themeKey = prefs.mAppInternal.themeCurrentBasedOn
        val isModified = prefs.mAppInternal.themeCurrentIsModified
        var metaOnly: ThemeMetaOnly? = metaDataCache[themeKey]
        if (metaOnly == null) {
            try {
                metaOnly = ThemeMetaOnly.loadFromJsonFile(context, "app_assets/theme/$themeKey.json")
            } catch (e: Exception) {
                return context.resources.getString(R.string.settings_theme_undefined)
            }
        }
        metaOnly ?: return context.resources.getString(R.string.settings_theme_undefined)
        return if (isModified) {
            String.format(
                context.resources.getString(R.string.settings_theme_preset_summary),
                metaOnly.displayName
            )
        } else {
            metaOnly.displayName
        }
    }


    private fun showThemeSelectorDialog() {
        Log.d(TAG, " showThemeSelectorDialog")
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = ThemeSelectorDialogBinding.inflate(inflater)
        val selectedThemeView = ThemeSelectorListItemBinding.inflate(inflater)
        selectedThemeView.title.text = generateSummaryText()
        dialogView.content.addView(selectedThemeView.root, 1)
        metaDataCache.clear()
        ThemeMetaOnly.loadAllFromDir(context, "app_assets/theme").forEach { metaData ->
            metaDataCache[metaData.name] = metaData
        }
        for ((themeKey, metaData) in metaDataCache) {
            if (themeKey == prefs.mAppInternal.themeCurrentBasedOn && !prefs.mAppInternal.themeCurrentIsModified) {
                continue
            }
            val availableThemeView = ThemeSelectorListItemBinding.inflate(inflater)
            availableThemeView.title.text = metaData.displayName
            availableThemeView.root.setOnClickListener {

                Log.d(TAG, " root setOnClickListener")
                applyThemePreset(metaData.name)
                dialog?.dismiss()
            }
            dialogView.content.addView(availableThemeView.root)
        }
        AlertDialog.Builder(context).apply {
            setTitle(this@LatestThemeSelectorPreference.title)
            setCancelable(true)
            setView(dialogView.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                //
            }
            setNeutralButton("Default") { _, _ ->
                //
            }
            setNegativeButton(android.R.string.cancel, null)
            setOnDismissListener { summary = generateSummaryText() }
            create()
            dialog = show()
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
        }
    }


    private fun applyThemePreset(themeKey: String) {
        Log.d("Themeing:", " applyThemePreset ${themeKey}")
        val theme = LatestCustomizeHelper.fromJsonFile(context, "app_assets/theme/$themeKey.json") ?: return
        LatestCustomizeHelper.saveThemingToPreferences(prefs, theme)
    }
}