package com.mentalab.utils;

import android.util.Log;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.io.BluetoothManager;
import com.mentalab.io.OneTopicSubscriber;
import com.mentalab.io.Subscriber;
import com.mentalab.io.constants.Topic;
import com.mentalab.packets.command.CommandAcknowledgment;
import com.mentalab.packets.command.CommandReceived;
import com.mentalab.packets.command.CommandStatus;
import com.mentalab.packets.Packet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class DeviceConfigurationTask implements Callable<Boolean> {

    final byte[] command;

    public DeviceConfigurationTask(byte[] encodedBytes) {
        this.command = encodedBytes;
    }


    @Override
    public Boolean call() throws IOException, InterruptedException, NoBluetoothException {
        final Subscriber sub = new OneTopicSubscriber(Topic.COMMAND);

        final OutputStream outputStream = BluetoothManager.getBTSocket().getOutputStream();
        outputStream.write(command);
        outputStream.flush();
        Log.d("DEBUG_SR", "Command sent. Awaiting acknowledgement.");

        Packet commandStatus = sub.getMessagesReceived().take();
        if (commandStatus instanceof CommandAcknowledgment) {
            Log.d("DEBUG_SR", "Ack packet received in callback");
        } else if (commandStatus instanceof CommandReceived) {
            Log.d("DEBUG_SR", "CommandReceivedPacket received in callback");
        } else if (commandStatus instanceof CommandStatus) {
            Log.d("DEBUG_SR", "CommandStatusPacket received in callback");
        }
        return true;
    }
}
