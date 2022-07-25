package com.mentalab.packets.info;

import static com.mentalab.packets.Attributes.OFFSET;
import static com.mentalab.packets.Attributes.SLOPE;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

public class CalibrationInfo extends InfoPacket implements Publishable {

  private float slope;
  private double offset;

  public CalibrationInfo(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.of(SLOPE, OFFSET);
  }

  /**
   * Converts binary data stream to human-readable voltage values.
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    slope =
        ByteBuffer.wrap(new byte[] {byteBuffer[1], byteBuffer[2], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 10;
    offset =
        ByteBuffer.wrap(new byte[] {byteBuffer[3], byteBuffer[4], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 0.001;
    super.data = new ArrayList<>(Arrays.asList((float) slope, (float) offset));
  }

  /** String representation of attributes */
  @Override
  public String toString() {
    return "CalibrationInfoPacket";
  }

  @Override
  public Topic getTopic() {
    return Topic.DEVICE_INFO;
  }
}
