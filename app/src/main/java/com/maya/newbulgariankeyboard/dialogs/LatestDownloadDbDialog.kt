package com.maya.newbulgariankeyboard.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.interfaces.LatestDownloadingDbCallback
import kotlinx.android.synthetic.main.layout_dialog_db_downloading.cvDismiss
import kotlinx.android.synthetic.main.layout_dialog_db_downloading.seekBar
import kotlinx.android.synthetic.main.layout_dialog_db_downloading.tvName

class LatestDownloadDbDialog : Dialog {

    private var mContext: Context
    private var callBack: LatestDownloadingDbCallback

    constructor(context: Context, callBack: LatestDownloadingDbCallback) : super(
        context
    ) {
        this.mContext = context
        this.callBack = callBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_dialog_db_downloading)
        initViews()
    }

    private fun initViews() {
        seekBar.isIndeterminate = true
        cvDismiss.setOnClickListener {
            callBack.onCancelled()
            dismissIt()
        }
    }

    fun setProgressToViews(progres: Int) {
        try {
            //seekBar.progress = progres
            //tvProgress.text = " $progres %"
        } catch (e: Exception) {
        }
    }

    fun setName(name: String) {
        tvName.text = name
    }

    fun dismissIt() {
        try {
            dismiss()
        } catch (e: Exception) {
        }
    }
}