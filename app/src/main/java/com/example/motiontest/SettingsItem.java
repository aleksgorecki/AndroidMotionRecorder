package com.example.motiontest;

public class SettingsItem {
    private Settings.SettingsType type;
    private String value;
    private int inputType;

    SettingsItem(Settings.SettingsType type, String value, int inputType) {
        this.type = type;
        this.value = value;
        this.inputType = inputType;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Settings.SettingsType getType() {
        return type;
    }

    public void setType(Settings.SettingsType type) {
        this.type = type;
    }
}
