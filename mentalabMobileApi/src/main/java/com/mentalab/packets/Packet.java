package com.mentalab.packets;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Packet {

  private static final int BYTES_PER_DOUBLE = 2;

  private final double timeStamp;

  public Set<Attributes> attributes;
  public List<Float> data = new ArrayList<>();

  protected Packet(double timeStamp) {
    this.timeStamp = timeStamp;
  }

  protected static double[] bytesToDouble(byte[] bytes) throws InvalidDataException {
    checkByteLength(bytes);

    final int arraySize = bytes.length / BYTES_PER_DOUBLE;
    final double[] values = new double[arraySize];
    return updateValues(bytes, values);
  }

  private static double[] updateValues(byte[] bytes, double[] values) {
    for (int i = 0; i < bytes.length; i += BYTES_PER_DOUBLE) {
      double value = parseByte(bytes, i);
      values[i / BYTES_PER_DOUBLE] = value;
    }
    return values;
  }

  protected static double parseByte(byte[] bytes, int i) {
    return ByteBuffer.wrap(new byte[] {bytes[i], bytes[i + 1]})
        .order(ByteOrder.LITTLE_ENDIAN)
        .getShort();
  }

  private static void checkByteLength(byte[] bytes) throws InvalidDataException {
    if (bytes.length % BYTES_PER_DOUBLE != 0) {
      throw new InvalidDataException("Illegal length", null);
    }
  }

  public double getTimeStamp() {
    return timeStamp;
  }

  /**
   * Converts binary data stream to human-readable voltage values.
   *
   * @param byteBuffer
   */
  public abstract void populate(byte[] byteBuffer) throws InvalidDataException;

  /** String representation of attributes */
  @NonNull
  public abstract String toString();

  /** Number of elements in each packet */
  public int getDataCount() {
    return this.data.size();
  }

  /** Get data values from packet structure */
  public List<Float> getData() {
    return this.data;
  }
}
