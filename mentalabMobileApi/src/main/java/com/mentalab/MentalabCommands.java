package com.mentalab;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.mentalab.io.Switch;
import com.mentalab.utils.constants.Topic;
import com.mentalab.service.ExecutorServiceManager;
import com.mentalab.commandtranslators.Command;
import com.mentalab.utils.constants.SamplingRate;
import com.mentalab.exception.*;
import com.mentalab.io.BluetoothManager;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.utils.FileGenerator;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;

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
    public static ExploreDevice connect(String deviceName) throws NoBluetoothException, NoConnectionException, IOException {
        final ExploreDevice device = getExploreDevice(deviceName);
        connectedDevice = BluetoothManager.connectToDevice(device);
        Log.i(TAG, "Connected to: " + deviceName);
        return connectedDevice;
    }


    public static ExploreDevice connect(BluetoothDevice device) throws NoConnectionException, NoBluetoothException, CommandFailedException, IOException {
        connect(device.getName());
        return connectedDevice;
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
    public static InputStream getRawData() throws NoBluetoothException, IOException, NoConnectionException {
        return BluetoothManager.getBTSocket().getInputStream();
    }


    /**
     * Returns an OutputStream with which to write data to the device.
     *
     * @return InputStream of raw bytes
     * @throws NoConnectionException when Bluetooth connection is lost during communication
     * @throws NoBluetoothException
     */
    public static OutputStream getOutputStream() throws NoBluetoothException, IOException, NoConnectionException {
        return BluetoothManager.getBTSocket().getOutputStream();
    }


    /**
     * Sets sampling rate of the device
     *
     * <p>Sampling rate only applies to ExG data. Orientation and Environment data are always sampled
     * at 20Hz.
     *
     * @param sr SamplingRate Can be either 250, 500 or 1000 Hz. Default is 250Hz.
     */
    public static Future<Boolean> setSamplingRate(SamplingRate sr) {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_SAMPLING_RATE_SET, sr.getValue());
        return ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(encodedBytes));
    }


    /**
     * Formats internal memory of device.
     */
    public static Future<Boolean> formatDeviceMemory() {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_MEMORY_FORMAT);
        return ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(encodedBytes));
    }


    /**
     * Formats internal memory of device. However, when the sampling rate has changed, this command fails.
     */
    public static Future<Boolean> softReset() {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(Command.CMD_SOFT_RESET);
        return ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(encodedBytes)); // TODO: How are we managing executors?
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
    public static Future<Boolean> setChannels(List<Switch> channels) throws InvalidCommandException {
        if (channels.stream().anyMatch(s -> s.isInGroup(Switch.Group.Module))) {
            throw new InvalidCommandException("Attempting to turn off channels with a module switch. Exiting.");
        }
        return connectedDevice.setActiveChannels(channels);
    }


    /**
     * Set a single channel on or off.
     * @param channel Switch The channel you would like to turn on (true) or off (false).
     *
     * @throws InvalidCommandException If the provided Switch is not of type Channel.
     */
    public static Future<Boolean> setChannel(Switch channel) throws InvalidCommandException {
        List<Switch> channelToList = new ArrayList<>();
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
    public static Future<Boolean> setModule(Switch module) throws InvalidCommandException {
        if (module.isInGroup(Switch.Group.Channel)) {
            throw new InvalidCommandException("Attempting to turn off channels with a module switch. Exiting.");
        }
        connectedDevice.setActiveModules(module);
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


    public void close() {
        ExecutorServiceManager.shutDownHook();
    }
}
