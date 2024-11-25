package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maya.newbulgariankeyboard.gif_model.Analytics;

import java.lang.reflect.Type;
import java.util.Collections;

public class LatestAnalyticsTypeConverters {

    @TypeConverter
    public static Analytics storedStringToMyObjects(String data) {
        Gson gson = new Gson();
        if (data == null) {
            return (Analytics) Collections.emptyList();
        }
        Type listType = new TypeToken<Analytics>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String myObjectsToStoredString(Analytics myObjects) {
        Gson gson = new Gson();
        return gson.toJson(myObjects);
    }
}
