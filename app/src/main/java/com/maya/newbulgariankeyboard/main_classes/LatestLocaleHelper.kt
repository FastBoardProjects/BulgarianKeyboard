package com.maya.newbulgariankeyboard.main_classes

import android.content.Context
import android.util.Log
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.database.SubtypesDao
import com.maya.newbulgariankeyboard.main_utils.LatestLocaleUtils
import com.maya.newbulgariankeyboard.main_utils.LatestSubtypesFillHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class LatestLocaleHelper(
    private val context: Context,
    private val prefs: LatestPreferencesHelper
) : CoroutineScope by MainScope() {

    companion object {
        const val App_IME_CONFIG_FILE_PATH = "app_assets/subtypes_details.json"
        const val APP_LIST_SEPERATOR = ";"
    }

    private val TAG = "SubTypeLogger:"
    private lateinit var subtypesDao: SubtypesDao

    var imeConfig: LatestKeyboardService.ImeConfig =
        LatestKeyboardService.ImeConfig(context.packageName)
    var languageModels: List<LanguageModel>
        get() {
            val listRaw = prefs.mAppLocalization.subtypes
            return if (listRaw.isBlank()) {
                listOf()
            } else {
                listRaw.split(APP_LIST_SEPERATOR).map {
                    LanguageModel.fromString(it)
                }
            }
        }
        set(v) {
            prefs.mAppLocalization.subtypes = v.joinToString(APP_LIST_SEPERATOR)
        }

    init {
        subtypesDao = LatestRoomDatabase.getInstance(context).subtypesDao()
        launch(Dispatchers.IO) {
            imeConfig = loadImeMainConfig(App_IME_CONFIG_FILE_PATH)
        }
    }

    private fun loadImeMainConfig(path: String): LatestKeyboardService.ImeConfig {
        val rawJsonData: String = try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        } ?: return LatestKeyboardService.ImeConfig(context.packageName)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(LatestLocaleUtils.JsonAdapter())
            .build()
        val layoutAdapter = moshi.adapter(LatestKeyboardService.ImeConfig::class.java)
        return layoutAdapter.fromJson(rawJsonData) ?: LatestKeyboardService.ImeConfig(
            context.packageName
        )
    }

    fun addAllSubtypesToIME() {
        Log.d(TAG, " addAllSubtypesToIME")
        launch(Dispatchers.IO) {
            val subtypeList = languageModels.toMutableList()
            val list = LatestSubtypesFillHelper.getAvailableSubtypesList()
            for (model in list) {
                try {
                    val subtypeId = model.id
                    val titleText = model.locale.displayName

                    //todo change

                    if (titleText == "Bulgarian (Bulgaria)") {
                        if (subtypesDao.getSubtypeById(subtypeId) == null) {
                            Log.d("AddLocale:", "Adding Bulgarian")
                            val langModel2 =
                                LatestLanguageDbModel(
                                    model.id
                                )
                            subtypesDao.insertSingleSubtype(langModel2)
                        } else {
                            Log.d("AddLocale:", "Not adding Bulgarian")
                        }
                    }

                   /* if (titleText == "Bulgarian") {
                        if (subtypesDao.getSubtypeById(subtypeId) == null) {
                            Log.d("AddLocale:", "Adding Bulgarian")
                            val langModel2 =
                                LatestLanguageDbModel(
                                    model.id
                                )
                            subtypesDao.insertSingleSubtype(langModel2)
                        } else {
                            Log.d("AddLocale:", "Not adding Bulgarian")
                        }
                    }*/

                    if (titleText == "English (United States)") {
                        if (subtypesDao.getSubtypeById(subtypeId) == null) {
                            Log.d("AddLocale:", "Adding english")
                            val langModel1 =
                                LatestLanguageDbModel(
                                    model.id
                                )
                            subtypesDao.insertSingleSubtype(langModel1)
                        } else {
                            Log.d("AddLocale:", "Not adding english")
                        }
                    }
                    Log.d("AddLocale:", " adding: ${model.locale}")
                    subtypeList.add(model)
                } catch (e: Exception) {
                }
            }
            languageModels = subtypeList
        }
    }

    private fun addSubtype(languageModelToAdd: LanguageModel): Boolean {
        val subtypeList = languageModels.toMutableList()
        if (subtypeList.contains(languageModelToAdd)) {
            return false
        }
        subtypeList.add(languageModelToAdd)
        languageModels = subtypeList
        return true
    }

    fun getActiveSubtype(): LanguageModel? {
        for (subtype in languageModels) {
            if (subtype.id == prefs.mAppLocalization.activeSubtypeId) {
                return subtype
            }
        }
        val subtypeList = languageModels
        return if (subtypeList.isNotEmpty()) {
            prefs.mAppLocalization.activeSubtypeId = subtypeList[0].id
            subtypeList[0]
        } else {
            prefs.mAppLocalization.activeSubtypeId = LanguageModel.DEFAULT.id
            null
        }
    }

    fun switchToNextFavouriteSubtype(): LanguageModel? {
        val subtypeList = filterSubtypesFromAllSubtypes()
        val activeSubtype = getActiveSubtype() ?: return null
        var triggerNextSubtype = false
        var newActiveLanguageModel: LanguageModel? = null
        for (subtype in subtypeList) {
            if (triggerNextSubtype) {
                triggerNextSubtype = false
                newActiveLanguageModel = subtype
            } else if (subtype == activeSubtype) {
                triggerNextSubtype = true
            }
        }
        if (triggerNextSubtype) {
            newActiveLanguageModel = subtypeList.first()
        }
        prefs.mAppLocalization.activeSubtypeId = when (newActiveLanguageModel) {
            null -> LanguageModel.DEFAULT.id
            else -> newActiveLanguageModel.id
        }
        return newActiveLanguageModel
    }

    fun switchToPrevFavouriteSubtype(): LanguageModel? {
        val subtypeList = filterSubtypesFromAllSubtypes()
        val activeSubtype = getActiveSubtype() ?: return null
        var triggerNextSubtype = false
        var newActiveLanguageModel: LanguageModel? = null
        for (subtype in subtypeList.reversed()) {
            if (triggerNextSubtype) {
                triggerNextSubtype = false
                newActiveLanguageModel = subtype
            } else if (subtype == activeSubtype) {
                triggerNextSubtype = true
            }
        }
        if (triggerNextSubtype) {
            newActiveLanguageModel = subtypeList.last()
        }
        prefs.mAppLocalization.activeSubtypeId = when (newActiveLanguageModel) {
            null -> LanguageModel.DEFAULT.id
            else -> newActiveLanguageModel.id
        }
        return newActiveLanguageModel
    }

    private fun filterSubtypesFromAllSubtypes(): ArrayList<LanguageModel> {
        val subtypesDao = LatestRoomDatabase.getInstance(context).subtypesDao()
        val listFiltered = ArrayList<LanguageModel>()
        for (model in languageModels) {
            if (subtypesDao.getSubtypeById(model.id) != null) {
                listFiltered.add(model)
            }
        }
        return listFiltered
    }

    fun switchToSelectedSubtype(newActiveLanguageModel: LanguageModel) {
        prefs.mAppLocalization.activeSubtypeId = when (newActiveLanguageModel) {
            null -> LanguageModel.DEFAULT.id
            else -> newActiveLanguageModel.id
        }
    }
}