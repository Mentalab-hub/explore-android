package com.mentalab.io;

import android.util.Log;
import com.mentalab.packets.Packet;
import com.mentalab.packets.sensors.exg.Eeg94;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChannelCountSubscriber extends Subscriber {

  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile int channelCount = 8;

  public ChannelCountSubscriber() {
    this.t = Topic.EXG;
  }

  /**
   * Performs this operation on the given argument.
   *
   * @param packet the input argument
   */
  @Override
  public void accept(Packet packet) {
    Log.d(Utils.DEBUG_DEV, "Received packet");
    channelCount = packet.getDataCount();
    latch.countDown();
  }

  public int getChannelCount() throws InterruptedException {
    latch.await(2000, TimeUnit.MILLISECONDS);
    return channelCount;
  }
}
