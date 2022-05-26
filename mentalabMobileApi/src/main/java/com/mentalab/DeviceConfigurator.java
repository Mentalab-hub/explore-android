package com.mentalab;

import com.mentalab.packets.info.DeviceInfoPacket;

public class DeviceConfigurator {

  private ExploreDevice device;
  private DeviceInfoPacket deviceInfoPacket;

  public DeviceConfigurator(ExploreDevice exploreDevice, DeviceInfoPacket infoPacket) {
    device = exploreDevice;
    deviceInfoPacket = infoPacket;
  }

  public DeviceConfigurator(ExploreDevice exploreDevice) {
    device = exploreDevice;
  }

  public void configureChannelCount(int channelCount) {
    device.channelCount = channelCount;
  }

  public void configureDeviceInfo() {
    device.samplingRate = deviceInfoPacket.getSamplingRate();
    device.channelMask = deviceInfoPacket.getChannelMask();
  }
}
