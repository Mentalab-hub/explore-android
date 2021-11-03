package com.mentalab;

import android.util.Log;
import com.mentalab.MentalabConstants.Command;
import java.io.IOException;
import java.io.OutputStream;

public class DeviceConfigurationTask extends Thread{

  OutputStream btOutputStream;
  byte[] byteArray = null;

  public DeviceConfigurationTask(byte[] encodedBytes, OutputStream outputStream) {
    btOutputStream = outputStream;
    byteArray = encodedBytes.clone();

  }

  @Override
  public void run() {
    PubSubManager.getInstance().subscribe("Command", this::commandCallback);
    try {
      btOutputStream.write(byteArray);
      btOutputStream.flush();
    } catch (IOException exception) {
      exception.printStackTrace();
    }

  }

  public void commandCallback(Packet packet) {
    if (packet instanceof AckPacket){
      Log.d("DEBUG_SR", "Ack packet received in callback");
    }

    if (packet instanceof CommandReceivedPacket){
      Log.d("DEBUG_SR", "CommandReceivedPacket received in callback");
    }

    if (packet instanceof CommandStatusPacket){
      Log.d("DEBUG_SR", "CommandStatusPacket received in callback");
    }
  }
}
