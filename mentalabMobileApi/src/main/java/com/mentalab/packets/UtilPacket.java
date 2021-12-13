package com.mentalab.packets;

import java.util.ArrayList;

/**
 * Interface for packets related to device synchronization
 */
abstract class UtilPacket extends Packet implements PublishablePacket {

    protected ArrayList<Float> convertedSamples;

    public UtilPacket(double timeStamp) {
        super(timeStamp);
    }
}
