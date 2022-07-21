package com.mentalab.packets.info;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CalibrationInfo extends InfoPacket implements Publishable {

  public CalibrationInfo(double timeStamp) {
    super(timeStamp);
  }

  /**
   * Converts binary data stream to human-readable voltage values.
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    int slope =
        ByteBuffer.wrap(new byte[] {byteBuffer[1], byteBuffer[2], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 10;
    double offset =
        ByteBuffer.wrap(new byte[] {byteBuffer[3], byteBuffer[4], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 0.001;
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
