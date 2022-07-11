package com.mentalab.service;

import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.service.io.DeviceInfoSubscriber;
import com.mentalab.utils.Utils;

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
  public Boolean accept() throws InterruptedException {
    final DeviceInfoPacket deviceInfo = getResultOf(new DeviceInfoSubscriber());
    return configureExploreDevice(deviceInfo);
  }

  private Boolean configureExploreDevice(DeviceInfoPacket deviceInfo) {
    this.device.setSR(deviceInfo.getSamplingRate());
    this.device.setChannelMask(deviceInfo.getChannelMask());
    Log.d(Utils.TAG, "Device info set.");
    return true;
  }
}
