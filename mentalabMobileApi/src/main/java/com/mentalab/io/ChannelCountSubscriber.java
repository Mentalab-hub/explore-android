package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChannelCountSubscriber extends Subscriber {

  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile int channelCount = 8;

  public ChannelCountSubscriber() {
    super(Topic.EXG);
  }

  /**
   * Performs this operation on the given argument.
   *
   * @param packet the input argument
   */
  @Override
  public void accept(Packet packet) {
    channelCount = packet.getDataCount();
    latch.countDown();
  }

  public int getChannelCount() throws InterruptedException {
    latch.await(2000, TimeUnit.MILLISECONDS);
    return channelCount;
  }
}
