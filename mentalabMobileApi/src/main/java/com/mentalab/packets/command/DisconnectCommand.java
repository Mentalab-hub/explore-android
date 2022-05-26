package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.packets.Packet;

/**
 * Disconnection packet is sent when the host machine is disconnected from the device
 */
class DisconnectCommand extends Packet {

  public DisconnectCommand(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {
  }

  @NonNull
  @Override
  public String toString() {
    return "Disconnected.";
  }

  @Override
  public int getDataCount() {
    return 0;
  }
}
