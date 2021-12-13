package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.Topic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public abstract class EEGPacket extends Packet implements PublishablePacket {

    public ArrayList<Float> convertedSamples;
    private static final int BUFFER_LENGTH = 3; // TODO: Why is this the case?

    public EEGPacket(double timeStamp) {
        super(timeStamp);
    }


    static double[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
        if (byteArray.length % BUFFER_LENGTH != 0) {
            throw new InvalidDataException("Byte buffer is not read properly", null);
        }

        int arraySize = byteArray.length / BUFFER_LENGTH;
        double[] values = new double[arraySize];

        for (int i = 0; i < byteArray.length; i += 3) {
            if (i == 0) {
                byte channelMask = byteArray[i]; // TODO: Why is this here?
            }

            int signBit = byteArray[i + 2] >> 7;
            double value;
            if (signBit == 0)
                value =
                        ByteBuffer.wrap(
                                        new byte[]{byteArray[i], byteArray[i + 1], byteArray[i + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
            else {
                int twosComplimentValue =
                        ByteBuffer.wrap(
                                        new byte[]{byteArray[i], byteArray[i + 1], byteArray[i + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                value = -1 * (Math.pow(2, 24) - twosComplimentValue);
            }
            values[i / 3] = value;
        }

        return values;
    }


    public ArrayList<Float> getData() {
        return convertedSamples;
    }


    @Override
    public Topic getTopic() {
        return Topic.EXG;
    }
}
