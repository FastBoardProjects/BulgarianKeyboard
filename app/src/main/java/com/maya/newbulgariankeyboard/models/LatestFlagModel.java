package com.maya.newbulgariankeyboard.models;

public class LatestFlagModel {

    private String flagCode;
    private int flagIcon;

    public LatestFlagModel(String flagCode, int flagIcon) {
        this.flagCode = flagCode;
        this.flagIcon = flagIcon;
    }

    public String getFlagCode() {
        return flagCode;
    }

    public void setFlagCode(String flagCode) {
        this.flagCode = flagCode;
    }

    public int getFlagIcon() {
        return flagIcon;
    }

    public void setFlagIcon(int flagIcon) {
        this.flagIcon = flagIcon;
    }
}
