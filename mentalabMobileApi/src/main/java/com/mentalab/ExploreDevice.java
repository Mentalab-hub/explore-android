package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.io.Switch;
import com.mentalab.service.ExecutorServiceManager;
import com.mentalab.commandtranslators.Command;
import com.mentalab.utils.DeviceConfigurationTask;

import java.util.List;
import java.util.concurrent.Future;

public class ExploreDevice {

    private final BluetoothDevice btDevice;

    private int noChannels = 4; // default 4


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
        final Command cmd = Command.CMD_CHANNEL_SET;
        cmd.setValue(generateChannelsArg(switches));
        final byte[] encodedBytes = MentalabCodec.encodeCommand(cmd);
        if (encodedBytes == null) {
            throw new InvalidCommandException("Failed to encode command for switches. Exiting.");
        }
        return ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(encodedBytes));
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


    public void setActiveModules(Switch s) throws InvalidCommandException {
        final Command cmd = s.isOn() ? Command.CMD_MODULE_ENABLE : Command.CMD_MODULE_DISABLE;
        cmd.setValue(s.getID());

        final byte[] encodedBytes = MentalabCodec.encodeCommand(cmd);
        if (encodedBytes == null) {
            throw new InvalidCommandException("Failed to encode command for switch: " + s + ". Exiting.");
        }
        ExecutorServiceManager.getExecutorService().submit(new DeviceConfigurationTask(encodedBytes));
    }
}
