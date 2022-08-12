package com.mentalab.packets.sensors.exg;

import static com.mentalab.packets.Attributes.CH1;
import static com.mentalab.packets.Attributes.CH8;

import java.util.EnumSet;

public class Eeg98 extends EEGPacket {

  private static final int CHANNEL_NUMBER = 8;

  public Eeg98(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    super.attributes = EnumSet.range(CH1, CH8);
  }
}
