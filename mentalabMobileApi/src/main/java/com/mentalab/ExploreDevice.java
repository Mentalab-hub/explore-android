package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.packets.info.ImpedanceInfo;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.ImpedanceCalculatorTask;
import com.mentalab.service.lsl.LslStreamerTask;
import com.mentalab.service.record.RecordTask;
import com.mentalab.utils.ConfigSwitch;
import com.mentalab.utils.Utils;
import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.constants.ChannelCount;
import com.mentalab.utils.constants.ConfigProtocol;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** A wrapper around BluetoothDevice */
public class ExploreDevice {

  private final BluetoothDevice btDevice;
  private final String deviceName;

  private ChannelCount channelCount = ChannelCount.CC_8;
  private SamplingRate samplingRate = SamplingRate.SR_250;
  private int channelMask = 0b11111111; // Initialization assumes the device has 8 channels
  private float slope;
  private double offset;

  private final ImpedanceCalculatorTask impedanceTask = new ImpedanceCalculatorTask(this);
  private RecordTask recordTask;

  public ExploreDevice(BluetoothDevice btDevice, String deviceName) {
    this.btDevice = btDevice;
    this.deviceName = deviceName;
  }

  BluetoothDevice getBluetoothDevice() {
    return btDevice;
  }

  /**
   * Start acquiring data from this device.
   *
   * <p>Before reading the Bluetooth input stream, two tasks are assigned that wait for packets and
   * configure the device based on those packets. If these packets are not received, or the device
   * is not configured, we cannot proceed.
   */
  protected ExploreDevice acquire()
      throws IOException, NoBluetoothException, ExecutionException, InterruptedException {
    List<CompletableFuture<Boolean>> deviceConfig =
        Arrays.asList(
            CompletableFuture.supplyAsync(new ConfigureChannelCountTask(this)),
            CompletableFuture.supplyAsync(new ConfigureDeviceInfoTask(this))); // start config first
    MentalabCodec.decodeInputStream(getInputStream());
    waitOnConfig(deviceConfig); // wait on config, otherwise connection failed
    return this;
  }

  private static void waitOnConfig(List<CompletableFuture<Boolean>> deviceConfig)
      throws ExecutionException, InterruptedException, IOException {
    for (CompletableFuture<Boolean> f : deviceConfig) {
      if (!f.get()) {
        MentalabCommands.shutdown();
        throw new IOException("Unable to initialise device. Cannot proceed.");
      }
    }
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
    return DeviceManager.submitCommand(c, () -> setChannelMask(c.getArg()));
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
    return DeviceManager.submitCommand(c);
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
    return DeviceManager.submitCommand(c, () -> setSR(sr));
  }

  /** Formats internal memory of device. */
  public Future<Boolean> formatMemory()
      throws InvalidCommandException, IOException, NoBluetoothException {
    return DeviceManager.submitCommand(Command.CMD_MEMORY_FORMAT);
  }

  /**
   * Formats internal memory of device. However, when the sampling rate has changed, this command
   * fails.
   */
  public Future<Boolean> softReset()
      throws InvalidCommandException, IOException, NoBluetoothException {
    return DeviceManager.submitCommand(Command.CMD_SOFT_RESET);
  }

  /** Returns the device data stream. */
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

  public Future<Boolean> calculateImpedance()
      throws NoBluetoothException, IOException, InvalidCommandException, ExecutionException,
          InterruptedException, CommandFailedException {
    CompletableFuture<Boolean> setSlopeOffset =
        DeviceManager.submitImpCommand().thenApply(impInfo -> setSlopeAndOffset(impInfo));
    if (setSlopeOffset.get()) {
      return ExploreExecutor.submitTask(impedanceTask);
    } else {
      throw new CommandFailedException(
          "Slope and offset not received. Unable to calculate impedance.");
    }
  }

  private boolean setSlopeAndOffset(ImpedanceInfo slopeOffset) {
    if (slopeOffset == null) {
      return false;
    }
    this.setOffset(slopeOffset.getOffset());
    this.setSlope(slopeOffset.getSlope());
    return true;
  }

  public void stopImpedanceCalculation()
      throws NoBluetoothException, IOException, InvalidCommandException {
    final Command c = Command.CMD_ZM_DISABLE;
    DeviceManager.submitCommand(c, impedanceTask::cancelTask);
  }

  public String getDeviceName() {
    return this.deviceName;
  }

  public ChannelCount getChannelCount() {
    return this.channelCount;
  }

  public SamplingRate getSamplingRate() {
    return this.samplingRate;
  }

  public float getSlope() {
    return this.slope;
  }

  public double getOffset() {
    return this.offset;
  }

  public void setChannelCount(ChannelCount count) {
    Log.d(Utils.TAG, "Channel count set to: " + count);
    this.channelCount = count;
  }

  public void setSR(SamplingRate sr) {
    Log.d(Utils.TAG, "Sampling rate set to: " + sr);
    this.samplingRate = sr;
  }

  public void setChannelMask(int mask) {
    Log.d(Utils.TAG, "Channel mask set to: " + Integer.toBinaryString(mask));
    this.channelMask = mask;
  }

  private void setSlope(float slope) {
    Log.d(Utils.TAG, "Impedance slope set to: " + slope);
    this.slope = slope;
  }

  private void setOffset(double offset) {
    Log.d(Utils.TAG, "Impedance offset set to: " + offset);
    this.offset = offset;
  }
}
