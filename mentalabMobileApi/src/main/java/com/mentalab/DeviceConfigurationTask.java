package com.mentalab;

import android.util.Log;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.packets.command.CommandAcknowledgment;
import com.mentalab.packets.command.CommandReceived;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.packets.Packet;

import java.io.IOException;

public class DeviceConfigurationTask extends Thread {

  // OutputStream btOutputStream;
  byte[] byteArray = null;

  public DeviceConfigurationTask(byte[] encodedBytes) {
    // btOutputStream = outputStream;
    byteArray = encodedBytes.clone();
    PubSubManager.getInstance().subscribe("Command", this::commandCallback);
  }

  @Override
  public void run() {

    try {

      MentalabCommands.getOutputStream().write(byteArray);
      MentalabCommands.getOutputStream().flush();
      // MentalabCommands.getOutputStream().close();

      Thread.sleep(1000);
      Log.d("DEBUG_SR", "Finished sending data..now waiting for acknowledgement!");

    } catch (IOException | NoBluetoothException | InterruptedException | NoConnectionException exception) {
      exception.printStackTrace();
    }
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
      Log.d("DEBUG_SR", "CommandStatusPacket received in callback");
    }
  }
}
