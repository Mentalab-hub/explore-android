package com.mentalab.service;

import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.utils.ButterworthFilter;
import com.mentalab.utils.constants.Topic;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ImpedanceCalculator implements Callable<Boolean> {

  private final ExploreDevice device;
  private final ButterworthFilter butterworthFilter;

  private int nyquistFreq;

  public ImpedanceCalculator(ExploreDevice device) {
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
    calculate();
    return true;
  }

  public void calculate() {
    ContentServer.getInstance()
        .registerSubscriber(
            new Subscriber(Topic.EXG) {
              @Override
              public void accept(Packet packet) {
                double[] doubleArray = convertArraylistToDoubleArray(packet);
                double[] notchedValues = butterworthFilter.bandStopFilter(doubleArray);
                double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues);
              }
            });
  }

  private double[] convertArraylistToDoubleArray(Packet packet) {
    List<Float> packetVoltageValues = packet.getData();
    double[] floatArray = new double[packetVoltageValues.size()];
    Object[] array = packetVoltageValues.toArray();
    for (int index = 0; index < packetVoltageValues.size(); index++) {
      floatArray[index] = packetVoltageValues.get(index).doubleValue();
    }
    return floatArray;
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
}
