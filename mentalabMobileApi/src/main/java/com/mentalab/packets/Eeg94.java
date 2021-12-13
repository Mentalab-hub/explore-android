package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

class Eeg94 extends DataPacket {

    private final int channelNumber = 4;

    public Eeg94(double timeStamp) {
        super(timeStamp);
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) {
        List<Float> values = new ArrayList<Float>();
        try {
            double[] data = DataPacket.toInt32(byteBuffer);

            for (int index = 0; index < data.length; index++) {
                // skip int representation for status bit
                if (index % 5 == 0) continue;
                // calculation for gain adjustment
                double exgUnit = Math.pow(10, -6);
                double vRef = 2.4;
                double gain = (exgUnit * (Math.pow(2, 23) - 1)) * 6;
                values.add((float) (data[index] * (vRef / gain)));
            }
        } catch (InvalidDataException | IOException e) {
            e.printStackTrace();
        }
        this.convertedSamples = new ArrayList<>(values);
    }

    @Override
    public String toString() {

        String data = "ExG 4 channel: [";
        ListIterator<Float> it = this.convertedSamples.listIterator();

        while (it.hasNext()) {
            data += it.next() + " ,";
        }
        return data + "]";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return this.channelNumber;
    }
}
