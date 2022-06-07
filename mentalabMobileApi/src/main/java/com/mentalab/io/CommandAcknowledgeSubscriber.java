package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandReceived;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.utils.constants.Topic;

public class CommandAcknowledgeSubscriber extends CountDownSubscriber<Boolean> {

  public CommandAcknowledgeSubscriber() {
    super(Topic.COMMAND);
  }

  @Override
  public void accept(Packet p) {
    if (p instanceof CommandReceived) {
      return; // no need to do anything. Await status.
    }

    if (p instanceof CommandStatus) {
      result = ((CommandStatus) p).getResult();
    } else {
      result = false;
    }

    latch.countDown();
  }
}
