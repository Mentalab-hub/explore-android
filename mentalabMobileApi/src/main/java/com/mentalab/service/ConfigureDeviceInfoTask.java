package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.service.io.DeviceInfoSubscriber;

import java.util.concurrent.Callable;

public class ConfigureDeviceInfoTask extends RegisterSubscriberTask<DeviceInfoPacket> {

  private final ExploreDevice device;

  public ConfigureDeviceInfoTask(ExploreDevice device) {
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
    final DeviceInfoPacket deviceInfo = getResultOf(new DeviceInfoSubscriber());
    return configureExploreDevice(deviceInfo);
  }

  private Boolean configureExploreDevice(DeviceInfoPacket deviceInfo) {
    final DeviceConfigurator configurator = new DeviceConfigurator(device);
    configurator.setDeviceInfo(deviceInfo);
    return true;
  }
}
