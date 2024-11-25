package com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts

import android.annotation.SuppressLint
import com.squareup.moshi.FromJson
import java.util.Locale


enum class MViewerCategories {

    //    changed names
    CHARACTERS,
    CHARACTERS_MOD,
    EXTENSION,
    NUMERIC,
    NUMERIC_ADVANCED,
    PHONE,
    PHONE2,
    SYMBOLS,
    SYMBOLS_MOD,
    SYMBOLS2,
    SYMBOLS2_MOD;

    @SuppressLint("DefaultLocale")
    override fun toString(): String {
        return super.toString().replace("_", "/").lowercase(Locale.getDefault())
    }

    companion object {
        @SuppressLint("DefaultLocale")
        fun fromString(string: String): MViewerCategories {
            return valueOf(string.replace("/", "_").uppercase(Locale.getDefault()))
        }
    }
}

class LayoutTypeAdapter {
    @FromJson
    fun fromJson(raw: String): MViewerCategories {
        return MViewerCategories.fromString(raw)
    }
}
