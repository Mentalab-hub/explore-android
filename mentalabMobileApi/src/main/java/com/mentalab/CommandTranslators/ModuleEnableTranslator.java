package com.mentalab.CommandTranslators;

public class ModuleEnableTranslator extends twoByteCommandTranslator {

  public ModuleEnableTranslator(int opcode, int argument) {
    super(opcode, argument);
  }

  @Override
  public byte[] translateCommand(int argument) {
    return convertIntegerToByteArray();

    // return new int[]{this.pId, this.count,
  }
}
