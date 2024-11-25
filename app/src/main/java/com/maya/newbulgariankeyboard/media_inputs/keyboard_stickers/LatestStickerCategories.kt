package com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers

enum class LatestStickerCategories {

    STICKER_RECENT,
    STICKER_ONE,
    STICKER_TWO,
    STICKER_THIRD,
    STICKER_FOUR,
    STICKER_FIFTH,
    STICKER_SIXTH,
    STICKER_SEVEN,
    STICKER_EIGHT,
    STICKER_NINE,
    STICKER_TEN,
    STICKER_ELEVEN,
    STICKER_TWELVE,
    STICKER_THIRTEEN,
    STICKER_FOURTEEN,
    STICKER_FIFTEEN,
    STICKER_SIXTEEN,
    STICKER_SEVENTEEN,
    STICKER_EIGHTEEN,
    STICKER_NINETEEN,
    STICKER_TWENTY;

    override fun toString(): String {
        return super.toString().replace("_", " & ")
    }
}