package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ConfigureChannelCountTask implements Callable<Boolean> {

  private ExploreDevice device;
  private final CountDownLatch latch = new CountDownLatch(1);
  private volatile Boolean result = false;

  public ConfigureChannelCountTask(ExploreDevice device) {
    this.device = device;
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws InterruptedException when calling process timeout forces return from packet subscriber
   */
  @Override
  public Boolean call() throws InterruptedException {
    Subscriber channelCountSubscriber =
        new Subscriber(Topic.EXG) {
          @Override
          public void accept(Packet packet) {
            int channelCount = packet.getDataCount();
            DeviceConfigurator configurator = new DeviceConfigurator(device);
            configurator.configureChannelCount(channelCount);
            result = true;
          }
        };
    ContentServer.getInstance().registerSubscriber(channelCountSubscriber);
    latch.await(1000, TimeUnit.MILLISECONDS);
    ContentServer.getInstance().deRegisterSubscriber(channelCountSubscriber);
    return result;
  }
}
