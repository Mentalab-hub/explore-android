package com.mentalab.packets.sensors;

import static com.mentalab.packets.Attributes.ACCX;
import static com.mentalab.packets.Attributes.GYROZ;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.utils.constants.Topic;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Orientation extends PublishablePacket {

  final List<Float> values = new ArrayList<>();

  public Orientation(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.range(ACCX, GYROZ);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final double[] convertedRawValues = bytesToDouble(byteBuffer);

    for (int i = 0; i < convertedRawValues.length; i++) {
      if (i < 3) {
        values.add((float) (convertedRawValues[i] * 0.061));
      } else if (i < 6) {
        values.add((float) (convertedRawValues[i] * 8.750));
      } else if (i == 6) {
        values.add((float) (convertedRawValues[i] * 1.52 * -1));
      } else {
        values.add((float) (convertedRawValues[i] * 1.52));
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
    return super.attributes.size();
  }

  @Override
  public Topic getTopic() {
    return Topic.ORN;
  }
}
