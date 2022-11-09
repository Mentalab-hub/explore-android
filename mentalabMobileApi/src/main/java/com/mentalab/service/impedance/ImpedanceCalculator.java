package com.mentalab.service.impedance;

import android.util.Log;
import com.github.psambit9791.jdsp.filter.Butterworth;
import com.mentalab.ExploreDevice;
import com.mentalab.utils.ButterworthFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

  public ImpedanceCalculator() {
    this.slope = 223660;
    this.offset = 42.218;
    this.channelCount = 4;
    this.butterworthFilter = new ButterworthFilter(250);
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

  private double[] transpose(List<Float> data) {
    int count = 0;
    final double[] doubleArray = new double[data.size()];
    for (int i = 0; i < channelCount; i++) {
      for (int j = i; j < data.size(); j += channelCount) {
        doubleArray[count] =  new BigDecimal(data.get(j)).setScale(2, RoundingMode.HALF_UP).doubleValue();
        count++;
      }
    }
    return doubleArray;
  }

  public List<Float> calculate2(List<Float> data) {
    Butterworth butterNotch = new Butterworth(250);

    double[] transposedData = transpose(data);
    // for bandstop we need to iterate over every channel data
    double[] notchedValues = applyFilter(butterNotch, transposedData, false, 48, 52);
    Butterworth butterNoise = new Butterworth(250);
    double[] noisedata = applyFilter(butterNoise, notchedValues, true, 65, 68);
    final double[] noiseLevel = getPeakToPeak(noisedata);
    Butterworth butterBandpass = new Butterworth(250);
    double[] impedanceSignal = applyFilter(butterBandpass, notchedValues, true, 61, 64);
    final double[] impPeakToPeak = getPeakToPeak(impedanceSignal);
    final double[] impedanceValue = calculateImpedance(impPeakToPeak, noiseLevel);
    return toFloatList(impedanceValue);
  }

  public double[] getPeakToPeak(double[] values) {
    int sampleNumbers = values.length / channelCount;
    double[] peakToPeakValues = new double[channelCount];

    for (int i = 0; i < channelCount; i++) {

      final double[] sorted =
          Arrays.stream(Arrays.copyOfRange(values, i * sampleNumbers, i * sampleNumbers + sampleNumbers))
              .sorted()
              .toArray();
      peakToPeakValues[i] = sorted[sorted.length - 1] - sorted[0];
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
    Log.d("HELLO__IMP", "");
    return result;
  }

  public double[] applyFilter(Butterworth filter, double[] data, boolean isBandpass, double lc, double hc){

    int sampleNumbers = data.length / channelCount;
    double[] result = new double[data.length];
    for(int i = 0; i < channelCount; i++)
    {
      double[] channelData = Arrays.stream(Arrays.copyOfRange(data,  i * sampleNumbers, i * sampleNumbers + sampleNumbers)).toArray();
      double[] channelFiltered;
      if(isBandpass){
         channelFiltered = filter.bandPassFilter(channelData, 5, lc, hc);
      }else{
        channelFiltered = filter.bandStopFilter(channelData, 5, lc, hc);
      }
      System.arraycopy(channelFiltered, 0, result, i * sampleNumbers, channelFiltered.length);
    }
    return result;
    }
}
