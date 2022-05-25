package com.mentalab.io;

import com.mentalab.DeviceConfigurator;
import com.mentalab.packets.Packet;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeviceInfoSubscriber extends Subscriber {
  private final CountDownLatch latch = new CountDownLatch(1);
  DeviceInfoPacket deviceInfoPacket;
  private volatile Boolean result;

  public DeviceInfoSubscriber() {
    this.t = Topic.DEVICE_INFO;
  }

  /**
   * Performs this operation on the given argument.
   *
   * @param packet the input argument
   */
  @Override
  public void accept(Packet packet) { // to be replaced with custom listener interface
    DeviceConfigurator configurator = new DeviceConfigurator(null);
    deviceInfoPacket = ((DeviceInfoPacket) packet);
    latch.countDown();
  }

  public DeviceInfoPacket getDeviceInfo() throws InterruptedException, TimeoutException {
    latch.await(2000, TimeUnit.MILLISECONDS);
    return deviceInfoPacket;
  }
}
