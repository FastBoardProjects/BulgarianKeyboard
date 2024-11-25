package com.maya.newbulgariankeyboard.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LatestGalleryThemeModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long itemId;
    private String itemBgImage;
    private String itemTextColor;
    private String itemDisplayText;
    private String itemKeyShapeColor;
    private String itemEnterShiftColor;

    public LatestGalleryThemeModel() {
    }

    public LatestGalleryThemeModel(long itemId, String itemBgImage, String itemTextColor, String itemDisplayText, String itemKeyShapeColor, String itemEnterShiftColor) {
        this.itemId = itemId;
        this.itemBgImage = itemBgImage;
        this.itemTextColor = itemTextColor;
        this.itemDisplayText = itemDisplayText;
        this.itemKeyShapeColor = itemKeyShapeColor;
        this.itemEnterShiftColor = itemEnterShiftColor;
    }

    protected LatestGalleryThemeModel(Parcel in) {
        itemId = in.readLong();
        itemBgImage = in.readString();
        itemTextColor = in.readString();
        itemDisplayText = in.readString();
        itemKeyShapeColor = in.readString();
        itemEnterShiftColor = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(itemId);
        dest.writeString(itemBgImage);
        dest.writeString(itemTextColor);
        dest.writeString(itemDisplayText);
        dest.writeString(itemKeyShapeColor);
        dest.writeString(itemEnterShiftColor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LatestGalleryThemeModel> CREATOR = new Creator<LatestGalleryThemeModel>() {
        @Override
        public LatestGalleryThemeModel createFromParcel(Parcel in) {
            return new LatestGalleryThemeModel(in);
        }

        @Override
        public LatestGalleryThemeModel[] newArray(int size) {
            return new LatestGalleryThemeModel[size];
        }
    };

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getItemBgImage() {
        return itemBgImage;
    }

    public void setItemBgImage(String itemBgImage) {
        this.itemBgImage = itemBgImage;
    }

    public String getItemTextColor() {
        return itemTextColor;
    }

    public void setItemTextColor(String itemTextColor) {
        this.itemTextColor = itemTextColor;
    }

    public String getItemDisplayText() {
        return itemDisplayText;
    }

    public void setItemDisplayText(String itemDisplayText) {
        this.itemDisplayText = itemDisplayText;
    }

    public String getItemKeyShapeColor() {
        return itemKeyShapeColor;
    }

    public void setItemKeyShapeColor(String itemKeyShapeColor) {
        this.itemKeyShapeColor = itemKeyShapeColor;
    }

    public String getItemEnterShiftColor() {
        return itemEnterShiftColor;
    }

    public void setItemEnterShiftColor(String itemEnterShiftColor) {
        this.itemEnterShiftColor = itemEnterShiftColor;
    }

}
