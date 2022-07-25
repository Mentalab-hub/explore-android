package com.mentalab.service;

import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.packets.info.CalibrationInfo;
import com.mentalab.utils.ButterworthFilter;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ImpedanceCalculatorTask implements Callable<Boolean> {

  private final ExploreDevice device;
  private final ButterworthFilter butterworthFilter;

  private int nyquistFreq;
  private volatile double slope;
  private volatile double offset;

  private Subscriber impedanceSubscriber;
  private Subscriber calibrationInforSubscriber;

  public ImpedanceCalculatorTask(ExploreDevice device) {
    this.device = device;
    butterworthFilter = new ButterworthFilter(device.getSamplingRate().getAsInt());
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws Exception if unable to compute a result
   */
  @Override
  public Boolean call() throws Exception {
    getCalibrationInfo();
    calculate();
    return true;
  }

  public void calculate() {
    impedanceSubscriber =
        new Subscriber(Topic.EXG) {
          @Override
          public void accept(Packet packet) {
            double[] doubleArray = Utils.convertArraylistToDoubleArray(packet);
            double[] notchedValues = butterworthFilter.bandStopFilter(doubleArray);
            double[] nosieLevel =
                getPeakToPeak(butterworthFilter.bandPassFilter(notchedValues, false));
            double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
            double[] denoised = calculateImpedance(getPeakToPeak(bandpassedValues), nosieLevel);
          }
        };
    ContentServer.getInstance().registerSubscriber(impedanceSubscriber);
  }

  private double[] getPeakToPeak(double[] values) {
    int columnSize = values.length / device.getChannelCount();
    double[] peakToPeakValues = new double[columnSize];

    for (int i = 0; i < values.length - 1; i += columnSize) {
      double[] slice = Arrays.copyOfRange(values, i, i + columnSize - 1);
      Arrays.sort(slice);
      peakToPeakValues[i / columnSize] = slice[slice.length] - slice[0];
    }
    return peakToPeakValues;
  }

  void getCalibrationInfo() {
    calibrationInforSubscriber =
        new Subscriber(Topic.DEVICE_INFO) {
          @Override
          public void accept(Packet packet) {
            if (packet instanceof CalibrationInfo) {
              List<Float> data = ((CalibrationInfo) packet).getData();
              slope = data.get(0);
              offset = data.get(1);
            }
          }
        };
    ContentServer.getInstance().registerSubscriber(calibrationInforSubscriber);
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
    ContentServer.getInstance().deRegisterSubscriber(calibrationInforSubscriber);
  }
}
