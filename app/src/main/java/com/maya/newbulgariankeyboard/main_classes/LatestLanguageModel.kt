package com.maya.newbulgariankeyboard.main_classes

import com.maya.newbulgariankeyboard.main_utils.LatestLocaleUtils
import com.squareup.moshi.Json
import java.util.InvalidPropertiesFormatException
import java.util.Locale


data class LanguageModel(
    var id: Int,
    var locale: Locale,
    var layout: String
) {
    companion object {
        //todo change
        val DEFAULT = LanguageModel(1701,
            LatestLocaleUtils.stringToLocale(
                ("bg_BG")
            ), "bulgarian_phonetic")

        fun fromString(string: String): LanguageModel {
            val data = string.split("/")
            if (data.size != 3) {
                throw InvalidPropertiesFormatException(
                    "Not having proper specs."
                )
            } else {
                val locale = LatestLocaleUtils.stringToLocale(data[1])
                return LanguageModel(
                    data[0].toInt(),
                    locale,
                    data[2]
                )
            }
        }
    }

    override fun toString(): String {
        val languageTag = locale.toLanguageTag()
        return "$id/$languageTag/$layout"
    }
}


data class DefaultSubtype(
    var id: Int,
    @Json(name = "languageTag")
    var locale: Locale,
    var preferredLayout: String
)
