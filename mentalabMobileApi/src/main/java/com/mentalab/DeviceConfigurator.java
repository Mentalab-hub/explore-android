package com.mentalab;

import com.mentalab.packets.info.DeviceInfoPacket;

public class DeviceConfigurator {

  private final ExploreDevice device;

  public DeviceConfigurator(ExploreDevice exploreDevice) {
    this.device = exploreDevice;
  }

  public void setDeviceChannelCount(int channelCount) {
    this.device.setChannelCount(channelCount);
  }

  public void setDeviceInfo(DeviceInfoPacket packet) {
    this.device.setSR(packet.getSamplingRate());
    this.device.setChannelMask(packet.getChannelMask());
  }
}
