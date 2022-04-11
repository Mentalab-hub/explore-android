package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.io.BluetoothManager;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.lsl.LslStreamerTask;
import com.mentalab.utils.InputSwitch;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.concurrent.Future;

import static com.mentalab.utils.Utils.generateChannelsArg;

public class ExploreDevice extends BluetoothManager {

  private final BluetoothDevice btDevice;
  private final String deviceName;

  private int channelCount = 8;
  private SamplingRate samplingRate = SamplingRate.SR_250;

  public ExploreDevice(BluetoothDevice btDevice, String deviceName) {
    this.btDevice = btDevice;
    this.deviceName = deviceName;
  }

  public BluetoothDevice getBluetoothDevice() {
    return btDevice;
  }

  /**
   * Enables or disables channels. By default data from all channels is collected.
   *
   * @param switches List of channel switches, indicating which channels should be on and off
   * @throws InvalidCommandException
   */
  public Future<Boolean> postActiveChannels(Set<InputSwitch> switches)
      throws InvalidCommandException {
    final Command c = Command.CMD_CHANNEL_SET;
    c.setArg(generateChannelsArg(switches, channelCount));

    return submitCommand(c);
  }

  public Future<Boolean> postActiveModules(InputSwitch s) throws InvalidCommandException {
    final Command c = s.isOn() ? Command.CMD_MODULE_ENABLE : Command.CMD_MODULE_DISABLE;
    c.setArg(s.getProtocol().getID());

    return submitCommand(c);
  }

  public Future<Boolean> postSamplingRate(SamplingRate sr) throws InvalidCommandException {
    final Command c = Command.CMD_SAMPLING_RATE_SET;
    c.setArg(sr.getValue());

    return submitCommand(c);
  }

  public Future<Boolean> formatDeviceMemory() throws InvalidCommandException {
    return submitCommand(Command.CMD_MEMORY_FORMAT);
  }

  public Future<Boolean> softReset() throws InvalidCommandException {
    return submitCommand(Command.CMD_SOFT_RESET);
  }

  public InputStream getInputStream() throws NoBluetoothException, IOException {
    if (mmSocket == null) {
      throw new NoBluetoothException("No Bluetooth socket available.");
    }
    return mmSocket.getInputStream();
  }

  public OutputStream getOutputStream() throws NoBluetoothException, IOException {
    if (mmSocket == null) {
      throw new NoBluetoothException("No Bluetooth socket available.");
    }
    return mmSocket.getOutputStream();
  }

  public void postBytes(byte[] bytes) throws NoBluetoothException, IOException {
    final OutputStream outputStream = getOutputStream();
    outputStream.write(bytes);
    outputStream.flush();
  }

  /**
   * Asynchronously submits a command to this device using the DeviceConfigurationTask.
   *
   * @param c Command the command to be sent to the device.
   * @return Future True if the command was successfully received. Otherwise false
   * @throws InvalidCommandException If the command cannot be encoded.
   */
  private Future<Boolean> submitCommand(Command c) throws InvalidCommandException {
    final byte[] encodedBytes = MentalabCodec.encodeCommand(c);
    if (encodedBytes == null) {
      throw new InvalidCommandException("Failed to encode command. Exiting.");
    }
    return ExploreExecutor.submitTask(new DeviceConfigurationTask(this, encodedBytes));
  }

  public Future<Boolean> pushToLSL() {
    return ExploreExecutor.submitTask(new LslStreamerTask(this));
  }

  public String getDeviceName() {
    return deviceName;
  }

  // todo: set these in first round of info packet and then if message sent
  public void setChannelCount(int channelCount) {
    this.channelCount = channelCount;
  }

  public int getChannelCount() {
    return channelCount;
  }

  public void setSamplingRate(SamplingRate sr) {
    this.samplingRate = sr;
  }

  public SamplingRate getSamplingRate() {
    return this.samplingRate;
  }
}
