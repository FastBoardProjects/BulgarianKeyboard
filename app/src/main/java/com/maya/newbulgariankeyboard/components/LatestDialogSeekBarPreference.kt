
package com.maya.newbulgariankeyboard.components

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceManager
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.databinding.SeekBarDialogBinding

class LatestDialogSeekBarPreference : Preference {

    private var defaultValue: Int = 0
    private var systemDefaultValue: Int = -1
    private var systemDefaultValueText: String? = null
    private var min: Int = 0
    private var max: Int = 100
    private var step: Int = 1
    private var unit: String = ""

    @Suppress("unused")
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        layoutResource = R.layout.home_list_item_layout
        context.obtainStyledAttributes(attrs, R.styleable.DialogSeekBarPreferenceAttrs).apply {
            min = getInt(R.styleable.DialogSeekBarPreferenceAttrs_min, min)
            max = getInt(R.styleable.DialogSeekBarPreferenceAttrs_max, max)
            step = getInt(R.styleable.DialogSeekBarPreferenceAttrs_seekBarIncrement, step)
            if (step < 1) {
                step = 1
            }
            defaultValue =
                getInt(R.styleable.DialogSeekBarPreferenceAttrs_android_defaultValue, defaultValue)
            systemDefaultValue =
                getInt(R.styleable.DialogSeekBarPreferenceAttrs_systemDefaultValue, min - 1)
            systemDefaultValueText =
                getString(R.styleable.DialogSeekBarPreferenceAttrs_systemDefaultValueText)
            unit = getString(R.styleable.DialogSeekBarPreferenceAttrs_unit) ?: unit
            recycle()
        }
        onPreferenceChangeListener = OnPreferenceChangeListener { _, newValue ->
            summary = getTextForValue(newValue.toString())
            true
        }
        onPreferenceClickListener = OnPreferenceClickListener {
            showSeekBarDialog()
            true
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)
        summary = getTextForValue(sharedPreferences.getInt(key, defaultValue))
    }


    private fun getTextForValue(value: Any): String {
        if (value !is Int) {
            return "??$unit"
        }
        val systemDefValText = systemDefaultValueText
        return if (value == systemDefaultValue && systemDefValText != null) {
            systemDefValText
        } else {
            value.toString() + unit
        }
    }

    /**
     * Shows the seek bar dialog.
     */
    private fun showSeekBarDialog() {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = SeekBarDialogBinding.inflate(inflater)
        val initValue = sharedPreferences.getInt(key, defaultValue)
        dialogView.seekBar.max = actualValueToSeekBarProgress(max)
        dialogView.seekBar.progress = actualValueToSeekBarProgress(initValue)
        dialogView.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                dialogView.seekBarValue.text =
                    getTextForValue(seekBarProgressToActualValue(progress))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        dialogView.seekBarValue.text = getTextForValue(initValue)
        AlertDialog.Builder(context).apply {
            setTitle(this@LatestDialogSeekBarPreference.title)
            setCancelable(true)
            setView(dialogView.root)
            setPositiveButton("Done") { _, _ ->
                val actualValue = seekBarProgressToActualValue(dialogView.seekBar.progress)
                sharedPreferences.edit().putInt(key, actualValue).apply()
            }
            setNeutralButton("Default") { _, _ ->
                sharedPreferences.edit().putInt(key, defaultValue).apply()
            }
            setNegativeButton("Dismiss", null)
            setOnDismissListener {
                summary = getTextForValue(sharedPreferences.getInt(key, defaultValue))
            }
            create()
            show()
        }
    }


    private fun actualValueToSeekBarProgress(actual: Int): Int {
        return (actual - min) / step
    }


    private fun seekBarProgressToActualValue(progress: Int): Int {
        return (progress * step) + min
    }
}