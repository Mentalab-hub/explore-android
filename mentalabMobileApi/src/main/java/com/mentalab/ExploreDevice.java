package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.io.Switch;
import com.mentalab.utils.MentalabConstants;

import java.util.List;

public class ExploreDevice {

    private final BluetoothDevice btDevice;


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
    public void setActiveChannels(List<Switch> switches) throws InvalidCommandException {
        final MentalabConstants.Command cmd = MentalabConstants.Command.CMD_CHANNEL_SET;
        final byte[] encodedBytes = MentalabCodec.encodeCommand(cmd, generateChannelsArg(switches));
        if (encodedBytes == null) {
            throw new InvalidCommandException("Failed to encode command for switches. Exiting.");
        }
        MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
    }


    private static int generateChannelsArg(List<Switch> switches) {
        StringBuilder binaryArgument = new StringBuilder("11111111"); // When 8 channels are active, we will be sending binary 11111111 = 255
        for (Switch aSwitch : switches) {
            if (!aSwitch.isOn()) {
                binaryArgument.setCharAt(aSwitch.getID(), '0');
            }
        }
        return Integer.parseInt(binaryArgument.toString(), 2);
    }


    public void setActiveModules(Switch aSwitch) throws InvalidCommandException {
        final MentalabConstants.Command cmd = aSwitch.isOn() ?
                MentalabConstants.Command.CMD_MODULE_ENABLE :
                MentalabConstants.Command.CMD_MODULE_DISABLE;

        final byte[] encodedBytes = MentalabCodec.encodeCommand(cmd, aSwitch.getID());
        if (encodedBytes == null) {
            throw new InvalidCommandException("Failed to encode command for switch: " + aSwitch + ". Exiting.");
        }
        MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
    }
}
