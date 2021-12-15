package com.mentalab.packets.info;

import androidx.annotation.NonNull;
import com.mentalab.packets.Packet;

/**
 * Disconnection packet is sent when the host machine is disconnected from the device
 */
class DisconnectionPacket extends Packet {


    public DisconnectionPacket(double timeStamp) {
        super(timeStamp);
    }


    @Override
    public void convertData(byte[] byteBuffer) {
    }


    @NonNull
    @Override
    public String toString() {
        return "DisconnectionPacket";
    }


    @Override
    public int getDataCount() {
        return 0;
    } // TODO: Explain
}
