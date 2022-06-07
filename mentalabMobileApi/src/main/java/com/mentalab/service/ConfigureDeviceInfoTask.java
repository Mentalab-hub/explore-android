package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.DeviceInfoSubscriber;
import com.mentalab.packets.info.DeviceInfoPacket;

import java.util.concurrent.Callable;

public class ConfigureDeviceInfoTask implements Callable<Boolean> {

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
    final DeviceInfoSubscriber sub = registerSubscriber();
    return awaitDeviceInfoAndSet(sub);
  }

  private DeviceInfoSubscriber registerSubscriber() {
    final DeviceInfoSubscriber sub = new DeviceInfoSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    return sub;
  }

  private Boolean awaitDeviceInfoAndSet(DeviceInfoSubscriber sub) throws InterruptedException {
    final DeviceInfoPacket deviceInfo = sub.awaitResultWithTimeout(1000);
    return configureExploreDevice(deviceInfo);
  }

  private Boolean configureExploreDevice(DeviceInfoPacket deviceInfo) {
    final DeviceConfigurator configurator = new DeviceConfigurator(device);
    configurator.setDeviceInfo(deviceInfo);
    return true;
  }
}
