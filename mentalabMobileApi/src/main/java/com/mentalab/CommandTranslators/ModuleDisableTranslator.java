package com.mentalab.CommandTranslators;

public class ModuleDisableTranslator extends twoByteCommandTranslator {

  public ModuleDisableTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();
  }
}
