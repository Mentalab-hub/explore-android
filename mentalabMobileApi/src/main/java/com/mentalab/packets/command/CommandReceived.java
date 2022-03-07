package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.utils.constants.Topic;

public class CommandReceived extends UtilPacket {

    float markerCode;

    public CommandReceived(double timeStamp) {
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
