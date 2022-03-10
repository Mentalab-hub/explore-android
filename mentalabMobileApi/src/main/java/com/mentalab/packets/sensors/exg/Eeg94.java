package com.mentalab.packets.sensors.exg;

import java.util.Arrays;

public class Eeg94 extends EEGPacket {

  private static final int CHANNEL_NUMBER = 4;


  public Eeg94(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    //TODO refactor to switches with new queue design for next release
    super.attributes = Arrays.asList("Channel_1", "Channel_2", "Channel_3", "Channel_4");
  }
}
