
package com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input

import java.util.Locale


enum class LatestGestureDistanceValues {
    VERY_SHORT,
    SHORT,
    NORMAL,
    LONG,
    VERY_LONG;

    companion object {
        fun fromString(string: String): LatestGestureDistanceValues {
            return valueOf(string.uppercase(Locale.ROOT))
        }
    }

    override fun toString(): String {
        return super.toString().lowercase(Locale.ROOT)
    }
}
