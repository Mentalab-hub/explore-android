package com.mentalab.packets.sensors.exg;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class EEGPacket extends Packet implements Publishable {

  private final int channelNumber;

  public EEGPacket(double timeStamp, int channelNumber) {
    super(timeStamp);
    this.channelNumber = channelNumber;
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException, IOException {
    final List<Float> array = new ArrayList<>();
    double[] data = PacketUtils.bytesToInt32s(byteBuffer);
    super.data = updateArrayWithData(array, data);
  }

  private List<Float> updateArrayWithData(List<Float> values, double[] data) {
    for (int i = 0; i < data.length; i++) {
      if (i % (channelNumber + 1) == 0) {
        continue; // skip int representation of status bit
      }
      values.add(adjustGain(data[i]));
    }
    return values;
  }

  private static Float adjustGain(double datum) {
    final double exgUnit = Math.pow(10, -6);
    final double vRef = 2.4;
    final double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
    return (float) (datum * (vRef / gain));
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: ExG";
  }

  @Override
  public int getDataCount() {
    return this.channelNumber;
  }

  @Override
  public Topic getTopic() {
    return Topic.EXG;
  }
}
