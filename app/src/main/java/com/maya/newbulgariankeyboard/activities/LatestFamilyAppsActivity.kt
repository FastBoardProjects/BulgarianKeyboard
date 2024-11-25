package com.maya.newbulgariankeyboard.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.maya.newbulgariankeyboard.activities.keyboardfamilyappsviews.AppCard
import com.maya.newbulgariankeyboard.activities.keyboardfamilyappsviews.theme.KeyboardAppThemee
import com.maya.newbulgariankeyboard.utils.FamilyAppsData

class LatestFamilyAppsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        changeTheme(themeVal,this@LatestFamilyAppsActivity)

        setContent {
            KeyboardAppThemee {

                MyAppScreen()

            }
        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAppScreen() {

        val context = LocalContext.current as Activity

        Scaffold(containerColor = MaterialTheme.colorScheme.onPrimary, topBar = {
            TopAppBar(title = {
                Text(
                    text = "Family Apps",
                    color = Color.White,
                )
            }, navigationIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        context.finish()
                    })
            },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }, content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Handles the content padding from Scaffold
            ) {
                items(FamilyAppsData.appItemList) { item ->
                    AppCard(
                        imageRes = item.img,
                        title = item.name,
                        rating = item.rating,
                        showNewIcon = false
                    ) {
                        
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                            intent.setPackage("com.android.chrome")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        })
    }

}