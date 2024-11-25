package com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyType
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyTypeAdapter
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyVariation
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.KeyVariationAdapter
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel

private typealias LTN = Pair<MViewerCategories, String>
private typealias KMS = Pair<LatestInputMode, LanguageModel>


class LatestViewerHelper(private val context: Context) : CoroutineScope by MainScope() {
    var sharedPref: SharedPreferences? = null


    private val computedLayoutCache: HashMap<KMS, Deferred<ComputedLayoutData>> = hashMapOf()


//    val fontIndex = DataStoreManager.getValue(context, intPreferencesKey("font"))

    private fun loadLayout(ltn: LTN?) = loadLayout(ltn?.first, ltn?.second)
    private fun loadLayout(type: MViewerCategories?, name: String?): MViewerData? {
        val x = try {
            sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


            sharedPref!!.getInt(context.getString(R.string.fonts), 0)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }


//        Log.d(TAG, "loadLayout: $fontIndex")

        if (type == null || name == null) {
            return null
        }
        try {
            val rawJsonData: String =  context.assets.open(
                "app_assets/text/$type/${
                    name.replace(
                        "qwerty",
                        if (x == 0) "qwerty" else "qwerty$x"
                    )
                }.json"
            )
                .bufferedReader().use { it.readText() }

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).add(LayoutTypeAdapter())
                .add(KeyTypeAdapter()).add(KeyVariationAdapter()).build()
            val layoutAdapter = moshi.adapter(MViewerData::class.java)
//        if (rawJsonData.contains("qwerty"))
//            Log.d("Malik", "loadExtendedPopupsInternal: ${rawJsonData.toString()}")
            return layoutAdapter.fromJson(rawJsonData)
        } catch (e: Exception) {
            null
        } ?: return null
    }

    private fun loadExtendedPopups(languageModel: LanguageModel): Map<String, List<LatestSingleKeyDating>> {
        Log.d("SYSSOL", "loadExtendedPopups: ")
        val lang = languageModel.locale.language
        val map =
            loadExtendedPopupsInternal("app_assets/text/characters/extended_popups/$lang.json")
        return map ?: mapOf()
    }

    private fun loadExtendedPopupsInternal(path: String): Map<String, List<LatestSingleKeyDating>>? {
        Log.d("SYSSOL", "loadExtendedPopupsInternal: ")
        val rawJsonData: String = try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        } ?: return null
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).add(KeyTypeAdapter()).build()
        val mapAdaptor: JsonAdapter<Map<String, List<LatestSingleKeyDating>>> = moshi.adapter(
            Types.newParameterizedType(
                Map::class.java, String::class.java, Types.newParameterizedType(
                    List::class.java, LatestSingleKeyDating::class.java
                )
            )
        )

//        Log.d("MujahidRasool", "loadExtendedPopupsInternal: ${rawJsonData.toString()}")
        return mapAdaptor.fromJson(rawJsonData)
    }


    private suspend fun mergeLayoutsAsync(
        latestInputMode: LatestInputMode,
        languageModel: LanguageModel,
        main: LTN? = null,
        modifier: LTN? = null,
        extension: LTN? = null,
        prefs: LatestPreferencesHelper
    ): ComputedLayoutData {
        Log.d("SYSSOL", "mergeLayoutsAsync: ")
        var mainLayout: MViewerData? = null
        val computedArrangement: ComputedLayoutDataArrangement = mutableListOf()
        try {
            mainLayout = loadLayout(main)
        } catch (e: Exception) {
        }
        val modifierToLoad = if (mainLayout != null && mainLayout.modifier != null) {
            LTN(MViewerCategories.CHARACTERS_MOD, mainLayout.modifier!!)
        } else {
            modifier
        }
        val modifierLayout = loadLayout(modifierToLoad)
        val extensionLayout = loadLayout(extension)
        if (extensionLayout != null) {
            val row = extensionLayout.arrangement.firstOrNull()
            if (row != null) {
                computedArrangement.add(row.toMutableList())
            }
        }

        if (mainLayout != null && modifierLayout != null) {
            for (mainRowI in mainLayout.arrangement.indices) {
                val mainRow = mainLayout.arrangement[mainRowI]
                if (mainRowI + 1 < mainLayout.arrangement.size) {
                    computedArrangement.add(mainRow.toMutableList())
                } else {
                    // merge main and mod here
                    val mergedRow = mutableListOf<LatestSingleKeyDating>()
                    val firstModRow = modifierLayout.arrangement.firstOrNull()
                    for (modKey in (firstModRow ?: listOf())) {
                        if (modKey.code == 0) {
                            mergedRow.addAll(mainRow)
                        } else {
                            mergedRow.add(modKey)
                        }
                    }
                    computedArrangement.add(mergedRow)
                }
            }
            for (modRowI in 1 until modifierLayout.arrangement.size) {
                val modRow = modifierLayout.arrangement[modRowI]
                computedArrangement.add(modRow.toMutableList())
            }
        } else if (mainLayout != null && modifierLayout == null) {
            for (mainRow in mainLayout.arrangement) {
                computedArrangement.add(mainRow.toMutableList())
            }
        } else if (mainLayout == null && modifierLayout != null) {
            for (modRow in modifierLayout.arrangement) {
                computedArrangement.add(modRow.toMutableList())
            }
        }

        // TODO: rewrite this part
        if (latestInputMode == LatestInputMode.CHARACTERS) {
            val extendedPopups = loadExtendedPopups(languageModel)
            for (computedRow in computedArrangement) {
                for (keyData in computedRow) {
                    if (keyData.variation != KeyVariation.ALL) {
                        if (keyData.label == "." && modifierLayout?.name != "dvorak" || keyData.label == "z" && modifierLayout?.name == "dvorak") {
                            val label = "." // keyData.label
                            if (keyData.variation == KeyVariation.NORMAL || keyData.variation == KeyVariation.PASSWORD) {
                                if (extendedPopups.containsKey("$label~normal")) {
                                    keyData.popup.addAll(
                                        extendedPopups["$label~normal"] ?: listOf()
                                    )
                                }
                            }
                            if (keyData.variation == KeyVariation.EMAIL_ADDRESS || keyData.variation == KeyVariation.URI) {
                                if (extendedPopups.containsKey("$label~uri")) {
                                    keyData.popup.addAll(extendedPopups["$label~uri"] ?: listOf())
                                }
                            }
                        }
                    } else if (extendedPopups.containsKey(keyData.label)) {
                        keyData.popup.addAll(extendedPopups[keyData.label] ?: listOf())
                    }
                }
            }
        }

        if (latestInputMode == LatestInputMode.CHARACTERS) {
            val symbolsComputedArrangement = fetchComputedLayoutAsync(
                LatestInputMode.SYMBOLS, languageModel, prefs
            ).await().arrangement
            val minRow = if (prefs.mAppKeyboard.numberRow) {
                1
            } else {
                0
            }
            for ((r, row) in computedArrangement.withIndex()) {
                if (r >= (3 + minRow) || r < minRow) {
                    continue
                }
                var kOffset = 0
                val symbolRow = symbolsComputedArrangement.getOrNull(r - minRow)
                if (symbolRow != null) {
                    for ((k, key) in row.withIndex()) {
                        val lastKey = row.getOrNull(k - 1)
                        if (key.variation != KeyVariation.ALL && lastKey != null && lastKey.variation != KeyVariation.ALL) {
                            kOffset++
                        }
                        val symbol = symbolRow.getOrNull(k - kOffset)
                        if (key.type == KeyType.CHARACTER && symbol?.type == KeyType.CHARACTER) {
                            if (r == minRow) {
                                key.hintedNumber = symbol
                            } else if (r > minRow) {
                                key.hintedSymbol = symbol
                            }
                        }
                    }
                }
            }
        }

        return ComputedLayoutData(
            latestInputMode, "computed", mainLayout?.direction ?: "ltr", computedArrangement
        )
    }


    private suspend fun computeLayoutFor(
        latestInputMode: LatestInputMode,
        languageModel: LanguageModel,
        prefs: LatestPreferencesHelper
    ): ComputedLayoutData {
        Log.d("SYSSOL", "computeLayoutFor: ")
        var main: LTN? = null
        var modifier: LTN? = null
        var extension: LTN? = null

        when (latestInputMode) {
            LatestInputMode.CHARACTERS -> {
                try {
                    if (prefs.mAppKeyboard.numberRow) {
                        extension = LTN(MViewerCategories.EXTENSION, "number_row")
                    }
                    main = LTN(MViewerCategories.CHARACTERS, languageModel.layout)
                    modifier = LTN(MViewerCategories.CHARACTERS_MOD, "default")
                } catch (_: Exception) {
                }
            }

            LatestInputMode.EDITING -> {
            }

            LatestInputMode.NUMERIC -> {
                try {
                    main = LTN(MViewerCategories.NUMERIC, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.NUMERIC_ADVANCED -> {
                try {
                    main = LTN(MViewerCategories.NUMERIC_ADVANCED, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.PHONE -> {
                try {
                    main = LTN(MViewerCategories.PHONE, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.PHONE2 -> {
                try {
                    main = LTN(MViewerCategories.PHONE2, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.SYMBOLS -> {
                try {
                    extension = LTN(MViewerCategories.EXTENSION, "number_row")
                    main = LTN(MViewerCategories.SYMBOLS, "western_default")
                    modifier = LTN(MViewerCategories.SYMBOLS_MOD, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.SYMBOLS2 -> {
                try {
                    main = LTN(MViewerCategories.SYMBOLS2, "western_default")
                    modifier = LTN(MViewerCategories.SYMBOLS2_MOD, "default")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.SMARTBAR_CLIPBOARD_CURSOR_ROW -> {
                try {
                    extension = LTN(MViewerCategories.EXTENSION, "clipboard_cursor_row")
                } catch (e: Exception) {
                }
            }

            LatestInputMode.SMARTBAR_NUMBER_ROW -> {
                try {
                    extension = LTN(MViewerCategories.EXTENSION, "number_row")
                } catch (e: Exception) {
                }
            }
        }
        return mergeLayoutsAsync(latestInputMode, languageModel, main, modifier, extension, prefs)
    }


    fun clearLayoutCache(latestInputMode: LatestInputMode? = null) {
        try {
            if (latestInputMode == null) {
                computedLayoutCache.clear()
            } else {
                val it = computedLayoutCache.iterator()
                while (it.hasNext()) {
                    val kms = it.next().key
                    if (kms.first == latestInputMode) {
                        it.remove()
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    @Synchronized
    fun fetchComputedLayoutAsync(
        latestInputMode: LatestInputMode,
        languageModel: LanguageModel,
        prefs: LatestPreferencesHelper
    ): Deferred<ComputedLayoutData> {
        Log.d("SYSSOL", "fetchComputedLayoutAsync: ")
        val kms = KMS(latestInputMode, languageModel)
        val cachedComputedLayout = computedLayoutCache[kms]
        return if (cachedComputedLayout != null) {
            cachedComputedLayout
        } else {
            val computedLayout = async(Dispatchers.IO) {
                computeLayoutFor(latestInputMode, languageModel, prefs)
            }
            computedLayoutCache[kms] = computedLayout
            computedLayout
        }
    }

    @Synchronized
    fun preloadComputedLayout(
        latestInputMode: LatestInputMode,
        languageModel: LanguageModel,
        prefs: LatestPreferencesHelper
    ) {

        Log.d("SYSSOL", "preloadComputedLayout: ")
        val kms = KMS(latestInputMode, languageModel)
        if (computedLayoutCache[kms] == null) {
            computedLayoutCache[kms] = async(Dispatchers.IO) {
                computeLayoutFor(latestInputMode, languageModel, prefs)
            }
        }
    }

    fun onDestroy() {
        cancel()
    }
}
