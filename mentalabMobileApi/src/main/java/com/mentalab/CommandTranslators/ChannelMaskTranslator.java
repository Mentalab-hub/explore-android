package com.mentalab.CommandTranslators;

public class ChannelMaskTranslator extends twoByteCommandTranslator {

  public ChannelMaskTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();
  }
}
