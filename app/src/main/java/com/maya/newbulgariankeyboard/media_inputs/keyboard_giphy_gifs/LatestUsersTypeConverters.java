package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maya.newbulgariankeyboard.gif_model.User;

import java.lang.reflect.Type;
import java.util.Collections;

public class LatestUsersTypeConverters {

    @TypeConverter
    public static User storedStringToMyObjects(String data) {
        Gson gson = new Gson();
        if (data == null) {
            return (User) Collections.emptyList();
        }
        Type listType = new TypeToken<User>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String myObjectsToStoredString(User myObjects) {
        Gson gson = new Gson();
        return gson.toJson(myObjects);
    }
}
