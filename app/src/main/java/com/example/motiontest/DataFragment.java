package com.example.motiontest;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.motiontest.databinding.FragmentDataBinding;


public class DataFragment extends Fragment {

    private FragmentDataBinding binding;

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

        MainActivityNav parentActivity = ((MainActivityNav) requireContext());

        if (!parentActivity.isAMotionRecorded()) {
            Toast.makeText(requireContext(), "No data to send available.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataset.isEmpty()) {
            Toast.makeText(requireContext(), "Dataset name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (motionClass.isEmpty()) {
            Toast.makeText(requireContext(), "Motion class name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        parentActivity.sendDatasetRequest(dataset, motionClass);
    }
}