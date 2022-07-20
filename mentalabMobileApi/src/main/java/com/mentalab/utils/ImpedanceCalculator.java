package com.mentalab.utils;

import com.mentalab.ExploreDevice;

public class ImpedanceCalculator {

  private final int notchFreq;
  private ExploreDevice device;
  public ImpedanceCalculator(ExploreDevice exploreDevice, int notchFrequency) {
    notchFreq = notchFrequency;
    device = exploreDevice;
    addfilters();
  }

  private void addfilters(){
    ButterworthFilter filter = new ButterworthFilter(device.getSamplingRate().getValue());
  }
}
