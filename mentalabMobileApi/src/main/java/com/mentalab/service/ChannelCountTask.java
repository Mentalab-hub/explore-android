package com.mentalab.service;

import android.util.Log;
import com.mentalab.DeviceConfigurator;
import com.mentalab.ExploreDevice;
import com.mentalab.io.ChannelCountSubscriber;
import com.mentalab.io.ContentServer;
import com.mentalab.utils.Utils;
import java.util.concurrent.Callable;

public class ChannelCountTask implements Callable<Boolean> {

  private ExploreDevice device;

  public ChannelCountTask(ExploreDevice device) {
    this.device = device;
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws InterruptedException when calling process timeout forces return from packet subscriber
   */
  @Override
  public Boolean call() throws InterruptedException {
    final ChannelCountSubscriber subscriber = new ChannelCountSubscriber();
    ContentServer.getInstance().registerSubscriber(subscriber);

    try {
      int channelCount = subscriber.getChannelCount();
      DeviceConfigurator configurator = new DeviceConfigurator(device);
      configurator.configureChannelCount(channelCount);
    } catch (InterruptedException exception) {
      Log.d(Utils.TAG, "No device info packet received");
      throw exception;
    }

    ContentServer.getInstance().deRegisterSubscriber(subscriber);
    return true;
  }
}
