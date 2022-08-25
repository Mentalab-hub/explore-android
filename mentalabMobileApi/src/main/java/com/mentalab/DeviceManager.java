package com.mentalab;

import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.packets.info.ImpedanceInfo;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.ImpedanceConfigurationTask;
import com.mentalab.service.SendCommandTask;
import com.mentalab.utils.commandtranslators.Command;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

interface DeviceManager {

  /**
   * Asynchronously submits a command to the OutputStream using task.
   *
   * @param task Task that will send command to the device.
   * @return Future<T>
   */
  static <T> CompletableFuture<T> submitConfigCommand(
      SendCommandTask<T> task, T exceptionalReturn) {
    return CompletableFuture.supplyAsync(task, ExploreExecutor.getExecutorInstance())
        .exceptionally(e -> exceptionalReturn); // if throws an exception, return gracefully
  }

  /**
   * Asynchronously submits a command to the OutputStream using the DeviceConfigurationTask.
   *
   * @param c Command the command to be sent to the device.
   * @return Future True if the command was successfully received. Otherwise false
   * @throws InvalidCommandException If the command cannot be encoded.
   */
  static CompletableFuture<Boolean> submitConfigCommand(Command c)
      throws IOException, NoBluetoothException, InvalidCommandException {
    final byte[] encodedBytes = encodeCommand(c);
    return submitConfigCommand(
        new DeviceConfigurationTask(BluetoothManager.getOutputStream(), encodedBytes), false);
  }

  /**
   * Asynchronously submits a command to the OutputStream using the DeviceConfigurationTask. If the
   * command was successful, run andThen.
   *
   * @param c Command to be sent to the device.
   * @param andThen Runnable to be completed only if the command is successfully received
   * @return Future True if the command was successfully received. Otherwise false
   * @throws InvalidCommandException If the command cannot be encoded.
   */
  static CompletableFuture<Boolean> submitConfigCommand(Command c, Runnable andThen)
      throws InvalidCommandException, IOException, NoBluetoothException {
    final CompletableFuture<Boolean> submittedCmd = submitConfigCommand(c);
    submittedCmd.thenAccept(
        x -> { // only perform the runnable if the submittedCommand is accepted
          if (x) {
            andThen.run();
          }
        });
    return submittedCmd;
  }

  /**
   * Asynchronously submits an impedance command to this device using the
   * ImpedanceConfigurationTask.
   *
   * @param c Command to be sent to the device.
   * @return ImpedanceInfo containing slope and offset, or else null.
   */
  static CompletableFuture<ImpedanceInfo> submitImpCommand(Command c)
      throws InvalidCommandException, IOException, NoBluetoothException {
    final byte[] encodedBytes = encodeCommand(c);
    return submitConfigCommand(
        new ImpedanceConfigurationTask(BluetoothManager.getOutputStream(), encodedBytes), null);
  }

  static byte[] encodeCommand(Command c) throws InvalidCommandException {
    final byte[] encodedBytes = MentalabCodec.encodeCommand(c);
    if (encodedBytes == null) {
      throw new InvalidCommandException("Failed to encode command. Exiting.");
    }
    return encodedBytes;
  }
}
