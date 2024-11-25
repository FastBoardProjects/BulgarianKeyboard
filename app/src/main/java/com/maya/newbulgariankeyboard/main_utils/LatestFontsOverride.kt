package com.maya.newbulgariankeyboard.main_utils

import android.content.Context
import android.graphics.Typeface
import java.lang.reflect.Field


class LatestFontsOverride {

    companion object {
        fun setDefaultFont(
            context: Context,
            androidTypefaceName: String?, fontAssetName: String?
        ) {
            val regular = Typeface.createFromAsset(
                context.assets,
                fontAssetName
            )
            replaceExistingFont(androidTypefaceName, regular)
        }

        fun replaceExistingFont(
            androidTypefaceName: String?,
            newTypeface: Typeface?
        ) {
            try {
                val staticField: Field = Typeface::class.java
                    .getDeclaredField(androidTypefaceName!!)
                staticField.isAccessible = true
                staticField.set(null, newTypeface)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}