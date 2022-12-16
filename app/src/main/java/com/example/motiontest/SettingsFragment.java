package com.example.motiontest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Preference resetPreference;
    private Preference checkServerPreference;
    private EditTextPreference serverAddressPreference;
    private EditTextPreference timeoutPreference;
    private EditTextPreference recordingDurationPreference;
    private EditTextPreference YMinPreference;
    private EditTextPreference YMaxPreference;
    private SwitchPreference countdownPreference;
    private EditTextPreference countdownSecPreference;
    private SwitchPreference cropPreference;
    private EditTextPreference cropLengthPreference;
    AlertDialog serverDialog;
    Callback checkServerCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            getActivity().runOnUiThread(() -> {
                serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                serverDialog.setMessage("Server failed to respond.");
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            getActivity().runOnUiThread(() -> {
                if (response.code() != 200) {
                    serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    serverDialog.setMessage("Response code " + response.code());
                }
                else {
                    serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    serverDialog.setMessage("Server responded OK");
                }
            });
        }
    };


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        bindPreferences();
        setupPreferenceInputTypes();
        setupPreferenceValidation();
    }

    public void checkServer() {
        MainActivityNav parentActivity = ((MainActivityNav) requireContext());
        OkHttpClient okHttpClient = parentActivity.getOkHttpClient();

        Request request = new Request.Builder().url("http://" + parentActivity.getServerAddress()).build();

        Call call = okHttpClient.newCall(request);

        serverDialog = new AlertDialog.Builder(getContext())
                .setView(getLayoutInflater().inflate(R.layout.server_dialog, null))
                .setTitle("Check server connection")
                .setMessage("Waiting for server...")
                .setNeutralButton("Cancel", null)
                .show();

        serverDialog.setOnDismissListener(dialogInterface -> {
            call.cancel();
        });

        call.enqueue(checkServerCallback);
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
        countdownPreference = preferenceManager.findPreference("countdown");
        countdownSecPreference = preferenceManager.findPreference("countdown_sec");
        cropPreference = preferenceManager.findPreference("crop");
        cropLengthPreference = preferenceManager.findPreference("crop_len");


        resetPreference.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Reset to defaults")
                    .setMessage("Are you sure you want to reset all preferences to default values?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        resetPreferencesToDefaults();
                    })
                    .show();
            return super.onPreferenceTreeClick(preference);
        });

        checkServerPreference.setOnPreferenceClickListener(preference -> {
            checkServer();
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

        countdownSecPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Countdown duration cannot be left empty.");
                return false;
            }
            return true;
        });

        cropLengthPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (((String) newValue).isEmpty() ) {
                showValidationErrorDialog("Cropped motion length cannot be left empty.");
                return false;
            }
            return true;
        });
    }

    private void setupPreferenceInputTypes() {
        timeoutPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSelection(editText.getText().length());
        });

        recordingDurationPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSelection(editText.getText().length());
        });

        YMinPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            editText.setSelection(editText.getText().length());
        });

        YMaxPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            editText.setSelection(editText.getText().length());
        });

        countdownSecPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSelection(editText.getText().length());
        });

        cropLengthPreference.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setSelection(editText.getText().length());
        });
    }
}