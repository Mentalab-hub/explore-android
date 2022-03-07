package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

/**
 * Packet sent from the device to sync clocks
 */
class TimeStamp extends Packet {


    public TimeStamp(double timeStamp) {
        super(timeStamp);
    }


    @Override
    public void convertData(byte[] byteBuffer) {
    }


    @NonNull
    @Override
    public String toString() {
        return "TimeStampPacket";
    }


    @Override
    public int getDataCount() {
        return 0; // TODO: Explanation...
    }
}
