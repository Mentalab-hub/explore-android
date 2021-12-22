package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.mentalab.io.constants.Switch;
import com.mentalab.io.constants.Topic;
import com.mentalab.utils.MentalabConstants.Command;
import com.mentalab.io.constants.SamplingRate;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.io.BluetoothManager;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;

import static com.mentalab.utils.Utils.TAG;


public final class MentalabCommands {

    private final static Set<BluetoothDevice> bondedExploreDevices = new HashSet<>();
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
        final Set<BluetoothDevice> bondedDevices = BluetoothManager.getBondedDevices();

        for (BluetoothDevice bt : bondedDevices) {
            final String b = bt.getName();
            if (b.startsWith("Explore_")) {
                bondedExploreDevices.add(bt);
                Log.i(Utils.TAG, "Explore device available: " + b);
            }
        }
        return bondedExploreDevices;
    }


    /**
     * Connect to a Mentalab Explore device.
     *
     * @param deviceName String Name of the device to connect to.
     * @throws NoConnectionException
     * @throws NoBluetoothException
     */
    public static void connect(String deviceName) throws NoBluetoothException, NoConnectionException, IOException {
        final ExploreDevice device = getExploreDevice(deviceName);
        connectedDevice = BluetoothManager.connectToDevice(device);
        Log.i(TAG, "Connected to: " + deviceName);
    }


    public static void connect(BluetoothDevice device) throws NoConnectionException, NoBluetoothException, CommandFailedException, IOException {
        connect(device.getName());
    }


    public static ExploreDevice getExploreDevice(String deviceName) throws NoConnectionException, NoBluetoothException {
        if (bondedExploreDevices.isEmpty()) {
            scan();
        }

        BluetoothDevice device = null;
        for (BluetoothDevice d : bondedExploreDevices) {
            if (d.getName().equals(deviceName)) {
                device = d;
            }
        }

        if (device == null) {
            throw new NoConnectionException("Bluetooth device: " + deviceName + " unavailable.");
        }
        return new ExploreDevice(device);
    }


    /**
     * Record data to CSV. Requires appropriate permissions from android,
     *
     * @param recordSubscriber - The subscriber which subscribes to parsed data and holds information about where to record.
     * @throws IOException - Can occur both in the generation of files and in the execution of the subscriber.
     * @see <a href="https://developer.android.com/guide/topics/permissions/overview">android permissions docs</a>.
     * <p>
     * Currently, a lot of functionality missing including: blocking on record, setting a duration for recording,
     * masking channels and overwriting previous files.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void record(RecordSubscriber recordSubscriber) throws IOException {
        final Map<Topic, Uri> generatedFiles = generateFiles(recordSubscriber);
        recordSubscriber.setGeneratedFiles(generatedFiles);
        Executors.newSingleThreadExecutor().execute(recordSubscriber);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static Map<Topic, Uri> generateFiles(RecordSubscriber recordSubscriber) throws IOException {
        final Context context = recordSubscriber.getContext();
        final boolean overwrite = recordSubscriber.getOverwrite();
        final FileGenerator androidFileGenerator = new FileGenerator(context, overwrite);

        final Uri directory = recordSubscriber.getDirectory();
        final String filename = recordSubscriber.getFilename();
        return androidFileGenerator.generateFiles(directory, filename);
    }


    /**
     * Returns the device data stream.
     *
     * @return InputStream of raw bytes
     * @throws IOException
     * @throws NoBluetoothException If Bluetooth connection is lost during communication
     */
    public static InputStream getRawData() throws NoBluetoothException, IOException {
        return BluetoothManager.getBTSocket().getInputStream();
    }


    /**
     * Returns an OutputStream with which to write data to the device.
     *
     * @return InputStream of raw bytes
     * @throws NoConnectionException when Bluetooth connection is lost during communication
     * @throws NoBluetoothException
     */
    public static OutputStream getOutputStream() throws NoBluetoothException, IOException {
        return BluetoothManager.getBTSocket().getOutputStream();
    }


    /**
     * Sets sampling rate of the device
     *
     * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
     * at 20Hz. Currently available sampling rates are 250,500 and 1000 Hz. Default is 250Hz.
     *
     * @param samplingRate enum
     * @throws CommandFailedException
     * @throws NoBluetoothException
     */
    public static void setSamplingRate(SamplingRate samplingRate) throws InvalidCommandException {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_SAMPLING_RATE_SET, samplingRate.getValue());
        MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes)); // TODO: How are we managing executors?
    }


    /**
     * Formats internal memory of device.
     *
     * @throws CommandFailedException
     * @throws NoBluetoothException
     */
    public static void formatDeviceMemory() throws InvalidCommandException {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_MEMORY_FORMAT, 0);
        MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes)); // TODO: How are we managing executors?
    }


    /**
     * Formats internal memory of device
     *
     * @throws CommandFailedException when sampling rate change fails
     * @throws NoBluetoothException
     */
    public static void softReset() throws InvalidCommandException {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_SOFT_RESET, 0);
        MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes)); // TODO: How are we managing executors?
    }


    /**
     * Enables or disables data collection per module or channel. Only support enabling/disabling one
     * module in one call. Mixing enable and disable switch will lead to erroneous result //todo: what?
     *
     * <p>By default data from all modules is collected. Disable modules you do not need to save
     * bandwidth and power. Calling setEnabled with a partial map is supported. Trying to enable a
     * channel that the device does not have results in a CommandFailedException thrown. When a
     * CommandFailedException is received from this method, none or only some of the switches may have
     * been set.
     *
     * @param onOff Map of modules to on (true) or off (false) state accelerometer, magnetometer,
     *                 gyroscope, environment, channel0 ..channel7
     */
    public static void setEnabled(Map<Switch, Boolean> onOff) throws InvalidCommandException {
        connectedDevice.setEnabled(onOff);
    }


    /**
     * Pushes ExG, Orientation and Marker packets to LSL(Lab Streaming Layer)
     *
     * @throws CommandFailedException when LSL service initialization fails
     * @throws IOException
     */
    public static void pushToLsl() {
        MentalabCodec.pushToLsl(connectedDevice);
    }


    public void clearDeviceList() {
        bondedExploreDevices.clear();
    }
}
