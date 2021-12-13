package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// class Eeg implements DataPacket {}
class Eeg98 extends DataPacket {
    private static int channelNumber = 8;

    public Eeg98(double timeStamp) {
        super(timeStamp);
    }

    @Override
    public void convertData(byte[] byteBuffer) {
        List<Float> values = new ArrayList<Float>();
        try {
            double[] data = DataPacket.toInt32(byteBuffer);

            for (int index = 0; index < data.length; index++) {
                // skip int representation for status bit
                if (index % 9 == 0) continue;
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

        String data = "ExG 8 channel: [";

        for (Float convertedSample : this.convertedSamples) {
            data += convertedSample + " ,";
        }
        return data + "]";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 8;
    }
}
