
package com.maya.newbulgariankeyboard.text_inputs.keyboard_layouts

import com.maya.newbulgariankeyboard.text_inputs.keyboard.LatestInputMode
import com.maya.newbulgariankeyboard.text_inputs.keyboard_keys.LatestSingleKeyDating

typealias LayoutDataArrangement = List<List<LatestSingleKeyDating>>
data class MViewerData(
    val type: MViewerCategories,
    val name: String,
    val direction: String,
    val modifier: String?,
    val arrangement: LayoutDataArrangement = listOf()
) {
    private fun getComputedLayoutDataArrangement(): ComputedLayoutDataArrangement {
        val ret = mutableListOf<MutableList<LatestSingleKeyDating>>()
        for (row in arrangement) {
            val retRow = mutableListOf<LatestSingleKeyDating>()
            for (keyData in row) {
                retRow.add(keyData)
            }
            ret.add(retRow)
        }
        return ret
    }

    fun toComputedLayoutData(latestInputMode: LatestInputMode): ComputedLayoutData {
        return ComputedLayoutData(
            latestInputMode, name, direction, getComputedLayoutDataArrangement()
        )
    }
}

typealias ComputedLayoutDataArrangement = MutableList<MutableList<LatestSingleKeyDating>>
data class ComputedLayoutData(
    val mode: LatestInputMode,
    val name: String,
    val direction: String,
    val arrangement: ComputedLayoutDataArrangement = mutableListOf()
)
