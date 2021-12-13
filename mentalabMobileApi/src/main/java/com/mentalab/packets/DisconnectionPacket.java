package com.mentalab.packets;

/**
 * Disconnection packet is sent when the host machine is disconnected from the device
 */
class DisconnectionPacket extends Packet {

    public DisconnectionPacket(double timeStamp) {
        super(timeStamp);
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer
     */
    @Override
    public void convertData(byte[] byteBuffer) {
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "DisconnectionPacket";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 0;
    }
}
