package com.mentalab;


import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

interface CommandByteLength{
  int TWO_BYTE_COMMAND = 0xA0,
  FOUR_BYTE_COMMAND = 0xB0;
}



abstract class CommandTranslator {
  int pId;
  int count = 0x00;
  int hostTimestamp;
  int opcode;
  int extraArgument;
  int[] payload;
  int dataLength;
  int commandLength;

  int[] fletcherBytes = new int[] {0xaf, 0xbe, 0xad, 0xde};

  abstract byte[] translateCommand(int arguments);

  byte[] convertIntegerToByteArray(){
    byte[] convertedData = new byte[dataLength];
//    for(int index = 0; index < dataLength; index++){
//      convertedData
//    }
    convertedData[0] = (byte) pId;
    Log.d("DEBUG_SR", "converted data is pId: "+ pId+ "converted is " + convertedData[0]);
    convertedData[1] = (byte) count;
    ByteBuffer timestampBuffer = ByteBuffer.allocate(2 * commandLength);
    timestampBuffer.order(ByteOrder.LITTLE_ENDIAN);
    timestampBuffer.putInt(hostTimestamp);
    byte[] byteArray = timestampBuffer.array();
    int index;
    for(index = 0; index < 2 * commandLength; index ++){
      convertedData[index + 2] = byteArray[index];
    }

    index = index +1;

    convertedData[index] = (byte) opcode;

    convertedData[index + 1] = (byte) extraArgument;

    int fletcherArrayIndex = 0;
    for(index = dataLength -4; index <commandLength; index++){
      convertedData[index] = (byte) fletcherBytes[fletcherArrayIndex];
      fletcherArrayIndex++;
    }

    return convertedData;
  }
}

abstract class twoByteCommandTranslator extends CommandTranslator {

  public twoByteCommandTranslator(int opCode, int argument) {
    pId = CommandByteLength.TWO_BYTE_COMMAND;
    payload = new int[]{0x10, 0x00};
    hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
    opcode = opcode;
    extraArgument = argument;
    dataLength = 14;

    commandLength = 2;
  }
}

abstract class fourByteCommandTranslator extends CommandTranslator {

  public fourByteCommandTranslator(int opcode, int argument) {
    pId= CommandByteLength.FOUR_BYTE_COMMAND;
    payload = new int[]{0x12, 0x00};
    hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
  }
}

class SamplingRateTranslator extends twoByteCommandTranslator{

  public SamplingRateTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();


    //return new int[]{this.pId, this.count,
    }
  }