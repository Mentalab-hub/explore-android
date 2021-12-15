package com.mentalab.packets.sensors.exg;

public class Eeg98 extends EEGPacket {

    private final static int CHANNEL_NUMBER = 8;


    public Eeg98(double timeStamp) {
        super(timeStamp, CHANNEL_NUMBER);
    }
}
