package com.mentalab.commandtranslators;

public class ModuleDisableTranslator extends twoByteCommandTranslator {


    public ModuleDisableTranslator(int opcode, int argument) {
        super(opcode, argument);
    }


    @Override
    public byte[] translateCommand() {
        return convertIntegerToByteArray();
    }
}
