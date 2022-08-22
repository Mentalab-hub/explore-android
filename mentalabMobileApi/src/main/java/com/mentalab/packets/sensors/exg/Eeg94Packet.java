package com.mentalab.packets.sensors.exg;

import static com.mentalab.packets.PacketDataType.CH1;
import static com.mentalab.packets.PacketDataType.CH4;

import java.util.EnumSet;

public class Eeg94Packet extends EEGPacket {

  private static final int CHANNEL_NUMBER = 4;

  public Eeg94Packet(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    super.type = EnumSet.range(CH1, CH4);
  }
}
