package com.mentalab;

import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.constants.ChannelCount;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DeviceManager {

  private final ExploreDevice device;

  public DeviceManager(ExploreDevice exploreDevice) {
    this.device = exploreDevice;
  }

  public void setDeviceChannelCount(ChannelCount channelCount) {
    this.device.setChannelCount(channelCount);
  }

  public void setDeviceInfo(DeviceInfoPacket packet) {
    this.device.setSR(packet.getSamplingRate());
    this.device.setChannelMask(packet.getChannelMask());
  }

  protected static CompletableFuture<Boolean> submitCommand(Command c, Runnable andThen)
      throws InvalidCommandException, IOException, NoBluetoothException {
    final CompletableFuture<Boolean> submittedCmd = submitCommand(c);
    submittedCmd.thenAccept(
        x -> { // only perform the runnable if the submittedCommand is accepted
          if (x) {
            andThen.run();
          }
        });
    return submittedCmd;
  }

  /**
   * Asynchronously submits a command to this device using the DeviceConfigurationTask.
   *
   * @param c Command the command to be sent to the device.
   * @return Future True if the command was successfully received. Otherwise false
   * @throws InvalidCommandException If the command cannot be encoded.
   */
  protected static CompletableFuture<Boolean> submitCommand(Command c)
      throws IOException, NoBluetoothException, InvalidCommandException {
    final byte[] encodedBytes = encodeCommand(c);
    return CompletableFuture.supplyAsync(
            new DeviceConfigurationTask(BluetoothManager.getOutputStream(), encodedBytes))
        .exceptionally(e -> false); // if DeviceConfigurationTask throws an exception, return false (gracefully)
  }

  private static byte[] encodeCommand(Command c) throws InvalidCommandException {
    final byte[] encodedBytes = MentalabCodec.encodeCommand(c);
    if (encodedBytes == null) {
      throw new InvalidCommandException("Failed to encode command. Exiting.");
    }
    return encodedBytes;
  }
}
