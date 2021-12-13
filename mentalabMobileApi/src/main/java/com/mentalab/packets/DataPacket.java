package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Interface for different EEG packets
 */
abstract class DataPacket extends Packet implements PublishablePacket {
    private static final String TAG = "Explore";
    private static byte channelMask;
    public ArrayList<Float> convertedSamples;

    public DataPacket(double timeStamp) {
        super(timeStamp);
    }

    static double[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
        if (byteArray.length % 3 != 0)
            throw new InvalidDataException("Byte buffer is not read properly", null);
        int arraySize = byteArray.length / 3;
        double[] values = new double[arraySize];

        for (int index = 0; index < byteArray.length; index += 3) {
            if (index == 0) {
                channelMask = byteArray[index];
            }
            int signBit = byteArray[index + 2] >> 7;
            double value;
            if (signBit == 0)
                value =
                        ByteBuffer.wrap(
                                        new byte[]{byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
            else {
                int twosComplimentValue =
                        ByteBuffer.wrap(
                                        new byte[]{byteArray[index], byteArray[index + 1], byteArray[index + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                value = -1 * (Math.pow(2, 24) - twosComplimentValue);
            }
            values[index / 3] = value;
        }

        return values;
    }

    public static byte getChannelMask() {
        return channelMask;
    }

    public static void setChannelMask(byte channelMask) {
        DataPacket.channelMask = channelMask;
    }

    public ArrayList<Float> getData() {
        return convertedSamples;
    }

    @Override
    public String getPacketTopic() {
        return "ExG";
    }
}
