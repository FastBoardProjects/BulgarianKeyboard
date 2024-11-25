package com.maya.newbulgariankeyboard.text_inputs.keyboard_keys


data class LatestSingleKeyDating(

    var code: Int,
    var label: String = "",
    var hintedNumber: LatestSingleKeyDating? = null,
    var hintedSymbol: LatestSingleKeyDating? = null,
    var popup: MutableList<LatestSingleKeyDating> = mutableListOf(),
    var type: KeyType = KeyType.CHARACTER,
    var variation: KeyVariation = KeyVariation.ALL
)
