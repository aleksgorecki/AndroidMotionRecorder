package com.example.motiontest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.motiontest.databinding.FragmentTestingBinding;
import com.example.motiontest.ml.BasicModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

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
    BasicModel model;
    ArrayList<String> labels = new ArrayList<>(Arrays.asList(
            "nothing",
            "x_negative",
            "x_positive",
            "y_negative",
            "y_positive",
            "z_negative",
            "z_positive"
    ));
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

            if (response.code() != 200) {
                getActivity().runOnUiThread(() -> {
                    serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    serverDialog.setMessage("Response code " + response.code());
                });
            }
            else {
                try {
                    JSONObject responseJson = new JSONObject(response.body().string());
                    String motionClass = responseJson.getString("class");
                    String predictionResult = responseJson.getString("result");
                    getActivity().runOnUiThread(() -> {
                        serverDialog.findViewById(R.id.progressBar).setVisibility(View.GONE);
                        serverDialog.setMessage(motionClass + ": " + String.format("%.2f", Float.parseFloat(predictionResult)));
                        binding.textViewResultsServer.setText("Last prediction - " + motionClass + ": " + String.format("%.2f", Float.parseFloat(predictionResult)));
                    });
                }
                catch (JSONException | IOException e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error retrieving JSON response body.", Toast.LENGTH_SHORT).show();
                    });
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
        binding.buttonPredictLocal.setOnClickListener(view -> predictLocalClicked());
        return binding.getRoot();
    }

    private void predictLocalClicked() {
        MainActivityNav parentActivity = ((MainActivityNav) requireContext());
        if (!parentActivity.isAMotionRecorded()) {
            Toast.makeText(requireContext(), "No data to use available.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            model = BasicModel.newInstance(requireContext());


            Motion motion = parentActivity.getLastRecordedMotion();
            motion.crop(motion.getGlobalExtremumPosition(), 120/2);

            float[] flattenedInput = new float[1 * 1 * 120 * 3];

            float[][] motionArray = motion.getRecordedSamples();
            int flattenedIndex = 0;
            for (int i = 0; i < 120; i++) {
                flattenedInput[flattenedIndex++] = motionArray[i][0];
                flattenedInput[flattenedIndex++] = motionArray[i][1];
                flattenedInput[flattenedIndex++] = motionArray[i][2];
            }

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 1, 120, 3}, DataType.FLOAT32);
            inputFeature0.loadArray(flattenedInput);
            BasicModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] outputArray = outputFeature0.getFloatArray();

            int mostLikelyIndex = 0;
            float highestProbability = 0;
            for (int i = 0; i < labels.size(); i++) {
                if (outputArray[i] > highestProbability) {
                    mostLikelyIndex = i;
                    highestProbability = outputArray[i];
                }
            }

            Dialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Local prediction")
                    .setMessage(labels.get(mostLikelyIndex) + ": " + String.format("%.2f", outputArray[mostLikelyIndex]))
                    .setPositiveButton("Cancel", null)
                    .show();
            binding.textViewResultsLocal.setText("Last prediction - " + labels.get(mostLikelyIndex) + ": " + String.format("%.2f", outputArray[mostLikelyIndex]));

            model.close();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Model error occured.", Toast.LENGTH_SHORT).show();
        }
    }

    private void predictServerClicked() {
        MainActivityNav parentActivity = ((MainActivityNav) requireContext());
        if (!parentActivity.isAMotionRecorded()) {
            Toast.makeText(requireContext(), "No data to send available.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ArrayList<Float>[] axesData = parentActivity.getLastRecordedMotion().getSeparatedAxes();

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
                    .setPositiveButton("Cancel", null)
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