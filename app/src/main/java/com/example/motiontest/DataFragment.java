package com.example.motiontest;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DataFragment extends Fragment {

    private FragmentDataBinding binding;
    Callback datasetCallback = new Callback() {
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
                Toast.makeText(requireContext(), "Server OK", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public DataFragment() {
        // Required empty public constructor
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
            JSONArray xJsonArray = new JSONArray(parentActivity.getxSamples());
            JSONArray yJsonArray = new JSONArray(parentActivity.getySamples());
            JSONArray zJsonArray = new JSONArray(parentActivity.getzSamples());

            String json = new JSONObject().put("class", motionClass).put("duration_ms", parentActivity.getMotionDurationMs()).put("x", xJsonArray).put("y", yJsonArray).put("z", zJsonArray).toString();
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            OkHttpClient okHttpClient = parentActivity.getOkHttpClient();
            Request request = new Request.Builder().url("http://" + parentActivity.getServerAddress() + "/new").post(body).build();
            Call call = okHttpClient.newCall(request);
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