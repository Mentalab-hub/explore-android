package com.mentalab.utils.constants;

public enum SamplingRate {
  SR_250(0x01, 250),
  SR_500(0x02, 500),
  SR_1000(0x03, 1000);

  private final int binaryCode;
  private final int integerRepresentation;

  SamplingRate(int b, int i) {
    this.binaryCode = b;
    this.integerRepresentation = i;
  }

  public int getCode() {
    return binaryCode;
  }

  public int getAsInt() {
    return integerRepresentation;
  }
}
