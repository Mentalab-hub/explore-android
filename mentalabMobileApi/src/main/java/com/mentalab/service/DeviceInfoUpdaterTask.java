package com.mentalab.service;

import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.Subscriber;
import com.mentalab.packets.Packet;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DeviceInfoUpdaterTask implements Callable<Boolean> {

  private final CountDownLatch latch = new CountDownLatch(1);
  private ExploreDevice device;

  public DeviceInfoUpdaterTask(ExploreDevice device) {
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
    ContentServer.getInstance()
        .registerSubscriber(
            new Subscriber(Topic.DEVICE_INFO) {
              @Override
              public void accept(Packet packet) {
                DeviceConfigurator configurator =
                    new DeviceConfigurator(device, (DeviceInfoPacket) packet);
                configurator.configureDeviceInfo();
                latch.countDown();
              }
            });

    latch.await(2000, TimeUnit.MILLISECONDS);
    return true;
  }
}
