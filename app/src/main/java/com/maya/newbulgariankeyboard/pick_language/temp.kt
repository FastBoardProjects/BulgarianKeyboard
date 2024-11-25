package com.maya.newbulgariankeyboard.pick_language

import com.maya.newbulgariankeyboard.R

val PossibleAppTranslationLanguage = listOf(
    AllLanguages("English", "en", R.drawable.language_en_svg),
    AllLanguages("Afrikaans", "af", R.drawable.language_af_svg),
    AllLanguages("Arabic", "ar", R.drawable.language_ar_svg),
    AllLanguages("Bengali", "bn", R.drawable.language_bn_svg),
    AllLanguages("Bulgarian", "bg", R.drawable.language_bg_svg),
    AllLanguages("Burmese", "my", R.drawable.language_my_svg),
    AllLanguages("Dutch", "nl", R.drawable.language_nl_svg),
    AllLanguages("French", "fr", R.drawable.language_fr_svg),
    AllLanguages("Georgian", "ka", R.drawable.language_ka_svg),
    AllLanguages("German", "de", R.drawable.language_de_svg),
    AllLanguages("Greek", "el", R.drawable.language_el_svg),
    AllLanguages("Hebrew", "iw", R.drawable.language_he_svg),
    AllLanguages("Hindi", "hi", R.drawable.language_hi_svg),
    AllLanguages("Hungarian", "hu", R.drawable.language_hu_svg),
    AllLanguages("Indonesian", "in", R.drawable.language_id_svg),
    AllLanguages("Italian", "it", R.drawable.language_it_svg),
    AllLanguages("Japanese", "ja", R.drawable.language_ja_svg),
    AllLanguages("Kazakh", "kk", R.drawable.language_kk_svg),
    AllLanguages("Korean", "ko", R.drawable.language_ko_svg),
    AllLanguages("Malay", "ms", R.drawable.language_ms_svg),
    AllLanguages("Mongolian", "mn", R.drawable.language_mn_svg),
    AllLanguages("Nepali", "ne", R.drawable.language_ne_svg),
    AllLanguages("Pashto", "ps", R.drawable.language_ps_svg),
    AllLanguages("Persian", "fa", R.drawable.language_fa_svg),
    AllLanguages("Portuguese", "pt", R.drawable.language_pt_svg),
    AllLanguages("Punjabi", "pa", R.drawable.language_pa_svg),
    AllLanguages("Russian", "ru", R.drawable.language_ru_svg),
    AllLanguages("Serbian", "sr", R.drawable.language_sr_svg),
    AllLanguages("Spanish", "es", R.drawable.language_es_svg),
    AllLanguages("Thai", "th", R.drawable.language_th_svg),
    AllLanguages("Turkish", "tr", R.drawable.language_tr_svg),
    AllLanguages("Urdu", "ur", R.drawable.language_ur_svg),
    AllLanguages("Uzbek", "uz", R.drawable.language_uz_svg),
    AllLanguages("Vietnamese", "vi", R.drawable.language_vi_svg)
)


data class AllLanguages(val name: String, val code: String, val img: Int)
