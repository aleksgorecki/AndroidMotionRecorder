package com.example.motiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView textViewXValue;
    private TextView textViewYValue;
    private TextView textViewZValue;
    private LineChart mainChart;
    private Button recordButton;

    private final ArrayList<Entry> xData = new ArrayList<Entry>();
    private final ArrayList<Entry> yData = new ArrayList<Entry>();
    private final ArrayList<Entry> zData = new ArrayList<Entry>();

    private int sampleNo = 0;
    private boolean isRecording = false;
    private final int motionDurationMs = 700;
    private final float maxY = 25;
    private final float minY = -25;
    private final int xColor = Color.RED;
    private final int yColor = Color.GREEN;
    private final int zColor = Color.BLUE;


    OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        textViewXValue = findViewById(R.id.textViewXValue);
        textViewYValue = findViewById(R.id.textViewYValue);
        textViewZValue = findViewById(R.id.textViewZValue);
        mainChart = findViewById(R.id.lineChart);
        recordButton = findViewById(R.id.buttonRecord);

        recordButton.setOnClickListener(view -> {
            startRecording();
        });

        initChart();
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
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        textViewXValue.setText(String.format("%.3f", x));
        textViewYValue.setText(String.format("%.3f", y));
        textViewZValue.setText(String.format("%.3f", z));

        if (isRecording) {
            xData.add(new Entry(sampleNo, x));
            yData.add(new Entry(sampleNo, y));
            zData.add(new Entry(sampleNo, z));

            sampleNo++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private void clearRecordedData() {
        xData.clear();
        yData.clear();
        zData.clear();
        sampleNo = 0;
    }

    private void drawChart() {
        LineDataSet xDataSet = new LineDataSet(xData, "X");
        LineDataSet yDataSet = new LineDataSet(yData, "Y");
        LineDataSet zDataSet = new LineDataSet(zData, "Z");

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
        clearRecordedData();
        isRecording = true;
        recordButton.setEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    stopRecording();
                    drawChart();
                    recordButton.setEnabled(true);
                });
            }
        }, motionDurationMs);
    }

    private void stopRecording() {
        isRecording = false;
    }

    private void initChart() {

        mainChart.setDrawGridBackground(false);
        mainChart.setNoDataText("Brak wykresu");
        mainChart.getAxisRight().setEnabled(false);
        mainChart.setVisibleYRange(minY, maxY, YAxis.AxisDependency.LEFT);
        mainChart.setAutoScaleMinMaxEnabled(false);
        mainChart.getAxisLeft().setAxisMinimum(minY);
        mainChart.getAxisLeft().setAxisMaximum(maxY);

        mainChart.setContentDescription("");

        XAxis xaxis = mainChart.getXAxis();
        YAxis yaxis = mainChart.getAxisLeft();

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


}