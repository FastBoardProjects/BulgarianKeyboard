package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maya.newbulgariankeyboard.gif_model.Images;

import java.lang.reflect.Type;
import java.util.Collections;

public class LatestImagesTypeConverters {

    @TypeConverter
    public static Images storedStringToMyObjects(String data) {
        Gson gson = new Gson();
        if (data == null) {
            return (Images) Collections.emptyList();
        }
        Type listType = new TypeToken<Images>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String myObjectsToStoredString(Images myObjects) {
        Gson gson = new Gson();
        return gson.toJson(myObjects);
    }
}
