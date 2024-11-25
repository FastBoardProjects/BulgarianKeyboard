package com.maya.newbulgariankeyboard.main_utils

import android.content.Context
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.models.LatestFlagModel
import com.maya.newbulgariankeyboard.models.LatestFontModel
import com.maya.newbulgariankeyboard.models.LatestSettingsModel

class LatestFillerHelper {

    companion object {

        fun fillFlagsModelList(): ArrayList<LatestFlagModel> {
            val list = ArrayList<LatestFlagModel>()
            //todo change

            list.add(LatestFlagModel("bg_BG", R.drawable.ic_bulgaria_flag))

            /*list.add(LatestFlagModel("bg", R.drawable.ic_bulgaria_flag))*/

            list.add(LatestFlagModel("en_US", R.drawable.ic_usa_flag))

            return list
        }

        fun fillSettingsList(context: Context): ArrayList<LatestSettingsModel> {
            val list = ArrayList<LatestSettingsModel>()

            list.add(
                LatestSettingsModel(
                    context.getString(R.string.languages_localization),
                    R.drawable.ic_languages_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.customize_themes),
                    R.drawable.ic_theme_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.keyboard_preferences),
                    R.drawable.ic_preferences_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.fonts_styles),
                    R.drawable.ic_font_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.text_correction),
                    R.drawable.ic_correction_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.enable_gesture_typing),
                    R.drawable.ic_gesture_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.enable_auto_suggestions),
                    R.drawable.ic_suggestion_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.one_hand_typing_mode),
                    R.drawable.ic_hand_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.customize_editing_panel),
                    R.drawable.ic_editing_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    context.getString(R.string.voice_recognition_typing),
                    R.drawable.ic_voice_input_icon
                )
            )
            /*list.add(
                LatestSettingsModel(
                    "Purchase Full Version",
                    R.drawable.ic_premium_icon
                )
            )

            list.add(
                LatestSettingsModel(
                    "Share App With Friends (Ad)",
                    R.drawable.ic_share_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    "Rate and Review App (Ad)",
                    R.drawable.ic_rate_icon
                )
            )
            list.add(
                LatestSettingsModel(
                    "Report Bugs or Suggestions",
                    R.drawable.ic_feedback_icon
                )
            )*/
            return list
        }

        fun fillFontsList(): ArrayList<LatestFontModel> {
            val list = ArrayList<LatestFontModel>()

            list.add(
                LatestFontModel(
                    "FONT",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ⒻⓄⓃⓉ",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD83C\uDD75\uD83C\uDD7E\uD83C\uDD7D\uD83C\uDD83",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD83C\uDD35\uD83C\uDD3E\uD83C\uDD3D\uD83C\uDD43",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD83C\uDD55\uD83C\uDD5E\uD83C\uDD5D\uD83C\uDD63",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD835\uDC05\uD835\uDC0E\uD835\uDC0D\uD835\uDC13",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ᖴOᑎT",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD835\uDD09\uD835\uDD12\uD835\uDD11\uD835\uDD17",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD835\uDD71\uD835\uDD7A\uD835\uDD79\uD835\uDD7F",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD835\uDD3D\uD835\uDD46ℕ\uD835\uDD4B",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ℱ\uD835\uDCAA\uD835\uDCA9\uD835\uDCAF",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ＦＯＮＴ",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "\uD835\uDDD9\uD835\uDDE2\uD835\uDDE1\uD835\uDDE7",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ᠻꪮ᭢ᡶ",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "₣Ø₦₮",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "fσит",
                    "app_fonts/open_sans.ttf"
                )
            )

            list.add(
                LatestFontModel(
                    "ϝσɳƚ",
                    "app_fonts/open_sans.ttf"
                )
            )
            list.add(
                LatestFontModel(
                    "\uD835\uDC39૦\uD835\uDC75\uD835\uDE83",
                    "app_fonts/open_sans.ttf"
                )
            )
            return list
        }
    }
}