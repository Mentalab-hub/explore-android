package com.mentalab;

import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;

interface CommandByteLength {
  int TWO_BYTE_COMMAND = 0xA0, FOUR_BYTE_COMMAND = 0xB0;
}

abstract class CommandTranslator {
  int pId;
  int count = 0x00;
  int hostTimestamp;
  int opcode;
  int extraArgument;
  int payload;
  int dataLength;
  int commandLength;

  int[] fletcherBytes = new int[] {0xAF, 0xBE, 0xAD, 0xDE};

  abstract byte[] translateCommand(int arguments);

  byte[] convertIntegerToByteArray() {
    byte[] convertedData = new byte[dataLength];
    convertedData[0] = (byte) pId;
    Log.d("DEBUG_SR", "converted data is pId: " + pId + "converted is " + convertedData[0]);
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

    convertedData[index++] = (byte) extraArgument;
    Log.d("DEBUG_SR", "Index is: " + index);
    for (int fletcherArrayIndex = 0; fletcherArrayIndex < 4; fletcherArrayIndex++) {
      convertedData[fletcherArrayIndex + index] = (byte) fletcherBytes[fletcherArrayIndex];
    }

    return convertedData;
  }
}

abstract class twoByteCommandTranslator extends CommandTranslator {

  public twoByteCommandTranslator(int opCode, int argument) {
    pId = CommandByteLength.TWO_BYTE_COMMAND;
    payload = 10;
    hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
    opcode = opCode;
    extraArgument = argument;
    dataLength = 14;
  }
}

abstract class fourByteCommandTranslator extends CommandTranslator {

  public fourByteCommandTranslator(int opcode, int argument) {
    pId = CommandByteLength.FOUR_BYTE_COMMAND;
    payload = 12;
    hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
  }
}

class SamplingRateTranslator extends twoByteCommandTranslator {

  public SamplingRateTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();

    // return new int[]{this.pId, this.count,
  }
}
