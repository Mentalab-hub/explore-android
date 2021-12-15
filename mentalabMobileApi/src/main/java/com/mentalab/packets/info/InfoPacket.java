package com.mentalab.packets.info;

import com.mentalab.packets.Packet;
import com.mentalab.packets.QueueablePacket;

import java.util.List;

/**
 * Interface for packets related to device information
 */
public abstract class InfoPacket extends Packet implements QueueablePacket {

    public List<String> attributes;

    public InfoPacket(double timeStamp) {
        super(timeStamp);
    }
}
