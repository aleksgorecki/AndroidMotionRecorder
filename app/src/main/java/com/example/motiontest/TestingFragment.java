package com.example.motiontest;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestingFragment extends Fragment {

    FragmentTestingBinding binding;
    Callback testingCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            if (response.code() != 200) {
                Toast.makeText(requireContext(), "Response code " + response.code(), Toast.LENGTH_SHORT).show();
            }
            else {
                try {
                    JSONObject responseJson = new JSONObject(response.body().string());
                    String motionClass = responseJson.getString("class");
                    String predictionResult = responseJson.getString("result");
                    Toast.makeText(requireContext(), motionClass + " " + predictionResult, Toast.LENGTH_SHORT).show();
                }
                catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error retrieving JSON response body.", Toast.LENGTH_SHORT).show();
                }
            }
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
            JSONArray xJsonArray = new JSONArray(parentActivity.getxSamples());
            JSONArray yJsonArray = new JSONArray(parentActivity.getySamples());
            JSONArray zJsonArray = new JSONArray(parentActivity.getzSamples());

            String json = new JSONObject().put("duration_ms", parentActivity.getMotionDurationMs()).put("x", xJsonArray).put("y", yJsonArray).put("z", zJsonArray).toString();
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            OkHttpClient okHttpClient = parentActivity.getOkHttpClient();
            Request request = new Request.Builder().url("http://" + parentActivity.getServerAddress() + "/predict").post(body).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(testingCallback);
        }
        catch (JSONException e) {
            Toast.makeText(requireContext(), "Error creating JSON body.", Toast.LENGTH_SHORT).show();
        }
    }
}