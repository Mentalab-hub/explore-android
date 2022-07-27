package com.mentalab.service;

import android.util.Log;
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

public class ConfigureDeviceInfoTask implements Callable<Boolean> {

  private final CountDownLatch latch = new CountDownLatch(1);
  private final ExploreDevice device;
  private volatile Boolean result = false;

  Subscriber deviceInfoSubscriber;

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
    deviceInfoSubscriber =
        new Subscriber(Topic.DEVICE_INFO) {
          @Override
          public void accept(Packet packet) {
            Log.d("EXPLORE_XX", "-----------------------------DEVICE_INFO");
            DeviceConfigurator configurator = new DeviceConfigurator(device);
            configurator.setDeviceInfo((DeviceInfoPacket) packet);
            // deregister subscriber on successful configuration
            deregisterOnExit();
            result = true;
            latch.countDown();
          }
        };
    ContentServer.getInstance().registerSubscriber(deviceInfoSubscriber);

    latch.await(1000, TimeUnit.MILLISECONDS);
    return result;
  }

  private void deregisterOnExit() {
    ContentServer.getInstance().deRegisterSubscriber(deviceInfoSubscriber);
  }
}
