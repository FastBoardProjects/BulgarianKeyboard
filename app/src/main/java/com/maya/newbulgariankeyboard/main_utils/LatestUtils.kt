package com.maya.newbulgariankeyboard.main_utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import com.maya.newbulgariankeyboard.R
import java.io.File
import java.io.FileNotFoundException

class LatestUtils {
    companion object {


        var FRAGMENT_NAME = "Languages"

        //todo change

        var DB_FILE_PATH2 =
            "https://drive.google.com/uc?export=download&id=12lLE0pcx4EiJwdLRoGrMmMDTXex4vCEA"



        @JvmStatic
        fun isConnectionAvailable(mContext: Context): Boolean {
            val connectivityManager =
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var activeNetworkInfo: NetworkInfo? = null
            try {
                activeNetworkInfo = connectivityManager.activeNetworkInfo
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun showInternetDialog(context: Context) {
            if (!isConnectionAvailable(context)) {
                try {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("No Internet !!!")
                    alertDialog.setIcon(R.drawable.ic_no_wifi)
                    alertDialog.setMessage("Kindly enable your internet connection for better experience.")
                    /*   alertDialog.setPositiveButton("Ok") { _, _ ->
                       }
   */
                    alertDialog.setNegativeButton("Ok") { dialog, _ ->
                        dialog.cancel()
                    }
                    alertDialog.show()
                } catch (e: Exception) {
                }
            }
        }

        fun intentToPlayStore(mContext: Context) {
            try {
                val intentRateApp =
                    Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R.string.rate_app_link)))
                mContext.startActivity(intentRateApp)
            } catch (e: Exception) {
            }
        }

        fun feedbackAppIntent(text: String, mContext: Context) {
            try {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "fastkeyboard.translator@gmail.com", null
                    )
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email for suggestion")
                emailIntent.putExtra(Intent.EXTRA_TEXT, text)
                mContext.startActivity(Intent.createChooser(emailIntent, "Send your feedback"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun shareToFriend(mContext: Activity) {
            try {
                val shareAppIntent = Intent(Intent.ACTION_SEND)
                shareAppIntent.type = "text/plain"
                val shareSub = "Check this Application on Google Play!\n"
                val shareBody = mContext.getString(R.string.share_app_link)
                shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
                shareAppIntent.putExtra(Intent.EXTRA_TEXT, shareSub + shareBody )
                mContext.startActivity(Intent.createChooser(shareAppIntent, "Share App using..."))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun furtherAppsOnPlayStore(mContext: Activity) {
            try {
                val intentMoreApps =
                    Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R.string.more_apps_link)))
                mContext.startActivity(intentMoreApps)
            } catch (e: Exception) {
            }
        }

        fun goToPrivacyPolicy(mContext: Context) {
            try {
                val intentMoreApps =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(mContext.getString(R.string.app_privacy_policy))
                    )
                mContext.startActivity(intentMoreApps)
            } catch (e: Exception) {
            }
        }

        fun goToSubTermsConditions(mContext: Context) {
            try {
                val intentMoreApps =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(mContext.getString(R.string.subscription_privacy_policy))
                    )
                mContext.startActivity(intentMoreApps)
            } catch (e: Exception) {
            }
        }

        fun getDbFilePathAndroid11(mContext: Context): String {
            var files: File? = null
            try {
                files = File(
                    mContext.filesDir.absolutePath + File.separator + "App Database" + File.separator + "E" + File.separator
                )
                files.mkdirs()
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return files!!.absolutePath
        }


        fun getDbFilePath(): String {
            var files: File? = null
            try {
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    files = File(
                        Environment.getExternalStorageDirectory().absolutePath +
                                "/Android/data/com.maya.newbulgariankeyboard/files" + File.separator + "App Database" + File.separator + "E" + File.separator
                    )
                    files.mkdirs()
                }
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

            return files!!.absolutePath
        }
    }
}