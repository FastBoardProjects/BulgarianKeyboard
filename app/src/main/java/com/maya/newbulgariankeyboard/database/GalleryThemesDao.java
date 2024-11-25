package com.maya.newbulgariankeyboard.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel;

import java.util.List;


@Dao
public interface GalleryThemesDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleGalleryTheme(LatestGalleryThemeModel model);

    @Query("SELECT *FROM LatestGalleryThemeModel WHERE itemId=:itemId")
    LatestGalleryThemeModel getGalleryThemeById(long itemId);

    @Query("SELECT *FROM LatestGalleryThemeModel")
    List<LatestGalleryThemeModel> getAllGalleryThemes();

    @Delete
    void deleteSingleGalleryTheme(LatestGalleryThemeModel model);

    @Query("DELETE FROM LatestGalleryThemeModel WHERE itemId=:itemId")
    void deleteGalleryThemeById(long itemId);

    @Query("DELETE FROM LatestGalleryThemeModel")
    void deleteAllGalleryThemes();


}
