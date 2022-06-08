package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.service.io.ChannelCountSubscriber;

import java.util.concurrent.Callable;

public class ConfigureChannelCountTask extends RegisterSubscriberTask<Integer> implements Callable<Boolean> {

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
    final int channelCount = getResultOf(new ChannelCountSubscriber());
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
