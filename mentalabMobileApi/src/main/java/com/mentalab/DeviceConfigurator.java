package com.mentalab;

import com.mentalab.packets.info.DeviceInfoPacket;

public class DeviceConfigurator {

  private ExploreDevice device;

  public DeviceConfigurator(ExploreDevice exploreDevice, DeviceInfoPacket deviceInfoPacket) {
    exploreDevice.samplingRate = deviceInfoPacket.getSamplingRate();
    exploreDevice.channelMask = deviceInfoPacket.getChannelMask();
    if (exploreDevice.channelCount == 0){
      
    }
  }

  public DeviceConfigurator(ExploreDevice exploreDevice) {
    device = exploreDevice;
  }

  public void configureChannelCount(int channelCount) {
    device.channelCount = channelCount;
  }
}
