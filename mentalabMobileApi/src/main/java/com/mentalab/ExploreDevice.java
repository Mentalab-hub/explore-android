package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.service.ConfigureChannelCountTask;
import com.mentalab.service.ConfigureDeviceInfoTask;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.lsl.LslStreamerTask;
import com.mentalab.utils.InputSwitch;
import com.mentalab.utils.Utils;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.constants.InputProtocol;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/** A wrapper around BluetoothDevice, which is a final class so cannot be extended. */
public class ExploreDevice {

  private final BluetoothDevice btDevice;
  private final String deviceName;

  private int channelCount = 8;
  private SamplingRate samplingRate = SamplingRate.SR_250;
  private int channelMask = 0b1111; // Initialization assumes the device has 4 channels

  public ExploreDevice(BluetoothDevice btDevice, String deviceName) {
    this.btDevice = btDevice;
    this.deviceName = deviceName;
  }

  BluetoothDevice getBluetoothDevice() {
    return btDevice;
  }

  /**
   * Enables or disables data collection of a channel. Sending a mix of enable and disable switches
   * does not work. \\todo: CHECK FOR THIS
   *
   * <p>By default data from all channels is collected. Disable channels you do not need to save
   * bandwidth and power. Calling setChannels with only some channels is supported. Trying to enable
   * a channel that the device does not have results in a CommandFailedException thrown. When a
   * CommandFailedException is received from this method, none or only some switches may have been
   * set.
   *
   * @param switches List of channels to set on (true) or off (false) channel0 ... channel7
   * @throws InvalidCommandException If the provided Switches are not all type Channel.
   */
  public Future<Boolean> setChannels(Set<InputSwitch> switches) throws InvalidCommandException {
    Utils.checkSwitchTypes(switches, InputProtocol.Type.Channel);
    final Command c = generateChannelCommand(switches);
    return submitCommand(c);
  }

  private Command generateChannelCommand(Set<InputSwitch> channelSwitches) {
    final Command c = Command.CMD_CHANNEL_SET;
    c.setArg(generateChannelCmdArg(channelSwitches));
    return c;
  }

  private int generateChannelCmdArg(Set<InputSwitch> switches) {
    for (InputSwitch s : switches) {
      channelMask = bitShiftIfOffSwitch(channelMask, s);
    }
    return channelMask;
  }

  private static int bitShiftIfOffSwitch(int binaryArg, InputSwitch s) {
    if (!s.isOn()) {
      final int channelID = s.getProtocol().getID();
      binaryArg &= ~(1 << channelID); // reverse the bit at the channel id
    }
    return binaryArg;
  }

  /**
   * Set a single channel on or off.
   *
   * @param channel Switch The channel you would like to turn on (true) or off (false).
   * @throws InvalidCommandException If the provided Switch is not of type Channel.
   */
  public Future<Boolean> setChannel(InputSwitch channel) throws InvalidCommandException {
    final Set<InputSwitch> channelToList = new HashSet<>();
    channelToList.add(channel);
    return setChannels(channelToList);
  }

  /**
   * Enables or disables data collection of a module.
   *
   * <p>By default data from all modules is collected. Disable modules you do not need to save
   * bandwidth and power. Calling setModules with only some modules is supported.
   *
   * @param mSwitch The module to be turned on or off ORN, ENVIRONMENT, EXG
   */
  public Future<Boolean> setModule(InputSwitch mSwitch) throws InvalidCommandException {
    Utils.checkSwitchType(mSwitch, InputProtocol.Type.Module);
    final Command c = generateModuleCommand(mSwitch);
    return submitCommand(c);
  }

  private static Command generateModuleCommand(InputSwitch module) {
    final Command c = module.isOn() ? Command.CMD_MODULE_ENABLE : Command.CMD_MODULE_DISABLE;
    c.setArg(module.getProtocol().getID());
    return c;
  }

  /**
   * Sets sampling rate of the device
   *
   * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
   * at 20Hz.
   *
   * @param sr SamplingRate Can be either 250, 500 or 1000 Hz. Default is 250Hz.
   */
  public Future<Boolean> setSamplingRate(SamplingRate sr) throws InvalidCommandException {
    final Command c = Command.CMD_SAMPLING_RATE_SET;
    c.setArg(sr.getValue());
    return submitCommand(c);
  }

  /** Formats internal memory of device. */
  public Future<Boolean> formatMemory() throws InvalidCommandException {
    return submitCommand(Command.CMD_MEMORY_FORMAT);
  }

  /**
   * Formats internal memory of device. However, when the sampling rate has changed, this command
   * fails.
   */
  public Future<Boolean> softReset() throws InvalidCommandException {
    return submitCommand(Command.CMD_SOFT_RESET);
  }

  /**
   * Returns the device data stream.
   *
   * @return InputStream of raw bytes
   * @throws IOException
   * @throws NoBluetoothException If Bluetooth connection is lost during communication
   */
  public InputStream getInputStream() throws NoBluetoothException, IOException {
    return BluetoothManager.getInputStream();
  }

  public void postBytes(byte[] bytes) throws NoBluetoothException, IOException {
    final OutputStream outputStream = BluetoothManager.getOutputStream();
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
    final byte[] encodedBytes = encodeCommand(c);
    return ExploreExecutor.submitTask(new DeviceConfigurationTask(this, encodedBytes));
  }

  private static byte[] encodeCommand(Command c) throws InvalidCommandException {
    final byte[] encodedBytes = MentalabCodec.encodeCommand(c);
    if (encodedBytes == null) {
      throw new InvalidCommandException("Failed to encode command. Exiting.");
    }
    return encodedBytes;
  }

  public Future<Boolean> pushToLSL() {
    return ExploreExecutor.submitTask(new LslStreamerTask(this));
  }

  public String getDeviceName() {
    return deviceName;
  }

  public int getChannelCount() {
    return channelCount;
  }

  public SamplingRate getSamplingRate() {
    return this.samplingRate;
  }

  void setChannelCount(int count) {
    this.channelCount = count;
  }

  void setSR(SamplingRate sr) {
    this.samplingRate = sr;
  }

  void setChannelMask(int mask) {
    this.channelMask = mask;
  }
}
