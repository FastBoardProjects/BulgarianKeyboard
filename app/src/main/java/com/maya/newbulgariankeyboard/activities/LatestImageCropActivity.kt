package com.maya.newbulgariankeyboard.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.main_utils.LatestPathsHelper
import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel
import com.takusemba.cropme.OnCropListener
import kotlinx.android.synthetic.main.activity_image_crop.cropView
import kotlinx.android.synthetic.main.activity_image_crop.cvDone
import kotlinx.android.synthetic.main.activity_image_crop.ivBack

class LatestImageCropActivity : AppCompatActivity() {

    private var uriReceived: Uri? = null
    private val TAG = "CropLogger:"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        iniViews()

    }

    private fun iniViews() {
        cropView.isOffFrame()
        ivBack.setOnClickListener {
            onBackPressed()
        }
        cvDone.setOnClickListener { /*1080 x 673*/
            cropView.crop()
        }
        uriReceived = intent.getStringExtra("Model")!!.toUri()
        if (uriReceived != null) {
            cropView.setUri(uriReceived!!)
        } else {
            onBackPressed()
        }
        cropView.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                Log.d(TAG, "Cropped: " + bitmap)
                val savedPath =
                    LatestPathsHelper.saveThisImageAndGivePathAndroid11(bitmap, this@LatestImageCropActivity)
                val intent =
                    Intent(this@LatestImageCropActivity, LatestImageBrightnessActivity::class.java)
                intent.putExtra("Model", savedPath)
                startActivityForResult(intent, 3)
            }

            override fun onFailure(e: Exception) {
                Log.d(TAG, "Cropping Failed: " + e.localizedMessage)
                Toast.makeText(this@LatestImageCropActivity, "Failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            3 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val finalPath: String? = data.getStringExtra("Model")
                    if (finalPath != null) {
                        addModelToDbAndMoveForward(finalPath)
                    }
                }
            }
        }
    }

    private fun addModelToDbAndMoveForward(path: String?) {
        if (path != null) {
            val dao = LatestRoomDatabase.getInstance(this).galleryThemesDao()
            val model =
                LatestGalleryThemeModel()
            model.itemId = System.currentTimeMillis()
            model.itemBgImage = path
            model.itemDisplayText = "Display Text"
            model.itemKeyShapeColor = "#40ffffff" /*35 percent*/
            model.itemEnterShiftColor = "#40ffffff"
            model.itemTextColor = "#ffffff"
            dao.insertSingleGalleryTheme(model)
            val intent = Intent()
            intent.putExtra("Model", model)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

//    override fun onBackPressed() {
//        LatestUtils.backFromCrop = true
//        val intent = Intent(this, LatestHomeFragmentActivity::class.java)
//        intent.putExtra("Model", 2)
//        startActivity(intent)
//        finish()
//    }
}