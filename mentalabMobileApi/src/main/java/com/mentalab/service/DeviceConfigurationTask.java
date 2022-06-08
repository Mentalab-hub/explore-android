package com.mentalab.service;

import android.util.Log;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.service.io.CommandAcknowledgeSubscriber;
import com.mentalab.service.io.ContentServer;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class DeviceConfigurationTask implements Callable<Boolean> {

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
   * @throws NoBluetoothException If no device is connected via BT.
   */
  @Override
  public Boolean call() throws IOException, InterruptedException {
    final CommandAcknowledgeSubscriber sub = sendCommand();
    return awaitAcknowledgement(sub);
  }

  private CommandAcknowledgeSubscriber sendCommand() throws IOException {
    final CommandAcknowledgeSubscriber sub = registerSubscriber();
    postCmdToOutputStream(command, outputStream);
    return sub;
  }

  private CommandAcknowledgeSubscriber registerSubscriber() {
    final CommandAcknowledgeSubscriber sub = new CommandAcknowledgeSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    return sub;
  }

  private static void postCmdToOutputStream(byte[] command, OutputStream outputStream) throws IOException {
    outputStream.write(command);
    outputStream.flush();
    Log.d(Utils.TAG, "Command sent.");
  }

  private boolean awaitAcknowledgement(CommandAcknowledgeSubscriber sub)
      throws InterruptedException {
    boolean result = sub.awaitResultWithTimeout(3000);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return result;
  }
}
