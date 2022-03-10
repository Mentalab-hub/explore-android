package com.mentalab.packets.sensors.exg;

import java.util.Arrays;

public class Eeg98 extends EEGPacket {

  private static final int CHANNEL_NUMBER = 8;

  public Eeg98(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    super.attributes =
        Arrays.asList(
            "Channel_1",
            "Channel_2",
            "Channel_3",
            "Channel_4",
            "Channel_5",
            "Channel_6",
            "Channel_7",
            "Channel_8");
  }
}
