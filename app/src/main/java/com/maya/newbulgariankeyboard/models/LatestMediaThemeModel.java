package com.maya.newbulgariankeyboard.models;

public class LatestMediaThemeModel {

    private int itemId;
    private int itemBgShape;
    private String itemTextColor;
    private String itemDisplayText;
    private String itemKeyShapeColor;
    private String itemEnterShiftColor;

    public LatestMediaThemeModel(int itemId, int itemBgShape, String itemTextColor, String itemDisplayText, String itemKeyShapeColor, String itemEnterShiftColor) {
        this.itemId = itemId;
        this.itemBgShape = itemBgShape;
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

    public int getItemBgShape() {
        return itemBgShape;
    }

    public void setItemBgShape(int itemBgShape) {
        this.itemBgShape = itemBgShape;
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
