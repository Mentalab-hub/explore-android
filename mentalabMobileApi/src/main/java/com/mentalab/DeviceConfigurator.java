package com.mentalab;

import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.utils.constants.SamplingRate;

public class DeviceConfigurator {
  private ExploreDevice device;

  public DeviceConfigurator(ExploreDevice exploreDevice, DeviceInfoPacket deviceInfoPacket) {
    exploreDevice.samplingRate = deviceInfoPacket.getSamplingRate();
    exploreDevice.channelMask = deviceInfoPacket.getChannelMask();
  }

  public DeviceConfigurator(ExploreDevice exploreDevice) {
    device = exploreDevice;
  }

  public void configureChannelCount(int channelCount) {
    device.channelCount = channelCount;
  }
}
