package com.mentalab.service.impedance;

import com.mentalab.ExploreDevice;
import com.mentalab.utils.ButterworthFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImpedanceCalculator {

  private final ButterworthFilter butterworthFilter;
  private final double slope;
  private final double offset;
  private final int channelCount;

  public ImpedanceCalculator(ExploreDevice device) {
    this.slope = device.getSlope();
    this.offset = device.getOffset();
    this.channelCount = device.getChannelCount().getAsInt();
    this.butterworthFilter = new ButterworthFilter(device.getSamplingRate().getAsInt());
  }

  public List<Float> calculate(List<Float> data) {
    final double[] notchedValues = butterworthFilter.bandStopFilter(toDoubleArray(data));
    final double[] values = butterworthFilter.bandPassFilter(notchedValues, false);
    final double[] noiseLevel = getPeakToPeak(values);
    final double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
    final double[] denoisedData = calculateImpedance(getPeakToPeak(bandpassedValues), noiseLevel);
    return toFloatList(denoisedData);
  }

  private double[] getPeakToPeak(double[] values) {
    int columnSize = values.length / channelCount;
    double[] peakToPeakValues = new double[channelCount];

    for (int i = 0; i < values.length - 1; i += columnSize) {
      double[] slice = Arrays.copyOfRange(values, i, i + columnSize);
      Arrays.sort(slice);
      peakToPeakValues[i / columnSize] = slice[slice.length - 1] - slice[0];
    }
    return peakToPeakValues;
  }

  private double[] calculateImpedance(double[] first, double[] second) {
    final int length = first.length;
    final double[] result = new double[length];
    for (int i = 0; i < length; i++) {
      double diff = first[i] - second[i];
      result[i] = diff * (slope / Math.pow(10, 6)) - offset;
    }
    return result;
  }

  private static double[] toDoubleArray(List<Float> floats) {
    final double[] doubleArray = new double[floats.size()];
    for (int i = 0; i < floats.size(); i++) {
      doubleArray[i] = floats.get(i).doubleValue();
    }
    return doubleArray;
  }

  private static List<Float> toFloatList(double[] doubles) {
    ArrayList<Float> list = new ArrayList<>();
    for (double v : doubles) {
      list.add((float) v);
    }
    return list;
  }
}
