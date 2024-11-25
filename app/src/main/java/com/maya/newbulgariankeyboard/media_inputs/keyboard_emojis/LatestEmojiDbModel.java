package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LatestEmojiDbModel {

    @PrimaryKey(autoGenerate = true)
    private int itemId;
    private String itemEmoji;

    public LatestEmojiDbModel(String itemEmoji) {
        this.itemEmoji = itemEmoji;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemEmoji() {
        return itemEmoji;
    }

    public void setItemEmoji(String itemEmoji) {
        this.itemEmoji = itemEmoji;
    }
}
