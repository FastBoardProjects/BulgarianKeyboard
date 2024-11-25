package com.maya.newbulgariankeyboard.main_classes;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LatestLanguageDbModel {
    @PrimaryKey()
    private int id;

    public LatestLanguageDbModel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
