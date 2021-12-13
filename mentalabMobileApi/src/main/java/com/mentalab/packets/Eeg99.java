package com.mentalab.packets;

class Eeg99 extends DataPacket {

    public Eeg99(double timeStamp) {
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
        return "Eeg99";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 0;
    }
}
