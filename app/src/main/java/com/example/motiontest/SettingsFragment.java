package com.example.motiontest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference resetPreference;
    private Preference checkServerPreference;
    private EditTextPreference serverAddressPreference;
    private EditTextPreference timeoutPreference;
    private EditTextPreference recordingDurationPreference;
    private EditTextPreference YMinPreference;
    private EditTextPreference YMaxPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        bindPreferences();
        setupPreferenceInputTypes();
        setupPreferenceValidation();
    }

    private void bindPreferences() {
        PreferenceManager preferenceManager = getPreferenceManager();
        resetPreference = preferenceManager.findPreference("reset");
        checkServerPreference = preferenceManager.findPreference("check_server");
        serverAddressPreference = preferenceManager.findPreference("server_address");
        timeoutPreference = preferenceManager.findPreference("timeout");
        recordingDurationPreference = preferenceManager.findPreference("recording_duration");
        YMinPreference = preferenceManager.findPreference("min_y");
        YMaxPreference = preferenceManager.findPreference("max_y");

        resetPreference.setOnPreferenceClickListener(preference -> {
            Log.e("TEST", serverAddressPreference.getText());
            new AlertDialog.Builder(getContext())
                    .setTitle("Reset to default")
                    .setMessage("Are you sure you want to reset all preferences to default values?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        resetPreferencesToDefaults();
                    })
                    .show();
            return super.onPreferenceTreeClick(preference);
        });

        checkServerPreference.setOnPreferenceClickListener(preference -> {
            return super.onPreferenceTreeClick(preference);
        });
    }

    private void resetPreferencesToDefaults() {
        Context context = requireContext();
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
        setPreferencesFromResource(R.xml.root_preferences, null);
        bindPreferences();
        setupPreferenceValidation();
        setupPreferenceInputTypes();
    }

    private void showValidationErrorDialog(String errorReason) {
        new AlertDialog.Builder(getContext())
                .setTitle("Validation error")
                .setMessage(errorReason)
                .setNeutralButton("Ok", null)
                .show();
    }

    private void setupPreferenceValidation() {

        serverAddressPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Server address cannot be left empty.");
                return false;
            }
            return true;
        });

        timeoutPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Timeout duration cannot be left empty.");
                return false;
            }
            return true;
        });

        recordingDurationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Recording duration cannot be left empty.");
                return false;
            }
            return true;
        });

        YMaxPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Maximal amplitude value cannot be left empty.");
                return false;
            }
            return true;
        });

        YMinPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Minimal amplitude value cannot be left empty.");
                return false;
            }
            return true;
        });
    }

    private void setupPreferenceInputTypes() {
        timeoutPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));

        recordingDurationPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));

        YMinPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED));

        YMaxPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED));
    }
}