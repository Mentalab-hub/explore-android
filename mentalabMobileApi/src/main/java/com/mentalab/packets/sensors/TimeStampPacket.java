package com.mentalab.packets.sensors;

import androidx.annotation.NonNull;
import com.mentalab.packets.Packet;

/**
 * Packet sent from the device to sync clocks
 */
class TimeStampPacket extends Packet {


    public TimeStampPacket(double timeStamp) {
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
