package com.mentalab.commandtranslators;

public class SamplingRateCommandTranslator extends twoByteCommandTranslator {

  public SamplingRateCommandTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand() {
    return convertIntegerToByteArray();

    // return new int[]{this.pId, this.count,
  }
}
