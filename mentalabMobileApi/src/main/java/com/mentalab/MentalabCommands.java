package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.mentalab.exception.*;
import com.mentalab.io.BluetoothManager;
import com.mentalab.service.RecordTask;
import com.mentalab.utils.constants.InputDataSwitch;
import com.mentalab.service.ExploreExecutor;
import com.mentalab.utils.FileGenerator;
import com.mentalab.utils.Utils;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.utils.constants.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    public static ExploreDevice connect(String deviceName) throws NoBluetoothException, NoConnectionException, IOException, InvalidDataException {
        final ExploreDevice device = getExploreDevice(deviceName);
        connectedDevice = BluetoothManager.connectToDevice(device);
        Log.i(TAG, "Connected to: " + deviceName);
        decode();
        return connectedDevice;
    }


    public static ExploreDevice connect(BluetoothDevice device) throws NoConnectionException, NoBluetoothException, CommandFailedException, IOException, InvalidDataException {
        connect(device.getName());
        return connectedDevice;
    }


    private static ExploreDevice getExploreDevice(String deviceName) throws NoConnectionException, NoBluetoothException {
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
     * Returns the device data stream.
     *
     * @return InputStream of raw bytes
     * @throws IOException
     * @throws NoBluetoothException If Bluetooth connection is lost during communication
     */
    public static InputStream getRawData() throws NoBluetoothException, IOException, NoConnectionException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        return connectedDevice.getInputStream();
    }


    /**
     * Returns an OutputStream with which to write data to the device.
     *
     * @return InputStream of raw bytes
     * @throws NoConnectionException when Bluetooth connection is lost during communication
     * @throws NoBluetoothException
     */
    public static OutputStream getOutputStream() throws NoBluetoothException, IOException, NoConnectionException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        return connectedDevice.getOutputStream();
    }


    private static void decode() throws NoConnectionException, IOException, NoBluetoothException, InvalidDataException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        final InputStream rawData = getRawData();
        MentalabCodec.startDecode(rawData);
    }


    /**
     * Sets sampling rate of the device
     *
     * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
     * at 20Hz.
     *
     * @param sr SamplingRate Can be either 250, 500 or 1000 Hz. Default is 250Hz.
     */
    public static Future<Boolean> setSamplingRate(SamplingRate sr) throws NoConnectionException, InvalidCommandException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        return connectedDevice.setSamplingRate(sr);
    }


    /**
     * Formats internal memory of device.
     */
    public static Future<Boolean> formatDeviceMemory() throws NoConnectionException, InvalidCommandException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        return connectedDevice.formatDeviceMemory();
    }


    /**
     * Formats internal memory of device. However, when the sampling rate has changed, this command fails.
     */
    public static Future<Boolean> softReset() throws NoConnectionException, InvalidCommandException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        return connectedDevice.softReset();
    }


    /**
     * Enables or disables data collection of a channel.
     * Sending a mix of enable and disable switches does not work. \\todo: CHECK FOR THIS
     *
     * <p>By default data from all channels is collected. Disable channels you do not need to save
     * bandwidth and power. Calling setChannels with only some channels is supported. Trying to enable a
     * channel that the device does not have results in a CommandFailedException thrown. When a
     * CommandFailedException is received from this method, none or only some switches may have
     * been set.
     *
     * @param channels List of channels to set on (true) or off (false) channel0 ... channel7
     * @throws InvalidCommandException If the provided Switches are not all type Channel.
     */
    public static Future<Boolean> setChannels(List<InputDataSwitch> channels) throws InvalidCommandException, NoConnectionException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        if (channels.stream().anyMatch(s -> s.isInGroup(InputDataSwitch.Group.Module))) {
            throw new InvalidCommandException("Attempting to turn off channels with a module switch. Exiting.");
        }
        return connectedDevice.setActiveChannels(channels);
    }


    /**
     * Set a single channel on or off.
     *
     * @param channel Switch The channel you would like to turn on (true) or off (false).
     * @throws InvalidCommandException If the provided Switch is not of type Channel.
     */
    public static Future<Boolean> setChannel(InputDataSwitch channel) throws InvalidCommandException, NoConnectionException {
        List<InputDataSwitch> channelToList = new ArrayList<>();
        channelToList.add(channel);
        return setChannels(channelToList);
    }


    /**
     * Enables or disables data collection of a module.
     *
     * <p>By default data from all modules is collected. Disable modules you do not need to save
     * bandwidth and power. Calling setModules with only some modules is supported.
     *
     * @param module The module to be turned on or off ORN, ENVIRONMENT, EXG
     */
    public static Future<Boolean> setModule(InputDataSwitch module) throws InvalidCommandException, NoConnectionException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        if (module.isInGroup(InputDataSwitch.Group.Channel)) {
            throw new InvalidCommandException("Attempting to turn off channels with a module switch. Exiting.");
        }
        return connectedDevice.setActiveModules(module);
    }


    /**
     * Pushes ExG, Orientation and Marker packets to LSL(Lab Streaming Layer)
     */
    public static void pushToLsl() throws NoConnectionException {
        if (connectedDevice == null) {
            throw new NoConnectionException("Not connected to a device. Exiting.");
        }
        connectedDevice.pushToLSL();
    }


    /**
     * Record data to CSV. Requires appropriate permissions from Android.
     *
     * @param recordSubscriber - The subscriber which subscribes to parsed data and holds information about where to record.
     * @throws IOException - Can occur both in the generation of files and in the execution of the subscriber.
     * @see <a href="https://developer.android.com/guide/topics/permissions/overview">android permissions docs</a>.
     * <p>
     * Currently, a lot of functionality missing including: blocking on record, setting a duration for recording,
     * masking channels and overwriting previous files.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void record(RecordTask recordSubscriber) throws IOException {
        final Map<Topic, Uri> generatedFiles = generateFiles(recordSubscriber);
        recordSubscriber.setGeneratedFiles(generatedFiles);
        Executors.newSingleThreadExecutor().execute(recordSubscriber);
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


    public void clearDeviceList() {
        bondedExploreDevices.clear();
    }


    public void close() throws IOException {
        clearDeviceList();
        connectedDevice = null;
        BluetoothManager.closeSocket();
        ExploreExecutor.shutDownHook();
    }
}
