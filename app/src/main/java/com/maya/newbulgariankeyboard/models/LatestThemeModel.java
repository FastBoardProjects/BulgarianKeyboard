package com.maya.newbulgariankeyboard.models;

public class LatestThemeModel {

    private int itemId;
    private String itemBgColor;
    private String itemTextColor;
    private String itemDisplayText;
    private String itemKeyShapeColor;
    private String itemEnterShiftColor;

    public LatestThemeModel(int itemId, String itemBgColor, String itemTextColor, String itemDisplayText, String itemKeyShapeColor, String itemEnterShiftColor) {
        this.itemId = itemId;
        this.itemBgColor = itemBgColor;
        this.itemTextColor = itemTextColor;
        this.itemDisplayText = itemDisplayText;
        this.itemKeyShapeColor = itemKeyShapeColor;
        this.itemEnterShiftColor = itemEnterShiftColor;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemBgColor() {
        return itemBgColor;
    }

    public void setItemBgColor(String itemBgColor) {
        this.itemBgColor = itemBgColor;
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
