package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.service.io.ChannelCountSubscriber;
import com.mentalab.service.io.ContentServer;

import java.util.concurrent.Callable;

public class ConfigureChannelCountTask implements Callable<Boolean> {

  private final ExploreDevice device;

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
    final int channelCount = obtainChannelCount();
    return configureExploreDevice(channelCount);
  }

  private static int obtainChannelCount() throws InterruptedException {
    final ChannelCountSubscriber sub = registerSubscriber();
    final int channelCount = sub.awaitResultWithTimeout(1000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return channelCount;
  }

  private static ChannelCountSubscriber registerSubscriber() {
    final ChannelCountSubscriber sub = new ChannelCountSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    return sub;
  }

  private Boolean configureExploreDevice(int channelCount) {
    if (channelCount > 1) {
      final DeviceConfigurator configurator = new DeviceConfigurator(device);
      configurator.setDeviceChannelCount(channelCount);
      return true;
    }
    return false;
  }
}
