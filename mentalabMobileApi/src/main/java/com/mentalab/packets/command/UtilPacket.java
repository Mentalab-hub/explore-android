package com.mentalab.packets.command;

import com.mentalab.packets.PublishablePacket;
import com.mentalab.utils.constants.Topic;

/** Interface for packets related to device synchronization */
abstract class UtilPacket extends PublishablePacket {

  public UtilPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public Topic getTopic() {
    return Topic.COMMAND;
  }
}
