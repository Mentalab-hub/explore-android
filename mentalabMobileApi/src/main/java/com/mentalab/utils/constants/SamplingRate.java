package com.mentalab.utils.constants;

public enum SamplingRate {
  SR_250(0x01),
  SR_500(0x02),
  SR_1000(0x03);


  private final int samplingRate;

  SamplingRate(final int samplingRate) {
    this.samplingRate = samplingRate;
  }


  public int getValue() {
    return samplingRate;
  }
}
