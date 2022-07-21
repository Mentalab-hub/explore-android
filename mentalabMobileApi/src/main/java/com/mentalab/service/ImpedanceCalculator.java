package com.mentalab.service;

import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.utils.ButterworthFilter;
import com.mentalab.utils.constants.Topic;
import java.util.List;

public class ImpedanceCalculator {

  private final ExploreDevice device;
  private final ButterworthFilter butterworthFilter;

  private int nyquistFreq;

  public ImpedanceCalculator(ExploreDevice device) {
    this.device = device;
    butterworthFilter = new ButterworthFilter(device.getSamplingRate().getAsInt());
  }

  public void calculate() {
    ContentServer.getInstance()
        .registerSubscriber(
            new Subscriber(Topic.EXG) {
              @Override
              public void accept(Packet packet) {
                double[] doubleArray = convertArraylistToDoubleArray(packet);
                double[] notchedValues = butterworthFilter.bandStopFilter(doubleArray);
              }
            });
  }

  double[] convertArraylistToDoubleArray(Packet packet) {
    List<Float> packetVoltageValues = packet.getData();
    double[] floatArray = new double[packetVoltageValues.size()];
    Object[] array = packetVoltageValues.toArray();
    for (int index = 0; index < packetVoltageValues.size(); index++) {
      floatArray[index] = packetVoltageValues.get(index).doubleValue();
    }
    return floatArray;
  }
}
