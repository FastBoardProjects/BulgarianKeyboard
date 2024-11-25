package com.maya.newbulgariankeyboard.text_inputs.keyboard_keys

import android.annotation.SuppressLint
import com.squareup.moshi.FromJson
import java.util.Locale

enum class KeyType {
    CHARACTER,
    MODIFIER,
    ENTER_EDITING,
    SYSTEM_GUI,
    NAVIGATION,
    FUNCTION,
    NUMERIC,
    LOCK,
    VIEW_NUMERIC_ADVANCED,
    VIEW_PHONE,
    VIEW_PHONE2,
    VIEW_SYMBOLS,
    VIEW_SYMBOLS2,
    ;

    companion object {
        @SuppressLint("DefaultLocale")
        fun fromString(string: String): KeyType {
            try {
                return valueOf(string.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                return CHARACTER
            }
        }
    }
}

class KeyTypeAdapter {
    @FromJson
    fun fromJson(raw: String): KeyType {
        try {
            return KeyType.fromString(raw)
        } catch (e: Exception) {
            return KeyType.CHARACTER
        }
    }
}
