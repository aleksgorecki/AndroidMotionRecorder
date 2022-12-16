package com.example.motiontest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class MotionBuffer {
    private EvictingQueue<double[]> memory;
    private ArrayList<double[]> recording;

    public MotionBuffer(int memorySize) {
        memory = new EvictingQueue<>(memorySize);
        recording = new ArrayList<>();
    }

    public void addToMemory(double[] sample) {
        memory.add(sample);
    }

    public void recordSample(double[] sample) {
        recording.add(sample);
    }

    public void clear() {
        memory.clear();
        recording.clear();
    }

    public Motion getMotion() {
        ArrayList<double[]> combined = new ArrayList<>(memory);
        combined.addAll(recording);
        return new Motion(combined);
    }

    public boolean isEmpty() {
        return (memory.isEmpty() && recording.isEmpty());
    }
}
