package com.maya.newbulgariankeyboard.main_utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class LatestCustomizeHelper(

val name: String,
val displayName: String,
val author: String,
val isNightTheme: Boolean = false,
@Json(name = "attributes")
private val rawAttrs: Map<String, Map<String, String>>
) {

    val parsedAttrs: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    companion object {


        fun saveThemingToPreferences(prefs: LatestPreferencesHelper, latestCustomizeHelper: LatestCustomizeHelper) {
            Log.d("Themeing:", " writeThemeToPrefs: ${latestCustomizeHelper.displayName}")

            prefs.mAppInternal.themeCurrentBasedOn = latestCustomizeHelper.name
            prefs.mAppInternal.themeCurrentIsNight = latestCustomizeHelper.isNightTheme
            prefs.mThemingApp.colorPrimary = latestCustomizeHelper.getAttr("window/colorPrimary", "#1971e3")
            prefs.mThemingApp.colorPrimaryDark = latestCustomizeHelper.getAttr("window/colorPrimaryDark", "#FF377EF1")
            prefs.mThemingApp.colorAccent = latestCustomizeHelper.getAttr("window/colorAccent", "#274A65")
            prefs.mThemingApp.navBarColor = latestCustomizeHelper.getAttr("window/navigationBarColor", "#E0E0E0")
            prefs.mThemingApp.navBarIsLight = (latestCustomizeHelper.getAttrOrNull("window/navigationBarLight") ?: 0) > 0
            prefs.mThemingApp.keyboardBgColor = latestCustomizeHelper.getAttr("keyboard/bgColor", "#E0E0E0")
            prefs.mThemingApp.keyBgColor = latestCustomizeHelper.getAttr("key/bgColor", "#ffffff")
            prefs.mThemingApp.keyBgColorPressed = latestCustomizeHelper.getAttr("key/bgColorPressed", "#F5F5F5")
            prefs.mThemingApp.keyFgColor = latestCustomizeHelper.getAttr("key/fgColor", "#000000")
            prefs.mThemingApp.keyEnterBgColor = latestCustomizeHelper.getAttr("keyEnter/bgColor", "#1971e3")
            prefs.mThemingApp.keyEnterBgColorPressed = latestCustomizeHelper.getAttr("keyEnter/bgColorPressed", "#FF377EF1")
            prefs.mThemingApp.keyEnterFgColor = latestCustomizeHelper.getAttr("keyEnter/fgColor", "#FFFFFF")

            prefs.mThemingApp.keyPopupBgColor = latestCustomizeHelper.getAttr("keyPopup/bgColor", "#EEEEEE")
            prefs.mThemingApp.keyPopupBgColorActive = latestCustomizeHelper.getAttr("keyPopup/bgColorActive", "#BDBDBD")
            prefs.mThemingApp.keyPopupFgColor = latestCustomizeHelper.getAttr("keyPopup/fgColor", "#000000")

            prefs.mThemingApp.keyShiftBgColor = latestCustomizeHelper.getAttr("keyShift/bgColor", "#FFFFFF")
            prefs.mThemingApp.keyShiftBgColorPressed = latestCustomizeHelper.getAttr("keyShift/bgColorPressed", "#F5F5F5")
            prefs.mThemingApp.keyShiftFgColor = latestCustomizeHelper.getAttr("keyShift/fgColor", "#000000")
            prefs.mThemingApp.keyShiftFgColorCapsLock =
                latestCustomizeHelper.getAttr("keyShift/fgColorCapsLock", "#274A65")

            prefs.mThemingApp.mediaFgColor = latestCustomizeHelper.getAttr("media/fgColor", "#000000")
            prefs.mThemingApp.mediaFgColorAlt = latestCustomizeHelper.getAttr("media/fgColorAlt", "#757575")

            prefs.mThemingApp.oneHandedBgColor = latestCustomizeHelper.getAttr("oneHanded/bgColor", "#FFFFFF")

            prefs.mThemingApp.oneHandedButtonFgColor = latestCustomizeHelper.getAttr("oneHandedButton/fgColor", "#424242")

            prefs.mThemingApp.smartbarBgColor = latestCustomizeHelper.getAttr("smartbar/bgColor", "#E0E0E0")
            prefs.mThemingApp.smartbarFgColor = latestCustomizeHelper.getAttr("smartbar/fgColor", "#000000")
            prefs.mThemingApp.smartbarFgColorAlt = latestCustomizeHelper.getAttr("smartbar/fgColorAlt", "#4A000000")

            prefs.mThemingApp.smartbarButtonBgColor = latestCustomizeHelper.getAttr("smartbarButton/bgColor", "#FFFFFF")
            prefs.mThemingApp.smartbarButtonFgColor = latestCustomizeHelper.getAttr("smartbarButton/fgColor", "#000000")
            prefs.mAppInternal.themeCurrentIsModified = false
        }

        fun fromJsonFile(context: Context, path: String): LatestCustomizeHelper? {
            val rawJsonData: String = try {
                context.assets.open(path).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                null
            } ?: return null
            return fromJsonString(
                rawJsonData
            )
        }


        fun fromJsonString(rawData: String): LatestCustomizeHelper? {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val layoutAdapter = moshi.adapter(LatestCustomizeHelper::class.java)
            return layoutAdapter.fromJson(rawData)
        }
    }

    init {

        val listOfAttrsToReevaluate = mutableListOf<Triple<String, String, String>>()
        for (group in rawAttrs) {
            val groupMap = mutableMapOf<String, Int>()
            parsedAttrs[group.key] = groupMap
            for (attr in group.value) {
                val colorRegex = """[#]([0-9a-fA-F]{8}|[0-9a-fA-F]{6})""".toRegex()
                val refRegex =
                    """[@]([a-zA-Z_][a-zA-Z0-9_]*)[/]([a-zA-Z_][a-zA-Z0-9_]*)""".toRegex()
                when {
                    attr.value.matches(colorRegex) -> {
                        groupMap[attr.key] = Color.parseColor(attr.value)
                    }
                    attr.value == "transparent" -> {
                        groupMap[attr.key] = Color.TRANSPARENT
                    }
                    attr.value == "true" -> {
                        groupMap[attr.key] = 0x1
                    }
                    attr.value == "false" -> {
                        groupMap[attr.key] = 0x0
                    }
                    attr.value.matches(refRegex) -> {
                        val attrValue = getAttrOrNull(attr.value.substring(1))
                        if (attrValue != null) {
                            groupMap[attr.key] = attrValue
                        } else {
                            listOfAttrsToReevaluate.add(Triple(group.key, attr.key, attr.value))
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("The specified attr '${attr.key}' = '${attr.value}' is not valid!")
                    }
                }
            }
        }
        for (attrToReevaluate in listOfAttrsToReevaluate) {
            val attrValue = getAttrOrNull(attrToReevaluate.third.substring(1))
            if (attrValue != null) {
                parsedAttrs[attrToReevaluate.first]?.put(attrToReevaluate.second, attrValue)
            } else {
                throw IllegalArgumentException("The attr '${attrToReevaluate.second}' = '${attrToReevaluate.third}' is not valid!")
            }
        }
    }

    fun getAttr(key: String, defaultColor: String): Int {
        return getAttrOrNull(key) ?: Color.parseColor(defaultColor)
    }

    fun getAttr(group: String, attr: String, defaultColor: String): Int {
        return getAttrOrNull(group, attr) ?: Color.parseColor(defaultColor)
    }

    fun getAttrOrNull(key: String): Int? {
        val regex = """([a-zA-Z_][a-zA-Z0-9_]*)[/]([a-zA-Z_][a-zA-Z0-9_]*)""".toRegex()
        return if (key.matches(regex)) {
            val split = key.split("/")
            getAttrOrNull(split[0], split[1])
        } else {
            null
        }
    }

    fun getAttrOrNull(group: String, attr: String): Int? {
        return parsedAttrs[group]?.get(attr)
    }
}


data class ThemeMetaOnly(
    val name: String,
    val displayName: String,
    val author: String,
    val isNightTheme: Boolean = false
) {
    companion object {

        fun loadFromJsonFile(context: Context, path: String): ThemeMetaOnly? {
            val rawJsonData: String = try {
                context.assets.open(path).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                null
            } ?: return null
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val layoutAdapter = moshi.adapter(ThemeMetaOnly::class.java)
            return layoutAdapter.fromJson(rawJsonData)
        }


        fun loadAllFromDir(context: Context, path: String): List<ThemeMetaOnly> {
            val ret = mutableListOf<ThemeMetaOnly>()
            try {
                val list = context.assets.list(path)
                if (list != null && list.isNotEmpty()) {
                    for (file in list) {
                        val subList = context.assets.list("$path/$file")
                        if (subList?.isEmpty() == true) {
                            val metaData =
                                loadFromJsonFile(
                                    context,
                                    "$path/$file"
                                )
                            if (metaData != null) {
                                ret.add(metaData)
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
            }
            return ret
        }
    }
}
