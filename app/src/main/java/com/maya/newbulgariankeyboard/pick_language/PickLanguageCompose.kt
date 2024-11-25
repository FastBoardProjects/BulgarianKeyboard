package com.maya.newbulgariankeyboard.pick_language

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun AppLanguagePicker(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var searchText by remember {
        mutableStateOf("")
    }
    var Languages2 by remember {
        mutableStateOf(PossibleAppTranslationLanguage)
    }

    val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)


//    LaunchedEffect(key1 = System.currentTimeMillis()) {
//        if (sharedPref != null) {
//            val x = sharedPref.getString("APP_LANGUAGE", null)
//            Toast.makeText(context, x.toString(), Toast.LENGTH_SHORT).show()
//        }
//    }


    LaunchedEffect(key1 = searchText) {
        Languages2 = PossibleAppTranslationLanguage.filter {
            it.name.lowercase().contains(searchText.lowercase())
        }
    }

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
//            Text(
//                text = "Pick App Language",
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.bodyLarge
//            )


            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Enter Language Name") },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "")
                },
                colors = TextFieldDefaults.colors()
            )


            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(Languages2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {

                                with(sharedPref.edit()) {
                                    putString("APP_LANGUAGE", it.code)
                                    apply()
                                }

                                Toast
                                    .makeText(context, "${it.name} Selected", Toast.LENGTH_SHORT)
                                    .show()
                                (context as Activity).finish()

                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = it.img),
                            contentDescription = "Flag of ${it.name}"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = it.name)
                    }
                }
            }
        }
    }
}
//}
