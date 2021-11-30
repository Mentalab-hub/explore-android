package com.mentalab.CommandTranslators;

import java.sql.Timestamp;

abstract class fourByteCommandTranslator extends CommandTranslator {

  public fourByteCommandTranslator(int opcode, int argument) {
    pId = CommandByteLength.FOUR_BYTE_COMMAND;
    payload = 12;
    hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
  }
}
