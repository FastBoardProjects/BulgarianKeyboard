package com.maya.newbulgariankeyboard.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.preference.PreferenceFragmentCompat
import com.maya.newbulgariankeyboard.R

class LatestVoiceTypingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_voice)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState) as LinearLayout?
        if (v != null) {
            val context = requireContext()

            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(R.attr.colorOnBackground, typedValue, true)
            val topColor = typedValue.data
            v.setBackgroundColor(topColor)
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isFocusableInTouchMode = true
        view.requestFocus()
    }
}