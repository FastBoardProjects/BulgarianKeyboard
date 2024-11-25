
package com.maya.newbulgariankeyboard.text_inputs.keyboard_gestures_input

import java.util.Locale


enum class LatestGesturesAction {
    NO_ACTION,
    DELETE_CHARACTERS,
    DELETE_WORD,
    DELETE_WORDS_PRECISELY,
    HIDE_KEYBOARD,
    MOVE_CURSOR_UP,
    MOVE_CURSOR_DOWN,
    MOVE_CURSOR_LEFT,
    MOVE_CURSOR_RIGHT,
    SHIFT,
    SWITCH_TO_PREV_SUBTYPE,
    SWITCH_TO_NEXT_SUBTYPE;

    companion object {
        fun fromString(string: String): LatestGesturesAction {
            return valueOf(string.uppercase(Locale.ROOT))
        }
    }

    override fun toString(): String {
        return super.toString().lowercase(Locale.ROOT)
    }
}
