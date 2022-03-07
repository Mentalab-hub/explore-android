package com.mentalab.commandtranslators;

public class ModuleEnableTranslator extends twoByteCommandTranslator {


    public ModuleEnableTranslator(int opcode, int argument) {
        super(opcode, argument);
    }


    @Override
    public byte[] translateCommand() {
        return convertIntegerToByteArray();
    }
}
