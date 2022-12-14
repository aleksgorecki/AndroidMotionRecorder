package com.example.motiontest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.motiontest.databinding.ActivityMainNavBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivityNav extends AppCompatActivity implements SensorEventListener {

    private ActivityMainNavBinding binding;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int recordedSamplesNumber = 0;
    private boolean isRecording = false;

    private final int xColor = Color.RED;
    private final int yColor = Color.GREEN;
    private final int zColor = Color.BLUE;

    private final ArrayList<Float> xSamples = new ArrayList<>();
    private final ArrayList<Float> ySamples = new ArrayList<>();
    private final ArrayList<Float> zSamples = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private String serverAddress;
    private int timeoutMs;
    private int motionDurationMs;
    private float maxY;
    private float minY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_dataset, R.id.navigation_testing, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main_nav);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        assignPreferencesValues();
        listener = (sharedPreferences, s) -> {
            assignPreferencesValues();
            initChartConfiguration();
            binding.mainChart.invalidate();
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        binding.fabRecord.setOnClickListener(view -> startRecording());

        initChartConfiguration();
    }

    private void initChartConfiguration() {

        LineChart mainChart = binding.mainChart;

        mainChart.setDrawGridBackground(false);
        mainChart.setNoDataText("Record a motion gesture to display it");
        mainChart.setNoDataTextColor(Color.GRAY);
        mainChart.getAxisRight().setEnabled(false);
        mainChart.setVisibleYRange(minY, maxY, YAxis.AxisDependency.LEFT);
        mainChart.setAutoScaleMinMaxEnabled(false);
        mainChart.getDescription().setEnabled(false);

        XAxis xaxis = mainChart.getXAxis();
        YAxis yaxis = mainChart.getAxisLeft();

        xaxis.setDrawGridLines(false);
        yaxis.setDrawGridLines(false);

        yaxis.setAxisMinimum(minY);
        yaxis.setAxisMaximum(maxY);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                xaxis.setTextColor(Color.WHITE);
                yaxis.setTextColor(Color.WHITE);
                mainChart.getLegend().setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                xaxis.setTextColor(Color.BLACK);
                yaxis.setTextColor(Color.BLACK);
                mainChart.getLegend().setTextColor(Color.BLACK);
                break;
        }
    }

    private void clearRecordedData() {
        xSamples.clear();
        ySamples.clear();
        zSamples.clear();
        recordedSamplesNumber = 0;
    }

    private void drawRecordedDataOnChart() {

        LineChart mainChart = binding.mainChart;

        ArrayList<Entry> xEntries = new ArrayList<>();
        ArrayList<Entry> yEntries = new ArrayList<>();
        ArrayList<Entry> zEntries = new ArrayList<>();

        for (int i = 0; i < recordedSamplesNumber; i++) {
            xEntries.add(new Entry(i, xSamples.get(i)));
            yEntries.add(new Entry(i, ySamples.get(i)));
            zEntries.add(new Entry(i, zSamples.get(i)));
        }
        LineDataSet xDataSet = new LineDataSet(xEntries, "X");
        LineDataSet yDataSet = new LineDataSet(yEntries, "Y");
        LineDataSet zDataSet = new LineDataSet(zEntries, "Z");

        styleDataSets(xDataSet, yDataSet, zDataSet);

        LineData combinedData = new LineData(xDataSet, yDataSet, zDataSet);
        mainChart.setAutoScaleMinMaxEnabled(true);
        mainChart.setData(combinedData);
        mainChart.notifyDataSetChanged();
        mainChart.invalidate();
    }

    private void styleDataSets(LineDataSet xDataSet, LineDataSet yDataSet, LineDataSet zDataSet) {
        xDataSet.setColor(xColor);
        yDataSet.setColor(yColor);
        zDataSet.setColor(zColor);

        LineDataSet[] dataSets = {xDataSet, yDataSet, zDataSet};
        for (LineDataSet dataSet: dataSets) {
            dataSet.setDrawValues(false);
            dataSet.setCircleColor(dataSet.getColor());
            dataSet.setCircleHoleColor(dataSet.getColor());
        }
    }

    private void startRecording() {

        FloatingActionButton recordButton = binding.fabRecord;

        clearRecordedData();
        isRecording = true;
        recordButton.setEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    isRecording = false;
                    drawRecordedDataOnChart();
                    recordButton.setEnabled(true);
                });
            }
        }, motionDurationMs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        if (isRecording) {
            xSamples.add(x);
            ySamples.add(y);
            zSamples.add(z);

            recordedSamplesNumber++;
        }
    }

    private void assignPreferencesValues() {
        serverAddress = sharedPreferences.getString("server_address", "192.168.0.20");
        timeoutMs = Integer.parseInt(sharedPreferences.getString("timeout", "5000"));
        motionDurationMs = Integer.parseInt(sharedPreferences.getString("recording_duration", "700"));
        maxY = Float.parseFloat(sharedPreferences.getString("max_y", "-25"));
        minY = Float.parseFloat(sharedPreferences.getString("min_y", "-25"));
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getMotionDurationMs() {
        return motionDurationMs;
    }

    public boolean checkServer() {
        return false;
    }

    public void sendPredictionRequest() {

    }

    public void sendDatasetRequest(String dataset, String motionClass) {

    }

    public boolean isAMotionRecorded() {
        return (!xSamples.isEmpty() && !ySamples.isEmpty() && !zSamples.isEmpty());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!isRecording) {
                startRecording();
            }
        }
        return true;
    }
}