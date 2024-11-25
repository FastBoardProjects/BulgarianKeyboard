package com.maya.newbulgariankeyboard.main_utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class LatestPathsHelper {

    /*android 11 may be internal storage*/

    companion object {

        fun saveThisImageAndGivePathAndroid11(bitmap: Bitmap, mContext: Context): String {
            var files: File? = null
            var file1: File? = null
            try {
                files = File(
                    mContext.filesDir.absolutePath + File.separator + ".Gallery Images" + File.separator
                )
                val success = files.mkdirs()
                file1 = File(
                    files,
                    "Image-" + System.currentTimeMillis() + ".jpg"
                )
                val output = FileOutputStream(file1)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
                output.close()

            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return file1!!.absolutePath
        }

        fun saveThisImageAndGivePath(bitmap: Bitmap, mContext: Context): String {
            var files: File? = null
            var file1: File? = null
            try {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    files = File(
                        Environment.getExternalStorageDirectory().absolutePath +
                                "/Android/data/com.maya.newbulgariankeyboard/files" + File.separator + ".Gallery Images" + File.separator
                    )
                    val success = files.mkdirs()
                }
                file1 = File(
                    files,
                    "Image-" + System.currentTimeMillis() + ".jpg"
                )
                val output = FileOutputStream(file1)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
                output.close()

            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return file1!!.absolutePath
        }

        fun replaceCurrentImageWithNew(bitmap: Bitmap, filePath: File): String {

            try {
                val output = FileOutputStream(filePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
                output.close()

            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return filePath.absolutePath
        }

    }
}