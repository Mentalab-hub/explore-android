package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ChannelCountSubscriber;
import com.mentalab.io.ContentServer;

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
    final ChannelCountSubscriber sub = registerSubscriber();
    return awaitChannelCountAndSet(sub);
  }

  private ChannelCountSubscriber registerSubscriber() {
    final ChannelCountSubscriber sub = new ChannelCountSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    return sub;
  }

  private Boolean awaitChannelCountAndSet(ChannelCountSubscriber sub) throws InterruptedException {
    final int channelCount = sub.awaitResultWithTimeout(1000);
    return configureExploreDevice(channelCount);
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
