package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public abstract class Packet {

  private final byte[] byteBuffer = null;
  private final double timeStamp;

  private int dataCount;


  protected Packet(double timeStamp) {
    this.timeStamp = timeStamp;
  }


  static double[] bytesToDouble(byte[] bytes, int numOfbytesPerNumber) throws InvalidDataException {
    if (bytes.length % numOfbytesPerNumber != 0) {
      throw new InvalidDataException("Illegal length", null);
    }

    int arraySize = bytes.length / numOfbytesPerNumber;
    double[] values = new double[arraySize];
    for (int index = 0; index < bytes.length; index += numOfbytesPerNumber) {
      int signBit = bytes[index + numOfbytesPerNumber - 1] >> 7;
      double value;

      value =
          ByteBuffer.wrap(new byte[] {bytes[index], bytes[index + 1]})
              .order(ByteOrder.LITTLE_ENDIAN)
              .getShort();
      if (signBit == 1) {
        value = -1 * (Math.pow(2, 8 * numOfbytesPerNumber) - value);
      }

      values[index / numOfbytesPerNumber] = value;
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


  /** String representation of attributes */
  public abstract String toString();


  /** Number of element in each packet */
  public abstract int getDataCount();


  /** Get data values from packet structure */
  public ArrayList<Float> getData() {
    return null;
  }
}

