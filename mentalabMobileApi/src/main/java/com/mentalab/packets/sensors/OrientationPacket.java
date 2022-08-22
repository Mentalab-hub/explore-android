package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.mentalab.packets.PacketDataType.ACCX;
import static com.mentalab.packets.PacketDataType.GYROZ;

public class OrientationPacket extends Packet implements Publishable {

  public OrientationPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.range(ACCX, GYROZ);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final List<Float> values = new ArrayList<>();
    final double[] val = PacketUtils.bytesToDoubles(byteBuffer);
    for (int i = 0; i < val.length; i++) {
      if (i < 3) {
        values.add((float) (val[i] * 0.061));
      } else if (i < 6) {
        values.add((float) (val[i] * 8.750));
      } else if (i == 6) {
        values.add((float) (val[i] * 1.52 * -1));
      } else {
        values.add((float) (val[i] * 1.52));
      }
    }
    super.data = new ArrayList<>(values);
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: Orientation";
  }

  @Override
  public int getDataCount() {
    return super.type.size();
  }

  @Override
  public Topic getTopic() {
    return Topic.ORN;
  }
}
