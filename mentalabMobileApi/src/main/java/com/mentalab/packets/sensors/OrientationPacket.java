package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

import java.util.EnumSet;

import static com.mentalab.packets.PacketDataType.ACCX;
import static com.mentalab.packets.PacketDataType.GYROZ;

public class OrientationPacket extends Packet implements Publishable {

  public OrientationPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.range(ACCX, GYROZ);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final double[] val = PacketUtils.bytesToDoubles(byteBuffer);
    for (int i = 0; i < val.length; i++) {
      if (i < 3) {
        super.data.add((float) (val[i] * 0.061));
      } else if (i < 6) {
        super.data.add((float) (val[i] * 8.750));
      } else if (i == 6) {
        super.data.add((float) (val[i] * 1.52 * -1));
      } else {
        super.data.add((float) (val[i] * 1.52));
      }
    }
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
