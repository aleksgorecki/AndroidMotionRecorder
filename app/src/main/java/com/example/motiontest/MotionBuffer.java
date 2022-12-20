package com.example.motiontest;

import java.util.ArrayList;

public class MotionBuffer {
    private EvictingQueue<float[]> memory;
    private ArrayList<float[]> recording;

    public MotionBuffer(int memorySize) {
        memory = new EvictingQueue<>(memorySize);
        recording = new ArrayList<>();
    }

    public void addToMemory(float[] sample) {
        memory.add(sample);
    }

    public void recordSample(float[] sample) {
        recording.add(sample);
    }

    public void clear() {
        memory.clear();
        recording.clear();
    }

    public Motion getMotion() {
        ArrayList<float[]> combined = new ArrayList<>(memory);
        combined.addAll(recording);
        return new Motion(combined.toArray(new float[combined.size()][3]), combined.size());
    }

    public boolean isEmpty() {
        return (memory.isEmpty() && recording.isEmpty());
    }
}
