package com.mentalab.packets.sensors.exg;

import static com.mentalab.packets.Attributes.CH1;
import static com.mentalab.packets.Attributes.CH4;

import java.util.EnumSet;

public class Eeg94 extends EEGPacket {

  private static final int CHANNEL_NUMBER = 4;

  public Eeg94(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);

    super.attributes = EnumSet.range(CH1, CH4);
  }
}
