
package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis


data class LatestEmoticonsKeyDetails(
    var codePoints: List<Int>,
    var label: String = "",
    var popup: MutableList<LatestEmoticonsKeyDetails> = mutableListOf()
) {

  
    fun getCodePointsAsString(): String {
        var ret = ""
        for (codePoint in codePoints) {
            ret += String(Character.toChars(codePoint))
        }
        return ret
    }
}
