package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.mentalab.exception.InitializationFailureException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.service.ConfigureChannelCountTask;
import com.mentalab.service.ConfigureDeviceInfoTask;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.service.RecordTask;
import com.mentalab.utils.FileGenerator;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.Topic;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class MentalabCommands {

  private Future<Boolean> channelCountConfigured;
  private Future<Boolean> deviceInfoConfigured;

  private static ExploreDevice connectedDevice;

  private MentalabCommands() { // Static class
  }

  /**
   * Start data acquisition process from explore device
   *
   * @throws IOException
   * @throws NoBluetoothException
   */
  public void startDataAcquisition() throws IOException, NoBluetoothException, InitializationFailureException, ExecutionException, InterruptedException {
    submitDeviceConfigTasks();
    acquireData();
    checkDeviceConfigured();
  }

  private void checkDeviceConfigured() throws ExecutionException, InterruptedException, InitializationFailureException {
    if (!(channelCountConfigured.get() && deviceInfoConfigured.get())) {
      throw new InitializationFailureException("Device Info not updated. Exiting.");
    }
  }

  private void acquireData() throws NoBluetoothException, IOException {
    MentalabCodec.decodeInputStream(connectedDevice.getInputStream());
  }

  private void submitDeviceConfigTasks() {
    channelCountConfigured =
        ExploreExecutor.submitTask(new ConfigureChannelCountTask(connectedDevice));
    deviceInfoConfigured = ExploreExecutor.submitTask(new ConfigureDeviceInfoTask(connectedDevice));
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
    final BluetoothDevice device = getBondedDeviceWithName(deviceName);
    if (device == null) {
      throw new NoConnectionException("Bluetooth device: " + deviceName + " unavailable. Exiting.");
    }
    return new ExploreDevice(device, deviceName);
  }

  private static BluetoothDevice getBondedDeviceWithName(String deviceName)
      throws NoBluetoothException, NoConnectionException {
    final Set<BluetoothDevice> bondedExploreDevices = getBondedBTDevices();
    return getBondedBTDeviceWithName(deviceName, bondedExploreDevices);
  }

  private static BluetoothDevice getBondedBTDeviceWithName(
      String deviceName, Set<BluetoothDevice> bondedExploreDevices) {
    BluetoothDevice device = null;
    for (BluetoothDevice d : bondedExploreDevices) {
      if (d.getName().equals(deviceName)) {
        device = d;
      }
    }
    return device;
  }

  private static Set<BluetoothDevice> getBondedBTDevices()
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
   * Record data to CSV. Requires appropriate permissions from Android.
   *
   * @param recordSubscriber - The subscriber which subscribes to parsed data and holds information
   *     about where to record.
   * @throws IOException - Can occur both in the generation of files and in the execution of the
   *     subscriber.
   * @see <a href="https://developer.android.com/guide/topics/permissions/overview">android
   *     permissions docs</a>.
   *     <p>Currently, a lot of functionality missing including: blocking on record, setting a
   *     duration for recording, masking channels and overwriting previous files.
   */
  @RequiresApi(api = Build.VERSION_CODES.Q)
  public static void record(RecordTask recordSubscriber) throws IOException {
    final Map<Topic, Uri> generatedFiles = generateFiles(recordSubscriber);
    recordSubscriber.setGeneratedFiles(generatedFiles);
    ExploreExecutor.submitTask(recordSubscriber);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static Map<Topic, Uri> generateFiles(RecordTask recordSubscriber) throws IOException {
    final Context context = recordSubscriber.getContext();
    final boolean overwrite = recordSubscriber.getOverwrite();
    final FileGenerator androidFileGenerator = new FileGenerator(context, overwrite);

    final Uri directory = recordSubscriber.getDirectory();
    final String filename = recordSubscriber.getFilename();
    return androidFileGenerator.generateFiles(directory, filename);
  }

  public static void shutdown() throws IOException {
    connectedDevice = null;
    BluetoothManager.closeSocket();
    ExploreExecutor.shutDown();
    MentalabCodec.shutdown();
  }
}
