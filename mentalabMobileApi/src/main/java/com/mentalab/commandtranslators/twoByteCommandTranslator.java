package com.mentalab.commandtranslators;

import java.sql.Timestamp;

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
