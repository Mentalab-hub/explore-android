package com.mentalab.packets;

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

    @Override
    public String toString() {
        return "TimeStampPacket";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 0;
    }
}
