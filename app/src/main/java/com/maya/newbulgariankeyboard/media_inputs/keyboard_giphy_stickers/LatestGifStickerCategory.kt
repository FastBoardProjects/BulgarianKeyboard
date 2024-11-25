package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers

enum class  LatestGifStickerCategory {
    STICKER_RECENT,
    STICKER_TRENDING,
    STICKER_HAPPY,
    STICKER_SAD,
    STICKER_FOOD,
    STICKER_ACTION,
    STICKER_ANIMAL,
    STICKER_CARTOON,
    STICKER_EMOTICON,
    STICKER_NATURE,
    STICKER_MUSIC;

    override fun toString(): String {
        return super.toString().replace("_", " & ")
    }
}