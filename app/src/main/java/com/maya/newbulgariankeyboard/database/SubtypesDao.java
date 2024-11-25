package com.maya.newbulgariankeyboard.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.maya.newbulgariankeyboard.main_classes.LatestLanguageDbModel;

import java.util.List;


@Dao
public interface SubtypesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleSubtype(LatestLanguageDbModel model);

    @Query("SELECT *FROM LatestLanguageDbModel WHERE id=:id")
    LatestLanguageDbModel getSubtypeById(int id);

    @Query("SELECT *FROM LatestLanguageDbModel")
    List<LatestLanguageDbModel> getAllSubtypes();

    @Delete
    void deleteSingleSubtype(LatestLanguageDbModel model);

    @Query("DELETE FROM LatestLanguageDbModel WHERE id=:id")
    void deleteSubtypeById(int id);

    @Query("DELETE FROM LatestLanguageDbModel")
    void deleteAllSubtypes();


}
