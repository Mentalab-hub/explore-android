package com.mentalab.packets;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Packet {

  private final double timeStamp;

  public Set<Attributes> attributes;
  public List<Float> data = new ArrayList<>();

  protected Packet(double timeStamp) {
    this.timeStamp = timeStamp;
  }

  protected static double[] bytesToDouble(byte[] bytes, int numOfbytesPerNumber)
      throws InvalidDataException { // TODO: IntelliJ suggests the second parameter is always 2
    if (bytes.length % numOfbytesPerNumber != 0) {
      throw new InvalidDataException("Illegal length", null);
    }

    int arraySize = bytes.length / numOfbytesPerNumber;
    double[] values = new double[arraySize];
    for (int i = 0; i < bytes.length; i += numOfbytesPerNumber) {
      int signBit = bytes[i + numOfbytesPerNumber - 1] >> 7;
      double value;

      value =
          ByteBuffer.wrap(new byte[]{bytes[i], bytes[i + 1]})
              .order(ByteOrder.LITTLE_ENDIAN)
              .getShort();
      if (signBit == 1) { // TODO: IntelliJ suggests this IF statement is redundant...
        value = -1 * (Math.pow(2, 8 * numOfbytesPerNumber) - value);
      }

      values[i / numOfbytesPerNumber] = value;
    }
    return values;
  }

  public double getTimeStamp() {
    return timeStamp;
  }

  /**
   * Converts binary data stream to human-readable voltage values.
   *
   * @param byteBuffer
   */
  public abstract void convertData(byte[] byteBuffer) throws InvalidDataException;

  /**
   * String representation of attributes
   */
  @NonNull
  public abstract String toString();

  /**
   * Number of elements in each packet
   */
  public int getDataCount() {
    return this.data.size();
  }

  /**
   * Get data values from packet structure
   */
  public List<Float> getData() {
    return this.data;
  }
}
