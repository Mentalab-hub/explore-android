package com.mentalab;

import android.bluetooth.BluetoothDevice;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.io.constants.Switch;
import com.mentalab.io.constants.Switch.Group;
import com.mentalab.utils.MentalabConstants;

import java.util.Map;

public class ExploreDevice {

    private final BluetoothDevice btDevice;


    public ExploreDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }


    public BluetoothDevice getBluetoothDevice() {
        return btDevice;
    }


    /**
     * Enables or disables data collection per module or channel. Only support enabling/disabling one
     * module in one call. Mixing enable and disable switch will lead to erroneous result // todo: what?
     *
     * <p>By default data from all modules is collected. Disable modules you do not need to save
     * bandwidth and power. Calling setEnabled with a partial map is supported. Trying to enable a
     * channel that the device does not have results in a CommandFailedException thrown. When a
     * CommandFailedException is received from this method, none or only some of the switches may have
     * been set.
     *
     * @param onOffSwitches Map of modules to on (true) or off (false) state accelerometer, magnetometer,
     *                      gyroscope, environment, channel0 ..channel7
     * @throws InvalidCommandException
     * @throws NoConnectionException
     * @throws NoBluetoothException
     * @throws CommandFailedException
     */
    public void setEnabled(Map<Switch, Boolean> onOffSwitches) throws InvalidCommandException {
        if (allSwitchesOfType(onOffSwitches, Switch.Group.Module)) {
            for (Map.Entry<Switch, Boolean> aSwitch : onOffSwitches.entrySet()) {
                MentalabConstants.Command cmd = aSwitch.getValue() ?
                        MentalabConstants.Command.CMD_MODULE_ENABLE :
                        MentalabConstants.Command.CMD_MODULE_DISABLE;
                byte[] encodedBytes =
                        MentalabCodec.encodeCommand(cmd, getModuleIndex(aSwitch.getKey()));
                MentalabCodec.getExecutorService().execute(new DeviceConfigurationTask(encodedBytes));
            }
        } else if (allSwitchesOfType(onOffSwitches, Switch.Group.Channel)) {
            byte[] encodedBytes =
                    MentalabCodec.encodeCommand(
                            MentalabConstants.Command.CMD_CHANNEL_SET,
                        generateBinaryChannelMask(
                                    MentalabConstants.Command.CMD_CHANNEL_SET,
                                    onOffSwitches.keySet().toArray(new String[0]),
                                    onOffSwitches.values().toArray(new Boolean[0])));
        } else {
            throw new InvalidCommandException("Turn on or off modules or channels separately. Invalid Command.");
        }
    }


    private boolean allSwitchesOfType(Map<Switch, Boolean> onOffSwitches, Switch.Group group) {
        return onOffSwitches
                .keySet()
                .stream()
                .allMatch(aSwitch -> aSwitch.isInGroup(group));
    }

    //TODO change device config to switch enumerator
    private static int generateBinaryChannelMask(MentalabConstants.Command command, String[] arguments, Boolean[] switches) {
        int argument = 255; // TODO: what?
            for (int i = 0; i < MentalabConstants.DeviceConfigSwitches.Channels.length; i++) {
                for (int indexArguments = 0; indexArguments < arguments.length; indexArguments++) {
                    if (arguments[indexArguments].equals(MentalabConstants.DeviceConfigSwitches.Channels[i])) {
                        if (switches[indexArguments]) { // todo: ?
                            argument = argument | (1 << i);
                        } else {
                            argument = argument & ~(1 << i);
                        }
                        break;
                    }
                }
            }
            return argument;
    }

    private int getModuleIndex(Switch switchName){
        Group[] moduleValues = Group.Module.values();
        for(int index = 0;index < moduleValues.length; index ++ )
        {
            if(switchName.name().equals(moduleValues[index])){
                return index;
            }
        }
        //TODO throw invalid command exception
        return 0;
    }
}
