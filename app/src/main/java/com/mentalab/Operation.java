package com.mentalab;

import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.InvalidDataException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;

public abstract class Operation {

    public abstract void run()
            throws NoConnectionException, InvalidDataException, IOException, NoBluetoothException,
            InvalidCommandException;

    public void handleException(Exception cause) {
        // default implementation
    }
}
