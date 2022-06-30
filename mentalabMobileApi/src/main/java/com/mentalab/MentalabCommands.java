package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InitializationFailureException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.service.ConfigureChannelCountTask;
import com.mentalab.service.ConfigureDeviceInfoTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class MentalabCommands {

  private static ExploreDevice connectedDevice;

  private MentalabCommands() { // Static class
  }

  /**
   * Connect to a Mentalab Explore device.
   *
   * @param deviceName String Name of the device to connect to.
   * @throws NoConnectionException
   * @throws NoBluetoothException
   */
  public static ExploreDevice connect(String deviceName)
      throws NoBluetoothException, NoConnectionException, IOException {
    deviceName = Utils.checkName(deviceName);
    connectToExploreDevice(deviceName);
    return connectedDevice;
  }

  private static void connectToExploreDevice(String deviceName)
      throws NoConnectionException, NoBluetoothException, IOException {
    final ExploreDevice device = getExploreDeviceFromName(deviceName);
    connectedDevice = BluetoothManager.connectToDevice(device);
  }

  private static ExploreDevice getExploreDeviceFromName(String deviceName)
      throws NoConnectionException, NoBluetoothException {
    final BluetoothDevice device = getBondedExploreDeviceWithName(deviceName);
    if (device == null) {
      throw new NoConnectionException("Bluetooth device: " + deviceName + " unavailable. Exiting.");
    }
    return new ExploreDevice(device, deviceName);
  }

  private static BluetoothDevice getBondedExploreDeviceWithName(String deviceName)
      throws NoBluetoothException, NoConnectionException {
    final Set<BluetoothDevice> bondedExploreDevices = getBondedExploreDevices();
    BluetoothDevice device = null;
    for (BluetoothDevice d : bondedExploreDevices) {
      if (d.getName().equals(deviceName)) {
        device = d;
      }
    }
    return device;
  }

  private static Set<BluetoothDevice> getBondedExploreDevices()
      throws NoBluetoothException, NoConnectionException {
    final Set<BluetoothDevice> bondedExploreDevices = scan();
    if (bondedExploreDevices.size() < 1) {
      throw new NoConnectionException("Not bonded to any Explore devices. Exiting.");
    }
    return bondedExploreDevices;
  }

  /**
   * Scan for Mentalab Explore devices
   *
   * @return Set<BluetoothDevice> Names of all Explore devices paired.
   * @throws NoBluetoothException
   */
  public static Set<BluetoothDevice> scan() throws NoBluetoothException {
    return BluetoothManager.getBondedExploreDevices();
  }

  /**
   * Start data acquisition process from explore device
   *
   * @throws IOException
   * @throws NoBluetoothException
   */
  public static void acquire()
      throws IOException, NoBluetoothException, InitializationFailureException, ExecutionException,
          InterruptedException {
    final Future<Boolean> channelCountConfigured =
        ExploreExecutor.submitTask(new ConfigureChannelCountTask(connectedDevice));
    final Future<Boolean> deviceInfoConfigured =
        ExploreExecutor.submitTask(new ConfigureDeviceInfoTask(connectedDevice));
    MentalabCodec.decodeInputStream(connectedDevice.getInputStream());
    if (!(channelCountConfigured.get() && deviceInfoConfigured.get())) {
      throw new InitializationFailureException("Device Info not updated. Exiting.");
    }
  }

  public static void shutdown() throws IOException {
    connectedDevice = null;
    BluetoothManager.closeSocket();
    ExploreExecutor.shutDown();
    MentalabCodec.shutdown();
  }
}
