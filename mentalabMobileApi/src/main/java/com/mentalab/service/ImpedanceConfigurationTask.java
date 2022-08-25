package com.mentalab.service;

import com.mentalab.packets.info.ImpedanceInfo;
import com.mentalab.service.io.CountDownSubscriber;
import com.mentalab.service.io.ImpedanceConfigSubscriber;

import java.io.OutputStream;

public class ImpedanceConfigurationTask extends SendCommandTask<ImpedanceInfo> {

  public ImpedanceConfigurationTask(OutputStream o, byte[] b) {
    super(o, b);
  }

  @Override
  CountDownSubscriber<ImpedanceInfo> getSubscriber() {
    return new ImpedanceConfigSubscriber();
  }
}
