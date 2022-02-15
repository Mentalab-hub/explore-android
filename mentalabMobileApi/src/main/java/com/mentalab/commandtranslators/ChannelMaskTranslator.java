package com.mentalab.commandtranslators;

public class ChannelMaskTranslator extends twoByteCommandTranslator {

  public ChannelMaskTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand() {
    return convertIntegerToByteArray();
  }
}
