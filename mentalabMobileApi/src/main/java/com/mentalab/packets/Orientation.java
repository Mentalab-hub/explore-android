package com.mentalab.packets;

import android.util.Log;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.io.Topic;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Device related information packet to transmit firmware version, ADC mask and sampling rate
 */
public class Orientation extends InfoPacket implements PublishablePacket {
    ArrayList<Float> listValues = new ArrayList<Float>();

    public Orientation(double timeStamp) {
        super(timeStamp);
        attributes =
                new ArrayList<String>(
                        Arrays.asList(
                                "Acc_X", "Acc_Y", "Acc_Z", "Mag_X", "Mag_Y", "Mag_Z", "Gyro_X", "Gyro_Y",
                                "Gyro_Z"));
    }

    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        double[] convertedRawValues = super.bytesToDouble(byteBuffer, 2);

        for (int index = 0; index < convertedRawValues.length; index++) {
            if (index < 3) {
                listValues.add((float) (convertedRawValues[index] * 0.061));
            } else if (index < 6) {
                listValues.add((float) (convertedRawValues[index] * 8.750));
            } else {
                if (index == 6) {
                    listValues.add((float) (convertedRawValues[index] * 1.52 * -1));
                } else {
                    listValues.add((float) (convertedRawValues[index] * 1.52));
                }
            }
        }
        this.convertedSamples = new ArrayList<>(listValues);
        Log.d("Explore", "Converted samples in the packets are: " + this.convertedSamples.toString());
    }

    @Override
    public String toString() {
        String data = "Orientation packets: [";

        for (int index = 0; index < convertedSamples.size(); index += 1) {
            if (index % 9 < 3) {
                data += " accelerometer: " + convertedSamples.get(index);
            } else if (index % 9 < 6) {
                data += " magnetometer: " + convertedSamples.get(index);
            } else {
                data += "gyroscope: " + convertedSamples.get(index);
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
        return 9;
    }

    /**
     * Number of element in each packet
     */
    @Override
    public ArrayList<Float> getData() {
        return this.convertedSamples;
    }

    @Override
    public Topic getTopic() {
        return Topic.ORN;
    }
}
