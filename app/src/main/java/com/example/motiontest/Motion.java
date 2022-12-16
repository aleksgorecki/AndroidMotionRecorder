package com.example.motiontest;

import java.util.ArrayList;

public class Motion {
    private double[][] recordedSamples;
    private int numSamples;

    public Motion(double[][] recordedSamples) {
        this.recordedSamples = recordedSamples;
    }

    public Motion(ArrayList<double[]> recordedSamples) {
        this.recordedSamples = recordedSamples.toArray(new double[recordedSamples.size()][3]);
        this.numSamples = recordedSamples.size();
    }

    public int getGlobalExtremumPosition() {
        int extremumPosition = 0;
        double extremumValue = 0;
        for (int i = 0; i < numSamples; i++) {
            double x = recordedSamples[i][0];
            double y = recordedSamples[i][1];
            double z = recordedSamples[i][2];
            if (x > extremumValue) {
                extremumPosition = i;
                extremumValue = x;
            }
            if (y > extremumValue) {
                extremumPosition = i;
                extremumValue = y;
            }
            if (z > extremumValue) {
                extremumPosition = i;
                extremumValue = z;
            }
        }
        return extremumPosition;
    }

    public double[][][] getThreeChannelTimeseries() {
        double[][][] multichannelArray = new double[1][numSamples][3];
        multichannelArray[0] = recordedSamples;
        return multichannelArray;
    }

    public void crop(int centerPosition, int halfSpan) {
        int lowerBound = centerPosition - halfSpan;
        int lowerPadding = 0;
        if (lowerBound < 0) {
            lowerPadding = Math.abs(lowerBound);
            lowerBound = 0;
        }
        int upperBound = centerPosition + halfSpan;
        int fullAxisLength = numSamples;
        int upperPadding = 0;
        if (upperBound > fullAxisLength) {
            upperPadding = upperBound - fullAxisLength;
            upperBound = fullAxisLength;
        }

        double[][] cropped = new double[2 * halfSpan][3];

        for (int i = 0; i < lowerPadding; i++) {
            cropped[i] = new double[] {0.0, 0.0, 0.0};
        }
        for (int i = lowerBound; i < upperBound; i++) {
            cropped[i] = recordedSamples[i];
        }
        for (int i = upperBound; i < upperPadding; i++) {
            cropped[i] = new double[] {0.0, 0.0, 0.0};
        }

        recordedSamples = cropped;
    }

    public int getNumSamples() {
        return numSamples;
    }
}
