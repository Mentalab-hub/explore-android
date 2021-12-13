package com.mentalab.packets;

class Eeg99s extends DataPacket {

    public Eeg99s(double timeStamp) {
        super(timeStamp);
    }

    /**
     * Converts binary data stream to human readable voltage values
     *
     * @param byteBuffer byte array with input data
     */
    @Override
    public void convertData(byte[] byteBuffer) {
    }

    /**
     * String representation of attributes
     */
    @Override
    public String toString() {
        return "Eeg99s";
    }

    /**
     * Number of element in each packet
     */
    @Override
    public int getDataCount() {
        return 0;
    }
}
