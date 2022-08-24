package com.mentalab.service;

import android.util.Log;
import com.mentalab.packets.info.ImpedanceInfo;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.ImpedanceConfigSubscriber;
import com.mentalab.utils.CheckedExceptionSupplier;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class RequestImpedanceTask implements CheckedExceptionSupplier<ImpedanceInfo> {

  private static final int TIMEOUT = 3000;

  private final byte[] startImpCmd;
  private final OutputStream outputStream;

  public RequestImpedanceTask(OutputStream outputStream, byte[] encodedBytes) {
    this.outputStream = outputStream;
    this.startImpCmd = encodedBytes;
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
  public ImpedanceInfo accept() throws IOException, InterruptedException {
    final ImpedanceConfigSubscriber sub = registerSubscriber();
    postCmdToOutputStream(startImpCmd, outputStream);
    final ImpedanceInfo impedanceInfo = sub.awaitResultWithTimeout(TIMEOUT);
    ContentServer.getInstance().deRegisterSubscriber(sub);
    return impedanceInfo;
  }

  private static ImpedanceConfigSubscriber registerSubscriber() {
    final ImpedanceConfigSubscriber sub = new ImpedanceConfigSubscriber();
    ContentServer.getInstance().registerSubscriber(sub);
    return sub;
  }

  private static void postCmdToOutputStream(byte[] command, OutputStream outputStream)
      throws IOException {
    outputStream.write(command);
    outputStream.flush();
    Log.d(Utils.TAG, "Command sent.");
  }
}
