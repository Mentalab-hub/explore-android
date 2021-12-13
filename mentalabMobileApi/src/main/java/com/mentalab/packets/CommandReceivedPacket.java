package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.Topic;

public class CommandReceivedPacket extends UtilPacket {
    float markerCode;

    public CommandReceivedPacket(double timeStamp) {
        super(timeStamp);
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Command received packet";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 1;
    }


    @Override
    public Topic getTopic() {
        return Topic.COMMAND;
    }
}
