package com.example.motiontest;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.motiontest.databinding.FragmentTestingBinding;

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

public class TestingFragment extends Fragment {

    FragmentTestingBinding binding;
    AlertDialog serverDialog;
    Callback testingCallback = new Callback() {
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
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        String motionClass = responseJson.getString("class");
                        String predictionResult = responseJson.getString("result");
                        serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        serverDialog.setMessage(motionClass + ": " + predictionResult);
                        binding.textViewResultsServer.setText(motionClass + ": " + predictionResult);
                    }
                    catch (JSONException | IOException e) {
                        Toast.makeText(requireContext(), "Error retrieving JSON response body.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    public TestingFragment() {
    }

    public static TestingFragment newInstance() {
        TestingFragment fragment = new TestingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =  FragmentTestingBinding.inflate(inflater, container, false);
        binding.buttonPredictServer.setOnClickListener(view -> predictServerClicked());
        return binding.getRoot();
    }

    private void predictServerClicked() {
        MainActivityNav parentActivity = ((MainActivityNav) requireContext());
        if (!parentActivity.isAMotionRecorded()) {
            Toast.makeText(requireContext(), "No data to send available.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ArrayList<Double>[] axesData = parentActivity.getLastRecordedMotion().getSeparatedAxes();

            JSONArray xJsonArray = new JSONArray(axesData[0]);
            JSONArray yJsonArray = new JSONArray(axesData[1]);
            JSONArray zJsonArray = new JSONArray(axesData[2]);

            String json = new JSONObject().put("duration_ms", parentActivity.getMotionDurationMs()).put("x", xJsonArray).put("y", yJsonArray).put("z", zJsonArray).toString();
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            OkHttpClient okHttpClient = parentActivity.getOkHttpClient();
            Request request = new Request.Builder().url("http://" + parentActivity.getServerAddress() + "/predict").post(body).build();
            Call call = okHttpClient.newCall(request);

            serverDialog = new AlertDialog.Builder(getContext())
                    .setView(getLayoutInflater().inflate(R.layout.server_dialog, null))
                    .setTitle("Predict on server")
                    .setMessage("Waiting for server...")
                    .setNeutralButton("Cancel", null)
                    .show();

            serverDialog.setOnDismissListener(dialogInterface -> {
                call.cancel();
            });

            call.enqueue(testingCallback);
        }
        catch (JSONException e) {
            Toast.makeText(requireContext(), "Error creating JSON body.", Toast.LENGTH_SHORT).show();
        }
    }
}