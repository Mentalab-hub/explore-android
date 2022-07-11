package com.mentalab.service;

import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.service.io.ChannelCountSubscriber;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.ChannelCount;

public class ConfigureChannelCountTask extends RegisterSubscriberTask<Integer> {

  private final ExploreDevice device;

  public ConfigureChannelCountTask(ExploreDevice device) {
    this.device = device;
  }

  /**
   * Computes a result, or throws an exception if unable to do so.
   *
   * @return computed result
   * @throws InterruptedException when calling process timeout forces return from packet subscriber
   */
  @Override
  public Boolean accept() throws InterruptedException {
    final int channelCount = getResultOf(new ChannelCountSubscriber());
    return configureExploreDevice(channelCount);
  }

  private Boolean configureExploreDevice(int channelCount) {
    if (channelCount < 1) {
      return false;
    }
    final ChannelCount cc = Utils.getChannelCountFromInt(channelCount);
    this.device.setChannelCount(cc);
    Log.d(Utils.TAG, "Channel count set:" + channelCount);
    return true;
  }
}
