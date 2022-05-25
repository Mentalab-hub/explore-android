package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.service.ChannelCountTask;
import com.mentalab.service.DeviceInfoUpdaterTask;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mentalab.utils.Utils.TAG;

public final class MentalabCommands {

  private static ExploreDevice connectedDevice;

  private MentalabCommands() { // Static class
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
  public static void startDataAcquisition() throws IOException, NoBluetoothException {
    Future<Boolean> isChannelCountCompleted = ExploreExecutor.submitTask(new ChannelCountTask(connectedDevice));
    Future<Boolean> isInfoUpdated = ExploreExecutor.submitTask(new DeviceInfoUpdaterTask(connectedDevice));
    try {
      MentalabCodec.decodeInputStream(connectedDevice.getInputStream());
    } catch (NoBluetoothException | IOException exception) {
      throw exception;
    }
    try {
      isChannelCountCompleted.get(1000, TimeUnit.MILLISECONDS);
      isInfoUpdated.get(1000, TimeUnit.MILLISECONDS);
    }
    catch (TimeoutException | InterruptedException |  ExecutionException exception) {
      // catch exception  and shutdown all processes
      Log.d(Utils.DEBUG_DEV, "Quitting!!");
      ExploreExecutor.shutDown();
    }
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

    final ExploreDevice device = getExploreDevice(deviceName);
    connectedDevice = BluetoothManager.connectToDevice(device);

    Log.i(TAG, "Connected to: " + deviceName);
    return connectedDevice;
  }

  public static ExploreDevice connect(BluetoothDevice device)
      throws NoConnectionException, NoBluetoothException, IOException {
    return connect(device.getName());
  }

  private static ExploreDevice getExploreDevice(String deviceName)
      throws NoConnectionException, NoBluetoothException {
    final Set<BluetoothDevice> bondedExploreDevices = scan();
    if (bondedExploreDevices.size() < 1) {
      throw new NoConnectionException("Not bonded to any Explore devices. Exiting.");
    }

    BluetoothDevice device = null;
    for (BluetoothDevice d : bondedExploreDevices) {
      if (d.getName().equals(deviceName)) {
        device = d;
      }
    }

    if (device == null) {
      throw new NoConnectionException("Bluetooth device: " + deviceName + " unavailable. Exiting.");
    }
    return new ExploreDevice(device, deviceName);
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

  public static void close() throws IOException {
    connectedDevice = null;
    BluetoothManager.closeSocket();
    ExploreExecutor.shutDown();
    MentalabCodec.shutdown();
  }
}
