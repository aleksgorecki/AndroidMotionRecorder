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
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.motiontest.databinding.ActivityMainNavBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class MainActivityNav extends AppCompatActivity implements SensorEventListener {

    private ActivityMainNavBinding binding;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int recordedSamplesNumber = 0;
    private boolean isRecording = false;
    private boolean isCountingDown = false;

    private final int xColor = Color.RED;
    private final int yColor = Color.GREEN;
    private final int zColor = Color.BLUE;

    private MotionBuffer motionBuffer = new MotionBuffer(50);

    private Motion lastRecordedMotion;

    private SharedPreferences sharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private String serverAddress;
    private int timeoutMs;
    private int motionDurationMs;
    private float maxY;
    private float minY;
    private boolean useCountdown;
    private int countdownSec;
    private boolean shouldCrop;
    private int cropLength;

    private OkHttpClient okHttpClient;


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

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        assignPreferencesValues();
        okHttpClient = new OkHttpClient.Builder().connectTimeout(timeoutMs, TimeUnit.MILLISECONDS).build();
        listener = (sharedPreferences, s) -> {
            assignPreferencesValues();
            okHttpClient = new OkHttpClient.Builder().connectTimeout(timeoutMs, TimeUnit.MILLISECONDS).build();
            binding.mainChart.getAxisLeft().setAxisMinimum(minY);
            binding.mainChart.getAxisLeft().setAxisMaximum(maxY);
            binding.mainChart.invalidate();
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        binding.fabRecord.setOnClickListener(view -> onFabClicked());

        initChartConfiguration();
        binding.textViewDelayCounter.setVisibility(View.GONE);
        binding.progressBarHorizontal.setVisibility(View.GONE);
        binding.textViewRecordingNow.setVisibility(View.GONE);
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
        motionBuffer.clear();
        recordedSamplesNumber = 0;
    }

    private void drawRecordedDataOnChart() {

        LineChart mainChart = binding.mainChart;

        ArrayList<Entry> xEntries = new ArrayList<>();
        ArrayList<Entry> yEntries = new ArrayList<>();
        ArrayList<Entry> zEntries = new ArrayList<>();

        ArrayList<Float>[] separatedAxes = lastRecordedMotion.getSeparatedAxes();
        for (int i = 0; i < lastRecordedMotion.getNumSamples(); i++) {
            xEntries.add(new Entry(i, separatedAxes[0].get(i)));
            yEntries.add(new Entry(i, separatedAxes[1].get(i)));
            zEntries.add(new Entry(i, separatedAxes[2].get(i)));
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

    private void onFabClicked() {
        clearRecordedData();
        binding.fabRecord.setEnabled(false);
        binding.progressBarHorizontal.setVisibility(View.VISIBLE);
        binding.cardViewChart.setVisibility(View.GONE);
        binding.cardView.setVisibility(View.GONE);

        CountDownTimer recordingTimer = new CountDownTimer(motionDurationMs, motionDurationMs/100) {

            @Override
            public void onTick(long l) {
                double progress = 100.0 * (l / (double) motionDurationMs);
                binding.progressBarHorizontal.setProgress((int) progress, false);
            }

            @Override
            public void onFinish() {
                runOnUiThread(MainActivityNav.this::onRecordingFinished);
            }
        };


        if (useCountdown) {
            isCountingDown = true;
            binding.textViewDelayCounter.setVisibility(View.VISIBLE);
            CountDownTimer countdownTimer = new CountDownTimer((1000 * countdownSec), 20) {
                @Override
                public void onTick(long l) {
                    binding.progressBarHorizontal.setProgress((int) (100.0 * (l / (1000.0 * countdownSec))), false);
                    binding.textViewDelayCounter.setText(Integer.toString((int) Math.ceil(l / 1000)));
                }

                @Override
                public void onFinish() { runOnUiThread(() -> {
                    isCountingDown = false;
                    binding.textViewDelayCounter.setVisibility(View.GONE);
                    recordingTimer.start();
                    onRecordingStarted();
                });
                }
            };
            countdownTimer.start();
        }
        else {
            onRecordingStarted();
            recordingTimer.start();
        }
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
            recordedSamplesNumber++;
            motionBuffer.recordSample(new float[]{x, y, z});
        }
    }

    private void assignPreferencesValues() {
        serverAddress = sharedPreferences.getString("server_address", "192.168.0.20");
        timeoutMs = Integer.parseInt(sharedPreferences.getString("timeout", "5000"));
        motionDurationMs = Integer.parseInt(sharedPreferences.getString("recording_duration", "700"));
        maxY = Float.parseFloat(sharedPreferences.getString("max_y", "-25"));
        minY = Float.parseFloat(sharedPreferences.getString("min_y", "-25"));
        useCountdown = sharedPreferences.getBoolean("countdown", false);
        countdownSec = Integer.parseInt(sharedPreferences.getString("countdown_sec", "3"));
        shouldCrop = sharedPreferences.getBoolean("crop", false);
        cropLength = Integer.parseInt(sharedPreferences.getString("crop_len", "3"));
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getMotionDurationMs() {
        return motionDurationMs;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public boolean isAMotionRecorded() {
        return (lastRecordedMotion != null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (!isRecording && !isCountingDown) {
                onFabClicked();
            }
            else {
                Toast.makeText(this, "Recording still running", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private void onRecordingStarted() {
        binding.textViewRecordingNow.setVisibility(View.VISIBLE);
        isRecording = true;
    }

    private void onRecordingFinished() {
        isRecording = false;
        lastRecordedMotion = motionBuffer.getMotion();
        if (shouldCrop) {
            lastRecordedMotion.crop(lastRecordedMotion.getGlobalExtremumPosition(), cropLength/2);
        }
        drawRecordedDataOnChart();
        binding.fabRecord.setEnabled(true);
        binding.progressBarHorizontal.setVisibility(View.GONE);
        binding.textViewRecordingNow.setVisibility(View.GONE);
        binding.cardViewChart.setVisibility(View.VISIBLE);
        binding.cardView.setVisibility(View.VISIBLE);
    }

    public Motion getLastRecordedMotion() {
        return lastRecordedMotion;
    }
}