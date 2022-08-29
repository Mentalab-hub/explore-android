package com.mentalab.service;

import com.mentalab.packets.info.ImpedanceInfoPacket;
import com.mentalab.service.io.CountDownSubscriber;
import com.mentalab.service.io.ImpedanceConfigSubscriber;

import java.io.OutputStream;

public class ImpedanceConfigurationTask extends SendCommandTask<ImpedanceInfoPacket> {

  public ImpedanceConfigurationTask(OutputStream o, byte[] b) {
    super(o, b);
  }

  @Override
  CountDownSubscriber<ImpedanceInfoPacket> getSubscriber() {
    return new ImpedanceConfigSubscriber();
  }
}
