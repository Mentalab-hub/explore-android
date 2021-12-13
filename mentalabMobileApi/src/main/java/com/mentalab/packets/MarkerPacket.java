package com.mentalab.packets;

import com.mentalab.exception.InvalidDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

class MarkerPacket extends InfoPacket implements PublishablePacket {

    int markerCode;

    // super.att = new ArrayList<String>(Arrays.asList("Marker"));

    public MarkerPacket(double timeStamp) {
        super(timeStamp);
        attributes = new ArrayList<String>(Arrays.asList("Marker"));
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        markerCode =
                ByteBuffer.wrap(new byte[]{byteBuffer[0], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
        convertedSamples = new ArrayList<Float>(Arrays.asList((float) markerCode));
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Marker: " + markerCode;
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 1;
    }

    /**
     * Get data values from packet structure
     */
    @Override
    public ArrayList<Float> getData() {
        return new ArrayList<Float>(markerCode);
    }

    @Override
    public String getPacketTopic() {
        return "Marker";
    }
}
