package com.maya.newbulgariankeyboard.activities.keyboardfamilyappsviews.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = onPrimaryDark

)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = onPrimaryLight
)

@Composable
fun KeyboardAppThemee(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {


    val sharedPref = LocalContext.current.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val themeVal = sharedPref.getInt("theme", 0)

    val colorScheme = if (themeVal == 0) {
        if (!darkTheme) {
            lightScheme
        } else {
            darkScheme
        }
    } else if (themeVal == 1) {
        lightScheme

    } else {
        darkScheme
    }

//    val colorScheme = lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}