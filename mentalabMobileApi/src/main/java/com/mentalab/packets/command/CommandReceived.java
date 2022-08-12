package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;

public class CommandReceived extends UtilPacket {

  float markerCode;

  public CommandReceived(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {}

  @NonNull
  @Override
  public String toString() {
    return "Command received.";
  }

  @Override
  public int getDataCount() {
    return 1;
  }
}
