package com.mentalab.service;

import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.io.CommandAcknowledgeSubscriber;
import com.mentalab.io.ContentServer;
import com.mentalab.utils.Utils;

import java.io.IOException;
import java.util.concurrent.Callable;

public class DeviceConfigurationTask implements Callable<Boolean> {

    final byte[] command;
    final ExploreDevice device;


    public DeviceConfigurationTask(ExploreDevice device, byte[] encodedBytes) {
        this.device = device;
        this.command = encodedBytes;
    }


    /**
     * Send a command to a connected Explore device.
     *
     * This function is blocking. If no acknowledgement packet arrives, the function
     * will wait until it does. The user can set a timeout using Future functions.
     *
     * @return boolean True when CommandAcknowledgement received, otherwise false
     * @throws IOException If the command cannot be written to the device OutputStream.
     * @throws InterruptedException If the command cannot be written to the device OutputStream.
     * @throws NoBluetoothException If no device is connected via BT.
     */
    @Override
    public Boolean call() throws IOException, InterruptedException, NoBluetoothException {
        final CommandAcknowledgeSubscriber sub = new CommandAcknowledgeSubscriber();
        ContentServer.getInstance().registerSubscriber(sub); // register subscriber before sending command

        this.device.sendBytes(command);
        Log.d(Utils.TAG, "Command sent. Awaiting acknowledgement.");

        return sub.getAcknowledgement(); // blocking for 3s
    }
}
