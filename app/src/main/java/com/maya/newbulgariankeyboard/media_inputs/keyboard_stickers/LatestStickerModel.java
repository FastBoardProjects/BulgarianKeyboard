package com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LatestStickerModel {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String url;

    public LatestStickerModel() {
    }

    public LatestStickerModel(int itemId, String itemUrl) {
        this.id = itemId;
        this.url = itemUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
