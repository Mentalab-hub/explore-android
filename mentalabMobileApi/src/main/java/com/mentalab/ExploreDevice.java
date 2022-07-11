package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.service.ConfigureChannelCountTask;
import com.mentalab.service.ConfigureDeviceInfoTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.lsl.LslStreamerTask;
import com.mentalab.service.record.RecordTask;
import com.mentalab.utils.ConfigSwitch;
import com.mentalab.utils.Utils;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.constants.ConfigProtocol;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/** A wrapper around BluetoothDevice */
public class ExploreDevice {

  private final BluetoothDevice btDevice;
  private final String deviceName;

  private int channelCount = 8;
  private SamplingRate samplingRate = SamplingRate.SR_250;
  private int channelMask = 0b11111111; // Initialization assumes the device has 8 channels

  private RecordTask recordTask;

  public ExploreDevice(BluetoothDevice btDevice, String deviceName) {
    this.btDevice = btDevice;
    this.deviceName = deviceName;
  }

  BluetoothDevice getBluetoothDevice() {
    return btDevice;
  }

  /**
   * Start data acquisition process from explore device
   *
   * @throws IOException
   * @throws NoBluetoothException
   */
  public void acquire() throws IOException, NoBluetoothException {
    CompletableFuture.supplyAsync(new ConfigureChannelCountTask(this));
    CompletableFuture.supplyAsync(new ConfigureDeviceInfoTask(this));
    MentalabCodec.decodeInputStream(getInputStream());
  }

  /**
   * Enables or disables data collection of a channel. Sending a mix of enable and disable switches
   * does not work. \\todo: CHECK FOR THIS
   *
   * <p>By default data from all channels is collected. Disable channels you do not need to save
   * bandwidth and power.
   *
   * @param switches List of channels to set on (true) or off (false) channel0 ... channel7
   * @throws InvalidCommandException If the provided Switches are not all type Channel.
   */
  public Future<Boolean> setChannels(Set<ConfigSwitch> switches)
      throws InvalidCommandException, IOException, NoBluetoothException {
    Utils.checkSwitchTypes(switches, ConfigProtocol.Type.Channel);
    final Command c = generateChannelCommand(switches);
    return DeviceConfigurator.submitCommand(c, () -> setChannelMask(c.getArg()));
  }

  private Command generateChannelCommand(Set<ConfigSwitch> channelSwitches) {
    final Command c = Command.CMD_CHANNEL_SET;
    c.setArg(generateChannelCmdArg(channelSwitches));
    return c;
  }

  private int generateChannelCmdArg(Set<ConfigSwitch> switches) {
    for (ConfigSwitch s : switches) {
      channelMask = bitShiftIfOffSwitch(channelMask, s);
    }
    return channelMask;
  }

  private static int bitShiftIfOffSwitch(int binaryArg, ConfigSwitch s) {
    if (!s.isOn()) {
      final int channelID = s.getProtocol().getID();
      binaryArg &= ~(1 << channelID); // reverse the bit at the channel id
    }
    return binaryArg;
  }

  /** Set a single channel on or off. */
  public Future<Boolean> setChannel(ConfigSwitch channel)
      throws InvalidCommandException, IOException, NoBluetoothException {
    final Set<ConfigSwitch> channelToList = new HashSet<>();
    channelToList.add(channel);
    return setChannels(channelToList);
  }

  /**
   * Enables or disables data collection of a module.
   *
   * <p>By default data from all modules is collected. Disable modules you do not need to save
   * bandwidth and power.
   */
  public Future<Boolean> setModule(ConfigSwitch mSwitch)
      throws InvalidCommandException, IOException, NoBluetoothException {
    Utils.checkSwitchType(mSwitch, ConfigProtocol.Type.Module);
    final Command c = generateModuleCommand(mSwitch);
    return DeviceConfigurator.submitCommand(c);
  }

  private static Command generateModuleCommand(ConfigSwitch module) {
    final Command c = module.isOn() ? Command.CMD_MODULE_ENABLE : Command.CMD_MODULE_DISABLE;
    c.setArg(module.getProtocol().getID());
    return c;
  }

  /**
   * Sets sampling rate of the device
   *
   * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
   * at 20Hz.
   */
  public CompletableFuture<Boolean> setSamplingRate(SamplingRate sr)
      throws InvalidCommandException, IOException, NoBluetoothException {
    final Command c = Command.CMD_SAMPLING_RATE_SET;
    c.setArg(sr.getCode());
    return DeviceConfigurator.submitCommand(c, () -> setSR(sr));
  }

  /** Formats internal memory of device. */
  public Future<Boolean> formatMemory()
      throws InvalidCommandException, IOException, NoBluetoothException {
    return DeviceConfigurator.submitCommand(Command.CMD_MEMORY_FORMAT);
  }

  /**
   * Formats internal memory of device. However, when the sampling rate has changed, this command
   * fails.
   */
  public Future<Boolean> softReset()
      throws InvalidCommandException, IOException, NoBluetoothException {
    return DeviceConfigurator.submitCommand(Command.CMD_SOFT_RESET);
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

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Future<Boolean> record(Context cxt, String filename) {
    recordTask = new RecordTask(cxt, filename, this);
    return ExploreExecutor.submitTask(recordTask);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Future<Boolean> record(Context cxt) {
    final String filename = String.valueOf(System.currentTimeMillis());
    return record(cxt, filename);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  public Future<Boolean> recordWithTimeout(Context cxt, int millis) {
    final String filename = String.valueOf(System.currentTimeMillis());
    recordTask = new RecordTask(cxt, filename, this);
    return ExploreExecutor.submitTimeoutTask(recordTask, millis, () -> recordTask.close());
  }

  public boolean stopRecord() {
    if (recordTask == null) {
      return false;
    }
    recordTask.close();
    return true;
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
