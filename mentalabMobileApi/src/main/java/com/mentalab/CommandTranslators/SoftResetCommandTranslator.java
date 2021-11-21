package com.mentalab.CommandTranslators;

public class SoftResetCommandTranslator extends twoByteCommandTranslator {

  public SoftResetCommandTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();

    // return new int[]{this.pId, this.count,
  }
}
