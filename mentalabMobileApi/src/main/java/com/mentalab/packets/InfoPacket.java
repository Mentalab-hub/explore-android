package com.mentalab.packets;

import java.util.ArrayList;

/**
 * Interface for packets related to device information
 */
public abstract class InfoPacket extends Packet implements QueueablePacket {
    public ArrayList<Float> convertedSamples = null;
    public ArrayList<String> attributes;

    public InfoPacket(double timeStamp) {
        super(timeStamp);
    }
}
