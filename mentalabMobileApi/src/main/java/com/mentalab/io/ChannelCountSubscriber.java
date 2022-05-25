package com.mentalab.io;

import com.mentalab.packets.Packet;
import com.mentalab.packets.sensors.exg.Eeg94;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChannelCountSubscriber extends Subscriber {

  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile int channelCount = 8;

  /**
   * Performs this operation on the given argument.
   *
   * @param packet the input argument
   */
  @Override
  public void accept(Packet packet) {
    if (packet instanceof Eeg94) {
      channelCount = 4;
    }
    latch.countDown();
  }

  public int getChannelCount() throws InterruptedException, TimeoutException {
    latch.await(2000, TimeUnit.MILLISECONDS);
    return channelCount;
  }
}
