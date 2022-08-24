package com.mentalab.service.io;

import com.mentalab.packets.Packet;
import com.mentalab.packets.info.ImpedanceInfo;
import com.mentalab.utils.constants.Topic;

public class ImpedanceConfigSubscriber extends CountDownSubscriber<ImpedanceInfo> {

  public ImpedanceConfigSubscriber() {
    super(Topic.DEVICE_INFO);
  }

  @Override
  public void accept(Packet packet) {
    if (packet instanceof ImpedanceInfo) {
      result = (ImpedanceInfo) packet;
      latch.countDown();
    }
  }
}
