
package com.maya.newbulgariankeyboard.main_utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout

object LatestViewLayoutUtils {
    fun updateLayoutHeightOf(window: Window, layoutHeight: Int) {
        val params = window.attributes
        if (params != null && params.height != layoutHeight) {
            params.height = layoutHeight
            window.attributes = params
        }
    }

    fun updateLayoutHeightOf(view: View, layoutHeight: Int) {
        val params = view.layoutParams
        if (params != null && params.height != layoutHeight) {
            params.height = layoutHeight
            view.layoutParams = params
        }
    }

    fun updateLayoutGravityOf(view: View, layoutGravity: Int) {
        val lp = view.layoutParams
        if (lp is LinearLayout.LayoutParams) {
            if (lp.gravity != layoutGravity) {
                lp.gravity = layoutGravity
                view.layoutParams = lp
            }
        } else if (lp is FrameLayout.LayoutParams) {
            if (lp.gravity != layoutGravity) {
                lp.gravity = layoutGravity
                view.layoutParams = lp
            }
        } else {
            throw IllegalArgumentException(
                "Layout parameter doesn't have gravity: "
                        + lp.javaClass.name
            )
        }
    }


    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
