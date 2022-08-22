package com.mentalab.packets.command;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.utils.Utils;

public class CommandStatus extends UtilPacket {

  private boolean commandStatus;

  public CommandStatus(double timeStamp) {
    super(timeStamp);
  }

  @Override
  public void convertData(byte[] byteBuffer) throws InvalidDataException {
    short status = Utils.bitsToShort(byteBuffer[5]);
    this.commandStatus = status != 0;
  }

  @NonNull
  @Override
  public String toString() {
    return "PACKET: CommandStatus";
  }

  @Override
  public int getDataCount() {
    return 1;
  }

  public boolean getResult() {
    return commandStatus;
  }
}
