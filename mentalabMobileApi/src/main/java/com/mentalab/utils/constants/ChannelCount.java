package com.mentalab.utils.constants;

public enum ChannelCount {
  CC_4(4),
  CC_8(8);

  private final int integerRepresentation;

  ChannelCount(int i) {
    this.integerRepresentation = i;
  }

  public int getAsInt() {
    return integerRepresentation;
  }
}
