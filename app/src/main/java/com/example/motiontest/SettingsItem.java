package com.example.motiontest;

import android.text.InputType;

public class SettingsItem {
    private String name;
    private InputType inputType;

    SettingsItem(String name, InputType inputType) {
        this.name = name;
        this.inputType = inputType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }
}
