package com.mentalab.packets.info;

import androidx.annotation.NonNull;

import com.mentalab.exception.InvalidDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment extends InfoPacket {


    float temperature, light, battery; // TODO: Why are these here?


    public Environment(double timeStamp) {
        super(timeStamp);
        super.attributes = Arrays
            .asList("Temperature ", "Light ", "Battery "); // TODO: Could this be a Bean Object??
    }


    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        final List<Float> listValues = new ArrayList<>();
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

        listValues.add(getBatteryPercentage(batteryLevelRaw));
        super.convertedSamples = new ArrayList<>(listValues);
    }


    @NonNull
    @Override
    public String toString() {
        final StringBuilder data = new StringBuilder("Environment packets: [");
        for (int i = 0; i < super.convertedSamples.size(); i++) {
            final float sample = super.convertedSamples.get(i);
            if (i % 9 < 3) {
                data.append(" Temperature: ").append(sample);
            } else if (i % 9 < 6) {
                data.append(" Light: ").append(sample);
            } else {
                data.append("Battery: ").append(sample);
            }
            data.append(",");
        }
        data.append("]");
        return data.toString();
    }


    @Override
    public int getDataCount() {
        return super.attributes.size();
    }


    private float getBatteryPercentage(float voltage) {
        double perc;
        if (voltage < 3.1) {
            perc = 1d;
        } else if (voltage < 3.5) {
            perc = 1d + (voltage - 3.1) / .4 * 10d;
        } else if (voltage < 3.8) {
            perc = 10d + (voltage - 3.5) / .3 * 40d;
        } else if (voltage < 3.9) {
            perc = 40d + (voltage - 3.8) / .1 * 20d;
        } else if (voltage < 4) {
            perc = 60d + (voltage - 3.9) / .1 * 15d;
        } else if (voltage < 4.1) {
            perc = 75d + (voltage - 4.0) / .1 * 15d;
        } else if (voltage < 4.2) {
            perc = 90d + (voltage - 4.1) / .1 * 10d;
        } else {
            perc = 100d;
        }

        return (float) perc;
    }
}
