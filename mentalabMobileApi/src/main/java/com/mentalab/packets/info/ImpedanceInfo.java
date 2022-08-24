package com.mentalab.packets.info;

import static com.mentalab.packets.Attributes.OFFSET;
import static com.mentalab.packets.Attributes.SLOPE;

import android.util.Log;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumSet;

public class ImpedanceInfo extends InfoPacket implements Publishable {

  private float slope;
  private double offset;

  public ImpedanceInfo(double timeStamp) {
    super(timeStamp);
    super.attributes = EnumSet.of(SLOPE, OFFSET);
  }

  /**
   * Converts binary data stream to human-readable voltage values.
   *
   * @param byteBuffer
   */
  @Override
  public void convertData(byte[] byteBuffer) {

    slope =
        ByteBuffer.wrap(new byte[] {byteBuffer[0], byteBuffer[1], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 10;
    offset =
        ByteBuffer.wrap(new byte[] {byteBuffer[2], byteBuffer[3], 0, 0})
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt()
            * 0.001;
    super.data = new ArrayList<>();
    data.add(slope);
    data.add((float)offset);
    Log.d("IMPEDANCE", "");
  }

  public float getSlope() {
    return slope;
  }

  public double getOffset() {
    return offset;
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
