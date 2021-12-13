package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Eeg94 extends EEGPacket {


    private static final int CHANNEL_NUMBER = 4;


    public Eeg94(double timeStamp) {
        super(timeStamp);
    }


    @Override
    public void convertData(byte[] byteBuffer) {
        List<Float> values = new ArrayList<>();
        try {
            double[] data = EEGPacket.toInt32(byteBuffer);

            for (int i = 0; i < data.length; i++) {
                if (i % (CHANNEL_NUMBER + 1) == 0) {
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
        this.convertedSamples = new ArrayList<>(values);
    }


    @Override
    public String toString() {
        StringBuilder data = new StringBuilder("ExG 4 channel: [");
        for (Float convertedSample : this.convertedSamples) {
            data.append(convertedSample).append(" ,");
        }
        return data + "]";
    }


    @Override
    public int getDataCount() {
        return this.CHANNEL_NUMBER;
    }
}
