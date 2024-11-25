package com.maya.newbulgariankeyboard.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.interfaces.LatestDownloadDialogAdapterCallback
import kotlinx.android.synthetic.main.layout_dialog_download_suggestion.animationView
import kotlinx.android.synthetic.main.layout_dialog_download_suggestion.cvLater
import kotlinx.android.synthetic.main.layout_dialog_download_suggestion.cvPermit
import kotlinx.android.synthetic.main.layout_dialog_download_suggestion.ivClear


class LatestDialogDownloadSuggestions : Dialog {

    private var mContext: Context
    private var callback: LatestDownloadDialogAdapterCallback

    constructor(context: Context, callback: LatestDownloadDialogAdapterCallback) : super(context) {
        this.mContext = context
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_dialog_download_suggestion)
        initViews()
    }

    private fun initViews() {
        animationView.enableMergePathsForKitKatAndAbove(true)
        ivClear.setOnClickListener {
            dismissIt()
        }
        cvLater.setOnClickListener {
            dismissIt()
        }
        cvPermit.setOnClickListener {
            callback.onItemDownloadStarted()
            dismissIt()
        }
    }

    private fun dismissIt() {
        try {
            dismiss()
        } catch (e: Exception) {
        }
    }
}