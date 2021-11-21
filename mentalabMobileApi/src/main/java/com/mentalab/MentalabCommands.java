package com.mentalab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.mentalab.MentalabConstants.Command;
import com.mentalab.MentalabConstants.DeviceConfigSwitches;
import com.mentalab.MentalabConstants.SamplingRate;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class MentalabCommands {

  // Tag for debugging purpose

  private static final String TAG = "Explore";
  private static BluetoothSocket mmSocket = null;
  private static InputStream mmInStream = null;
  private static OutputStream mmOutputStream = null;
  private static String connectedDeviceName = null;

  /**
   * Scan for Mentalab devices
   *
   * @return Set of Bluetooth devices: found Mentalab device instances of BluetoothDevice class
   */
  public static Set<String> scan() throws NoBluetoothException {
    Log.d(TAG, "Trying to find devices nearby..");
    final BluetoothAdapter BA = BluetoothAdapter.getDefaultAdapter();

    if (BA == null) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "no bluetooth adapter!");
      }
      throw new NoBluetoothException("No Bluetooth adapter!", null);
    }

    final Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

    if (pairedDevices == null) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "No paired devices available. Exiting.");
      }
      return null;
    }

    Set<String> devices = new HashSet<>();
    for (BluetoothDevice bt : pairedDevices) {
      final String b = bt.getName();
      Log.d(TAG, "Paired dev=" + b);
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Paired dev=" + b);
      }
      if (b.startsWith("Explore_")) {
        Log.d(TAG, "adding Explore Device! with name:" + b);
        devices.add(b);
      }
    }
    return devices;
  }

  /**
   * Connect to Explore Device
   *
   * @param deviceName name of the device to connect to
   * @throws NoConnectionException
   * @throws NoBluetoothException
   */
  public static void connect(String deviceName)
      throws CommandFailedException, NoBluetoothException, NoConnectionException {
    final String uuidBluetoothSpp = "00001101-0000-1000-8000-00805f9b34fb";
    UUID uuid = UUID.fromString(uuidBluetoothSpp);
    final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    if (btAdapter == null) {
      throw new NoBluetoothException("Bluetooth service not available", null);
    }

    final Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
    Log.e(TAG, "Number of bonded device found is: " + pairedDevices.size());
    BluetoothDevice btDevice = null;
    for (BluetoothDevice device : pairedDevices) {
      if (device.getName().equals(deviceName)) {
        btDevice = device;
      }
    }

    if (mmSocket != null) {
      try {
        mmSocket.close();
      } catch (Exception ignored) {
      }
    }

    if (btDevice == null) {
      Log.e(TAG, "Bluetooth device is null.");
      throw new NoConnectionException("Bluetooth device not found", null);
    }

    try {
      mmSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
    } catch (Exception exception) {
      Log.d(TAG, "Could not get rfComm socket:", exception);
      try {
        mmSocket.close();
      } catch (Exception ignored) {
        Log.e(TAG, "Exception occurred!! rfComm socket:", exception);
      }
      mmSocket = null;
      throw new NoConnectionException("Connection to device failed", exception);
    }

    Log.v(TAG, "Got rfComm socket!");

    if (mmSocket != null) {
      try {
        mmSocket.connect();
      } catch (IOException e) {
        Log.d(TAG, "mmSocket.connect() failed with message: " + e.getMessage());
        if (mmSocket != null) {
          try {
            mmSocket.close();
          } catch (Exception exception) {
            Log.d(TAG, "mmSocket.close()  failed with message:" + e.getMessage());
            throw new CommandFailedException("Connection to device failed", exception);
          }
        }
        mmSocket = null;
        Log.d(TAG, "mmSocket.connect() failed");
        throw new CommandFailedException("Connection to device failed", null);
      }
    }
    connectedDeviceName = deviceName;
    Log.d(TAG, "Connected to Mentalab Explore!");
  }

  /**
   * Reset device to default settings
   *
   * <p>Defaults: 250Hz sampling rate, data collection from all modules is enabled.
   *
   * @throws CommandFailedException
   * @throws NoConnectionException
   * @throws NoBluetoothException
   */
  /*
    public static void
    softReset()
            throws CommandFailedException, NoConnectionException, NoBluetoothException {...}

  */

  /**
   * Returns the device data stream
   *
   * @throws NoConnectionException when Bluetooth connection is lost during communication
   * @throws NoBluetoothException
   * @return InputStream of raw bytes
   */
  public static InputStream getRawData() throws NoBluetoothException {
    try {
      assert mmSocket != null;
      mmInStream = mmSocket.getInputStream();

    } catch (IOException exception) {
      Log.d(TAG, "NoBluetoothException occurred");
      throw new NoBluetoothException("NoBluetoothException occurred", null);
    }

    return mmInStream;
  }

  /**
   * Returns the device data stream
   *
   * @throws NoConnectionException when Bluetooth connection is lost during communication
   * @throws NoBluetoothException
   * @return InputStream of raw bytes
   */
  public static OutputStream getOutputStream() throws NoBluetoothException {
    try {
      assert mmSocket != null;
      mmOutputStream = mmSocket.getOutputStream();

    } catch (IOException exception) {
      Log.d(TAG, "NoBluetoothException occurred");
      throw new NoBluetoothException("NoBluetoothException occurred", null);
    }

    return mmOutputStream;
  }

  /**
   * Sets sampling rate of the device
   *
   * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
   * at 20Hz. Currently available sampling rates are 250,500 and 1000 Hz. Default is 250Hz.
   *
   * @param samplingRate enumerator to choose sampling rate
   * @throws CommandFailedException when sampling rate change fails
   * @throws NoBluetoothException
   */
  public static void setSamplingRate(SamplingRate samplingRate)
      throws CommandFailedException, NoBluetoothException, InvalidCommandException, IOException {

    byte[] encodedBytes =
        MentalabCodec.encodeCommand(Command.CMD_SAMPLING_RATE_SET, samplingRate.getValue());

    mmOutputStream = mmSocket.getOutputStream();
    MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
  }

  /**
   * Formats internal memory of device
   *
   * @throws CommandFailedException when sampling rate change fails
   * @throws NoBluetoothException
   */
  public static void formatDeviceMemory()
      throws CommandFailedException, NoBluetoothException, InvalidCommandException, IOException {

    byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_MEMORY_FORMAT, 0);

    mmOutputStream = mmSocket.getOutputStream();
    MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
  }

  /**
   * Formats internal memory of device
   *
   * @throws CommandFailedException when sampling rate change fails
   * @throws NoBluetoothException
   */
  public static void softReset()
      throws CommandFailedException, NoBluetoothException, InvalidCommandException, IOException {

    byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_SOFT_RESET, 0);

    mmOutputStream = mmSocket.getOutputStream();
    MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
  }
  /**
   * Enables or disables data collection per module or channel. Only support enabling/disabling one module in one
   * call. Mixing enable and disable switch will lead to erroneous result
   *
   * <p>By default data from all modules is collected. Disable modules you do not need to save
   * bandwidth and power. Calling setEnabled with a partial map is supported. Trying to enable a
   * channel that the device does not have results in a CommandFailedException thrown. When a
   * CommandFailedException is received from this method, none or only some of the switches may have
   * been set.
   *
   * @param switches Map of modules to on (true) or off (false) state accelerometer, magnetometer,
   *     gyroscope, environment, channel0 ..channel7
   * @throws CommandFailedException
   * @throws NoConnectionException
   * @throws NoBluetoothException
   */
  public static void setEnabled(Map<String, Boolean> switches)
      throws CommandFailedException, NoConnectionException, NoBluetoothException, InvalidCommandException, IOException {

    byte[] encodedBytes = null;
    ArrayList<String> keySet = new ArrayList<>(switches.keySet());
    boolean isModulesOnly =
        keySet.stream()
            .allMatch(element -> Arrays.asList(DeviceConfigSwitches.Modules).contains(element));

    if (isModulesOnly) {
      Log.d("DEBUG_SR", "Only Module!!");

      if (switches.values().iterator().next())
      encodedBytes = MentalabCodec.encodeCommand(Command.CMD_MODULE_ENABLE, generateExtraParameters(Command.CMD_MODULE_ENABLE, new String[]{keySet.iterator().next()}, null));
    else{
        encodedBytes = MentalabCodec.encodeCommand(Command.CMD_MODULE_DISABLE, generateExtraParameters(Command.CMD_MODULE_DISABLE, new String[]{keySet.iterator().next()}, null));
      }
    } else {
      boolean isChannelsOnly =
          keySet.stream()
              .allMatch(element -> Arrays.asList(DeviceConfigSwitches.Channels).contains(element));
      if (isChannelsOnly) {
        Log.d("DEBUG_SR", "Only Channels!!");
        encodedBytes = MentalabCodec.encodeCommand(Command.CMD_CHANNEL_SET, generateExtraParameters(Command.CMD_CHANNEL_SET,
            switches.keySet().toArray(new String[0]), switches.values().toArray(new Boolean[0])));

      } else {
        Log.d("DEBUG_SR", "Mixed Modules, has to throw Exception");
        throw new InvalidCommandException("Invalid Command", null);
      }
    }

    for(int i = 0; i < encodedBytes.length; i++){
      Log.d("DEBUG_SR","Converted data for index: " + "is " + String.format("%02X", encodedBytes[i]));
    }

    mmOutputStream = mmSocket.getOutputStream();
    MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
  }

  /**
   * Pushes ExG, Orientation and Marker packets to LSL(Lab Streaming Layer)
   *
   * @throws CommandFailedException when LSL service initialization fails
   * @throws IOException
   */
  public static void pushToLsl() throws CommandFailedException, IOException {

    MentalabCodec.pushToLsl(connectedDeviceName);
  }

  private static int generateExtraParameters(Command command, String[] arguments, Boolean[] switches){
    int argument = 255;
if (command == Command.CMD_MODULE_ENABLE || command == Command.CMD_MODULE_DISABLE){
  for(int index = 0; index < DeviceConfigSwitches.Modules.length; index ++){
    if (DeviceConfigSwitches.Modules[index].equals(arguments[0])){
      return index;
    }
  }
}
else{
  for(int index = 0; index < DeviceConfigSwitches.Channels.length; index ++){
    for (int indexArguments = 0; indexArguments <arguments.length;indexArguments++){
      if (arguments[indexArguments].equals(DeviceConfigSwitches.Channels[index])){
        if(switches[indexArguments]){
          argument = argument | (1 << index);
        }else{
          argument = argument & ~(1 << index);
        }
        break;
      }
    }
  }
  return argument;
}
return argument;
  }
}
