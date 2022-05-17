package com.mentalab.packets.sensors.exg;

import java.util.EnumSet;

import static com.mentalab.packets.Attributes.CH1;
import static com.mentalab.packets.Attributes.CH4;

public class Eeg94 extends EEGPacket {

  private static final int CHANNEL_NUMBER = 4;

  public Eeg94(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    // TODO refactor to switches with new queue design for next release
    super.attributes = EnumSet.range(CH1, CH4);
  }
}
