package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.mentalab.packets.PacketDataType.BATTERY;
import static com.mentalab.packets.PacketDataType.TEMP;

public class EnvironmentPacket extends Packet {

  public EnvironmentPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.range(TEMP, BATTERY);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    final List<Float> vals = new ArrayList<>();
    vals.add((float) PacketUtils.bytesToInt(byteBuffer[0]));
    vals.add((float) PacketUtils.bytesToInt(byteBuffer[1], byteBuffer[2]) * (1000 / 4095));
    float batteryLevelRaw =
        (float) ((PacketUtils.bytesToInt(byteBuffer[3], byteBuffer[4]) * 16.8 / 6.8) * (1.8 / 2457));

    vals.add((float) getBatteryPercentage(batteryLevelRaw));
    super.data = new ArrayList<>(vals);
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: Environment";
  }

  @Override
  public int getDataCount() {
    return super.type.size();
  }

  private double getBatteryPercentage(float voltage) {
    if (voltage < 3.1) {
      return 1d;
    } else if (voltage < 3.5) {
      return 1 + (voltage - 3.1) / .4 * 10;
    } else if (voltage < 3.8) {
      return 10d + (voltage - 3.5) / .3 * 40d;
    } else if (voltage < 3.9) {
      return 40d + (voltage - 3.8) / .1 * 20d;
    } else if (voltage < 4) {
      return 60d + (voltage - 3.9) / .1 * 15d;
    } else if (voltage < 4.1) {
      return 75d + (voltage - 4.0) / .1 * 15d;
    } else if (voltage < 4.2) {
      return 90d + (voltage - 4.1) / .1 * 10d;
    } else {
      return 100d;
    }
  }
}
