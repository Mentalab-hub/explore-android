package com.mentalab.io;

import com.mentalab.DeviceConfigurator;
import com.mentalab.packets.Packet;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.packets.info.DeviceInfoPacket;
import com.mentalab.utils.constants.Topic;

public class DeviceInfoSubscriber extends Subscriber{
    private volatile boolean result;

    public DeviceInfoSubscriber() {
        this.t = Topic.DEVICE_INFO;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param packet the input argument
     */
    @Override
    public void accept(Packet packet) {
        DeviceConfigurator configurator = new DeviceConfigurator(null);
        ((DeviceInfoPacket) packet).getSamplingRate(); //to be replced with custom listener interface
    }

    boolean isDeviceInfoReceived() {
        return false;
    }
}
