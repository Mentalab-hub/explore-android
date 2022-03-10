package com.mentalab.packets.sensors;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Publishable;
import com.mentalab.packets.info.InfoPacket;
import com.mentalab.utils.constants.Topic;

import java.util.ArrayList;
import java.util.Arrays;

/** Device related information packet to transmit firmware version, ADC mask and sampling rate */
public class Orientation extends InfoPacket implements Publishable {

  final ArrayList<Float> listValues = new ArrayList<>();

  public Orientation(double timeStamp) {
    super(timeStamp);
    super.attributes =
        Arrays.asList(
            "Acc_X", "Acc_Y", "Acc_Z", "Mag_X", "Mag_Y", "Mag_Z", "Gyro_X", "Gyro_Y", "Gyro_Z");
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final double[] convertedRawValues = bytesToDouble(byteBuffer, 2);

    for (int i = 0; i < convertedRawValues.length; i++) {
      if (i < 3) {
        listValues.add((float) (convertedRawValues[i] * 0.061));
      } else if (i < 6) {
        listValues.add((float) (convertedRawValues[i] * 8.750));
      } else {
        if (i == 6) {
          listValues.add((float) (convertedRawValues[i] * 1.52 * -1));
        } else {
          listValues.add((float) (convertedRawValues[i] * 1.52));
        }
      }
    }
    super.convertedSamples = new ArrayList<>(listValues);
    Log.d("Explore", "Converted samples in the packets are: " + super.convertedSamples);
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder data = new StringBuilder("Orientation packets: [");
    for (int i = 0; i < convertedSamples.size(); i++) {
      final float sample = super.convertedSamples.get(i);
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
  public ArrayList<Float> getData() {
    return super.convertedSamples;
  }

  @Override
  public Topic getTopic() {
    return Topic.ORN;
  }
}
