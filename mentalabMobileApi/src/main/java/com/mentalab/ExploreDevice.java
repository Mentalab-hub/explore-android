package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.io.BluetoothManager;
import com.mentalab.io.Switch;
import com.mentalab.service.ExecutorServiceManager;
import com.mentalab.commandtranslators.Command;
import com.mentalab.service.DeviceConfigurationTask;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Future;

public class ExploreDevice extends BluetoothManager {

    private final BluetoothDevice btDevice;

    private int noChannels = 4; // default 4 todo: how do we change this


    public ExploreDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }


    public BluetoothDevice getBluetoothDevice() {
        return btDevice;
    }


    /**
     * Enables or disables channels. By default data from all channels is collected.
     *
     * @param switches List of channel switches, indicating which channels should be on and off
     * @throws InvalidCommandException
     */
    public Future<Boolean> setActiveChannels(List<Switch> switches) throws InvalidCommandException {
        final Command c = Command.CMD_CHANNEL_SET;
        c.setArg(generateChannelsArg(switches));

        return submitCommand(c);
    }


    // todo: 1) should be #channels-charsAt, 2) the number of channels matters, 3) do we do binary?
    private static int generateChannelsArg(List<Switch> switches) {
        StringBuilder binaryArgument = new StringBuilder("11111111"); // When 8 channels are active, we will be sending binary 11111111 = 255
        for (Switch aSwitch : switches) {
            if (!aSwitch.isOn()) {
                binaryArgument.setCharAt(aSwitch.getID(), '0');
            }
        }
        return Integer.parseInt(binaryArgument.toString(), 2);
    }


    public Future<Boolean> setActiveModules(Switch s) throws InvalidCommandException {
        final Command c = s.isOn() ? Command.CMD_MODULE_ENABLE : Command.CMD_MODULE_DISABLE;
        c.setArg(s.getID());

        return submitCommand(c);
    }


    public Future<Boolean> setSamplingRate(SamplingRate sr) throws InvalidCommandException {
        final Command c = Command.CMD_SAMPLING_RATE_SET;
        c.setArg(sr.getValue());

        return submitCommand(c);
    }


    public Future<Boolean> formatDeviceMemory() throws InvalidCommandException {
        return submitCommand(Command.CMD_MEMORY_FORMAT);
    }


    public Future<Boolean> softReset() throws InvalidCommandException {
        return submitCommand(Command.CMD_SOFT_RESET);
    }


    public InputStream getInputStream() throws NoBluetoothException, IOException {
        if (mmSocket == null) {
            throw new NoBluetoothException("No Bluetooth socket available.");
        }
        return mmSocket.getInputStream();
    }


    public OutputStream getOutputStream() throws NoBluetoothException, IOException {
        if (mmSocket == null) {
            throw new NoBluetoothException("No Bluetooth socket available.");
        }
        return mmSocket.getOutputStream();
    }


    public void sendBytes(byte[] bytes) throws NoBluetoothException, IOException {
        final OutputStream outputStream = getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
    }


    /**
     * Asynchronously submits a command to this device using the DeviceConfigurationTask.
     *
     * @param c Command the command to be sent to the device.
     * @return Future True if the command was successfully received. Otherwise false
     * @throws InvalidCommandException If the command cannot be encoded.
     */
    private Future<Boolean> submitCommand(Command c) throws InvalidCommandException {
        final byte[] encodedBytes = MentalabCodec.encodeCommand(c);
        if (encodedBytes == null) {
            throw new InvalidCommandException("Failed to encode command. Exiting.");
        }
        return ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(this, encodedBytes));
    }
}
