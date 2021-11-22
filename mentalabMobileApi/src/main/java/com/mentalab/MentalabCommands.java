package com.mentalab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

public class MentalabCommands {

  // Tag for debugging purpose

  private static final String TAG = "Explore";
  private static BluetoothSocket mmSocket = null;
  private static InputStream mmInStream = null;
  private static OutputStream mmOutputStream = null;

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

    Log.d(TAG, "Connected to Mentalab Explore!");
  }


  @RequiresApi(api = Build.VERSION_CODES.Q)
  public static void recordToFile(RecordSubscriber recordSubscriber) {
    FileGenerator androidFileGenerator = new FileGenerator(recordSubscriber);
    Set<UriTopicBean> generatedFiles = androidFileGenerator.generateFiles(recordSubscriber.getDirectory(), recordSubscriber.getFilename());
    recordSubscriber.setGeneratedFiles(generatedFiles);
    Executors.newSingleThreadExecutor().execute(recordSubscriber);
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

    } catch (Exception exception) {
      Log.d(TAG, "NoBluetoothException occurred");
      throw new NoBluetoothException("NoBluetoothException occurred", null);
    }

    return mmInStream;
  }

  public static void closeSockets() {
    try {
      mmSocket.close();
    } catch (IOException e) {
      Log.e(TAG, "Unable to close socket.");
    }
    mmSocket = null;
  }

  /* */
  /**
   * Sets sampling rate of the device
   *
   * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
   * at 20Hz. Currently available sampling rates are 250,500 and 1000 Hz. Default is 250Hz.
   *
   * @param SamplingRate enumerator to choose sampling rate
   * @throws CommandFailedException when sampling rate change fails
   * @throws NoBluetoothException
   */
  /*
  public static void
  setSamplingRate(SamplingRate s)
          throws CommandFailedException, NoBluetoothException {...}





  */
  /**
   * Enables or disables data collection per module or channel.
   *
   * <p>By default data from all modules is collected. Disable modules you do not need to save
   * bandwidth and power. Calling setEnabled with a partial map is supported. Trying to enable a
   * channel that the device does not have results in a CommandFailedException thrown. When a
   * CommandFailedException is received from this method, none or only some of the switches may have
   * been set.
   *
   * @param switches Map of modules to on (true) or off (false) state accelerometer, magnetometer,
   *     gyroscope, environment, channel0 .. 31
   * @throws CommandFailedException
   * @throws NoConnectionException
   * @throws NoBluetoothException
   */
  /*
  public static void
  setEnabled(Map<String, Boolean> switches)
          throws CommandFailedException, NoConnectionException, NoBluetoothException {...}




  */
  /** Available sampling rates */
  enum SamplingRate {
    SR_250,
    SR_500,
    SR_100,
  }
}
