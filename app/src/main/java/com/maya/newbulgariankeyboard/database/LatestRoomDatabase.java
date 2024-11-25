package com.maya.newbulgariankeyboard.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.maya.newbulgariankeyboard.gif_model.Datum;
import com.maya.newbulgariankeyboard.main_classes.LatestLanguageDbModel;
import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmojiDbModel;
import com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers.LatestStickerModel;
import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel;


@Database(entities = {LatestStickerModel.class, Datum.class, LatestEmojiDbModel.class, LatestGalleryThemeModel.class, LatestLanguageDbModel.class}, version = 1, exportSchema = false)
public abstract class LatestRoomDatabase extends RoomDatabase {

    private static final String APPLICATION_DB_NAME = "data_key";
    private static LatestRoomDatabase instance = null;

    public abstract SticekrsDao getStickersDao();

    public abstract GifsDao gifsDao();

    public abstract EmojiDao emojiDao();

    public abstract GalleryThemesDao galleryThemesDao();

    public abstract SubtypesDao subtypesDao();

    public static LatestRoomDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, LatestRoomDatabase.class, APPLICATION_DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }

}
