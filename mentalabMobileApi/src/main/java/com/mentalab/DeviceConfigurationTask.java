package com.mentalab;

import android.util.Log;
import com.mentalab.exception.NoBluetoothException;
import java.io.IOException;

public class DeviceConfigurationTask extends Thread {

  // OutputStream btOutputStream;
  byte[] byteArray = null;

  public DeviceConfigurationTask(byte[] encodedBytes) {
    // btOutputStream = outputStream;
    byteArray = encodedBytes.clone();
    for(int i = 0; i < byteArray.length; i++){
      Log.d("DEBUG_SR","Converted data for index: " + "is " + String.format("%02X", byteArray[i]));
    }
    PubSubManager.getInstance().subscribe("Command", this::commandCallback);
  }

  @Override
  public void run() {

    try {

      MentalabCommands.getOutputStream().write(byteArray);
      MentalabCommands.getOutputStream().flush();
      //MentalabCommands.getOutputStream().close();

      Thread.sleep(1000);
      Log.d("DEBUG_SR", "Finished sending data..now waiting for acknowledgement!");

    } catch (IOException | NoBluetoothException | InterruptedException exception) {
      exception.printStackTrace();
    }
  }

  public void commandCallback(Packet packet) {
    Log.d("DEBUG_SR", "Data received in callback");
    if (packet instanceof AckPacket) {
      Log.d("DEBUG_SR", "Ack packet received in callback");
    }

    if (packet instanceof CommandReceivedPacket) {
      Log.d("DEBUG_SR", "CommandReceivedPacket received in callback");
    }

    if (packet instanceof CommandStatusPacket) {
      Log.d("DEBUG_SR", "CommandStatusPacket received in callback");
    }
  }
}
