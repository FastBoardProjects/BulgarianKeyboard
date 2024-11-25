
package com.maya.newbulgariankeyboard.main_utils

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.Locale


object LatestLocaleUtils {
    private val DELIMITER = """[_-]""".toRegex()

    fun stringToLocale(string: String): Locale {
        return when {
            string.contains(DELIMITER) -> {
                val lc = string.split(DELIMITER)
                Locale(lc[0], lc[1])
            }
            else -> {
                Locale(string)
            }
        }
    }

    class JsonAdapter {
        @FromJson
        fun fromJson(raw: String): Locale {
            return stringToLocale(
                raw
            )
        }
        @ToJson
        fun toJson(raw: Locale): String {
            return raw.toString()
        }
    }
}
