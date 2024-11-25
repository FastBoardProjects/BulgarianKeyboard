

package com.maya.newbulgariankeyboard.text_inputs.keyboard_keys

import android.annotation.SuppressLint
import com.squareup.moshi.FromJson
import java.util.Locale

enum class KeyVariation {
    ALL,
    EMAIL_ADDRESS,
    NORMAL,
    PASSWORD,
    URI;

    companion object {
        @SuppressLint("DefaultLocale")
        fun fromString(string: String): KeyVariation {
            return valueOf(string.uppercase(Locale.getDefault()))
        }
    }
}

class KeyVariationAdapter {
    @FromJson
    fun fromJson(raw: String): KeyVariation {
        return KeyVariation.fromString(raw)
    }
}
