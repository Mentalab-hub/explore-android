package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class CommandStatusPacket extends UtilPacket {
    boolean commandStatus;

    public CommandStatusPacket(double timeStamp) {
        super(timeStamp);
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);
        short status =
                ByteBuffer.wrap(new byte[]{byteBuffer[5], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
        commandStatus = status != 0;
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Command status is " + commandStatus;
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 1;
    }

    @Override
    public String getPacketTopic() {
        return "Command";
    }
}
