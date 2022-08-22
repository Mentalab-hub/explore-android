package com.mentalab.packets.sensors.exg;

import static com.mentalab.packets.PacketDataType.CH1;
import static com.mentalab.packets.PacketDataType.CH8;

import java.util.EnumSet;

public class Eeg98Packet extends EEGPacket {

  private static final int CHANNEL_NUMBER = 8;

  public Eeg98Packet(double timeStamp) {
    super(timeStamp, CHANNEL_NUMBER);
    super.type = EnumSet.range(CH1, CH8);
  }
}
