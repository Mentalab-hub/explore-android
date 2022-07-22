package com.mentalab.service;

import android.util.Log;
import com.mentalab.io.CommandAcknowledgeSubscriber;
import com.mentalab.io.ContentServer;
import com.mentalab.utils.CheckedExceptionSupplier;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class DeviceConfigurationTask implements CheckedExceptionSupplier<Boolean> {

  final byte[] command;
  final OutputStream outputStream;

  public DeviceConfigurationTask(OutputStream outputStream, byte[] encodedBytes) {
    this.outputStream = outputStream;
    this.command = encodedBytes;
  }

  /**
   * Send a command to a connected Explore device.
   *
   * <p>This function is blocking. If no acknowledgement packet arrives, the function will wait
   * until it does. The user can set a timeout using Future functions.
   *
   * @return boolean True when CommandAcknowledgement received, otherwise false
   * @throws IOException If the command cannot be written to the device OutputStream.
   * @throws InterruptedException If the command cannot be written to the device OutputStream.
   */
  @Override
  public Boolean accept() throws IOException, InterruptedException {
    final CommandAcknowledgeSubscriber sub = new CommandAcknowledgeSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    sendCommand();
    return sub.getAcknowledgement(); // blocking for 3s
  }

  private void sendCommand() throws IOException {
    outputStream.write(command);
    outputStream.flush();
    Log.d(Utils.TAG, "Command sent.");
  }
}
