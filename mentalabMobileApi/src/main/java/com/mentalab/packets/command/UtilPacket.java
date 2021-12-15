package com.mentalab.packets.command;

import com.mentalab.packets.Packet;
import com.mentalab.packets.PublishablePacket;

/**
 * Interface for packets related to device synchronization
 */
abstract class UtilPacket extends Packet implements PublishablePacket {

    public UtilPacket(double timeStamp) {
        super(timeStamp);
    }
}
