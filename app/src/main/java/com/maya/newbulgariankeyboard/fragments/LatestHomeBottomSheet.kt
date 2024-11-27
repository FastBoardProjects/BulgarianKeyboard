package com.maya.newbulgariankeyboard.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestUtils
import com.maya.newbulgariankeyboard.monetization.LatestBillingHelper
import com.maya.newbulgariankeyboard.utils.CommonFun.loadNativeAdSmallExit

class LatestHomeBottomSheet : BottomSheetDialogFragment() {

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val dialogView = inflater.inflate(R.layout.exit_bottom_dialog, container, false)
        val shimmerLayout = dialogView.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        val adPlaceholder = dialogView.findViewById<FrameLayout>(R.id.adPlaceholder)
        val exitButton = dialogView.findViewById<Button>(R.id.exitButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        // Initially disable the exit and cancel buttons and set their countdown text
        exitButton.isEnabled = false
        exitButton.text = "Exit in 5 seconds"

        cancelButton.isEnabled = false
        cancelButton.text = "Cancel in 5 seconds"


        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                if (exitButton.isEnabled) {
//                    dismiss() // Allow dismissal when the Exit button is enabled
                    true // Indicate that the event has been handled and should propagate
                } else {
                    true // Block dismissal when the Exit button is not enabled
                }
            } else {
                false // Allow other key events to propagate
            }
        }

        // Hook into BottomSheetBehavior to disable gestures
        dialog?.setOnShowListener {
            val bottomSheet =
                dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            bottomSheetBehavior?.isDraggable = false // Disable dragging initially
        }

        // Timer for enabling the Exit button
        val exitCountDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exitButton.text = "Exit in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                exitButton.isEnabled = true
                exitButton.text = "Exit"
                bottomSheetBehavior?.isDraggable = true // Re-enable dragging after timer ends
            }
        }
        exitCountDownTimer.start()

        // Timer for enabling the Cancel button
        val cancelCountDownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                cancelButton.text = "Cancel in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                cancelButton.isEnabled = true
                cancelButton.text = "Cancel"
            }
        }
        cancelCountDownTimer.start()

        // Check network connection and load ad if eligible
        if (LatestBillingHelper(requireContext()).shouldApplyMonetization() && LatestUtils.isConnectionAvailable(
                requireContext()
            )
        ) {
            loadNativeAdSmallExit(requireContext()) { adContainer ->
                adContainer?.let {
                    shimmerLayout.visibility = View.GONE
                    adPlaceholder.addView(it)
                } ?: Log.d("Malik", "Ad failed to load or display")
            }
        }

        // Set up the Exit button action
        exitButton.setOnClickListener {
            if (exitButton.isEnabled) {
                dismiss()
                requireActivity().finish()
            }
        }

        // Set up the Cancel button action
        cancelButton.setOnClickListener {
            if (cancelButton.isEnabled) {
                dismiss()
            }
        }

        return dialogView
    }
}