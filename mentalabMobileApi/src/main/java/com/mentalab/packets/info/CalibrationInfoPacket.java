package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.PacketUtils;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

import java.util.EnumSet;

import static com.mentalab.packets.PacketDataType.OFFSET;
import static com.mentalab.packets.PacketDataType.SLOPE;

public class CalibrationInfoPacket extends Packet implements Publishable {

  private float slope;
  private double offset;

  public CalibrationInfoPacket(double timeStamp) {
    super(timeStamp);
    super.type = EnumSet.of(SLOPE, OFFSET);
  }

  /** Converts binary data stream to human-readable voltage values. */
  @Override
  public void populate(byte[] data) throws InvalidDataException {
    this.slope = PacketUtils.bytesToInt(data[0], data[1]) * 10;
    this.offset = PacketUtils.bytesToInt(data[2], data[3]) * 0.001;
  }

  public float getSlope() {
    return this.slope;
  }

  public double getOffset() {
    return this.offset;
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: CalibrationInfo";
  }

  @Override
  public Topic getTopic() {
    return Topic.DEVICE_INFO;
  }
}
