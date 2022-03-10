package com.mentalab.packets.sensors.exg;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;
import com.mentalab.packets.Packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public abstract class EEGPacket extends Packet implements Publishable {

    private static final int BUFFER_LENGTH = 3; // EEG packets are 24 bits = 3 bytes
    private final int channelNumber;


    public EEGPacket(double timeStamp, int channelNumber) {
        super(timeStamp);
        this.channelNumber = channelNumber;
    }


    @Override
    public void convertData(byte[] byteBuffer) {
        final List<Float> values = new ArrayList<>();
        try {
            double[] data = EEGPacket.toInt32(byteBuffer);

            for (int i = 0; i < data.length; i++) {
                if (i % (channelNumber + 1) == 0) {
                    continue; // skip int representation of status bit
                }

                // calculation for gain adjustment
                double exgUnit = Math.pow(10, -6);
                double vRef = 2.4;
                double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
                values.add((float) (data[i] * (vRef / gain)));
            }
        } catch (InvalidDataException | IOException e) {
            e.printStackTrace(); // TODO: React appropriately
        }
        super.convertedSamples = new ArrayList<>(values); // TODO: Do we need to reinitialise a new list?
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder data = new StringBuilder("ExG ");
        data.append(channelNumber);
        data.append(" channel: [");
        for (float sample : super.convertedSamples) {
            data.append(sample).append(" ,");
        }
        data.append("]");
        return data.toString();
    }


    @Override
    public int getDataCount() {
        return this.channelNumber;
    }


    @Override
    public Topic getTopic() {
        return Topic.EXG;
    }


    private static double[] toInt32(byte[] byteArray) throws InvalidDataException, IOException {
        if (byteArray.length % BUFFER_LENGTH != 0) {
            throw new InvalidDataException("Byte buffer is not read properly", null);
        }

        int arraySize = byteArray.length / BUFFER_LENGTH;
        double[] values = new double[arraySize];

        for (int i = 0; i < byteArray.length; i += 3) {
            if (i == 0) {
                continue;  // skip first byte because 0 is the adc mask
            }

            int signBit = byteArray[i + 2] >> 7;
            double value;
            if (signBit == 0)
                value = ByteBuffer.wrap(new byte[]{byteArray[i], byteArray[i + 1], byteArray[i + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
            else {
                int twosComplimentValue =
                        ByteBuffer.wrap(new byte[]{byteArray[i], byteArray[i + 1], byteArray[i + 2], 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt();
                value = -1 * (Math.pow(2, 24) - twosComplimentValue);
            }
            values[i / 3] = value;
        }
        return values;
    }


    public ArrayList<Float> getData() {
        return super.convertedSamples;
    }
}
