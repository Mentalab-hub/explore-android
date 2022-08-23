package com.mentalab.packets;

import androidx.annotation.NonNull;
import com.mentalab.exception.InvalidDataException;

public class EmptyPacket extends Packet {

    public EmptyPacket(double timeStamp) {
        super(timeStamp);
    }

    @Override
    public void populate(byte[] byteBuffer) throws InvalidDataException {
        // ignored
    }

    @NonNull
    @Override
    public String toString() {
        return "";
    }
}
