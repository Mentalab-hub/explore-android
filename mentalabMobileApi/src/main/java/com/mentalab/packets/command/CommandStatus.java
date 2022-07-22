package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommandStatus extends UtilPacket {

  private boolean commandStatus;

  public CommandStatus(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    short status =
        ByteBuffer.wrap(new byte[] {byteBuffer[5], 0}).order(ByteOrder.LITTLE_ENDIAN).getShort();
    commandStatus = status != 0;
  }

  @NonNull
  @Override
  public String toString() {
    return "Command status is " + commandStatus + ".";
  }

  @Override
  public int getDataCount() {
    return 1;
  }

  public boolean getResult() {
    return commandStatus;
  }
}
