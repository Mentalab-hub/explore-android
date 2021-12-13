package com.mentalab.packets;

import java.util.ArrayList;

/**
 * Interface for packets related to device information
 */
abstract class InfoPacket extends Packet implements QueueablePacket {
    ArrayList<Float> convertedSamples = null;
    ArrayList<String> attributes;

    public InfoPacket(double timeStamp) {
        super(timeStamp);
    }
}
