package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs

enum class  LatestGifCategory {

    GIF_RECENT,
    GIF_TRENDING,
    GIF_HAPPY,
    GIF_SAD,
    GIF_FOOD,
    GIF_ACTION,
    GIF_ANIMAL,
    GIF_CARTOON,
    GIF_EMOTICON,
    GIF_NATURE,
    GIF_MUSIC;

    override fun toString(): String {
        return super.toString().replace("_", " & ")
    }
}