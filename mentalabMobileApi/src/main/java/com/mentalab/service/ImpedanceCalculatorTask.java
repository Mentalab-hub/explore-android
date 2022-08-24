package com.mentalab.service;

import com.mentalab.ExploreDevice;
import com.mentalab.packets.Packet;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.ButterworthFilter;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ImpedanceCalculatorTask implements Callable<Boolean> {

  private final ButterworthFilter butterworthFilter;
  private final double slope;
  private final double offset;
  private final int channelCount;
  private Subscriber<EEGPacket> impedanceSubscriber;

  public ImpedanceCalculatorTask(ExploreDevice device) {
    this.slope = device.getSlope();
    this.offset = device.getOffset();
    this.channelCount = device.getChannelCount().getAsInt();
    this.butterworthFilter = new ButterworthFilter(device.getSamplingRate().getAsInt());
  }

  @Override
  public Boolean call() throws ArithmeticException {
    if (slope == 0) {
      throw new ArithmeticException(
          "Cannot proceed with impedance calculation. Zero slope assigned.");
    }
    this.impedanceSubscriber = new ImpedanceSubscriber(Topic.EXG);
    ContentServer.getInstance().registerSubscriber(impedanceSubscriber);
    return true;
  }

  public void cancelTask() {
    ContentServer.getInstance().deRegisterSubscriber(impedanceSubscriber);
    this.impedanceSubscriber = null;
  }

  private class ImpedanceSubscriber extends Subscriber<EEGPacket> {

    public ImpedanceSubscriber(Topic t) {
      super(t);
    }

    @Override
    public void accept(Packet packet) {
      final List<Float> rawData = packet.getData();
      final double[] denoisedData = denoise(toDoubleArray(rawData));
      final Packet impedancePacket = updatePacket(packet, toFloatList(denoisedData));
      publishImpedanceValues(impedancePacket);
    }

    private double[] denoise(double[] data) {
      final double[] notchedValues = butterworthFilter.bandStopFilter(data);
      final double[] values = butterworthFilter.bandPassFilter(notchedValues, false);
      final double[] noiseLevel = getPeakToPeak(values);
      final double[] bandpassedValues = butterworthFilter.bandPassFilter(notchedValues, true);
      return calculateImpedance(getPeakToPeak(bandpassedValues), noiseLevel);
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

    public double[] toDoubleArray(List<Float> floats) {
      final double[] doubleArray = new double[floats.size()];
      for (int i = 0; i < floats.size(); i++) {
        doubleArray[i] = floats.get(i).doubleValue();
      }
      return doubleArray;
    }

    public List<Float> toFloatList(double[] doubles) {
      ArrayList<Float> list = new ArrayList<>();
      for (double v : doubles) {
        list.add((float) v);
      }
      return list;
    }

    private Packet updatePacket(Packet packet, List<Float> formatted) {
      packet.getData().clear();
      packet.data = formatted;
      return packet;
    }

    private void publishImpedanceValues(Packet packet) {
      ContentServer.getInstance().publish(Topic.IMPEDANCE, packet);
    }
  }
}
