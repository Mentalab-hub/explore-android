package com.mentalab.service.impedance;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.mentalab.ExploreDevice;
import com.mentalab.utils.ButterworthFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  public List<Float> calculate(List<Float> data) {
    final double[] notchedValues = butterworthFilter.bandStopFilter(toDoubleArray(data));
    final double[] values = butterworthFilter.bandPassFilter(notchedValues, false);
    final double[] noiseLevel = getPeakToPeak(values);
    final double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
    final double[] denoisedData = calculateImpedance(getPeakToPeak(bandpassedValues), noiseLevel);
    return toFloatList(denoisedData);
  }

  public List<Float> calculate2(List<Float> data) {
    Butterworth butterNotch = new Butterworth(250);
    final double[] notchedValues  = butterNotch.bandStopFilter(toDoubleArray(data), 5, 48, 52);
    //final double[] notchedValues = butterworthFilter.bandStopFilter(toDoubleArray(data));
    Butterworth butterNoise = new Butterworth(250);
    final double[] values = butterNoise.bandPassFilter(notchedValues, 5, 65, 68);
    //final double[] values = butterworthFilter.bandPassFilter(notchedValues, false);
    final double[] noiseLevel = getPeakToPeak(values);
    Butterworth butterBandpass = new Butterworth(250);
    final double[] bandpassedValues = butterBandpass.bandPassFilter(notchedValues, 5, 61, 64);
    //final double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
    final double[] denoisedData = calculateImpedance(getPeakToPeak(bandpassedValues), noiseLevel);
    return toFloatList(denoisedData);
  }
  private double[] getPeakToPeak(double[] values) {
    int sampleNumbers = values.length / channelCount;
    double[] peakToPeakValues = new double[channelCount];

    for (int i = 0; i < channelCount; i++) {
      //double[] slice = Arrays.copyOfRange(values, i, i + channelCount);
      ArrayList<Float> slice = new ArrayList<>();
      for (int j = 0; j< values.length; j = j + channelCount){
        slice.add((float) values[j]);
      }
      Collections.sort(slice);
      peakToPeakValues[i] = slice.get(slice.size() - 1) - slice.get(0);
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
}
