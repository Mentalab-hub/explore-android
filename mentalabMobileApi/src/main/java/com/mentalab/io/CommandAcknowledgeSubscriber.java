package com.mentalab.io;

import android.util.Log;

import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

public class CommandAcknowledgeSubscriber extends Subscriber {

  volatile Boolean result;

  public CommandAcknowledgeSubscriber() {
    this.t = Topic.COMMAND;
  }

  @Override
  public void accept(Packet p) {
    Log.d(Utils.TAG, "CommandAcknowledgeSubscriber" + p.toString());
    if (p instanceof CommandStatus) {
      result = ((CommandStatus) p).getResult();
    } else {
      result = false;
    }
    synchronized (this) {
      result.notify();
    }
  }

  public boolean getAcknowledgement() throws InterruptedException {
    synchronized (this) {
      result.wait(3_000);
    }
    return result;
  }
}
