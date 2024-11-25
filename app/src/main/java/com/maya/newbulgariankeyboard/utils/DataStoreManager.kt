//package com.maya.newbulgariankeyboard.utils
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.preferencesDataStore
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//
//
//object DataStoreManager {
//    // Define the DataStore instance as a Singleton
//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
//
//    // Optional: Add methods to simplify read/write operations
//    suspend fun setValue(context: Context, key: Preferences.Key<Int>, value: Int) {
//        context.dataStore.edit { preferences ->
//            preferences[key] = value
//        }
//    }
//
//    fun getValue(context: Context, key: Preferences.Key<Int>): Flow<Int?> {
//        return context.dataStore.data.map { preferences ->
//            preferences[key] ?: 0
//        }
//    }
//}