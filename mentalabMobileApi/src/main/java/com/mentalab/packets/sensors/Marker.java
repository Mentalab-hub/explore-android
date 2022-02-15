package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.utils.constants.Topic;
import com.mentalab.packets.PublishablePacket;
import com.mentalab.packets.info.InfoPacket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;

public class Marker extends InfoPacket implements PublishablePacket {

    int markerCode;


    public Marker(double timeStamp) {
        super(timeStamp);
        super.attributes = Collections.singletonList("Marker");
    }


    @Override
    public void convertData(byte[] byteBuffer) throws InvalidDataException {
        markerCode =
                ByteBuffer.wrap(new byte[]{byteBuffer[0], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
        super.convertedSamples = new ArrayList<>(Collections.singletonList((float) markerCode));
    }


    @NonNull
    @Override
    public String toString() {
        return "Marker: " + markerCode;
    }


    @Override
    public int getDataCount() {
        return 1;
    }


    @Override
    public ArrayList<Float> getData() {
        return new ArrayList<>(markerCode);
    }


    @Override
    public Topic getTopic() {
        return Topic.COMMAND;
    }
}
