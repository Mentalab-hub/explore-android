package com.mentalab.tasks;

import android.util.Log;
import com.mentalab.MentalabCommands;
import com.mentalab.PubSubManager;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandAcknowledgment;
import com.mentalab.packets.command.CommandReceived;
import com.mentalab.packets.command.CommandStatus;
import java.io.IOException;
import java.util.concurrent.Callable;

public class DeviceConfigurationTask implements Callable<Boolean> {

  // OutputStream btOutputStream;
  byte[] byteArray;
  boolean result;

  public DeviceConfigurationTask(byte[] encodedBytes) {
    // btOutputStream = outputStream;
    byteArray = encodedBytes.clone();
    PubSubManager.getInstance().subscribe("Command", this::commandCallback);
  }

  @Override
  public Boolean call() throws CommandFailedException {

    try {

      MentalabCommands.getOutputStream().write(byteArray);
      MentalabCommands.getOutputStream().flush();
      // MentalabCommands.getOutputStream().close();

      Thread.sleep(3000);
      Log.d("DEBUG_SR", "Finished sending data..now waiting for acknowledgement!");

    } catch (IOException
        | InterruptedException
        | NoBluetoothException
        | NoConnectionException exception) {
      throw new CommandFailedException("Could not execute Command", null);
    }
    return result;
  }

  public void commandCallback(Packet packet) {
    Log.d("DEBUG_SR", "Data received in callback");
    if (packet instanceof CommandAcknowledgment) {
      Log.d("DEBUG_SR", "Ack packet received in callback");
    }

    if (packet instanceof CommandReceived) {
      Log.d("DEBUG_SR", "CommandReceivedPacket received in callback");
    }

    if (packet instanceof CommandStatus) {
      CommandStatus executionResult = ((CommandStatus) packet);
      result = executionResult.commandStatus;
      Log.d("DEBUG_SR", "CommandStatusPacket received in callback");
    }
  }
}
