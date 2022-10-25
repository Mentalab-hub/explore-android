package com.mentalab.packets.sensors.exg;

import static com.mentalab.packets.PacketDataType.CH1;
import static com.mentalab.packets.PacketDataType.CH32;

import java.util.EnumSet;

public class Eeg32Packet extends EEGPacket {
  private static final int NO_CHANNELS = 32;

  public Eeg32Packet(double timeStamp) {
    super(timeStamp, NO_CHANNELS);
    super.type = EnumSet.range(CH1, CH32);
  }
}
