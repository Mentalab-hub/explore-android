package com.mentalab.packets.command;

import com.mentalab.packets.Packet;
import com.mentalab.packets.Publishable;
import com.mentalab.utils.constants.Topic;

/** Interface for packets related to device synchronization */
abstract class UtilPacket extends Packet implements Publishable {

  public UtilPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public Topic getTopic() {
    return Topic.COMMAND;
  }
}
