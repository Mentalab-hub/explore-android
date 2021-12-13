package com.mentalab.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Device related information packet to transmit firmware version, ADC mask and sampling rate
 */
class DeviceInfoPacket extends InfoPacket {
    int adsMask;
    int samplingRate;

    public DeviceInfoPacket(double timeStamp) {
        super(timeStamp);
        attributes = new ArrayList<String>(Arrays.asList("Ads_Mask", "Sampling_Rate"));
    }

    @Override
    public void convertData(byte[] byteBuffer) {
        int samplingRateMultiplier =
                ByteBuffer.wrap(new byte[]{byteBuffer[2], 0, 0, 0})
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .getInt();
        samplingRate = (int) (16000 / (Math.pow(2, samplingRateMultiplier)));
        adsMask = byteBuffer[3] & 0xFF;

        this.convertedSamples =
                new ArrayList<Float>(
                        Arrays.asList(new Float[]{Float.valueOf(adsMask), Float.valueOf(samplingRate)}));
    }

    /**
     * Return list of elements in each packet
     */
    @Override
    public ArrayList<Float> getData() {
        return this.convertedSamples;
    }

    @Override
    public String toString() {
        return "DeviceInfoPacket";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 2;
    }
}
