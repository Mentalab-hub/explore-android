package com.mentalab.service;

import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.packets.Packet;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.ButterworthFilter;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ImpedanceCalculatorTask implements Callable<Boolean> {

  private final ExploreDevice device;
  private final ButterworthFilter butterworthFilter;

  private int nyquistFreq;
  private volatile double slope;
  private volatile double offset;
  private int channelCount;
  private Subscriber impedanceSubscriber;

  public ImpedanceCalculatorTask(ExploreDevice device) {
    this.device = device;
    this.slope = device.getSlope();
    this.offset = device.getOffset();
    channelCount = device.getChannelCount().getAsInt();
    butterworthFilter = new ButterworthFilter(device.getSamplingRate().getAsInt());
  }

  @Override
  public Boolean call() {
    calculate();
    return true;
  }

  public void calculate() {
    impedanceSubscriber =
        new Subscriber<Packet>(Topic.EXG) {
          @Override
          public void accept(Packet packet) {
            Log.d("IMPEDANCE", "===================================================== here");
            if (slope != 0);
            double[] doubleArray = Utils.convertArraylistToDoubleArray(packet);
            double[] notchedValues = butterworthFilter.bandStopFilter(doubleArray);
            final double[] values = butterworthFilter.bandPassFilter(notchedValues, false);
            double[] noiseLevel = getPeakToPeak(values);
            double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
            double[] denoised = calculateImpedance(getPeakToPeak(bandpassedValues), noiseLevel);
            packet.data.clear();
            packet.data = toFloatArray(denoised);
            publishImpedanceValues(packet);
          }
        };
    ContentServer.getInstance().registerSubscriber(impedanceSubscriber);
  }

  private ArrayList<Float> toFloatArray(double[] denoised) {
    ArrayList<Float> impedanceValues = new ArrayList<>();
    for (int i = 0; i < denoised.length; i++) {
      impedanceValues.add(new Float(denoised[i]));
    }
    return impedanceValues;
  }

  private double[] getPeakToPeak(double[] values) {
    int columnSize = values.length / channelCount;
    double[] peakToPeakValues = new double[columnSize];

    for (int i = 0; i < values.length - 1; i += columnSize) {
      double[] slice = Arrays.copyOfRange(values, i, i + columnSize);
      Arrays.sort(slice);
      peakToPeakValues[i / columnSize] = slice[slice.length - 1] - slice[0];
    }
    return peakToPeakValues;
  }

  double[] calculateImpedance(double[] first, double[] second) {
    int length = first.length;
    double[] result = new double[length];
    for (int i = 0; i < length; i++) {
      result[i] = first[i] - second[i];
      result[i] = (slope / Math.pow(10, 6)) - offset;
    }

    return result;
  }

  public void cancelTask() {
    ContentServer.getInstance().deRegisterSubscriber(impedanceSubscriber);
  }

  private void publishImpedanceValues(Packet packet) {
    ContentServer.getInstance().publish(Topic.IMPEDANCE, packet);
  }
}
