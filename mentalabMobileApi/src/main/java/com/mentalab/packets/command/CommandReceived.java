package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;

public class CommandReceived extends UtilPacket {

  public CommandReceived(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
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
