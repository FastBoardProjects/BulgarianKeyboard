package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis

import android.annotation.SuppressLint
import java.util.Locale

enum class LatestCategories {

    SMILEYS_RECENT,
    SMILEYS_EMOTION,
    PEOPLE_BODY,
    ANIMALS_NATURE,
    FOOD_DRINK,
    TRAVEL_PLACES,
    ACTIVITIES,
    OBJECTS,
    SYMBOLS,
    FLAGS;

    override fun toString(): String {
        return super.toString().replace("_", " & ")
    }

    companion object {
        @SuppressLint("DefaultLocale")
        fun fromString(string: String): LatestCategories {
            return valueOf(string.replace(" & ", "_").uppercase(Locale.getDefault()))
        }
    }
}
