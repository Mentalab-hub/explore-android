package com.mentalab.service;

import android.util.Log;
import com.mentalab.packets.Packet;
import com.mentalab.packets.info.CalibrationInfoPacket;
import com.mentalab.service.io.ContentServer;
import com.mentalab.service.io.Subscriber;
import com.mentalab.utils.CheckedExceptionSupplier;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ImpedanceConfigurationTask implements CheckedExceptionSupplier<CalibrationInfoPacket> {
  final byte[] command;
  final OutputStream outputStream;
  private final CountDownLatch latch = new CountDownLatch(1);
  Subscriber ImpCommandSubscriber;
  CalibrationInfoPacket calibrationInfoPacket;

  public ImpedanceConfigurationTask(OutputStream outputStream, byte[] encodedBytes) {
    super();
    this.outputStream = outputStream;
    this.command = encodedBytes;

    ImpCommandSubscriber =
        new Subscriber<Packet>(Topic.DEVICE_INFO) {
          @Override
          public void accept(Packet p) {

            if (p instanceof CalibrationInfoPacket) {
              calibrationInfoPacket = (CalibrationInfoPacket) p;
              latch.countDown();
            }
          }
        };
    ContentServer.getInstance().registerSubscriber(ImpCommandSubscriber);
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
  public CalibrationInfoPacket accept() throws IOException, InterruptedException {

    sendCommand();
    latch.await(3000, TimeUnit.MILLISECONDS);
    ContentServer.getInstance().deRegisterSubscriber(ImpCommandSubscriber);
    return calibrationInfoPacket;
  }

  private void sendCommand() throws IOException {
    outputStream.write(command);
    outputStream.flush();
    Log.d(Utils.TAG, "Command sent.");
  }
}
