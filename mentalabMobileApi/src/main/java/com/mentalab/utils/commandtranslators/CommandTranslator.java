package com.mentalab.utils.commandtranslators;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class CommandTranslator {

  private static final int[] FLETCHER_BYTES = new int[] {0xAF, 0xBE, 0xAD, 0xDE};

  int pId;
  int count = 0x00;
  int hostTimestamp;
  int opcode;
  int arg;
  int payload;
  int dataLength;

  public abstract byte[] translateCommand();

  byte[] convertIntegerToByteArray() {
    byte[] convertedData = new byte[dataLength];
    convertedData[0] = (byte) pId;
    convertedData[1] = (byte) count;
    convertedData[2] = (byte) payload;
    convertedData[3] = (byte) 0;

    ByteBuffer timestampBuffer = ByteBuffer.allocate(4);
    timestampBuffer.order(ByteOrder.LITTLE_ENDIAN);
    timestampBuffer.putInt(hostTimestamp);
    byte[] byteArray = timestampBuffer.array();
    int index;
    for (index = 0; index < 4; index++) {
      convertedData[index + 4] = byteArray[index];
    }

    index = index + 4;

    convertedData[index++] = (byte) opcode;

    convertedData[index++] = (byte) arg;
    Log.d("DEBUG_SR", "Index is: " + index);
    for (int fletcherArrayIndex = 0; fletcherArrayIndex < 4; fletcherArrayIndex++) {
      convertedData[fletcherArrayIndex + index] = (byte) FLETCHER_BYTES[fletcherArrayIndex];
    }

    return convertedData;
  }
}
