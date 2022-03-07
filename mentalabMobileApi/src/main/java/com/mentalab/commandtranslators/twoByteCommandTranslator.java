package com.mentalab.commandtranslators;

import java.sql.Timestamp;

abstract class twoByteCommandTranslator extends CommandTranslator {


    public twoByteCommandTranslator(int opCode, int argument) {
        this.pId = CommandByteLength.TWO_BYTE_COMMAND;
        this.payload = 10;
        this.hostTimestamp = new Timestamp(System.currentTimeMillis()).getNanos();
        this.opcode = opCode;
        this.arg = argument;
        this.dataLength = 14;
    }
}
