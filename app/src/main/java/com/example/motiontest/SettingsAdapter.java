package com.example.motiontest;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.motiontest.databinding.SettingsItemBinding;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private ArrayList<SettingsItem> settingsItems;

    public SettingsAdapter(ArrayList<SettingsItem> settingsItems) {
        this.settingsItems = settingsItems;
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewSettingsDescription;
        private EditText editTextSettingsValue;

        private SettingsItemBinding binding;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSettingsDescription = itemView.findViewById(R.id.textViewSettingsDescription);
            editTextSettingsValue = itemView.findViewById(R.id.editTextSettingsValue);
        }
    }

    @NonNull
    @Override
    public SettingsAdapter.SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View settingsItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item, parent, false);
        return new SettingsViewHolder(settingsItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.SettingsViewHolder holder, int position) {

        SettingsItem settingsItem = settingsItems.get(position);

        switch (settingsItem.getType()) {
            case SERVER_ADDRESS:
                holder.textViewSettingsDescription.setText("Dataset and model server address (IP and port)");
                break;
            case SERVER_TIMEOUT_MS:
                holder.textViewSettingsDescription.setText("Server request timeout (in ms)");
                break;
            case Y_MAX:
                holder.textViewSettingsDescription.setText("Maximum amplitude value displayed on chart");
                break;
            case Y_MIN:
                holder.textViewSettingsDescription.setText("Minimum amplitude value displayed on chart");
                break;
        }

        holder.editTextSettingsValue.setText(settingsItem.getValue());

        holder.editTextSettingsValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return settingsItems.size();
    }
}
