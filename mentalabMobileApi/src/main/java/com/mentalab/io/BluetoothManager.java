package com.mentalab.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.mentalab.ExploreDevice;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static com.mentalab.utils.Utils.TAG;

public class BluetoothManager {

    private final static String UUID_BLUETOOTH_SPP = "00001101-0000-1000-8000-00805f9b34fb";
    private static BluetoothSocket mmSocket = null;


    public static Set<BluetoothDevice> getBondedDevices() throws NoBluetoothException {
        Log.i(TAG, "Searching for nearby devices...");
        final Set<BluetoothDevice> bondedDevices = getBluetoothAdapter().getBondedDevices();
        if (bondedDevices == null) {
            throw new NoBluetoothException("No Bluetooth devices available.");
        }
        return bondedDevices;
    }


    public static BluetoothAdapter getBluetoothAdapter() throws NoBluetoothException {
        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            throw new NoBluetoothException("Bluetooth service not available", null);
        }
        return btAdapter;
    }


    private static void establishRFCommWithDevice(BluetoothDevice device) throws NoConnectionException, IOException {
        closeSocket();

        try {
            final UUID uuid = UUID.fromString(UUID_BLUETOOTH_SPP);
            mmSocket = device.createRfcommSocketToServiceRecord(uuid);
        } catch (Exception e) {
            closeSocket();
            throw new NoConnectionException("Connection to device failed.", e);
        }
        Log.i(TAG, "Received rfComm socket.");

    }


    public static ExploreDevice connectToDevice(ExploreDevice device) throws NoConnectionException, IOException {
        BluetoothManager.establishRFCommWithDevice(device.getBluetoothDevice());
        try {
            mmSocket.connect();
        } catch (IOException e) {
            BluetoothManager.closeSocket();
            throw new IOException(e);
        }
        return device;
    }


    public static void closeSocket() throws IOException {
        if (mmSocket == null) {
            return;
        }
        mmSocket.close();
        mmSocket = null;
    }


    public static BluetoothSocket getBTSocket() throws NoBluetoothException {
        if (mmSocket == null) {
            throw new NoBluetoothException("No Bluetooth socket available.");
        }
        return mmSocket;
    }
}
