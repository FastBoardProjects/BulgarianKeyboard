package com.maya.newbulgariankeyboard.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers.LatestStickerModel;

import java.util.List;


@Dao
public interface SticekrsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleSticker(LatestStickerModel model);

    @Query("SELECT *FROM LatestStickerModel WHERE id=:id")
    LatestStickerModel getStickerById(int id);

    @Query("SELECT *FROM LatestStickerModel")
    List<LatestStickerModel> getAllStickers();

    @Delete
    void deleteSingleSticker(LatestStickerModel model);

    @Query("DELETE FROM LatestStickerModel WHERE id=:id")
    void deleteStickerById(int id);


    @Query("DELETE FROM LatestStickerModel")
    void deleteAllStickers();


}
