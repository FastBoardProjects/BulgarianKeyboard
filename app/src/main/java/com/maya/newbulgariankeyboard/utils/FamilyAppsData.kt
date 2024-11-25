package com.maya.newbulgariankeyboard.utils

import com.maya.newbulgariankeyboard.R

object FamilyAppsData {

    val appItemList = listOf(
        AppItem(R.drawable.ic_all_lang_keyboard, "All Language Keyboard", 4.4f, "https://tinyurl.com/37wemrkm"),
        AppItem(R.drawable.ic_translator, "All Language Translator", 4.4f, "https://tinyurl.com/4ftkfp63"),
        AppItem(R.drawable.ic_qr_code, "QR Code Scanner & Barcode Scanner", 4.4f, "https://tinyurl.com/2s4h85n3"),
        AppItem(R.drawable.arabic_keyboard_icon, "Arabic Keyboard", 4.4f, "https://tinyurl.com/yhc7zfd3"),
        AppItem(R.drawable.afghan_keyboard_icon, "Afghan Keyboard", 4.4f, "https://tinyurl.com/2ckcdnrz"),
        AppItem(R.drawable.assamese_keyboard_icon, "Assamese Keyboard", 4.4f, "https://tinyurl.com/yc3yzsrc"),
        AppItem(R.drawable.bangla_keyboard_icon, "Bangla Keyboard", 4.4f, "https://tinyurl.com/4uxxpv7j"),
        AppItem(R.drawable.bulgarian_keyboard_icon, "Bulgarian Keyboard", 4.4f, "https://tinyurl.com/ym3yyvbh"),
        AppItem(R.drawable.french_keyboard_icon, "French Keyboard", 4.4f, "https://tinyurl.com/mwjtw4p7"),
        AppItem(R.drawable.german_keyboard_icon, "German Keyboard", 4.4f, "https://tinyurl.com/5ytmypfm"),
        AppItem(R.drawable.georgian_keyboard_icon, "Georgian Keyboard", 4.4f, "https://tinyurl.com/2mcdb64k"),
        AppItem(R.drawable.greek_keyboard_icon, "Greek Keyboard", 4.4f, "https://tinyurl.com/54bxph55"),
        AppItem(R.drawable.hebrew_keyboard_icon, "Hebrew Keyboard", 4.4f, "https://tinyurl.com/mryax348"),
        AppItem(R.drawable.hungarian_keyboard_icon, "Hungarian Keyboard", 4.4f, "https://tinyurl.com/yr45z5tp"),
        AppItem(R.drawable.kazakh_keyboard_icon, "Kazakh Keyboard", 4.4f, "https://tinyurl.com/42svfmyb"),
        AppItem(R.drawable.korean_keyboard_icon, "Korean Keyboard", 4.4f, "https://tinyurl.com/2k7jtwat"),
        AppItem(R.drawable.lao_keyboard_icon, "Lao Keyboard", 4.4f, "https://tinyurl.com/2jvvymwp"),
        AppItem(R.drawable.mongolian_keyboard_icon, "Mongolian Keyboard", 4.4f, "https://tinyurl.com/4dh5yrd6"),
        AppItem(R.drawable.myanmar_keyboard_icon, "Myanmar Keyboard", 4.4f, "https://tinyurl.com/2abf6jm7"),
        AppItem(R.drawable.norwegian_keyboard_icon, "Norwegian Keyboard", 4.4f, "https://tinyurl.com/3uh6vy7e"),
        AppItem(R.drawable.persian_keyboard_icon, "Persian Keyboard", 4.4f, "https://tinyurl.com/bdct96vn"),
        AppItem(R.drawable.russian_keyboard_icon, "Russian Keyboard", 4.4f, "https://tinyurl.com/5erxepn5"),
        AppItem(R.drawable.serbian_keyboard_icon, "Serbian Keyboard", 4.4f, "https://tinyurl.com/z7n2fhbr"),
        AppItem(R.drawable.spanish_keyboard_icon, "Spanish Keyboard", 4.4f, "https://tinyurl.com/mzjvv23p"),
        AppItem(R.drawable.swedish_keyboard_icon, "Swedish Keyboard", 4.4f, "https://tinyurl.com/5bh87c5t"),
        AppItem(R.drawable.thai_keyboard_icon, "Thai Keyboard", 4.4f, "https://tinyurl.com/2p8vm3xs"),
        AppItem(R.drawable.uzbek_keyboard_icon, "Uzbek Keyboard", 4.4f, "https://tinyurl.com/29nsru7n"),
        AppItem(R.drawable.vietnamese_keyboard_icon, "Vietnamese Keyboard", 4.4f, "https://tinyurl.com/3ny23zay"))



}


data class AppItem(val img:Int , val name : String, val rating : Float , val url : String)