package com.example.motiontest;

import android.text.InputType;

import java.util.ArrayList;


public class Settings {
    public String serverAddress;
    public int motionDurationMs;
    public int serverTimeoutMs;
    public int Ymax;
    public int Ymin;

    enum SettingsType {
        SERVER_ADDRESS, MOTION_DURATION_MS, SERVER_TIMEOUT_MS, Y_MAX, Y_MIN
    }

    public ArrayList<SettingsItem> toCollection() {
        return new ArrayList<SettingsItem>() {
            {
                add(new SettingsItem(SettingsType.SERVER_ADDRESS, serverAddress, InputType.TYPE_CLASS_TEXT));
                add(new SettingsItem(SettingsType.MOTION_DURATION_MS, Integer.toString(motionDurationMs), InputType.TYPE_CLASS_NUMBER));
                add(new SettingsItem(SettingsType.SERVER_TIMEOUT_MS, Integer.toString(serverTimeoutMs), InputType.TYPE_CLASS_NUMBER));
                add(new SettingsItem(SettingsType.Y_MAX, Integer.toString(Ymax), InputType.TYPE_CLASS_NUMBER));
                add(new SettingsItem(SettingsType.Y_MIN, Integer.toString(Ymin), InputType.TYPE_CLASS_NUMBER));
            }
        };
    }

    public void fromCollection(ArrayList<SettingsItem> collection) {

        if (collection.isEmpty()) {
            return;
        }

        collection.forEach(settingsItem -> {
            switch (settingsItem.getType()) {
                case SERVER_ADDRESS:
                    serverAddress = settingsItem.getValue();
                    break;
                case MOTION_DURATION_MS:
                    motionDurationMs = Integer.parseInt(settingsItem.getValue());
                    break;
                case SERVER_TIMEOUT_MS:
                    serverTimeoutMs = Integer.parseInt(settingsItem.getValue());
                    break;
                case Y_MAX:
                    Ymax = Integer.parseInt(settingsItem.getValue());
                    break;
                case Y_MIN:
                    Ymin = Integer.parseInt(settingsItem.getValue());
                    break;
            }
        });
    }

    public void setDefaults() {
        serverAddress = "192.168.0.20:8080";
        motionDurationMs = 800;
        serverTimeoutMs = 10000;
        Ymax = 30;
        Ymin = -30;
    }
}
