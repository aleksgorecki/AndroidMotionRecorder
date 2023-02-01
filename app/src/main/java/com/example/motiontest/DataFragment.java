package com.example.motiontest;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.motiontest.databinding.FragmentDataBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DataFragment extends Fragment {

    private FragmentDataBinding binding;
    private AlertDialog serverDialog;
    Callback datasetCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            getActivity().runOnUiThread(() -> {
                serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                serverDialog.setMessage("Server failed to respond.");
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            if (response.code() != 200) {
                getActivity().runOnUiThread(() -> {
                    serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    serverDialog.setMessage("Response code " + response.code());
                });
            }
            else {
                getActivity().runOnUiThread(() -> {
                    serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    serverDialog.setMessage("Recording saved on the server.");
                });
            }
        }
    };

    public DataFragment() {
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDataBinding.inflate(inflater, container, false);
        binding.buttonSendRecorded.setOnClickListener(view -> onSendButtonClicked());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.textViewServerAddressInfo.setText("Send to " + ((MainActivityNav) requireContext()).getServerAddress());
    }

    private void onSendButtonClicked() {
        String dataset = binding.editTextDataset.getText().toString();
        String motionClass = binding.editTextClassName.getText().toString();

        if (!validateBeforeRequest(dataset, motionClass)) {
            return;
        }

        MainActivityNav parentActivity = ((MainActivityNav) requireContext());

        try {

            ArrayList<Float>[] axesData = parentActivity.getLastRecordedMotion().getSeparatedAxes();

            JSONArray xJsonArray = new JSONArray(axesData[0]);
            JSONArray yJsonArray = new JSONArray(axesData[1]);
            JSONArray zJsonArray = new JSONArray(axesData[2]);

            String json = new JSONObject().put("class", motionClass).put("dataset", dataset).put("duration_ms", parentActivity.getMotionDurationMs()).put("x", xJsonArray).put("y", yJsonArray).put("z", zJsonArray).toString();
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            OkHttpClient okHttpClient = parentActivity.getOkHttpClient();
            Request request = new Request.Builder().url("http://" + parentActivity.getServerAddress() + "/new").post(body).build();
            Call call = okHttpClient.newCall(request);

            serverDialog = new AlertDialog.Builder(getContext())
                    .setView(getLayoutInflater().inflate(R.layout.server_dialog, null))
                    .setTitle("Send recorded motion")
                    .setMessage("Waiting for server...")
                    .setPositiveButton("Cancel", null)
                    .show();

            serverDialog.setOnDismissListener(dialogInterface -> {
                call.cancel();
            });

            call.enqueue(datasetCallback);
        }
        catch (JSONException e) {
            Toast.makeText(requireContext(), "Error creating JSON body.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateBeforeRequest(String dataset, String motionClass) {
        MainActivityNav parentActivity = ((MainActivityNav) requireContext());

        if (!parentActivity.isAMotionRecorded()) {
            Toast.makeText(requireContext(), "No data to send available.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dataset.isEmpty()) {
            Toast.makeText(requireContext(), "Dataset name cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (motionClass.isEmpty()) {
            Toast.makeText(requireContext(), "Motion class name cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}