package com.maya.newbulgariankeyboard.main_utils

import android.util.Log
import com.maya.newbulgariankeyboard.main_classes.LanguageModel

class LatestSubtypesFillHelper {
    companion object {
        fun getAvailableSubtypesList(): ArrayList<LanguageModel> {
            val list = ArrayList<LanguageModel>()

            list.add(
                LanguageModel(
                    101,
                    LatestLocaleUtils.stringToLocale(
                        ("en_US")
                    ), "qwerty"
                )
            )
            //todo change
            list.add(
                LanguageModel(1701,
                    LatestLocaleUtils.stringToLocale(
                        ("bg_BG")
                    ), "bulgarian_phonetic")
            )

            /*list.add(
                LanguageModel(1702,
                    LatestLocaleUtils.stringToLocale(
                        ("bg")
                    ), "bulgarian_bds")
            )*/

            Log.d("sdfsdFSDFsDZDsdfsdf", "getAvailableSubtypesList: $list")
            return list
        }
    }
}