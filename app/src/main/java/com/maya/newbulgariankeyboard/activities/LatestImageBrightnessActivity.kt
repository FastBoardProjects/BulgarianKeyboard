package com.maya.newbulgariankeyboard.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.main_utils.LatestPathsHelper
import kotlinx.android.synthetic.main.activity_image_brightness.cvDone
import kotlinx.android.synthetic.main.activity_image_brightness.im_brightness
import kotlinx.android.synthetic.main.activity_image_brightness.ivBack
import kotlinx.android.synthetic.main.activity_image_brightness.sb_value
import java.io.File


class LatestImageBrightnessActivity : AppCompatActivity() {

    private var pathReceived: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_brightness)
        initViews()

    }

    private fun initViews() {
        pathReceived = intent.getStringExtra("Model")
        ivBack.setOnClickListener {
            onBackPressed()
        }
        if (pathReceived != null) {
            Glide.with(this).load(pathReceived).into(im_brightness)
        }
        sb_value.progress = 125
        sb_value.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                val brightness = setBrightness(progress)
                Log.d("BriLogger: ", "Brightness: ${brightness}")
                im_brightness.colorFilter = brightness
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        cvDone.setOnClickListener {
            im_brightness.buildDrawingCache()
            val bitmap: Bitmap? = im_brightness.drawingCache
            if (bitmap != null) {
                val savedPath =
                    LatestPathsHelper.replaceCurrentImageWithNew(
                        bitmap,
                        File(pathReceived!!)
                    )
                val intent = Intent()
                intent.putExtra("Model", savedPath)
                setResult(Activity.RESULT_OK, intent)
                Toast.makeText(
                    this,
                    "Theme Successfully Applied",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
            }
        }
    }

    fun setBrightness(progress: Int): PorterDuffColorFilter {
        Log.d("BriLogger: ", "Progress: ${progress}")
        return if (progress >= 100) {
            val value = (progress - 100) * 255 / 100
            PorterDuffColorFilter(Color.argb(value, 255, 255, 255), PorterDuff.Mode.SRC_OVER)
        } else {
            val value = (100 - progress) * 255 / 100
            PorterDuffColorFilter(Color.argb(value, 0, 0, 0), PorterDuff.Mode.SRC_ATOP)
        }
    }
}