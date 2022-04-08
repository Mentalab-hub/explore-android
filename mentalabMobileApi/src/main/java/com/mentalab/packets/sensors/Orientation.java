package com.mentalab.packets.sensors;

import android.util.Log;
import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.mentalab.packets.Attributes.*;

public class Orientation extends PublishablePacket {

  final ArrayList<Float> values = new ArrayList<>();

  public Orientation(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.range(ACCX, GYROZ);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final double[] convertedRawValues = bytesToDouble(byteBuffer, 2);

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
    Log.d(Utils.TAG, "Converted samples in the packets are: " + super.data);
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder data = new StringBuilder("Orientation packets: [");
    for (int i = 0; i < this.data.size(); i++) {
      final float sample = super.data.get(i);
      if (i % 9 < 3) {
        data.append(" accelerometer: ").append(sample);
      } else if (i % 9 < 6) {
        data.append(" magnetometer: ").append(sample);
      } else {
        data.append("gyroscope: ").append(sample);
      }
      data.append(",");
    }
    data.append("]");
    return data.toString();
  }

  @Override
  public int getDataCount() {
    return super.attributes.size();
  }

  @Override
  public List<Float> getData() {
    return super.data;
  }

  @Override
  public Topic getTopic() {
    return Topic.ORN;
  }
}
