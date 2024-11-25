package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.core.graphics.PaintCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.EnumMap
import java.util.Locale

private const val GROUP_IDENTIFIER = "# group: "
private const val SUBGROUP_IDENTIFIER = "# subgroup: "
private const val EOF_IDENTIFIER = "#EOF"

private const val FULLY_QUALIFIED = "fully-qualified"
private const val MINIMALLY_QUALIFIED = "minimally-qualified"
private const val UNQUALIFIED = "unqualified"

private const val LIGHT_SKIN_TONE = "1F3FB"
private const val MEDIUM_LIGHT_SKIN_TONE = "1F3FC"
private const val MEDIUM_SKIN_TONE = "1F3FD"
private const val MEDIUM_DARK_SKIN_TONE = "1F3FE"
private const val DARK_SKIN_TONE = "1F3FF"

private const val RED_HAIR = "1F9B0"
private const val CURLY_HAIR = "1F9B1"
private const val WHITE_HAIR = "1F9B2"
private const val BALD = "1F9B3"

private val NAME_JUNK_SPLIT_REGEX = "E([0-9]+)\\.([0-9]+)\\s+".toRegex()
private val CODE_POINT_REGEX = "([a-fA-F0-9]+)\\s".toRegex()


typealias EmojiLayoutDataMap = EnumMap<LatestCategories, MutableList<LatestEmoticonsKeyDetails>>


private fun listStringToListInt(list: List<String>): List<Int> {
    val ret: MutableList<Int> = mutableListOf()
    for (num in list) {
        try {
            ret.add(num.toInt(16))
        } catch (e: Exception) {
        }
    }
    return ret.toList()
}

fun parseRawEmojiSpecsFile(
    context: Context, path: String
): EmojiLayoutDataMap {
    val layouts = EmojiLayoutDataMap(LatestCategories::class.java)
    for (category in LatestCategories.values()) {
        layouts[category] = mutableListOf()
    }
    var reader: BufferedReader? = null
    try {
        reader = BufferedReader(
            InputStreamReader(context.assets.open(path))
        )
        val paint = Paint().apply {
            typeface = Typeface.DEFAULT
        }
        var ec: LatestCategories? = null
        var lastKeyLatest: LatestEmoticonsKeyDetails? = null
        var skipUntilNextGroup = true
        for (line in reader.readLines()) {
            if (line.startsWith("#")) {
                // Comment line
                if (line.startsWith(GROUP_IDENTIFIER, true)) {
                    // A new group begins
                    val rawGroupName = line.trim().substring(GROUP_IDENTIFIER.length)
                    if (rawGroupName.uppercase(Locale.ENGLISH) == "COMPONENT") {
                        skipUntilNextGroup = true
                        continue
                    } else {
                        skipUntilNextGroup = false
                        ec = LatestCategories.fromString(rawGroupName)
                    }
                } else if (line.startsWith(SUBGROUP_IDENTIFIER, true)) {
                    // A new subgroup begins
                } else if (line.startsWith(EOF_IDENTIFIER, true)) {
                    break
                }
            } else if (line.trim().isEmpty() || skipUntilNextGroup || ec == null) {
                // Empty line or skipUntilNextGroup true
                continue
            } else {
                // Assume it is a data line
                val data = line.split("#")
                if (data.size == 2) {
                    val data2 = data[0].split(";")
                    if (data2.size == 2 && data[1].contains(NAME_JUNK_SPLIT_REGEX)) {
                        val dataC = data2[0].trim()
                        val dataQ = data2[1].trim()
                        val dataN = data[1].split(NAME_JUNK_SPLIT_REGEX)[1]
                        if (dataQ.lowercase(Locale.ENGLISH) == FULLY_QUALIFIED) {
                            // Only fully-qualified emojis are accepted
                            val dataCPs = dataC.split(" ")
                            val key = LatestEmoticonsKeyDetails(
                                listStringToListInt(dataCPs),
                                dataN
                            )
                            // Check if system font can render the emoji, else skip it as it makes
                            //  no include it in the emoji keyboard as it will be the default
                            //  glyph not found box.
                            if (PaintCompat.hasGlyph(paint, key.getCodePointsAsString())) {
                                if (dataCPs.size > 1) {
                                    // Emoji COULD be an extension
                                    when (dataCPs[1]) {
                                        LIGHT_SKIN_TONE,
                                        MEDIUM_LIGHT_SKIN_TONE,
                                        MEDIUM_SKIN_TONE,
                                        MEDIUM_DARK_SKIN_TONE,
                                        DARK_SKIN_TONE,
                                        RED_HAIR,
                                        CURLY_HAIR,
                                        WHITE_HAIR,
                                        BALD -> {
                                            // Emoji is extension, add it as popup to last one
                                            lastKeyLatest?.popup?.add(key)
                                        }
                                        else -> {
                                            // Emoji is standalone
                                            layouts[ec]?.add(key)
                                            lastKeyLatest = key
                                        }
                                    }
                                } else {
                                    // Emoji is standalone
                                    layouts[ec]?.add(key)
                                    lastKeyLatest = key
                                }
                            } else {
                                lastKeyLatest = null
                            }
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        Log.e("EmojiLayoutDataMap", "parseRawEmojiSpecsFile(): $e")
    } finally {
        if (reader != null) {
            try {
                reader.close()
            } catch (e: IOException) {
                Log.e("EmojiLayoutDataMap", "parseRawEmojiSpecsFile(): $e")
            }
        }
    }
    return layouts
}
