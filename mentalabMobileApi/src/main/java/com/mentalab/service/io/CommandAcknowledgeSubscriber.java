package com.mentalab.service.io;

import android.util.Log;
import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandReceived;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CommandAcknowledgeSubscriber extends Subscriber {

  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile boolean result;

  public CommandAcknowledgeSubscriber() {
    super(Topic.COMMAND);
  }

  @Override
  public void accept(Packet p) {
    Log.d(Utils.TAG, p.toString());

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

  public boolean getAcknowledgement() throws InterruptedException {
    latch.await(3000, TimeUnit.MILLISECONDS);
    return result;
  }
}
