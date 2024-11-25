package com.maya.newbulgariankeyboard.models;

public class LatestFontModel {

    private String fontText;
    private String fontName;

    public LatestFontModel(String fontText, String fontName) {
        this.fontText = fontText;
        this.fontName = fontName;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontText() {
        return fontText;
    }

    public void setFontText(String fontText) {
        this.fontText = fontText;
    }
}
