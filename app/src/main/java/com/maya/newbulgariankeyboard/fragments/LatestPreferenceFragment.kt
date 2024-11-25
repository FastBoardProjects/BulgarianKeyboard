package com.maya.newbulgariankeyboard.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.preference.PreferenceFragmentCompat
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.components.LatestDialogSeekBarPreference
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper

class LatestPreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var heightFactorCustom: LatestDialogSeekBarPreference? = null
    private var sharedPrefs: SharedPreferences? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_preferences)
        initViews()
    }

    private fun initViews() {
        heightFactorCustom = findPreference(LatestPreferencesHelper.AppKeyboard.HEIGHT_FACTOR_CUSTOM)
        onSharedPreferenceChanged(null, LatestPreferencesHelper.AppKeyboard.HEIGHT_FACTOR)
    }

    override fun onResume() {
        super.onResume()
        sharedPrefs?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPrefs?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("MyLogger:", " onSharedPreferenceChanged: ${key}")
        if (key == LatestPreferencesHelper.AppKeyboard.HEIGHT_FACTOR) {
            //heightFactorCustom?.isVisible = sharedPrefs?.getString(key, "") == "custom"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bgColor = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.colorOnBackground, bgColor, true)
        val color = bgColor.data

        val v = super.onCreateView(inflater, container, savedInstanceState) as LinearLayout?
        if (v != null) {
            v.setBackgroundColor(color)
            val context = requireContext()
        }
        return v
    }
}