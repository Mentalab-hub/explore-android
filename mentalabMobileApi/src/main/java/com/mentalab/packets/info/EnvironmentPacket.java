package com.mentalab.packets.info;

import static com.mentalab.packets.Attributes.BATTERY;
import static com.mentalab.packets.Attributes.TEMP;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class EnvironmentPacket extends InfoPacket {

  public EnvironmentPacket(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.range(TEMP, BATTERY);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final List<Float> vals = new ArrayList<>();
    vals.add(
        (float)
            ByteBuffer.wrap(new byte[]{byteBuffer[0], 0, 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt());
    vals.add(
        (float)
            (ByteBuffer.wrap(new byte[]{byteBuffer[1], byteBuffer[2], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt())
            * (1000 / 4095));
    float batteryLevelRaw =
        (float)
            ((ByteBuffer.wrap(new byte[]{byteBuffer[3], byteBuffer[4], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
                * 16.8
                / 6.8)
                * (1.8 / 2457));

    vals.add(getBatteryPercentage(batteryLevelRaw));
    super.data = new ArrayList<>(vals);
  }

  @NonNull
  @Override
  public String toString() {
    final StringBuilder data = new StringBuilder("Environment packets: [");
    for (int i = 0; i < super.data.size(); i++) {
      final float sample = super.data.get(i);
      if (i % 9 < 3) {
        data.append(" Temperature: ").append(sample);
      } else if (i % 9 < 6) {
        data.append(" Light: ").append(sample);
      } else {
        data.append("Battery: ").append(sample);
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

  private float getBatteryPercentage(float voltage) {
    double perc;
    if (voltage < 3.1) {
      perc = 1d;
    } else if (voltage < 3.5) {
      perc = 1d + (voltage - 3.1) / .4 * 10d;
    } else if (voltage < 3.8) {
      perc = 10d + (voltage - 3.5) / .3 * 40d;
    } else if (voltage < 3.9) {
      perc = 40d + (voltage - 3.8) / .1 * 20d;
    } else if (voltage < 4) {
      perc = 60d + (voltage - 3.9) / .1 * 15d;
    } else if (voltage < 4.1) {
      perc = 75d + (voltage - 4.0) / .1 * 15d;
    } else if (voltage < 4.2) {
      perc = 90d + (voltage - 4.1) / .1 * 10d;
    } else {
      perc = 100d;
    }

    return (float) perc;
  }
}
