package com.mentalab.packets.info;

import com.mentalab.packets.Publishable;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

import java.util.EnumSet;

import static com.mentalab.packets.Attributes.OFFSET;
import static com.mentalab.packets.Attributes.SLOPE;

public class CalibrationInfo extends InfoPacket implements Publishable {

  private float slope;
  private double offset;

  public CalibrationInfo(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.of(SLOPE, OFFSET);
  }

  /** Converts binary data stream to human-readable voltage values. */
  @Override
  public void convertData(byte[] byteBuffer) {
    this.slope = Utils.bitsToInt(byteBuffer[0], byteBuffer[1]) * 10;
    this.offset = Utils.bitsToInt(byteBuffer[2], byteBuffer[3]) * 0.001;
  }

  public float getSlope() {
    return this.slope;
  }

  public double getOffset() {
    return this.offset;
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "PACKET: CalibrationInfo";
  }

  @Override
  public Topic getTopic() {
    return Topic.DEVICE_INFO;
  }
}
