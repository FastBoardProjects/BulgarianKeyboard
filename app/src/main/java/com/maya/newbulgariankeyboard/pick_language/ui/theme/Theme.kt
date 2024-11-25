package com.maya.newbulgariankeyboard.pick_language.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = CardDark,
    secondary = CardDark,
    tertiary = Pink80,
    onPrimary = Color.White


)

private val LightColorScheme = lightColorScheme(
    primary = CardLight,
    secondary = CardLight,
    tertiary = Pink40,
    onPrimary = Color(0xFF333333)

)

@Composable
fun NewKeyboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    val sharedPref = LocalContext.current.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val themeVal = sharedPref.getInt("theme", 0)

    val colorScheme = if (themeVal == 0) {
        if (!darkTheme) {
            LightColorScheme
        } else {
            DarkColorScheme
        }
    } else if (themeVal == 1) {
        LightColorScheme

    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}