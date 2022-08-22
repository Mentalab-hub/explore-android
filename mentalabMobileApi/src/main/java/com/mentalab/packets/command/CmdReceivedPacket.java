package com.mentalab.packets.command;

import androidx.annotation.NonNull;

public class CmdReceivedPacket extends UtilPacket {

  public CmdReceivedPacket(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) {
    // ignored
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: CommandReceived";
  }

  @Override
  public int getDataCount() {
    return 1;
  }
}
