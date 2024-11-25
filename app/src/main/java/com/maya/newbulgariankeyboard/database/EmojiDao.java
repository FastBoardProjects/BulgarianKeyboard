package com.maya.newbulgariankeyboard.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis.LatestEmojiDbModel;

import java.util.List;


@Dao
public interface EmojiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleGif(LatestEmojiDbModel model);

    @Query("SELECT *FROM LatestEmojiDbModel WHERE itemId=:itemId")
    LatestEmojiDbModel getGifById(int itemId);

    @Query("SELECT *FROM LatestEmojiDbModel WHERE itemEmoji=:itemEmoji")
    LatestEmojiDbModel getGifByEmojiTxt(String itemEmoji);

    @Query("SELECT *FROM LatestEmojiDbModel")
    List<LatestEmojiDbModel> getAllGifs();

    @Delete
    void deleteSingleGif(LatestEmojiDbModel model);

    @Query("DELETE FROM LatestEmojiDbModel WHERE itemId=:itemId")
    void deleteGifById(int itemId);

    @Query("DELETE FROM LatestEmojiDbModel")
    void deleteAllGifs();

}
