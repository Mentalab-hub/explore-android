package com.mentalab.service;

import android.util.Log;
import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ContentServer;
import com.mentalab.io.DeviceInfoSubscriber;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.utils.Utils;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class DeviceInfoUpdaterTask implements Callable<Boolean> {

  private ExploreDevice device;
  private DeviceInfoPacket deviceInfoPacket;

  public DeviceInfoUpdaterTask(ExploreDevice device) {
    this.device = device;
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws Exception if unable to compute a result
   */
  @Override
  public Boolean call() throws InterruptedException, TimeoutException {
    final DeviceInfoSubscriber subscriber = new DeviceInfoSubscriber();
    ContentServer.getInstance().registerSubscriber(subscriber);

    try {
      deviceInfoPacket = subscriber.getDeviceInfo();
    } catch (InterruptedException | TimeoutException exception) {
      Log.d(Utils.TAG, "Timeout exception as no device info packet is received");
      throw exception;
    }
    DeviceConfigurator configurator = new DeviceConfigurator(device, deviceInfoPacket);
    return null;
  }
}
