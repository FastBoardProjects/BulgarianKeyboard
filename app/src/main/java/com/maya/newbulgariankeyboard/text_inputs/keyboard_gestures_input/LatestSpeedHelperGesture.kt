
package com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input

import java.util.Locale

enum class LatestSpeedHelperGesture {
    VERY_SLOW,
    SLOW,
    NORMAL,
    FAST,
    VERY_FAST;

    companion object {
        fun fromString(string: String): LatestSpeedHelperGesture {
            return valueOf(string.uppercase(Locale.ROOT))
        }
    }

    override fun toString(): String {
        return super.toString().lowercase(Locale.ROOT)
    }
}
