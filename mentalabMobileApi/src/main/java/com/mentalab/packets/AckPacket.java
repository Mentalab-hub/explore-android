package com.mentalab.packets;

/**
 * Acknowledgement packet is sent when a configuration command is successfully executed on the
 * device
 */
class AckPacket extends UtilPacket {

    public AckPacket(double timeStamp) {
        super(timeStamp);
    }

    @Override
    public void convertData(byte[] byteBuffer) {
    }

    @Override
    public String toString() {
        return "AckPacket";
    }

    @Override
    public int getDataCount() {
        return 0;
    }

    @Override
    public String getPacketTopic() {
        return "Command";
    }
}
