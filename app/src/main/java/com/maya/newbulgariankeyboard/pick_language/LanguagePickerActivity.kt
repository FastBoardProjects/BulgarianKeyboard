package com.maya.newbulgariankeyboard.pick_language

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.maya.newbulgariankeyboard.pick_language.ui.theme.NewKeyboardTheme

class LanguagePickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            NewKeyboardTheme {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.secondary // Set background color for the Scaffold
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Fill the entire Scaffold
                            .background(MaterialTheme.colorScheme.secondary) // Apply background color
                            .padding(it) // Apply padding from the Scaffold
                    ) {
                        AppLanguagePicker()
                    }
                }
            }
        }
    }
}

