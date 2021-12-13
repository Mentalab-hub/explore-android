package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Environment extends InfoPacket {
    float temperature, light, battery;

    public Environment(double timeStamp) {
        super(timeStamp);
        super.attributes = new ArrayList(Arrays.asList("Temperature ", "Light ", "Battery "));
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        List<Float> listValues = new ArrayList<Float>();

        listValues.add(
                (float)
                        ByteBuffer.wrap(new byte[]{byteBuffer[0], 0, 0, 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt());
        listValues.add(
                (float)
                        (ByteBuffer.wrap(new byte[]{byteBuffer[1], byteBuffer[2], 0, 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt())
                        * (1000 / 4095));
        float batteryLevelRaw =
                (float)
                        ((ByteBuffer.wrap(new byte[]{byteBuffer[3], byteBuffer[4], 0, 0})
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt()
                                * 16.8
                                / 6.8)
                                * (1.8 / 2457));

        listValues.add(getBatteryParcentage(batteryLevelRaw));
        this.convertedSamples = new ArrayList<>(listValues);
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        String data = "Environment packets: [";

        for (int index = 0; index < convertedSamples.size(); index += 1) {
            if (index % 9 < 3) {
                data += " Temperature: " + convertedSamples.get(index);
            } else if (index % 9 < 6) {
                data += " Light: " + convertedSamples.get(index);
            } else {
                data += "Battery: " + convertedSamples.get(index);
            }

            data += ",";
        }

        return data + "]";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 3;
    }

    float getBatteryParcentage(float voltage) {
        double parcentage = 0;
        if (voltage < 3.1) {
            parcentage = 1;
        } else if (voltage < 3.5) {
            parcentage = (1 + (voltage - 3.1) / .4 * 10);
        } else if (voltage < 3.8) {
            parcentage = 10 + (voltage - 3.5) / .3 * 40;
        } else if (voltage < 3.9) {
            parcentage = 40 + (voltage - 3.8) / .1 * 20;
        } else if (voltage < 4) {
            parcentage = 60 + (voltage - 3.9) / .1 * 15;
        } else if (voltage < 4.1) {
            parcentage = 75 + (voltage - 4.) / .1 * 15;
        } else if (voltage < 4.2) {
            parcentage = 90 + (voltage - 4.1) / .1 * 10;
        } else {
            parcentage = 100;
        }

        return (float) parcentage;
    }
}
