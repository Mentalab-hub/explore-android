package com.mentalab.packets.info;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;

/**
 * Interface for packets related to device information
 */
public abstract class InfoPacket extends Packet {


    public InfoPacket(double timeStamp) {
        super(timeStamp);
    }
}
