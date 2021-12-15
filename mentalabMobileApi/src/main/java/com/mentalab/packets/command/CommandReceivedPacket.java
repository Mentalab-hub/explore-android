package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.Topic;

public class CommandReceivedPacket extends UtilPacket {

    float markerCode; // TODO: Why is this here?

    public CommandReceivedPacket(double timeStamp) {
        super(timeStamp);
    }


    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
    }


    @NonNull
    @Override
    public String toString() {
        return "Command received packet";
    }


    @Override
    public int getDataCount() {
        return 1;
    }


    @Override
    public Topic getTopic() {
        return Topic.COMMAND;
    }
}
