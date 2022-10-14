package com.mentalab.service.impedance;

import com.mentalab.ExploreDevice;
import com.mentalab.ExploreExecutor;
import com.mentalab.packets.sensors.exg.EEGPacket;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.ImpedanceSubscriber;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.constants.Topic;
import java.util.concurrent.Callable;

public class ImpedanceCalculatorTask implements Callable<Boolean> {

  private final ExploreDevice device;
  private Subscriber<EEGPacket> impedanceSubscriber;

  public ImpedanceCalculatorTask(ExploreDevice device) {
    this.device = device;
  }

  @Override
  public Boolean call() throws ArithmeticException {
    ExploreExecutor.getInstance().getLock().set(false); // block other tasks

    if (device.getSlope() == 0) {
      throw new ArithmeticException(
          "Cannot proceed with impedance calculation. Zero slope assigned.");
    }
    final ImpedanceCalculator calculator = new ImpedanceCalculator(device);
    this.impedanceSubscriber = new ImpedanceSubscriber(Topic.EXG, calculator);
    ContentServer.getInstance().registerSubscriber(impedanceSubscriber);
    return true;
  }

  public void cancelTask() {
    ContentServer.getInstance().deRegisterSubscriber(impedanceSubscriber);
    this.impedanceSubscriber = null;
  }
}