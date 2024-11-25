package com.maya.newbulgariankeyboard.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maya.newbulgariankeyboard.gif_model.Datum;

import java.util.List;


@Dao
public interface GifsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleGif(Datum model);

    @Query("SELECT *FROM Datum WHERE itemId=:itemId")
    Datum getGifById(int itemId);

    @Query("SELECT *FROM Datum")
    List<Datum> getAllGifs();

    @Delete
    void deleteSingleGif(Datum model);

    @Query("DELETE FROM Datum WHERE itemId=:itemId")
    void deleteGifById(int itemId);


    @Query("DELETE FROM Datum")
    void deleteAllGifs();


}
